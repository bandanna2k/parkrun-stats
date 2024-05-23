package dnt.parkrun.database.individualqueries;

import dnt.parkrun.database.AthleteDao;
import dnt.parkrun.database.Database;
import dnt.parkrun.database.LiveDatabase;
import dnt.parkrun.database.ResultDao;
import dnt.parkrun.datastructures.Athlete;
import dnt.parkrun.datastructures.Country;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static dnt.parkrun.database.DataSourceUrlBuilder.getDataSourceUrl;
import static dnt.parkrun.datastructures.Country.NZ;

public class IndividualTest
{
    private Country country = NZ;
    private AthleteDao athleteDao;
    private ResultDao resultDao;
    private Map<Integer, Athlete> athleteToName;

    @Before
    public void setUp() throws Exception
    {
        Database database = new LiveDatabase(country, getDataSourceUrl(), "stats", "4b0e7ff1");
        athleteDao = new AthleteDao(database);
        resultDao = new ResultDao(database);

        athleteToName = athleteDao.getAllAthletes();
    }

    @Test
    public void bestFriendsNz()
    {
        howManyRunsWithOthers(new Object[][] {
                { "David NORTH", 414811 },
                { "John PAYNTER", 417537 },
                { "Zoe NORTH", 4072508 },
                { "Martin SULLIVAN", 291411 }
        });
    }

    @Test
    public void howManyRunsWithOthers()
    {
        howManyRunsWithOthers(new Object[][] {
                { "David NORTH", 414811 },
                { "John PAYNTER", 417537 },
                { "Zoe NORTH", 4072508 },
                { "Martin SULLIVAN", 291411 }
        });
    }
    public void howManyRunsWithOthers(Object[][] nameAndAthleteId)
    {
        List<HowManyRunsWithOthers> processors = Arrays.stream(nameAndAthleteId).map(objects ->
        {
            int athleteId = (int) objects[1];
            return new HowManyRunsWithOthers(athleteId);
        }).collect(Collectors.toList());
        resultDao.tableScan(result -> {
            processors.forEach(processor -> processor.visitInOrder(result));
        }, "order by course_id desc, date desc");

        for (HowManyRunsWithOthers processor : processors)
        {
            Athlete athlete = athleteToName.get(processor.inputAthleteId);
            List<AthleteIdCount> listOfRunsWithOthers = processor.after();

            for (int j = 0; j < Math.min(20, listOfRunsWithOthers.size()); j++)
            {
                AthleteIdCount runWithOther = listOfRunsWithOthers.get(j);
                Athlete otherAthlete = athleteToName.get(runWithOther.athleteId);
                System.out.printf("%s has run %d times with %s.%n", athlete.name, runWithOther.count, otherAthlete.name);
            }
            System.out.println();
        }
    }

    @Test
    public void howManyRunsWithFriends()
    {
        howManyRunsWithFriends(414811, new int[] { 4072508, 403732, 116049, 1 } );
    }
    public void howManyRunsWithFriends(int inputAthleteId, int[] friendAthleteIds)
    {
        HowManyRunsWithFriends processor = new HowManyRunsWithFriends(inputAthleteId, friendAthleteIds);
        resultDao.tableScan(processor::visitInOrder, "order by course_id desc, date desc");
        Athlete athlete = athleteToName.get(processor.inputAthleteId);
        List<AthleteIdCount> runsWithFriends = processor.after();

        for (AthleteIdCount runsWithFriend : runsWithFriends)
        {
            Athlete friend = athleteToName.get(runsWithFriend.athleteId);
            if(friend == null)
            {
                System.out.println("Count not find friend with this ID. " + runsWithFriend.athleteId);
                continue;
            }

            System.out.printf("%s. Your friend %s, you have ran with %d amount of times in NZ.%n",
                    athlete.name, friend.name, runsWithFriend.count);
        }
        System.out.println();
    }
}
