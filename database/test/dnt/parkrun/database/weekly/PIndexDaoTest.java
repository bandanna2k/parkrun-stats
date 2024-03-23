package dnt.parkrun.database.weekly;

import com.mysql.jdbc.Driver;
import dnt.parkrun.database.BaseDaoTest;
import dnt.parkrun.datastructures.Athlete;
import dnt.parkrun.pindex.PIndex;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;
import java.time.Instant;
import java.util.Date;

public class PIndexDaoTest extends BaseDaoTest
{
    private PIndexDao pIndexDao;

    @Before
    public void setUp() throws Exception
    {
        DataSource dataSource = new SimpleDriverDataSource(new Driver(),
                "jdbc:mysql://localhost/parkrun_stats_test", "test", "qa");
        pIndexDao = new PIndexDao(dataSource, Date.from(Instant.EPOCH));
    }

    @Test
    public void shouldInsertAndReturnResult()
    {
        Athlete athlete = Athlete.fromAthleteSummaryLink("Massimilino PAGININ", "https://www.parkrun.co.nz/parkrunner/7001007/");
        pIndexDao.writePIndexRecord(new PIndexDao.PIndexRecord(athlete.athleteId, 2, 1));

        PIndex.Result result = pIndexDao.getPIndexForAthlete(athlete.athleteId);
        Assertions.assertThat(result.pIndex).isEqualTo(2);
        Assertions.assertThat(result.neededForNextPIndex).isEqualTo(1);
    }

    @Test
    public void shouldOverwriteRecords()
    {
        Athlete athlete = Athlete.fromAthleteSummaryLink("Paolo MALDINI", "https://www.parkrun.co.nz/parkrunner/3456789/");
        pIndexDao.writePIndexRecord(new PIndexDao.PIndexRecord(athlete.athleteId, 8, 3));

        {
            PIndex.Result result = pIndexDao.getPIndexForAthlete(athlete.athleteId);
            Assertions.assertThat(result.pIndex).isEqualTo(8);
            Assertions.assertThat(result.neededForNextPIndex).isEqualTo(3);
        }
        pIndexDao.writePIndexRecord(new PIndexDao.PIndexRecord(athlete.athleteId, 8, 2));
        {
            PIndex.Result result = pIndexDao.getPIndexForAthlete(athlete.athleteId);
            Assertions.assertThat(result.pIndex).isEqualTo(8);
            Assertions.assertThat(result.neededForNextPIndex).isEqualTo(2);
        }
    }
}