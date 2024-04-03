package dnt.parkrun.database;

import com.mysql.jdbc.Driver;
import dnt.parkrun.datastructures.Course;
import dnt.parkrun.datastructures.CourseRepository;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;

import static dnt.parkrun.datastructures.Country.NZ;
import static dnt.parkrun.datastructures.Course.Status.RUNNING;
import static org.assertj.core.api.Assertions.assertThat;

public class CourseDaoTest extends BaseDaoTest
{
    private NamedParameterJdbcTemplate jdbc;
    private CourseDao courseDao;
    private CourseRepository courseRepository;

    @Before
    public void setUp() throws Exception
    {
        DataSource dataSource = new SimpleDriverDataSource(new Driver(),
                "jdbc:mysql://localhost/parkrun_stats_test", "test", "qa");
        courseRepository = new CourseRepository();
        courseDao = new CourseDao(dataSource, courseRepository);

        jdbc = new NamedParameterJdbcTemplate(dataSource);
        jdbc.update("delete from course", EmptySqlParameterSource.INSTANCE);
    }

    @Test
    public void shouldInsertUtf8Course()
    {
        Course course = courseDao.insert(new Course(9999, "otakiriver", NZ, "\u014ctaki River parkrun", RUNNING));
        System.out.println(course);
        assertThat(course.courseId).isGreaterThan(0);
        assertThat(course.name).isEqualTo("otakiriver");
    }
}
