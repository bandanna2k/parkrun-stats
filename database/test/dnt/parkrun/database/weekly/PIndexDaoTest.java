package dnt.parkrun.database.weekly;

import com.mysql.jdbc.Driver;
import dnt.parkrun.database.AthleteDao;
import dnt.parkrun.database.BaseDaoTest;
import dnt.parkrun.database.VolunteerDao;
import dnt.parkrun.datastructures.Athlete;
import dnt.parkrun.pindex.PIndex;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;
import java.time.Instant;
import java.util.Date;

public class PIndexDaoTest extends BaseDaoTest
{
    private VolunteerDao volunteerDao;
    private AthleteDao athleteDao;
    private NamedParameterJdbcTemplate jdbc;
    private PIndexDao pIndexDao;

    @Before
    public void setUp() throws Exception
    {
        DataSource dataSource = new SimpleDriverDataSource(new Driver(),
                "jdbc:mysql://localhost/parkrun_stats_test", "test", "qa");
        athleteDao = new AthleteDao(dataSource);
        pIndexDao = new PIndexDao(dataSource, Date.from(Instant.EPOCH));

        jdbc = new NamedParameterJdbcTemplate(dataSource);
        jdbc.update("delete from event_volunteer", EmptySqlParameterSource.INSTANCE);
        jdbc.update("delete from athlete", EmptySqlParameterSource.INSTANCE);
    }

    @Test
    public void shouldInsertAndReturnResult()
    {
        Athlete athlete = Athlete.fromAthleteSummaryLink("Massimilino PAGININ", "https://www.parkrun.co.nz/parkrunner/7001007/");
        pIndexDao.writePIndexRecord(athlete.athleteId, new PIndex.Result(2, 1));

        PIndex.Result result = pIndexDao.getPIndexForAthlete(athlete.athleteId);
        Assertions.assertThat(result.pIndex).isEqualTo(2);
        Assertions.assertThat(result.neededForNextPIndex).isEqualTo(1);
    }

    @Test
    public void shouldOverwriteRecords()
    {
        Athlete athlete = Athlete.fromAthleteSummaryLink("Paolo MALDINI", "https://www.parkrun.co.nz/parkrunner/3456789/");
        pIndexDao.writePIndexRecord(athlete.athleteId, new PIndex.Result(8, 3));

        {
            PIndex.Result result = pIndexDao.getPIndexForAthlete(athlete.athleteId);
            Assertions.assertThat(result.pIndex).isEqualTo(8);
            Assertions.assertThat(result.neededForNextPIndex).isEqualTo(3);
        }
        pIndexDao.writePIndexRecord(athlete.athleteId, new PIndex.Result(8, 2));
        {
            PIndex.Result result = pIndexDao.getPIndexForAthlete(athlete.athleteId);
            Assertions.assertThat(result.pIndex).isEqualTo(8);
            Assertions.assertThat(result.neededForNextPIndex).isEqualTo(2);
        }
    }
}
