package dnt.parkrun.database;

import dnt.parkrun.datastructures.Country;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;

public abstract class BaseDao
{
    protected final NamedParameterJdbcTemplate jdbc;
    private final String globalDatabaseName;
    private final String countryDatabaseName;
    protected final String weeklyDatabaseName;

    @Deprecated
    public BaseDao(Country country, DataSource statsDataSource)
    {
        jdbc = new NamedParameterJdbcTemplate(statsDataSource);
        this.globalDatabaseName = "parkrun_stats";
        this.countryDatabaseName = "parkrun_stats_" + country.name();
        this.weeklyDatabaseName = "weekly_stats_" + country.name();
    }

    public BaseDao(Database database)
    {
        jdbc = new NamedParameterJdbcTemplate(database.dataSource);
        this.globalDatabaseName = database.getGlobalDatabaseName();
        this.weeklyDatabaseName = database.getWeeklyDatabaseName();
        this.countryDatabaseName = database.getCountryDatabaseName();
    }

    private static boolean test()
    {
        return null != System.getProperty("TEST") && Boolean.parseBoolean(System.getProperty("TEST"));
    }

    protected String courseEventSummaryTable()
    {
        return test() ? "parkrun_stats_test.course_event_summary" : countryDatabaseName + ".course_event_summary";
    }
    protected String courseTable()
    {
        return test() ? "parkrun_stats_test.course" : globalDatabaseName + ".course";
    }
    protected String athleteTable()
    {
        return test() ? "parkrun_stats_test.athlete" : globalDatabaseName + ".athlete";
    }
    protected String resultTable()
    {
        return test() ? "parkrun_stats_test.result" : countryDatabaseName + ".result";
    }
    protected String volunteerTable()
    {
        return test() ? "parkrun_stats_test.event_volunteer" : countryDatabaseName + ".event_volunteer";
    }
}
