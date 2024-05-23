package dnt.parkrun.stats.invariants.postdownload;

import dnt.parkrun.database.Database;
import dnt.parkrun.database.LiveDatabase;
import dnt.parkrun.database.stats.MostEventsDao;
import dnt.parkrun.database.weekly.PIndexDao;
import dnt.parkrun.stats.invariants.AbstractDatabaseInvariantTest;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static dnt.parkrun.common.ParkrunDay.getParkrunDay;
import static dnt.parkrun.database.DataSourceUrlBuilder.getDataSourceUrl;

public class DatabaseWeeklyResultsInvariantTest extends AbstractDatabaseInvariantTest
{
    @Test
    public void weeklyMostEventCountsShouldBeTheSameOrEqual()
    {
        Database database = new LiveDatabase(country, getDataSourceUrl(), "stats", "4b0e7ff1");

        Date parkrunDay = getParkrunDay(new Date());
        List<MostEventsDao.MostEventsRecord> recordsToCheck = new ArrayList<>();
        {
            Date lastWeek = Date.from(parkrunDay.toInstant().minus(7, ChronoUnit.DAYS));
            MostEventsDao mostEventsDao = MostEventsDao.getOrCreate(database, lastWeek);
            List<MostEventsDao.MostEventsRecord> mostEvents = mostEventsDao.getMostEvents();

            Assertions.assertThat(mostEvents.size()).isGreaterThan(11);

            for (int i = 0; i < 10; i++)
            {
                recordsToCheck.add(mostEvents.get(i));
            }
            recordsToCheck.add(mostEvents.getLast());

        }
        recordsToCheck.forEach(System.out::println);

        {
            MostEventsDao mostEventsDao = MostEventsDao.getOrCreate(database, parkrunDay);
            List<MostEventsDao.MostEventsRecord> mostEventsForThisWeek = mostEventsDao.getMostEvents();

            recordsToCheck.forEach(recordToCheckFromLastWeek -> {
                MostEventsDao.MostEventsRecord recordToCheckThisWeek = mostEventsForThisWeek.stream()
                        .filter(r -> r.athlete.athleteId == recordToCheckFromLastWeek.athlete.athleteId).findFirst().orElseThrow();
                Assertions.assertThat(recordToCheckThisWeek.totalRuns).isGreaterThanOrEqualTo(recordToCheckFromLastWeek.totalRuns);
                Assertions.assertThat(recordToCheckThisWeek.totalRegionRuns).isGreaterThanOrEqualTo(recordToCheckFromLastWeek.totalRegionRuns);
                Assertions.assertThat(recordToCheckThisWeek.differentCourseCount).isGreaterThanOrEqualTo(recordToCheckFromLastWeek.differentCourseCount);
                Assertions.assertThat(recordToCheckThisWeek.differentRegionCourseCount).isGreaterThanOrEqualTo(recordToCheckFromLastWeek.differentRegionCourseCount);
            });
        }
    }

    @Test
    public void weeklyPIndexShouldBeTheSameOrEqual()
    {
        Database database = new LiveDatabase(country, getDataSourceUrl(), "stats", "4b0e7ff1");

        Date parkrunDay = getParkrunDay(new Date());
        List<PIndexDao.PIndexRecord> recordsToCheck = new ArrayList<>();
        {
            Date lastWeek = Date.from(parkrunDay.toInstant().minus(7, ChronoUnit.DAYS));
            PIndexDao pIndexDao = new PIndexDao(database, lastWeek);
            List<PIndexDao.PIndexRecord> pIndexRecords = pIndexDao.getPIndexRecords(lastWeek);

            Assertions.assertThat(pIndexRecords.size()).isGreaterThan(11);

            for (int i = 0; i < 10; i++)
            {
                recordsToCheck.add(pIndexRecords.get(i));
            }
            recordsToCheck.add(pIndexRecords.getLast());

        }
//        recordsToCheck.forEach(System.out::println);

        {
            PIndexDao pIndexDao = new PIndexDao(database, parkrunDay);
            recordsToCheck.forEach(recordToCheckFromLastWeek -> {
                PIndexDao.PIndexRecord recordToCheckThisWeek = pIndexDao.getPIndexForAthlete(recordToCheckFromLastWeek.athleteId);
                Assertions.assertThat(recordToCheckThisWeek.pIndex).isGreaterThanOrEqualTo(recordToCheckFromLastWeek.pIndex);
            });
        }
    }
}
