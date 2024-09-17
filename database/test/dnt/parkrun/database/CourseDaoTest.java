package dnt.parkrun.database;

import com.mysql.jdbc.Driver;
import dnt.parkrun.datastructures.Course;
import dnt.parkrun.datastructures.CourseRepository;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

import static dnt.parkrun.datastructures.Country.*;
import static dnt.parkrun.datastructures.Course.Status.RUNNING;
import static org.assertj.core.api.Assertions.assertThat;

public class CourseDaoTest extends BaseDaoTest
{
    private CourseDao courseDao;
    private CourseRepository courseRepository;

    @Before
    public void setUp() throws Exception
    {
        jdbc.update("delete from course", EmptySqlParameterSource.INSTANCE);

        courseRepository = new CourseRepository();
        courseDao = new CourseDao(TEST_DATABASE, courseRepository);
    }

    @Test
    @Ignore
    public void shouldInsertIntoRealDatabase() throws SQLException
    {
        DataSource realDataSource = new SimpleDriverDataSource(new Driver(),
                "jdbc:mysql://localhost/parkrun_stats", "dao", "0b851094");

        NamedParameterJdbcTemplate realJdbc = new NamedParameterJdbcTemplate(realDataSource);
        String sql = STR."""
    INSERT INTO course (
    course_id, course_name, course_long_name, country_code, country, status
    ) VALUES (
    48, 'orakeibay', 'Ōrākei Bay parkrun', 65, 'NZ', 'P'
    );
                """;
        realJdbc.update(sql, EmptySqlParameterSource.INSTANCE);
    }

    @Test
    @Ignore
    public void shouldUpdateRealDatabase() throws SQLException
    {
        DataSource realDataSource = new SimpleDriverDataSource(new Driver(),
                "jdbc:mysql://proliant.local/parkrun_stats", "dao", "0b851094");

        NamedParameterJdbcTemplate realJdbc = new NamedParameterJdbcTemplate(realDataSource);
        String sql = STR."""
    UPDATE course
    SET course_long_name = 'Y Promenâd parkrun, Aberhonddu'
    WHERE course_name = 'ypromenad'
    """;
        realJdbc.update(sql, EmptySqlParameterSource.INSTANCE);
    }

    @Test
    public void shouldInsertUtf8Course()
    {
        Course course = courseDao.insert(new Course(Course.NO_COURSE_ID, "otakiriver", NZ, "\u014ctaki River parkrun", RUNNING));
        System.out.println(course);
        assertThat(course.courseId).isGreaterThan(0);
        assertThat(course.name).isEqualTo("otakiriver");
    }

    @Test
    public void shouldGetCourseByName()
    {
        Course course = courseDao.insert(new Course(Course.NO_COURSE_ID, "otakiriver", NZ, "\u014ctaki River parkrun", RUNNING));

        assertThat(courseDao.getCourse("otaki")).isNull();
        assertThat(courseDao.getCourse("otakiriver")).isNotNull();
    }

    @Test
    public void shouldGetCoursesForCountry()
    {
        courseDao.insert(CORNWALL);
        courseDao.insert(ELLIÐAÁRDALUR);

        {
            List<Course> courses = courseDao.getCourses(NZ);
            assertThat(courses.size()).isEqualTo(1);
            assertThat(courses.get(0).name).isEqualTo("cornwall");
        }
        {
            List<Course> courses = courseDao.getCourses(UNKNOWN);
            assertThat(courses.size()).isEqualTo(1);
            assertThat(courses.get(0).name).isEqualTo("ellidaardalur");
        }
        {
            List<Course> courses = courseDao.getCourses(USA);
            assertThat(courses.size()).isEqualTo(0);
        }
    }
}
