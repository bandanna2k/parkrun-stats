package dnt.parkrun.database;

import dnt.parkrun.datastructures.Athlete;
import dnt.parkrun.datastructures.Course;
import dnt.parkrun.datastructures.CourseEventSummary;
import dnt.parkrun.datastructures.CourseRepository;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import java.util.*;

public class CourseEventSummaryDao
{
    private final NamedParameterJdbcOperations jdbc;
    private final CourseRepository courseRepository;

    public CourseEventSummaryDao(DataSource dataSource, CourseRepository courseRepository)
    {
        this.jdbc = new NamedParameterJdbcTemplate(dataSource);
        this.courseRepository = courseRepository;
    }

    public List<CourseEventSummary> getCourseEventSummaries()
    {
        String sql = "select course_id, event_number, date, finishers," +
                "fma.name as first_male_name, first_male_athlete_id, " +
                "ffa.name as first_female_name, first_female_athlete_id " +
                "from course_event_summary " +
                "left join athlete fma on first_male_athlete_id = fma.athlete_id " +
                "left join athlete ffa on first_female_athlete_id = ffa.athlete_id ";
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

    @Deprecated
    public void backfillFinishers(String name, int eventNumber, int finishers, Date date)
    {
        int update = jdbc.update("update course_event_summary " +
                        "set date = :date " +
                        "where course_name = :courseName and " +
                        " event_number = :eventNumber and " +
                        " date is null",
                new MapSqlParameterSource()
                        .addValue("courseName", name)
                        .addValue("eventNumber", eventNumber)
                        .addValue("date", date)
                        .addValue("finishers", finishers));
    }

    public Map<String, Integer> getCourseCount()
    {
        Map<String, Integer> courseToCount = new HashMap<>();
        String sql =
                "select course_name, max(event_number) as count\n" +
                        "from course_event_summary ces\n" +
                        "group by course_name";
        jdbc.query(sql, EmptySqlParameterSource.INSTANCE, (rs, rowNum) -> {
            courseToCount.put(rs.getString("course_name"), rs.getInt("count"));
            return null;
        });
        return courseToCount;
    }
}
