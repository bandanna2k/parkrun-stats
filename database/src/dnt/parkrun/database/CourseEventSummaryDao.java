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
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class CourseEventSummaryDao
{
    private final NamedParameterJdbcOperations jdbc;
    private final CourseRepository courseRepository;

    public CourseEventSummaryDao(DataSource dataSource, CourseRepository courseRepository) throws SQLException
    {
        this.jdbc = new NamedParameterJdbcTemplate(dataSource);
        this.courseRepository = courseRepository;
    }

    public List<CourseEventSummary> getCourseEventSummaries()
    {
        String sql = "select course_name, event_number, date, finishers," +
                "fma.name as first_male_name, first_male_athlete_id, " +
                "ffa.name as first_female_name, first_female_athlete_id " +
                "from parkrun_stats.course_event_summary " +
                "left join parkrun_stats.athlete fma on first_male_athlete_id = fma.athlete_id " +
                "left join parkrun_stats.athlete ffa on first_female_athlete_id = ffa.athlete_id ";
        return jdbc.query(sql, EmptySqlParameterSource.INSTANCE, (rs, rowNum) ->
        {
            String courseName = rs.getString("course_name");
            Course course = courseRepository.getCourseFromName(courseName);
            assert course != null : "Could not find " + courseName;

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
        String sql = "insert into parkrun_stats.course_event_summary (" +
                "course_name, event_number, date, finishers, first_male_athlete_id, first_female_athlete_id" +
                ") values ( " +
                ":courseName, :eventNumber, :date, :finishers, :firstMaleAthleteId, :firstFemaleAthleteId" +
                ")";
        jdbc.update(sql, new MapSqlParameterSource()
                .addValue("courseName", courseEventSummary.course.name)
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
}
