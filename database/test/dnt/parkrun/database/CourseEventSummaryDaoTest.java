package dnt.parkrun.database;

import dnt.parkrun.common.DateConverter;
import dnt.parkrun.datastructures.*;
import dnt.parkrun.datastructures.Course.Status;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static dnt.parkrun.datastructures.Country.NZ;
import static org.assertj.core.api.Assertions.assertThat;

public class CourseEventSummaryDaoTest extends BaseDaoTest
{
    private CourseEventSummaryDao dao;
    private AthleteDao athleteDao;
    private CourseDao courseDao;

    @Before
    public void setUp() throws Exception
    {
        jdbc.update("delete from parkrun_stats_test.athlete", EmptySqlParameterSource.INSTANCE);
        jdbc.update("delete from parkrun_stats_test.course", EmptySqlParameterSource.INSTANCE);
        jdbc.update("delete from parkrun_stats_test.course_event_summary", EmptySqlParameterSource.INSTANCE);

        CourseRepository courseRepository = new CourseRepository();
        dao = new CourseEventSummaryDao(TEST_DATABASE, courseRepository);
        courseDao = new CourseDao(TEST_DATABASE, courseRepository);
        athleteDao = new AthleteDao(TEST_DATABASE);
    }

    @Test
    public void shouldInsertCourseEventSummary()
    {
        Athlete firstMan = Athlete.fromAthleteSummaryLink("Davey JONES", "https://www.parkrun.co.nz/parkrunner/902393/");
        Athlete firstWoman = Athlete.fromAthleteSummaryLink("Terry EVANS", "https://www.parkrun.co.nz/parkrunner/12345/");
        athleteDao.insert(firstWoman);
        athleteDao.insert(firstMan);

        Course course = courseDao.insert(new Course(Course.NO_COURSE_ID, "cornwall", NZ, null, Status.RUNNING));
        CourseEventSummary ces = new CourseEventSummary(
                course, 1, Date.from(Instant.now()), 1234, Optional.of(firstMan), Optional.of(firstWoman));
        dao.insert(ces);
        System.out.println(ces);
        assertThat(dao.getCourseEventSummaries()).isNotEmpty();
    }

    @Test
    public void shouldReturnStartDate()
    {
        Athlete firstMan = Athlete.fromAthleteSummaryLink("Davey JONES", "https://www.parkrun.co.nz/parkrunner/902393/");
        Athlete firstWoman = Athlete.fromAthleteSummaryLink("Terry EVANS", "https://www.parkrun.co.nz/parkrunner/12345/");
        athleteDao.insert(firstWoman);
        athleteDao.insert(firstMan);

        Course course = courseDao.insert(new Course(Course.NO_COURSE_ID, "cornwall", NZ, null, Status.RUNNING));

        CourseEventSummary ces = new CourseEventSummary(
                course, 1, DateConverter.parseWebsiteDate("25/12/2023"), 1234, Optional.of(firstMan), Optional.of(firstWoman));
        dao.insert(ces);

        List<CourseDate> courseStartDates = dao.getCourseStartDates();
        assertThat(courseStartDates.size()).isEqualTo(1);

        CourseDate courseDate = courseStartDates.get(0);
        assertThat(courseDate.course.courseId).isEqualTo(course.courseId);
        assertThat(courseDate.date).isEqualTo(DateConverter.parseWebsiteDate("25/12/2023"));
    }
}
