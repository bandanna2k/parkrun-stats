package dnt.parkrun.database;

import com.mysql.jdbc.Driver;
import dnt.parkrun.common.DateConverter;
import dnt.parkrun.datastructures.Athlete;
import dnt.parkrun.datastructures.Course;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import java.util.Date;

import static dnt.parkrun.database.DataSourceUrlBuilder.getTestDataSourceUrl;
import static dnt.parkrun.datastructures.Country.NZ;
import static dnt.parkrun.datastructures.Country.UNKNOWN;
import static dnt.parkrun.datastructures.Course.NO_COURSE_ID;

public abstract class BaseDaoTest
{
    private static Driver DRIVER = dnt.parkrun.database.Driver.getDriver();

    public static final Date EPOCH_PLUS_7 = DateConverter.parseWebsiteDate("01/01/1970");
    public static final Date EPOCH_PLUS_14 = DateConverter.parseWebsiteDate("08/01/1970");
    public static final Date EPOCH_PLUS_21 = DateConverter.parseWebsiteDate("15/01/1970");
    public static final Date EPOCH_PLUS_28 = DateConverter.parseWebsiteDate("22/01/1970");

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


    protected NamedParameterJdbcTemplate jdbc;

    public BaseDaoTest()
    {
        jdbc = new NamedParameterJdbcTemplate(TEST_DATABASE.dataSource);
    }

    public static final Database TEST_DATABASE = new Database(
            NZ,
            new SimpleDriverDataSource(DRIVER, getTestDataSourceUrl(), "test", "qa"))
    {
        public static final String testDatabaseName = "parkrun_stats_test";

        @Override
        public String getGlobalDatabaseName()
        {
            return testDatabaseName;
        }

        @Override
        public String getCountryDatabaseName()
        {
            return testDatabaseName;
        }

        @Override
        public String getWeeklyDatabaseName()
        {
            return testDatabaseName;
        }
    };

}