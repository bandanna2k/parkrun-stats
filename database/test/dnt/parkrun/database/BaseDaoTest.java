package dnt.parkrun.database;

import com.mysql.jdbc.Driver;
import dnt.parkrun.datastructures.Athlete;
import dnt.parkrun.datastructures.Course;
import org.junit.Before;
import org.junit.BeforeClass;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;

import static dnt.parkrun.database.DataSourceUrlBuilder.getDataSourceUrl;
import static dnt.parkrun.datastructures.Country.NZ;
import static dnt.parkrun.datastructures.Country.UNKNOWN;
import static dnt.parkrun.datastructures.Course.NO_COURSE_ID;
import static org.junit.Assume.assumeTrue;

public abstract class BaseDaoTest
{
    public static final Course ELLIÐAÁRDALUR =
            new Course(NO_COURSE_ID, "ellidaardalur", UNKNOWN, "Elliðaárdalur", Course.Status.RUNNING);
    public static final Course CORNWALL =
            new Course(NO_COURSE_ID, "cornwall", NZ, "Cornwall Park", Course.Status.RUNNING);

    public static Athlete janeDoe = Athlete.fromAthleteSummaryLink(
            "Jane DOE", "https://www.parkrun.co.nz/parkrunner/902393/");
    public static Athlete johnDoe = Athlete.fromAthleteSummaryLink(
            "John DOE", "https://www.parkrun.co.nz/parkrunner/902394/");
    public static Athlete juniorDoeAdere = Athlete.fromAthleteSummaryLink(
            "Junior DOE-ADERE", "https://www.parkrun.co.nz/parkrunner/902395/");



    @BeforeClass
    public static void beforeClass()
    {
        assumeTrue("Env TEST not on: " + System.getProperty("TEST"), BaseDaoTest.isTesting());
    }

    static boolean isTesting()
    {
        return null != System.getProperty("TEST") && Boolean.parseBoolean(System.getProperty("TEST"));
    }



    protected DataSource dataSource;
    protected NamedParameterJdbcTemplate jdbc;

    @Before
    public void baseClassSetUp() throws Exception
    {
        dataSource = new SimpleDriverDataSource(new Driver(),
                getDataSourceUrl("parkrun_stats_test"), "test", "qa");
        jdbc = new NamedParameterJdbcTemplate(dataSource);
    }
}