package dnt.parkrun.database.individualqueries;

import com.mysql.jdbc.Driver;
import dnt.parkrun.common.DateConverter;
import dnt.parkrun.database.AthleteDao;
import dnt.parkrun.database.ResultDao;
import dnt.parkrun.database.weekly.Top10AtCourseDao;
import dnt.parkrun.datastructures.Athlete;
import dnt.parkrun.datastructures.stats.AtEvent;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;
import java.util.*;
import java.util.stream.Collectors;

public class BestFriendsTest
{
    private AthleteDao athleteDao;
    private ResultDao resultDao;
    private Map<Integer, Athlete> athleteToName;
    private Top10AtCourseDao top10AtCourseDao;

    @Before
    public void setUp() throws Exception
    {
        System.setProperty("TEST", "false");

        final DataSource dataSource = new SimpleDriverDataSource(new Driver(),
                "jdbc:mysql://localhost/parkrun_stats", "dao", "daoFractaldao");
        final DataSource weeklyDataSource = new SimpleDriverDataSource(new Driver(),
                "jdbc:mysql://localhost/weekly_stats", "stats", "statsfractalstats");
        athleteDao = new AthleteDao(dataSource);
        resultDao = new ResultDao(dataSource);

        athleteToName = athleteDao.getAllAthletes();

        top10AtCourseDao = Top10AtCourseDao.getInstance(weeklyDataSource, DateConverter.parseWebsiteDate("13/04/2024"));
    }

    @Test
    public void bestFriendsNz()
    {
        Set<Integer> highRunCountAthletes = new HashSet<>();
        List<AtEvent> top10InRegion = top10AtCourseDao.getTop10InRegion(50);
        top10InRegion.forEach(atEvent -> highRunCountAthletes.add(atEvent.athlete.athleteId));

        List<HowManyRunsWithOthers> processors = highRunCountAthletes.stream()
                .map(HowManyRunsWithOthers::new).collect(Collectors.toList());

        Map<Integer, AthleteIdCount> athleteIdToFriendCount = new HashMap<>();
        resultDao.tableScan(result ->
        {
            processors.forEach(processor -> processor.visitInOrder(result));
        }, "order by course_id desc, date desc");

        processors.forEach(processor ->
        {
            AthleteIdCount athleteIdCount = processor.after().get(0);
            athleteIdToFriendCount.put(processor.inputAthleteId, athleteIdCount);
        });

        athleteIdToFriendCount
                .entrySet().stream()
                .sorted((r1, r2) ->
                {
                    if (r1.getValue().count < r2.getValue().count) return 1;
                    if (r1.getValue().count > r2.getValue().count) return -1;
                    return 0;
                })
                .forEach(entry ->
                {
                    int athleteId = entry.getKey();
                    AthleteIdCount bffAthleteIdCount = entry.getValue();
                    Athlete athlete = athleteToName.get(athleteId);
                    Athlete bff = athleteToName.get(bffAthleteIdCount.athleteId);

                    System.out.printf("%s has run %d with %s.%n", athlete.name, bffAthleteIdCount.count, bff.name);
                });
    }
}