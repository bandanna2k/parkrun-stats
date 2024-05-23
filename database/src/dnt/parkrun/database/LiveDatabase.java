package dnt.parkrun.database;

import dnt.parkrun.datastructures.Country;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

public class LiveDatabase extends Database
{
    public LiveDatabase(Country country, String url, String user, String password)
    {
        super(country, new SimpleDriverDataSource(Driver.getDriver(), url, user, password));
    }

    @Override
    public String getWeeklyDatabaseName()
    {
        return "weekly_stats_" + country.name();
    }

    @Override
    public String getGlobalDatabaseName()
    {
        return "parkrun_stats" + country.name();
    }

    @Override
    public String getCountryDatabaseName()
    {
        return "parkrun_stats_" + country.name();
    }
}
