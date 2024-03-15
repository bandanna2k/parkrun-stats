package dnt.parkrun.database;

import com.mysql.jdbc.Driver;
import dnt.parkrun.datastructures.*;
import dnt.parkrun.datastructures.Course.Status;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

public class CourseEventSummaryDaoTest
{
    private CourseEventSummaryDao dao;
    private NamedParameterJdbcTemplate jdbc;
    private AthleteDao athleteDao;

    @Before
    public void setUp() throws Exception
    {
        CourseRepository courseRepository = new CourseRepository();
        courseRepository.addCourse(new Course(9999, "cornwall", Country.NZ, null, Status.RUNNING));

        DataSource dataSource = new SimpleDriverDataSource(new Driver(),
                "jdbc:mysql://localhost/parkrun_stats_test", "dao", "daoFractaldao");
        dao = new CourseEventSummaryDao(dataSource, courseRepository);
        athleteDao = new AthleteDao(dataSource);

        jdbc = new NamedParameterJdbcTemplate(dataSource);
    }
    @After
    public void tearDown()
    {
        jdbc.update("delete from course_event_summary", EmptySqlParameterSource.INSTANCE);
    }

    @Test
    public void shouldInsertCourseEventSummary()
    {
        Athlete firstMan = Athlete.fromAthleteSummaryLink("Davey JONES", "https://www.parkrun.co.nz/parkrunner/902393/");
        Athlete firstWoman = Athlete.fromAthleteSummaryLink("Terry EVANS", "https://www.parkrun.co.nz/parkrunner/12345/");
        athleteDao.insert(firstWoman);
        athleteDao.insert(firstMan);

        Course course = new Course(9999, "cornwall", Country.NZ, null, Status.RUNNING);
        CourseEventSummary ces = new CourseEventSummary(
                course, 1, Date.from(Instant.now()), 1234, Optional.of(firstMan), Optional.of(firstWoman));
        dao.insert(ces);
        System.out.println(ces);
        Assertions.assertThat(dao.getCourseEventSummaries()).isNotEmpty();
    }
}
