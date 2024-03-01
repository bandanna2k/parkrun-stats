package dnt.parkrun.mostevents.dao;

import dnt.parkrun.datastructures.Athlete;
import dnt.parkrun.datastructures.Course;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import java.sql.SQLException;

public class CourseDao
{

    private final NamedParameterJdbcOperations jdbc;

    public CourseDao(DataSource dataSource) throws SQLException
    {
        jdbc = new NamedParameterJdbcTemplate(dataSource);
    }

    public void insert(Course course)
    {
        try
        {
            String sql = "insert ignore into parkrun_stats.course (" +
                    "course_name, course_long_name, country_code, status " +
                    ") values ( " +
                    ":courseName, :courseLongName, :countryCode, :status" +
                    ")";
            jdbc.update(sql, new MapSqlParameterSource()
                    .addValue("courseName", course.name)
                    .addValue("courseLongName", course.longName)
                    .addValue("countryCode", course.country.getCountryCodeForDb())
                    .addValue("status", course.getStatusForDb())
            );
        }
        catch (DuplicateKeyException ex)
        {
            // System.out.println("DEBUG: Duplicate key for athlete: " + athlete);
        }
    }

    public Athlete getAthlete(long athleteId)
    {
        Athlete athlete = jdbc.queryForObject("select * from parkrun_stats.athlete",
                new MapSqlParameterSource("athleteId", athleteId),
                (rs, rowNum) ->
                        Athlete.fromDao(
                                rs.getString("name"),
                                rs.getLong("athlete_id")
                        ));
        return athlete;
    }
}
