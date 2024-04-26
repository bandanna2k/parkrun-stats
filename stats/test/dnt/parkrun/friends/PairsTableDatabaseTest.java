package dnt.parkrun.friends;

import com.mysql.jdbc.Driver;
import dnt.parkrun.database.ResultDao;
import dnt.parkrun.datastructures.Athlete;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;
import java.util.*;

public class PairsTableDatabaseTest
{
    private final Random random = new Random();

    private List<Athlete> athletes;
    private ResultDao resultDao;

    @Before
    public void setUp() throws Exception
    {
        final DataSource dataSource = new SimpleDriverDataSource(new Driver(),
                "jdbc:mysql://localhost/parkrun_stats", "dao", "daoFractaldao");
        resultDao = new ResultDao(dataSource);

        athletes = new ArrayList<>();
        athletes.add(Athlete.from("Julie GORDON", 293223));
        athletes.add(Athlete.from("Dan JOE", 4225353));
        athletes.add(Athlete.from("Paul GORDON", 293227));
        athletes.add(Athlete.from("Alison KING", 2147564));
        athletes.add(Athlete.from("Andrew CAPEL", 141825));
        athletes.add(Athlete.from("Andy MEARS", 2225176));
        athletes.add(Athlete.from("Martin O'SULLIVAN", 291411));
        athletes.add(Athlete.from("Yvonne TSE", 1590564));
        athletes.add(Athlete.from("David NORTH", 414811));
        athletes.add(Athlete.from("Zoe NORTH", 4072508));
    }

    @Test
    public void test1()
    {
        PairsTable<Athlete> pairsTable = new PairsTable<>(athletes);

        System.out.printf("%1$10s\t", "");
        pairsTable.consumeFirst((rowAthlete, athletes) -> {
            athletes.forEach(colAthlete -> {
                System.out.printf("%s\t", colAthlete.name.charAt(0));
            });
        });
        System.out.println();

        Map<String, HowManyRunsWithFriend> processors = new HashMap<>();
        pairsTable.forEach((rowAthlete, athletes) -> {
            athletes.forEach(colAthlete -> {
                String key = rowAthlete.athleteId + " " + colAthlete.athleteId;
                processors.put(key, new HowManyRunsWithFriend(rowAthlete.athleteId, colAthlete.athleteId));
            });
        });

        resultDao.tableScan(result -> {
            processors.values().forEach(processor -> processor.visitInOrder(result));
        }, "order by course_id desc, date desc");

        pairsTable.forEach((rowAthlete, athletes) -> {

            String name10Characters = String.format("%1$10s", rowAthlete.name).substring(0, 10); // Row names
            System.out.printf(name10Characters + "\t");

            athletes.forEach(colAthlete -> {
                String key = rowAthlete.athleteId + " " + colAthlete.athleteId;
                HowManyRunsWithFriend processor = processors.get(key);
                System.out.printf("%d\t", processors.get(key).runs.size());
                //System.out.printf("%s\t", processor.inputAthleteId);
            });
            System.out.println();
        });
    }
}