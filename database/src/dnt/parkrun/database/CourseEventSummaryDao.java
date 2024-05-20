package dnt.parkrun.database;

import dnt.parkrun.common.DateConverter;
import dnt.parkrun.datastructures.*;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import javax.sql.DataSource;
import java.util.*;

public class CourseEventSummaryDao extends BaseDao
{
    private final CourseRepository courseRepository;

    public CourseEventSummaryDao(Country country, DataSource dataSource, CourseRepository courseRepository)
    {
        super(country, dataSource);
        this.courseRepository = courseRepository;
    }

    public List<CourseEventSummary> getCourseEventSummaries(Course course)
    {
        String sql = STR."""
                select course_id, event_number, date, finishers,
                    fma.name as first_male_name, first_male_athlete_id,
                    ffa.name as first_female_name, first_female_athlete_id
                from course_event_summary
                left join athlete fma on first_male_athlete_id = fma.athlete_id
                left join athlete ffa on first_female_athlete_id = ffa.athlete_id
                where course_id = :courseId
                """;
        return jdbc.query(sql, new MapSqlParameterSource("courseId", course.courseId), (rs, rowNum) ->
        {
            int firstMaleAthleteId = rs.getInt("first_male_athlete_id");
            int firstFemaleAthleteId = rs.getInt("first_female_athlete_id");
            Optional<Athlete> firstMale = Optional.ofNullable(
                    firstMaleAthleteId == Athlete.NO_ATHLETE_ID ? null : Athlete.from(rs.getString("first_male_name"), firstMaleAthleteId));
            Optional<Athlete> firstFemale = Optional.ofNullable(
                    firstFemaleAthleteId == Athlete.NO_ATHLETE_ID ? null : Athlete.from(rs.getString("first_female_name"), firstFemaleAthleteId));
            return new CourseEventSummary(
                    course,
                    rs.getInt("event_number"),
                    rs.getDate("date"),
                    rs.getInt("finishers"),
                    firstMale,
                    firstFemale
            );
        });
    }

    public List<CourseEventSummary> getCourseEventSummaries()
    {
        String sql = STR."""
                select course_id, event_number, date, finishers,
                    fma.name as first_male_name, first_male_athlete_id,
                    ffa.name as first_female_name, first_female_athlete_id
                from \{courseEventSummaryTable()}
                left join \{athleteTable()} fma on first_male_athlete_id = fma.athlete_id
                left join \{athleteTable()} ffa on first_female_athlete_id = ffa.athlete_id
                order by date asc, course_id asc
                """;
        return jdbc.query(sql, EmptySqlParameterSource.INSTANCE, (rs, rowNum) ->
        {
            int courseId = rs.getInt("course_id");
            Course course = courseRepository.getCourse(courseId);
            assert course != null : "Could not find " + courseId;

            int firstMaleAthleteId = rs.getInt("first_male_athlete_id");
            int firstFemaleAthleteId = rs.getInt("first_female_athlete_id");
            Optional<Athlete> firstMale = Optional.ofNullable(
                    firstMaleAthleteId == Athlete.NO_ATHLETE_ID ? null : Athlete.from(rs.getString("first_male_name"), firstMaleAthleteId));
            Optional<Athlete> firstFemale = Optional.ofNullable(
                    firstFemaleAthleteId == Athlete.NO_ATHLETE_ID ? null : Athlete.from(rs.getString("first_female_name"), firstFemaleAthleteId));
            return new CourseEventSummary(
                    course,
                    rs.getInt("event_number"),
                    rs.getDate("date"),
                    rs.getInt("finishers"),
                    firstMale,
                    firstFemale
            );
        });
    }

    public void insert(CourseEventSummary courseEventSummary)
    {
        String sql = "insert into course_event_summary (" +
                "course_id, event_number, date, finishers, first_male_athlete_id, first_female_athlete_id" +
                ") values ( " +
                ":courseId, :eventNumber, :date, :finishers, :firstMaleAthleteId, :firstFemaleAthleteId" +
                ")";
        jdbc.update(sql, new MapSqlParameterSource()
                .addValue("courseId", courseEventSummary.course.courseId)
                .addValue("eventNumber", courseEventSummary.eventNumber)
                .addValue("date", courseEventSummary.date)
                .addValue("finishers", courseEventSummary.finishers)
                .addValue("firstMaleAthleteId", courseEventSummary.firstMale.map(a -> a.athleteId).orElse(Athlete.NO_ATHLETE_ID))
                .addValue("firstFemaleAthleteId", courseEventSummary.firstFemale.map(a -> a.athleteId).orElse(Athlete.NO_ATHLETE_ID))
        );
    }

    public Map<String, Integer> getCourseCount()
    {
        Map<String, Integer> courseToCount = new HashMap<>();
        String sql = STR."""
                select course_name, max(event_number) as count
                from \{courseEventSummaryTable()} ces
                join \{courseTable()} using (course_id)
                group by course_name
                """;
        jdbc.query(sql, EmptySqlParameterSource.INSTANCE, (rs, rowNum) ->
        {
            courseToCount.put(rs.getString("course_name"), rs.getInt("count"));
            return null;
        });
        return courseToCount;
    }

//    public Map<Integer, Date> getStartDates()
//    {
//        Map<Integer, Date> result = new HashMap<>();
//        String sql = "select course_id, date " +
//                "from course join course_event_summary using (course_id)  " +
//                "where event_number = 1 " +
//                "order by date desc;";
//        jdbc.query(sql, EmptySqlParameterSource.INSTANCE, (rs, rowNum) -> {
//            result.put(rs.getInt("course_id"), rs.getDate("date"));
//            return null;
//        });
//        return result;
//    }
//

    /**
     * Course Start Dates sorted.
     */
    public List<CourseDate> getCourseStartDates()
    {
        String sql = STR."""
                select course_id, date
                from \{courseTable()}
                join \{courseEventSummaryTable()} using (course_id)
                where event_number = 1
                order by date asc
                """;
        return jdbc.query(sql, EmptySqlParameterSource.INSTANCE,
                (rs, rowNum) ->
                        new CourseDate(
                                courseRepository.getCourse(rs.getInt("course_id")),
                                rs.getDate("date"))
        );
    }

    public List<CourseDate> getCourseStopDates(Country country)
    {
        String sql = "select course_id, max(date) as max_date\n" +
                "from (select * from " + courseTable() + " where status = :stoppedStatus and country_code = :countryCode) as sub1\n" +
                "join " + courseEventSummaryTable() + " using (course_id)\n" +
                "group by course_id\n" +
                "order by max_date asc";
        return jdbc.query(sql,
                new MapSqlParameterSource()
                        .addValue("stoppedStatus", Course.Status.STOPPED.getStatusForDb())
                        .addValue("countryCode", country.countryCode),
                (rs, rowNum) ->
                        new CourseDate(
                                courseRepository.getCourse(rs.getInt("course_id")),
                                rs.getDate("max_date"))
        );
    }

    public void delete(int courseId, Date date)
    {
        String sql = STR."""
                delete from \{courseEventSummaryTable()}
                where course_id = :courseId and date = :date
                """;
        jdbc.update(sql, new MapSqlParameterSource()
                .addValue("courseId", courseId)
                .addValue("date", DateConverter.formatDateForDbTable(date)));
    }
}
