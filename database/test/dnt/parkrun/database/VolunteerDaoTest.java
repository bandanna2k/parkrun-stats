package dnt.parkrun.database;

import com.mysql.jdbc.Driver;
import dnt.parkrun.datastructures.Athlete;
import dnt.parkrun.datastructures.Volunteer;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

import static dnt.parkrun.database.VolunteerDao.MIN_VOLUNTEER_COUNT;

public class VolunteerDaoTest extends BaseDaoTest
{
    private VolunteerDao volunteerDao;
    private AthleteDao athleteDao;
    private NamedParameterJdbcTemplate jdbc;

    @Before
    public void setUp() throws Exception
    {
        DataSource dataSource = new SimpleDriverDataSource(new Driver(),
                "jdbc:mysql://localhost/parkrun_stats_test", "test", "qa");
        volunteerDao = new VolunteerDao(dataSource);
        athleteDao = new AthleteDao(dataSource);

        jdbc = new NamedParameterJdbcTemplate(dataSource);
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

    @Test
    public void shouldNotAccountTwoVolunteersOnTheSameDayAsTwoShouldBeOne()
    {
        Athlete athlete = Athlete.fromAthleteSummaryLink("Massimilino PAGININ", "https://www.parkrun.co.nz/parkrunner/7001007/");
        athleteDao.insert(athlete);

        Instant instant = Instant.EPOCH;
        int courseId1 = 1;
        int courseId2 = 1000;
        for (int i = 0; i < MIN_VOLUNTEER_COUNT; i++)
        {
            volunteerDao.insert(new Volunteer(courseId1++, Date.from(instant), athlete));
            volunteerDao.insert(new Volunteer(courseId2++, Date.from(instant), athlete));
            instant = instant.plus(7, ChronoUnit.DAYS);
        }

        Assertions.assertThat(volunteerDao.getMostVolunteers().size()).isEqualTo(1);
    }
}
