package dnt.parkrun.database.weekly;

import dnt.parkrun.database.BaseDaoTest;
import dnt.parkrun.datastructures.Athlete;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;
import java.util.Date;

public class PIndexDaoTest extends BaseDaoTest
{
    private PIndexDao pIndexDao;

    @Before
    public void setUp() throws Exception
    {
        pIndexDao = PIndexDao.getInstance(TEST_DATABASE, Date.from(Instant.EPOCH));
    }

    @Test
    public void shouldInsertAndReturnResult()
    {
        Athlete athlete = Athlete.fromAthleteSummaryLink("Massimilino PAGININ", "https://www.parkrun.co.nz/parkrunner/7001007/");
        pIndexDao.writePIndexRecord(new PIndexDao.PIndexRecord(athlete.athleteId, 2, 1, 1.0));

        PIndexDao.PIndexRecord result = pIndexDao.getPIndexForAthlete(athlete.athleteId);
        Assertions.assertThat(result.pIndex).isEqualTo(2);
        Assertions.assertThat(result.neededForNextPIndex).isEqualTo(1);
    }

    @Test
    public void shouldOverwriteRecords()
    {
        Athlete athlete = Athlete.fromAthleteSummaryLink("Paolo MALDINI", "https://www.parkrun.co.nz/parkrunner/3456789/");
        pIndexDao.writePIndexRecord(new PIndexDao.PIndexRecord(athlete.athleteId, 8, 3, 1.0));

        {
            PIndexDao.PIndexRecord result = pIndexDao.getPIndexForAthlete(athlete.athleteId);
            Assertions.assertThat(result.pIndex).isEqualTo(8);
            Assertions.assertThat(result.neededForNextPIndex).isEqualTo(3);
        }
        pIndexDao.writePIndexRecord(new PIndexDao.PIndexRecord(athlete.athleteId, 8, 2, 1.0));
        {
            PIndexDao.PIndexRecord result = pIndexDao.getPIndexForAthlete(athlete.athleteId);
            Assertions.assertThat(result.pIndex).isEqualTo(8);
            Assertions.assertThat(result.neededForNextPIndex).isEqualTo(2);
        }
    }
}
