package dnt.parkrun.database;

import dnt.parkrun.datastructures.Country;
import dnt.parkrun.datastructures.Course;
import dnt.parkrun.datastructures.CourseRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
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

    /*
        Adds course to DB and populates CourseRepository
     */
    public CourseDao(DataSource dataSource, CourseRepository courseRepository)
    {
        jdbc = new NamedParameterJdbcTemplate(dataSource);
        jdbc.query(
                "select course_id, course_name, course_long_name, country_code, status " +
                        "from course " +
                        "order by course_name",
                EmptySqlParameterSource.INSTANCE,
                (rs, rowNum) ->
                {
                    Country country = Country.valueOf(rs.getInt("country_code"));
                    Course course = new Course(
                            rs.getInt("course_id"),
                            rs.getString("course_name"),
                            country,
                            rs.getString("course_long_name"),
                            Course.Status.fromDb(rs.getString("status"))
                    );
                    courseRepository.addCourse(course);
                    return null;
                });
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
                        new Course(
                                rs.getInt("course_id"),
                                rs.getString("course_name"),
                                null,
                                rs.getString("course_long_name"),
                                Course.Status.fromDb(rs.getString("status"))
        ));
    }

    public List<Course> getCourses(Country country)
    {
        return jdbc.query(
                "select course_id, course_name, course_long_name, country_code, status " +
                    "from course " +
                    "where country_code = :countryCode " +
                    "order by course_name",
                new MapSqlParameterSource("countryCode", country.getCountryCode()),
                (rs, rowNum) ->
                        new Course(
                                rs.getInt("course_id"),
                                rs.getString("course_name"),
                                country,
                                rs.getString("course_long_name"),
                                Course.Status.fromDb(rs.getString("status"))
                        ));
    }

    public void setCourseStatus(String courseName, Course.Status status)
    {
        String sql = "update course set status = :status where course_name = :courseName";
        jdbc.update(sql, new MapSqlParameterSource()
                .addValue("courseName", courseName)
                .addValue("status", status.getStatusForDb())
        );
    }
}
