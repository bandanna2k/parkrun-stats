package dnt.parkrun.database;

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
                    "course_name, course_long_name, country_code, country, status " +
                    ") values ( " +
                    ":courseName, :courseLongName, :countryCode, :country, :status" +
                    ")";
            jdbc.update(sql, new MapSqlParameterSource()
                    .addValue("courseName", course.name)
                    .addValue("courseLongName", course.longName)
                    .addValue("countryCode", course.country.getCountryCode())
                    .addValue("country", course.country.getCountryDbCode())
                    .addValue("status", course.getStatusDbCode())
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
                                rs.getInt("athlete_id")
                        ));
        return athlete;
    }
}
