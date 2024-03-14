package dnt.parkrun.database;

import com.mysql.jdbc.Driver;
import dnt.parkrun.datastructures.Country;
import dnt.parkrun.datastructures.Course;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;

import static dnt.parkrun.datastructures.CountryEnum.NZ;
import static dnt.parkrun.datastructures.Course.Status.RUNNING;

public class CourseDaoTest
{
    private NamedParameterJdbcTemplate jdbc;
    private CourseDao courseDao;

    @Before
    public void setUp() throws Exception
    {
        DataSource dataSource = new SimpleDriverDataSource(new Driver(),
                "jdbc:mysql://localhost/parkrun_stats_test", "dao", "daoFractaldao");
        courseDao = new CourseDao(dataSource);

        jdbc = new NamedParameterJdbcTemplate(dataSource);
        jdbc.update("delete from course", EmptySqlParameterSource.INSTANCE);
    }

    @Test
    public void shouldReturnUtf8Result()
    {
        courseDao.insert(new Course(9999, "otakiriver", new Country(NZ, null), "\u014ctaki River parkrun", RUNNING));

        Course course = courseDao.getCourse("otakiriver");
        System.out.println(course);
    }
}
