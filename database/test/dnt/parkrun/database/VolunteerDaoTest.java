package dnt.parkrun.database;

import dnt.parkrun.datastructures.*;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

import static dnt.parkrun.database.VolunteerDao.MIN_VOLUNTEER_COUNT;
import static dnt.parkrun.datastructures.AgeCategory.*;
import static dnt.parkrun.datastructures.Country.NZ;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Enclosed.class)
public class VolunteerDaoTest extends BaseDaoTest
{
    public static class Insert extends BaseDaoTest
    {
        private VolunteerDao volunteerDao;
        private AthleteDao athleteDao;

        @Before
        public void setUp() throws Exception
        {
            volunteerDao = new VolunteerDao(country, dataSource);
            athleteDao = new AthleteDao(dataSource);

            jdbc.update("delete from course", EmptySqlParameterSource.INSTANCE);
            jdbc.update("delete from event_volunteer", EmptySqlParameterSource.INSTANCE);
            jdbc.update("delete from athlete", EmptySqlParameterSource.INSTANCE);
        }

        @Test
        public void shouldInsertAndReturnResult()
        {
            Athlete athlete = Athlete.fromAthleteSummaryLink("Massimilino PAGININ", "https://www.parkrun.co.nz/parkrunner/7001007/");
            athleteDao.insert(athlete);

            Volunteer volunteer = new Volunteer(100, new Date(), athlete);
            volunteerDao.insert(volunteer);

            List<Volunteer> results = volunteerDao.getVolunteers();
            Assertions.assertThat(results).isNotEmpty();
            System.out.println(results);
        }
    }

    public static class MostVolunteers extends BaseDaoTest
    {
        private VolunteerDao volunteerDao;
        private AthleteDao athleteDao;
        private CourseDao courseDao;

        @Before
        public void setUp() throws Exception
        {
            volunteerDao = new VolunteerDao(country, dataSource);
            athleteDao = new AthleteDao(dataSource);
            CourseRepository courseRepository = new CourseRepository();
            courseDao = new CourseDao(dataSource, courseRepository);

            jdbc.update("delete from event_volunteer", EmptySqlParameterSource.INSTANCE);
            jdbc.update("delete from athlete", EmptySqlParameterSource.INSTANCE);
            jdbc.update("delete from course", EmptySqlParameterSource.INSTANCE);
        }

        @Test
        public void shouldGetMostVolunteerCount()
        {
            Athlete athlete = Athlete.fromAthleteSummaryLink("Massimilino PAGININ", "https://www.parkrun.co.nz/parkrunner/7001007/");
            athleteDao.insert(athlete);

            Instant instant = Instant.EPOCH;
            int courseId1 = 1;
            for (int i = 0; i < MIN_VOLUNTEER_COUNT; i++)
            {
                volunteerDao.insert(new Volunteer(courseId1++, Date.from(instant), athlete));
                instant = instant.plus(7, ChronoUnit.DAYS);
            }

            Assertions.assertThat(volunteerDao.getMostVolunteers().size()).isEqualTo(1);
            Assertions.assertThat(volunteerDao.getMostVolunteers().get(0)[2]).isEqualTo(20);
        }

        @Test
        public void mostVolunteerCountShouldNotIncludeVolunteersOnTheSameDayButDifferentCourse()
        {
            Athlete athlete = Athlete.fromAthleteSummaryLink("Massimilino PAGININ", "https://www.parkrun.co.nz/parkrunner/7001007/");
            athleteDao.insert(athlete);

            Instant instant = Instant.EPOCH;
            for (int i = 0; i < MIN_VOLUNTEER_COUNT; i++)
            {
                String name = "course" + i;
                Course course = courseDao.insert(new Course(Course.NO_COURSE_ID, name, NZ, name, Course.Status.RUNNING));
                volunteerDao.insert(new Volunteer(course.courseId, Date.from(instant), athlete));
                instant = instant.plus(7, ChronoUnit.DAYS);
            }
            Course icelandicCourse = courseDao.insert(ELLIÐAÁRDALUR);
            Course cornwall = courseDao.insert(CORNWALL);

            volunteerDao.insert(new Volunteer(icelandicCourse.courseId, Date.from(instant), athlete));
            volunteerDao.insert(new Volunteer(cornwall.courseId, Date.from(instant), athlete));

            Assertions.assertThat(volunteerDao.getMostVolunteers().size()).isEqualTo(1);
            Assertions.assertThat(volunteerDao.getMostVolunteers().get(0)[2]).isEqualTo(21);
        }

