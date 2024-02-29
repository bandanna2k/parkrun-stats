package dnt.parkrun.mostevents.dao;

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
import java.util.List;

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
        String sql = "select course_name, event_number, " +
                "fma.name as first_male_name, first_male_athlete_id, " +
                "ffa.name as first_male_name, first_female_athlete_id " +
                "from parkrun_stats.course_event_summary " +
                "left join parkrun_stats.athlete fma on first_male_athlete_id = fma.athlete_id " +
                "left join parkrun_stats.athlete ffa on first_female_athlete_id = ffa.athlete_id ";
        return jdbc.query(sql, EmptySqlParameterSource.INSTANCE, (rs, rowNum) ->
        {
            String courseName = rs.getString("course_name");
            Course course = courseRepository.getCourse(courseName);
            assert course != null : "Could not find " + courseName;
            return new CourseEventSummary(
                    course,
                    rs.getInt("event_number"),
                    null,
                    Athlete.fromDao(
                            rs.getString("first_male_name"),
                            rs.getInt("first_male_athlete_id")),
                    Athlete.fromDao(
                            rs.getString("first_male_name"),
                            rs.getInt("first_male_athlete_id"))
            );
        });
    }

    public void insert(CourseEventSummary courseEventSummary)
    {
        String sql = "insert into parkrun_stats.course_event_summary (" +
                "course_name, event_number, first_male_athlete_id, first_female_athlete_id" +
                ") values ( " +
                ":courseName, :eventNumber, :firstMaleAthleteId, :firstFemaleAthleteId" +
                ")";
        jdbc.update(sql, new MapSqlParameterSource()
                .addValue("courseName", courseEventSummary.course.name)
                .addValue("eventNumber", courseEventSummary.eventNumber)
                .addValue("firstMaleAthleteId", courseEventSummary.firstMale.athleteId)
                .addValue("firstFemaleAthleteId", courseEventSummary.firstFemale.athleteId)
        );
    }

    public List<CourseEventSummary> getCourseEventSummariesWithNoResults()
    {
        return null;
    }
}
