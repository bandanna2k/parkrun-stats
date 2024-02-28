import com.mysql.jdbc.Driver;
import dnt.parkrun.datastructures.Athlete;
import dnt.parkrun.mostevents.dao.AthleteDao;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;

public class AthleteDaoTest
{
    private AthleteDao athleteDao;

    public AthleteDaoTest()
    {

    }

    @Before
    public void setUp() throws Exception
    {
        DataSource dataSource = new SimpleDriverDataSource(new Driver(),
                "jdbc:mysql://localhost", "dao", "daoFractaldao");
        athleteDao = new AthleteDao(dataSource);

        NamedParameterJdbcTemplate jdbc = new NamedParameterJdbcTemplate(dataSource);
        jdbc.update("delete from parkrun_stats.athlete", EmptySqlParameterSource.INSTANCE);
    }

    @Test
    public void shouldInsertAthlete()
    {
        Athlete athlete = Athlete.fromAthleteSummaryLink("Davey JONES", "https://www.parkrun.co.nz/parkrunner/902393/");
        athleteDao.insert(athlete);
        System.out.println(athlete);
        assertThat(athleteDao.getAthlete(athlete.athleteId)).isNotNull();
    }
}
