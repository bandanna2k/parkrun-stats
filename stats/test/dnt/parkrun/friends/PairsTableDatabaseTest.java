package dnt.parkrun.friends;

import com.mysql.jdbc.Driver;
import dnt.parkrun.database.AthleteDao;
import dnt.parkrun.database.CourseDao;
import dnt.parkrun.database.ResultDao;
import dnt.parkrun.datastructures.Athlete;
import dnt.parkrun.datastructures.Country;
import dnt.parkrun.datastructures.Course;
import dnt.parkrun.datastructures.CourseRepository;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;
import java.util.*;

import static dnt.parkrun.database.DataSourceUrlBuilder.Type.PARKRUN_STATS;
import static dnt.parkrun.database.DataSourceUrlBuilder.getDataSourceUrl;
import static dnt.parkrun.datastructures.Country.NZ;

public class PairsTableDatabaseTest
{
    private List<Athlete> athletes;
    private ResultDao resultDao;
    private AthleteDao athleteDao;
    private CourseRepository courseRepository;

    @Before
    public void setUp() throws Exception
    {
        final Country country = NZ;
        final DataSource dataSource = new SimpleDriverDataSource(new Driver(),
                getDataSourceUrl(PARKRUN_STATS, country), "stats", "statsfractalstats");
        resultDao = new ResultDao(country, dataSource);
        athleteDao = new AthleteDao(dataSource);
        courseRepository = new CourseRepository();
        new CourseDao(country, dataSource, courseRepository);

        athletes = new ArrayList<>();
        athletes.add(athleteDao.getAthlete(1340853)); // Jonathan
        athletes.add(athleteDao.getAthlete(293223)); // Julie GORDON
        athletes.add(athleteDao.getAthlete(1048005)); // Sarah JANTSCHER
        athletes.add(athleteDao.getAthlete(4225353)); // Dan JOE
        athletes.add(athleteDao.getAthlete(293227)); // Paul GORDON
        athletes.add(athleteDao.getAthlete(2147564)); // Alison KING
        athletes.add(athleteDao.getAthlete(141825)); // Andrew CAPEL
        athletes.add(athleteDao.getAthlete(2225176)); // Andy MEARS
        athletes.add(athleteDao.getAthlete(391111)); // Gene RAND
        athletes.add(athleteDao.getAthlete(291411)); // Martin O'SULLIVAN
        athletes.add(athleteDao.getAthlete(1590564)); // Yvonne TSE
        athletes.add(athleteDao.getAthlete(116049)); // Richard NORTH
        athletes.add(athleteDao.getAthlete(414811)); // David NORTH
        athletes.add(athleteDao.getAthlete(547976)); // Allan JANES
        athletes.add(athleteDao.getAthlete(4072508)); // Zoe NORTH
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