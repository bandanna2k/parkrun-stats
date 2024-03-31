package dnt.parkrun.database;

import com.mysql.jdbc.Driver;
import dnt.parkrun.datastructures.Course;
import org.junit.Before;
import org.junit.BeforeClass;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;

import static dnt.parkrun.datastructures.Country.UNKNOWN;
import static org.junit.Assume.assumeTrue;

public abstract class BaseDaoTest
{
    public static final Course ELLIÐAÁRDALUR =
            new Course(9999, "Elliðaárdalur", UNKNOWN, "Elliðaárdalur", Course.Status.RUNNING);
    public static final Course CORNWALL =
            new Course(9998, "Cornwall Park", UNKNOWN, "Cornwall Park", Course.Status.RUNNING);
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
                "jdbc:mysql://localhost/parkrun_stats_test", "test", "qa");
        jdbc = new NamedParameterJdbcTemplate(dataSource);
    }
}