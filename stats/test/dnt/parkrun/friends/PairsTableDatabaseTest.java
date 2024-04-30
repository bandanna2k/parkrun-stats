package dnt.parkrun.friends;

import com.mysql.jdbc.Driver;
import dnt.parkrun.database.AthleteDao;
import dnt.parkrun.database.CourseDao;
import dnt.parkrun.database.ResultDao;
import dnt.parkrun.datastructures.Athlete;
import dnt.parkrun.datastructures.Course;
import dnt.parkrun.datastructures.CourseRepository;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;
import java.util.*;

import static dnt.parkrun.database.DataSourceUrlBuilder.getDataSourceUrl;

public class PairsTableDatabaseTest
{
    private List<Athlete> athletes;
    private ResultDao resultDao;
    private AthleteDao athleteDao;
    private CourseRepository courseRepository;

    @Before
    public void setUp() throws Exception
    {
        final DataSource dataSource = new SimpleDriverDataSource(new Driver(),
                getDataSourceUrl("parkrun_stats"), "stats", "statsfractalstats");
        resultDao = new ResultDao(dataSource);
        athleteDao = new AthleteDao(dataSource);
        courseRepository = new CourseRepository();
        new CourseDao(dataSource, courseRepository);

        athletes = new ArrayList<>();
        athletes.add(Athlete.from("Julie GORDON", 293223));
        athletes.add(Athlete.from("Sarah JANTSCHER", 1048005));
        athletes.add(Athlete.from("Dan JOE", 4225353));
        athletes.add(Athlete.from("Paul GORDON", 293227));
        athletes.add(Athlete.from("Alison KING", 2147564));
        athletes.add(Athlete.from("Andrew CAPEL", 141825));
        athletes.add(Athlete.from("Andy MEARS", 2225176));
        athletes.add(Athlete.from("Gene RAND", 391111));
        athletes.add(Athlete.from("Martin O'SULLIVAN", 291411));
        athletes.add(Athlete.from("Yvonne TSE", 1590564));
        athletes.add(Athlete.from("Richard NORTH", 116049));
        athletes.add(Athlete.from("David NORTH", 414811));
        athletes.add(Athlete.from("Allan JANES", 547976));
        athletes.add(Athlete.from("Zoe NORTH", 4072508));
        athletes.sort(Comparator.comparingInt(r -> -r.athleteId));
    }

    @Test
    public void test1()
    {
        Map<Integer, Athlete> allAthletes = athleteDao.getAllAthletes();

        PairsTable<Athlete> pairsTable = new PairsTable<>(athletes);

        System.out.printf("%1$10s\t", "");
        pairsTable.consumeFirst((rowAthlete, athletes) -> {
            athletes.forEach(colAthlete -> {
                String name = colAthlete.name;
                System.out.printf("%s", name.charAt(0));
                System.out.printf("%s", name.substring(name.indexOf(" ") + 1, name.indexOf(" ") + 2));
                System.out.print("\t");
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
//            System.out.printf("%d\t", rowAthlete.athleteId);

            athletes.forEach(colAthlete -> {
                String key = rowAthlete.athleteId + " " + colAthlete.athleteId;
                HowManyRunsWithFriend processor = processors.get(key);
                System.out.printf("%d\t", processors.get(key).runs.size());
                //System.out.printf("%d\t", colAthlete.athleteId);
            });
            System.out.println();
        });


        processors.values().forEach(processor -> {
            Athlete input = allAthletes.get(processor.inputAthleteId);
            Athlete friend = allAthletes.get(processor.friendAthleteId);

            if(processor.runs.size() < 10)
            {
                processor.runs.forEach(runValues ->
                {
                    int courseId = (int)runValues[0];
                    Course course = courseRepository.getCourse(courseId);
                    System.out.printf("%s\t%s\t ran together on %s at %s%n", input.name, friend.name, runValues[1], course.longName);
                });
            }
        });
    }
}