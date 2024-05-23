package dnt.parkrun.database;

import com.mysql.jdbc.Driver;
import dnt.parkrun.datastructures.Athlete;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;

public class AthleteDaoTest extends BaseDaoTest
{
    private AthleteDao athleteDao;

    @Before
    public void setUp() throws Exception
    {
        DataSource dataSource = new SimpleDriverDataSource(new Driver(),
                "jdbc:mysql://localhost/parkrun_stats_test", "test", "qa");
        athleteDao = new AthleteDao(TEST_DATABASE);

        jdbc = new NamedParameterJdbcTemplate(dataSource);
        jdbc.update("delete from athlete", EmptySqlParameterSource.INSTANCE);
    }

    @Test
    public void shouldInsertAthlete()
    {
        Athlete athlete = Athlete.fromAthleteSummaryLink("Davey JONES", "https://www.parkrun.co.nz/parkrunner/902393/");
        athleteDao.insert(athlete);
        System.out.println(athlete);
        assertThat(athleteDao.getAthlete(athlete.athleteId)).isNotNull();
    }

    @Test
    public void shouldUpdateAthleteName()
    {
        {
            Athlete athlete = Athlete.fromAthleteSummaryLink("Davey JONES", "https://www.parkrun.co.nz/parkrunner/902393/");
            athleteDao.insert(athlete);

            System.out.println(athlete);
            assertThat(athleteDao.getAthlete(athlete.athleteId).name).isEqualTo("Davey JONES");
        }
        {
            Athlete athlete = Athlete.fromAthleteSummaryLink("Davey JONES-BRIGHT", "https://www.parkrun.co.nz/parkrunner/902393/");
            athleteDao.insert(athlete);

            System.out.println(athlete);
            assertThat(athleteDao.getAthlete(athlete.athleteId).name).isEqualTo("Davey JONES-BRIGHT");
        }
    }
}