        @Test
        public void shouldNotReturnWithNotEnoughCourseRuns()
        {
            Athlete athlete = Athlete.fromAthleteSummaryLink("Massimilino PAGININ", "https://www.parkrun.co.nz/parkrunner/7001007/");
            athleteDao.insert(athlete);

            Instant instant = Instant.EPOCH;
            int courseId1 = 1;
            for (int i = 0; i < (MIN_VOLUNTEER_COUNT - 1); i++)
            {
                volunteerDao.insert(new Volunteer(courseId1++, Date.from(instant), athlete));
                instant = instant.plus(7, ChronoUnit.DAYS);
            }

            Assertions.assertThat(volunteerDao.getMostVolunteers().size()).isEqualTo(0);
        }
    }

    public static class Delete extends BaseDaoTest
    {
        private VolunteerDao volunteerDao;
        private AthleteDao athleteDao;

        @Before
        public void setUp() throws Exception
        {
            volunteerDao = new VolunteerDao(country, dataSource);
            athleteDao = new AthleteDao(dataSource);

            jdbc.update("delete from event_volunteer", EmptySqlParameterSource.INSTANCE);
            jdbc.update("delete from athlete", EmptySqlParameterSource.INSTANCE);
        }

        @Test
        public void shouldDeleteResults() throws InterruptedException
        {
            athleteDao.insert(janeDoe);
            athleteDao.insert(johnDoe);
            athleteDao.insert(juniorDoeAdere);
            {
                Result result1 = new Result(501, EPOCH_PLUS_7, 1, johnDoe, Time.from("1:31:02"), VM45_49, AgeGrade.newInstance(68.49));
                volunteerDao.insert(new Volunteer(result1.courseId, result1.date, result1.athlete));
            }
            {
                Result result1 = new Result(502, EPOCH_PLUS_14, 1, johnDoe, Time.from("1:32:02"), VM45_49, AgeGrade.newInstance(68.49));
                Result result2 = new Result(502, EPOCH_PLUS_14, 2, janeDoe, Time.from("1:32:03"), VW45_49, AgeGrade.newInstance(68.50));
                volunteerDao.insert(new Volunteer(result1.courseId, result1.date, result1.athlete));
                volunteerDao.insert(new Volunteer(result2.courseId, result2.date, result2.athlete));
            }
            {
                Result result1 = new Result(503, EPOCH_PLUS_21, 1, johnDoe, Time.from("1:32:02"), VM45_49, AgeGrade.newInstance(68.49));
                Result result2 = new Result(503, EPOCH_PLUS_21, 2, janeDoe, Time.from("1:32:03"), VW45_49, AgeGrade.newInstance(68.50));
                Result result3 = new Result(503, EPOCH_PLUS_21, 3, juniorDoeAdere, Time.from("1:32:04"), JM11_14, AgeGrade.newInstance(68.51));
                volunteerDao.insert(new Volunteer(result1.courseId, result1.date, result1.athlete));
                volunteerDao.insert(new Volunteer(result2.courseId, result2.date, result2.athlete));
                volunteerDao.insert(new Volunteer(result3.courseId, result3.date, result3.athlete));
            }
            {
                List<Volunteer> results = volunteerDao.getVolunteers();
                assertThat(results.size()).isEqualTo(6);
            }
            {
                volunteerDao.delete(502, EPOCH_PLUS_14);
                List<Volunteer> results = volunteerDao.getVolunteers();
                assertThat(results.size()).isEqualTo(4);
            }
            {
                volunteerDao.delete(501, EPOCH_PLUS_7);
                List<Volunteer> results = volunteerDao.getVolunteers();
                assertThat(results.size()).isEqualTo(3);
            }
            {
                volunteerDao.delete(503, EPOCH_PLUS_21);
                List<Volunteer> results = volunteerDao.getVolunteers();
                assertThat(results.size()).isEqualTo(0);
            }
        }
    }
}
