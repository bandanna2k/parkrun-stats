package dnt.parkrun.database;

import dnt.parkrun.datastructures.Country;
import dnt.parkrun.datastructures.Course;
import dnt.parkrun.datastructures.CourseRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import java.util.List;

public class CourseDao extends BaseDao
{
    private final CourseRepository courseRepository;

    /*
        Adds course to DB and populates CourseRepository
     */
    public CourseDao(Database database, CourseRepository courseRepository)
    {
        super(database);
        this.courseRepository = courseRepository;

        jdbc.query(STR."""
                        select course_id, course_name, course_long_name, country_code, status
                        from \{courseTable()}
                        order by course_name
                        """,
                EmptySqlParameterSource.INSTANCE,
                (rs, rowNum) ->
                {
                    Course course = new Course(
                            rs.getInt("course_id"),
                            rs.getString("course_name"),
                            Country.valueOf(rs.getInt("country_code")),
                            rs.getString("course_long_name"),
                            Course.Status.fromDb(rs.getString("status"))
                    );
                    courseRepository.addCourse(course);
                    return null;
                });
    }

    public Course insert(Course course)
    {
        assert course.courseId == Course.NO_COURSE_ID;
        assert courseRepository.getCourseFromName(course.name) == null &&
                courseRepository.getCourseFromLongName(course.longName) == null
                : "Course already exists " + course;
        try
        {
            String sql = STR."""
                    insert ignore into \{courseTable()}
                    (course_name, course_long_name, country_code, country, status) 
                    values 
                    (:courseName, :courseLongName, :countryCode, :country, :status)
                    """;
            jdbc.update(sql, new MapSqlParameterSource()
                    .addValue("courseName", course.name)
                    .addValue("courseLongName", course.longName)
                    .addValue("countryCode", course.country.getCountryCode())
                    .addValue("country", course.country.getCountryDbCode())
                    .addValue("status", course.getStatusDbCode())
            );
            Course result = getCourse(course.name);
            courseRepository.addCourse(result);
            return result;
        }
        catch (DuplicateKeyException ex)
        {
            // System.out.println("DEBUG: Duplicate key for athlete: " + athlete);
        }
        throw new RuntimeException("Should not get here.");
    }

    public Course getCourse(String courseName)
    {
        try
        {
            return jdbc.queryForObject(STR."""
                            select *
                            from \{courseTable()}
                            where course_name = :courseName
                            """,
                    new MapSqlParameterSource("courseName", courseName),
                    (rs, rowNum) ->
                            new Course(
                                    rs.getInt("course_id"),
                                    rs.getString("course_name"),
                                    Country.valueOf(rs.getInt("country_code")),
                                    rs.getString("course_long_name"),
                                    Course.Status.fromDb(rs.getString("status"))
                            ));
        }
        catch (EmptyResultDataAccessException ex)
        {
            return null;
        }
    }

    public List<Course> getCourses(Country country)
    {
        return jdbc.query(STR."""
                        select course_id, course_name, course_long_name, country_code, status
                        from \{courseTable()}
                        where country_code = :countryCode
                        order by course_name
                        """,
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
