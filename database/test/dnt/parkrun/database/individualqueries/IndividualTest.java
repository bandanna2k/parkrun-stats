package dnt.parkrun.database.individualqueries;

import com.mysql.jdbc.Driver;
import dnt.parkrun.database.AthleteDao;
import dnt.parkrun.database.ResultDao;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class IndividualTest
{
    DataSource dataSource;
    AthleteDao athleteDao;
    ResultDao resultDao;

    @Before
    public void setUp() throws Exception
    {
        final DataSource dataSource = new SimpleDriverDataSource(new Driver(),
                "jdbc:mysql://localhost/parkrun_stats", "dao", "daoFractaldao");
        athleteDao = new AthleteDao(dataSource);
        resultDao = new ResultDao(dataSource);
    }

    @Test
    public void howManyRunsWithOthers()
    {
//        howManyRunsWithOthers(414811); // David North
//        howManyRunsWithOthers(417537); // John Paynter
        howManyRunsWithOthers(4072508);
    }
    public void howManyRunsWithOthers(int inputAthleteId)
    {
        Map<Integer, Integer> athleteIdToCount = new HashMap<>();

        AtomicInteger prevCourseId = new AtomicInteger(-1);
        AtomicReference<Date> prevDate = new AtomicReference<>();

        List<Integer> athleteIdsAtCourse = new ArrayList<>();
        AtomicBoolean didInputAthleteRun = new AtomicBoolean(false);

        resultDao.tableScan(result -> {

            int scanCourseId = result.courseId;
            Date scanDate = result.date;
            int scanAthleteId = result.athlete.athleteId;

            if(prevCourseId.get() != scanCourseId || !scanDate.equals(prevDate.get()))
            {
                // Next course
                if(didInputAthleteRun.get())
                {
                    athleteIdsAtCourse.forEach(coathleteId -> {
                        Integer count = athleteIdToCount.get(coathleteId);
                        if(null == count)
                        {
                            athleteIdToCount.put(coathleteId, 1);
                        }
                        else
                        {
                            athleteIdToCount.put(coathleteId, count + 1);
                        }
                    });
                }

                // Reset variables
                prevDate.set(scanDate);
                prevCourseId.set(scanCourseId);
                athleteIdsAtCourse.clear();
                didInputAthleteRun.set(false);
            }

            if(scanAthleteId == inputAthleteId)
            {
                didInputAthleteRun.set(true);
                return;
            }

            // Add athlete to temp list
            athleteIdsAtCourse.add(result.athlete.athleteId);

        }, "order by course_id desc, date desc");

        List<AthleteIdCount> listOfCrossRuns = athleteIdToCount.entrySet().stream()
                .map(entry -> new AthleteIdCount(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
        listOfCrossRuns.sort((r1, r2) -> {
            if(r1.count < r2.count) return 1;
            if(r1.count > r2.count) return -1;
            if(r1.athleteId > r2.athleteId) return 1;
            if(r1.athleteId < r2.athleteId) return -1;
            return 0;
        });

        for (int i = 0; i < Math.min(20, listOfCrossRuns.size()); i++)
        {
            AthleteIdCount athleteIdCount = listOfCrossRuns.get(i);
            System.out.printf("%d:\t%d\t%d%n", i, athleteIdCount.athleteId, athleteIdCount.count);
        }
        System.out.println();
    }


    @Test
    public void howManyRunsWithFriends()
    {
        howManyRunsWithFriends(414811, new int[] { 4072508, 403732, 116306, 1 } );
    }
    public void howManyRunsWithFriends(int inputAthleteId, int[] friendAthleteIds)
    {
        Map<Integer, Integer> athleteIdToCount = new HashMap<>();

        AtomicInteger prevCourseId = new AtomicInteger(-1);
        AtomicReference<Date> prevDate = new AtomicReference<>();

        List<Integer> athleteIdsAtCourse = new ArrayList<>();
        AtomicBoolean didInputAthleteRun = new AtomicBoolean(false);

        resultDao.tableScan(result -> {

            int scanCourseId = result.courseId;
            Date scanDate = result.date;
            int scanAthleteId = result.athlete.athleteId;

            if(prevCourseId.get() != scanCourseId || !scanDate.equals(prevDate.get()))
            {
                // Next course
                if(didInputAthleteRun.get())
                {
                    athleteIdsAtCourse.forEach(coathleteId -> {
                        Integer count = athleteIdToCount.get(coathleteId);
                        if(null == count)
                        {
                            athleteIdToCount.put(coathleteId, 1);
                        }
                        else
                        {
                            athleteIdToCount.put(coathleteId, count + 1);
                        }
                    });
                }

                // Reset variables
                prevDate.set(scanDate);
                prevCourseId.set(scanCourseId);
                athleteIdsAtCourse.clear();
                didInputAthleteRun.set(false);
            }

            if(scanAthleteId == inputAthleteId)
            {
                didInputAthleteRun.set(true);
                return;
            }

            // Add athlete to temp list
            athleteIdsAtCourse.add(result.athlete.athleteId);

        }, "order by course_id desc, date desc");

        Arrays.stream(friendAthleteIds).forEach(friendAthleteId -> {
            int count = athleteIdToCount.getOrDefault(friendAthleteId, 0);
            System.out.println("Your friend " + friendAthleteId + ", you have ran with " + count + " amount of times in NZ.");
        });



        List<AthleteIdCount> listOfCrossRuns = athleteIdToCount.entrySet().stream()
                .map(entry -> new AthleteIdCount(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
        listOfCrossRuns.sort((r1, r2) -> {
            if(r1.count < r2.count) return 1;
            if(r1.count > r2.count) return -1;
            if(r1.athleteId > r2.athleteId) return 1;
            if(r1.athleteId < r2.athleteId) return -1;
            return 0;
        });

        for (int i = 0; i < Math.min(20, listOfCrossRuns.size()); i++)
        {
            AthleteIdCount athleteIdCount = listOfCrossRuns.get(i);
            System.out.printf("%d:\t%d\t%d%n", i, athleteIdCount.athleteId, athleteIdCount.count);
        }
        System.out.println();
    }



    private static class AthleteIdCount
    {
        public final int athleteId;
        public final int count;

        public AthleteIdCount(int athleteId, int count)
        {
            this.athleteId = athleteId;
            this.count = count;
        }

        @Override
        public String toString()
        {
            return "AthleteIdCount{" +
                    "athleteId=" + athleteId +
                    ", count=" + count +
                    '}';
        }
    }
}
