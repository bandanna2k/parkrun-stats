package dnt.parkrun.database;

import dnt.parkrun.datastructures.Athlete;
import dnt.parkrun.datastructures.Volunteer;
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
            volunteerDao = new VolunteerDao(dataSource);
            athleteDao = new AthleteDao(dataSource);

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

        @Before
        public void setUp() throws Exception
        {
            volunteerDao = new VolunteerDao(dataSource);
            athleteDao = new AthleteDao(dataSource);

            jdbc.update("delete from event_volunteer", EmptySqlParameterSource.INSTANCE);
            jdbc.update("delete from athlete", EmptySqlParameterSource.INSTANCE);
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
            int courseId1 = 1;
            for (int i = 0; i < MIN_VOLUNTEER_COUNT; i++)
            {
                volunteerDao.insert(new Volunteer(courseId1++, Date.from(instant), athlete));
                instant = instant.plus(7, ChronoUnit.DAYS);
            }
            volunteerDao.insert(new Volunteer(ELLIÐAÁRDALUR.courseId, Date.from(instant), athlete));
            volunteerDao.insert(new Volunteer(CORNWALL.courseId, Date.from(instant), athlete));

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
}
