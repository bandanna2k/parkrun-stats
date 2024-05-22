package dnt.parkrun.stats;

import dnt.parkrun.database.Database;
import dnt.parkrun.datastructures.Country;

import javax.sql.DataSource;

public class StatsDatabase extends Database
{
    public StatsDatabase(Country country, DataSource dataSource)
    {
        super(country, dataSource);
    }

    @Override
    public String getGlobalDatabaseName()
    {
        return "parkrun_stats";
    }

    @Override
    public String getCountryDatabaseName()
    {
        return "parkrun_stats_" + country.name();
    }

    @Override
    public String getWeeklyDatabaseName()
    {
        return "weekly_stats_" + country.name();
    }
}
