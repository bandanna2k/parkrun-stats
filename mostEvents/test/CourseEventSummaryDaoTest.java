import com.mysql.jdbc.Driver;
import dnt.parkrun.datastructures.*;
import dnt.parkrun.mostevents.dao.AthleteDao;
import dnt.parkrun.mostevents.dao.CourseEventSummaryDao;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;
import java.time.Instant;
import java.util.Date;

import static dnt.parkrun.datastructures.Course.Status.RUNNING;
import static org.assertj.core.api.Assertions.assertThat;

public class CourseEventSummaryDaoTest
{
    private static final Country NZ = new Country(CountryEnum.NZ, null);

    private CourseEventSummaryDao dao;
    private NamedParameterJdbcTemplate jdbc;
    private AthleteDao athleteDao;

    @Before
    public void setUp() throws Exception
    {
        CourseRepository courseRepository = new CourseRepository();
        courseRepository.addCountry(NZ);
        courseRepository.addCourse(new Course("cornwall", NZ, null, RUNNING));

        DataSource dataSource = new SimpleDriverDataSource(new Driver(),
                "jdbc:mysql://localhost", "dao", "daoFractaldao");
        dao = new CourseEventSummaryDao(dataSource, courseRepository);
        athleteDao = new AthleteDao(dataSource);

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
        Athlete firstMan = Athlete.fromAthleteSummaryLink("Davey JONES", "https://www.parkrun.co.nz/parkrunner/902393/");
        Athlete firstWoman = Athlete.fromAthleteSummaryLink("Terry EVANS", "https://www.parkrun.co.nz/parkrunner/12345/");
        athleteDao.insert(firstWoman);
        athleteDao.insert(firstMan);

        Course course = new Course("cornwall", NZ, null, RUNNING);
        CourseEventSummary ces = new CourseEventSummary(course, 1, Date.from(Instant.now()), firstMan, firstWoman);
        dao.insert(ces);
        System.out.println(ces);
        assertThat(dao.getCourseEventSummaries()).isNotEmpty();
    }
}
