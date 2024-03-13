package dnt.parkrun.database;

import dnt.parkrun.datastructures.Country;
import dnt.parkrun.datastructures.CountryEnum;
import dnt.parkrun.datastructures.Course;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import java.util.List;

public class CourseDao
{

    private final NamedParameterJdbcOperations jdbc;

    public CourseDao(DataSource dataSource)
    {
        jdbc = new NamedParameterJdbcTemplate(dataSource);
    }

    public void insert(Course course)
    {
        try
        {
            String sql = "insert ignore into course (" +
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

    public Course getCourse(String courseName)
    {
        return jdbc.queryForObject("select * from course where course_name = :courseName",
                new MapSqlParameterSource("courseName", courseName),
                (rs, rowNum) ->
                        new Course(rs.getString("course_name"),
                                null,
                                rs.getString("course_long_name"),
                                Course.Status.fromDb(rs.getString("status"))
        ));
    }

    public List<Course> getCourses(CountryEnum countryEnum)
    {
        return jdbc.query(
                "select course_name, course_long_name, country_code, status " +
                    "from course " +
                    "where country_code = :countryCode " +
                    "order by course_name",
                new MapSqlParameterSource("countryCode", countryEnum.getCountryCode()),
                (rs, rowNum) ->
                        new Course(rs.getString("course_name"),
                                new Country(countryEnum, null),
                                rs.getString("course_long_name"),
                                Course.Status.fromDb(rs.getString("status"))
                        ));
    }
}
