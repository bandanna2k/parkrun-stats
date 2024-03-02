package dnt.parkrun.database;

import com.mysql.jdbc.Driver;
import dnt.parkrun.datastructures.Course;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;

public class CourseDaoTest
{
    private NamedParameterJdbcTemplate jdbc;
    private CourseDao courseDao;

    @Before
    public void setUp() throws Exception
    {
        DataSource dataSource = new SimpleDriverDataSource(new Driver(),
                "jdbc:mysql://localhost", "dao", "daoFractaldao");
        courseDao = new CourseDao(dataSource);

        jdbc = new NamedParameterJdbcTemplate(dataSource);
    }

    @Test
    public void shouldReturnUtf8Result()
    {
        Course course = courseDao.getCourse("otakiriver");
        System.out.println(course);
    }
}
