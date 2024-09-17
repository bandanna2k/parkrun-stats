package dnt.parkrun.database.weekly;

import dnt.parkrun.database.AthleteDao;
import dnt.parkrun.database.BaseDaoTest;
import dnt.parkrun.database.VolunteerDao;
import dnt.parkrun.datastructures.Athlete;
import dnt.parkrun.datastructures.Volunteer;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import static dnt.parkrun.database.VolunteerDao.MIN_VOLUNTEER_COUNT;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Enclosed.class)
public class VolunteerCountDaoTest extends BaseDaoTest
{
    public static class MostVolunteers extends BaseDaoTest
    {
        private VolunteerDao volunteerDao;
        private AthleteDao athleteDao;
        private VolunteerCountDao volunteerCountDao;

        @Before
        public void setUp() throws Exception
        {
            volunteerDao = new VolunteerDao(TEST_DATABASE);
            athleteDao = new AthleteDao(TEST_DATABASE);
            volunteerCountDao = VolunteerCountDao.getInstance(TEST_DATABASE, Date.from(Instant.EPOCH));

            jdbc.update("delete from event_volunteer", EmptySqlParameterSource.INSTANCE);
            jdbc.update("delete from athlete", EmptySqlParameterSource.INSTANCE);
            jdbc.update("delete from volunteer_count_1970_01_01", EmptySqlParameterSource.INSTANCE);
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

            volunteerCountDao.insertVolunteerCount(athlete.athleteId, 25, 5, 3);

            assertThat(volunteerDao.getMostVolunteers().size()).isEqualTo(1);
            assertThat(volunteerDao.getMostVolunteers().get(0)[2]).isEqualTo(20);

            assertThat(volunteerCountDao.getMostVolunteers().size()).isEqualTo(1);
            assertThat(volunteerCountDao.getMostVolunteers().get(0)[2]).isEqualTo(20);
            assertThat(volunteerCountDao.getMostVolunteers().get(0)[3]).isEqualTo(25);
        }
    }
}
