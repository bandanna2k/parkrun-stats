package dnt.parkrun.database;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public abstract class BaseDao
{
    protected final NamedParameterJdbcTemplate jdbc;
    private final String globalDatabaseName;
    private final String countryDatabaseName;
    protected final String weeklyDatabaseName;

//    @Deprecated
//    public BaseDao(Country country, DataSource statsDataSource)
//    {
//        jdbc = new NamedParameterJdbcTemplate(statsDataSource);
//        this.globalDatabaseName = "parkrun_stats";
//        this.countryDatabaseName = "parkrun_stats_" + country.name();
//        this.weeklyDatabaseName = "weekly_stats_" + country.name();
//    }

    public BaseDao(Database database)
    {
        jdbc = new NamedParameterJdbcTemplate(database.dataSource);
        this.globalDatabaseName = database.getGlobalDatabaseName();
        this.weeklyDatabaseName = database.getWeeklyDatabaseName();
        this.countryDatabaseName = database.getCountryDatabaseName();
    }

    protected String courseEventSummaryTable()
    {
        return countryDatabaseName + ".course_event_summary";
    }
    protected String courseTable()
    {
        return globalDatabaseName + ".course";
    }
    protected String athleteTable()
    {
        return globalDatabaseName + ".athlete";
    }
    protected String resultTable()
    {
        return countryDatabaseName + ".result";
    }
    protected String volunteerTable()
    {
        return countryDatabaseName + ".event_volunteer";
    }
}
