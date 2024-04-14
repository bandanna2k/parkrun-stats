package dnt.parkrun.database.individualqueries;

import com.mysql.jdbc.Driver;
import dnt.parkrun.database.AthleteDao;
import dnt.parkrun.database.ResultDao;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class IndividualTest
{
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
        howManyRunsWithOthers(new Object[][] {
                { "David NORTH", 414811 },
                { "John PAYNTER", 417537 },
                { "Zoe NORTH", 4072508 },
                { "Martin SULLIVAN", 291411 }
        });
    }
    public void howManyRunsWithOthers(Object[][] nameAndAthleteId)
    {
        System.out.println(nameAndAthleteId);
        List<HowManyRunsWithOthers> howManyRunsWithOthers = Arrays.stream(nameAndAthleteId).map(objects ->
                new HowManyRunsWithOthers((int) objects[1])).collect(Collectors.toList());
        System.out.println(howManyRunsWithOthers);
        resultDao.tableScan(result -> {
            howManyRunsWithOthers.stream().forEach(proceesor -> proceesor.visitInOrder(result));
        }, "order by course_id desc, date desc");

        for (int i = 0; i < howManyRunsWithOthers.size(); i++)
        {
            Object[] objects = nameAndAthleteId[i];
            System.out.println(objects[0]);
            howManyRunsWithOthers.get(i).after();
            System.out.println();
        }
    }

    @Test
    public void howManyRunsWithFriends()
    {
        howManyRunsWithFriends(414811, new int[] { 4072508, 403732, 116306, 1 } );
    }
    public void howManyRunsWithFriends(int inputAthleteId, int[] friendAthleteIds)
    {
        HowManyRunsWithFriends processor = new HowManyRunsWithFriends(inputAthleteId, friendAthleteIds);
        resultDao.tableScan(processor::visitInOrder, "order by course_id desc, date desc");
        processor.after();
    }
}
