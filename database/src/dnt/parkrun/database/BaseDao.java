package dnt.parkrun.database;

import dnt.parkrun.datastructures.Country;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;

public abstract class BaseDao
{
    protected final NamedParameterJdbcTemplate jdbc;
    protected final String databaseName;

    public BaseDao(Country country, DataSource statsDataSource)
    {
        jdbc = new NamedParameterJdbcTemplate(statsDataSource);
        this.databaseName = "parkrun_stats_" + country.name();
    }

    private static boolean test()
    {
        return null != System.getProperty("TEST") && Boolean.parseBoolean(System.getProperty("TEST"));
    }

    protected String courseEventSummaryTable()
    {
        return test() ? "parkrun_stats_test.course_event_summary" : databaseName + ".course_event_summary";
    }
    protected String courseTable()
    {
        return test() ? "parkrun_stats_test.course" : databaseName + ".course";
    }
    protected String athleteTable()
    {
        return test() ? "parkrun_stats_test.athlete" : databaseName + ".athlete";
    }
    protected String resultTable()
    {
        return test() ? "parkrun_stats_test.result" : databaseName + ".result";
    }
    protected String volunteerTable()
    {
        return test() ? "parkrun_stats_test.event_volunteer" : databaseName + ".event_volunteer";
    }
}
