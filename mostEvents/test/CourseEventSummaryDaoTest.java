import com.mysql.jdbc.Driver;
import dnt.parkrun.datastructures.*;
import dnt.parkrun.mostevents.dao.CourseEventSummaryDao;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;

public class CourseEventSummaryDaoTest
{
    private CourseEventSummaryDao dao;
    private NamedParameterJdbcTemplate jdbc;

    @Before
    public void setUp() throws Exception
    {
        CourseRepository courseRepository = new CourseRepository();
        courseRepository.addCountry(new Country(1, null));
        courseRepository.addCourse(new Course("cornwall", 1));

        DataSource dataSource = new SimpleDriverDataSource(new Driver(),
                "jdbc:mysql://localhost", "dao", "daoFractaldao");
        dao = new CourseEventSummaryDao(dataSource, courseRepository);

        jdbc = new NamedParameterJdbcTemplate(dataSource);
    }
    @After
    public void tearDown()
    {
        jdbc.update("delete from parkrun_stats.course_event_summary", EmptySqlParameterSource.INSTANCE);
    }

    @Test
    public void shouldInsertCourseEventSummary()
    {
        Course course = new Course("cornwall", 1);
        CourseEventSummary ces = new CourseEventSummary(course, 1, Athlete.NO_ATHLETE, Athlete.NO_ATHLETE);
        dao.insert(ces);
        System.out.println(ces);
        assertThat(dao.getCourseEventSummaries()).isNotEmpty();
    }
}
