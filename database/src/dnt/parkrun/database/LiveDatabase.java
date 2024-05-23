package dnt.parkrun.database;

import dnt.parkrun.datastructures.Country;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import java.util.Arrays;

public class LiveDatabase extends Database
{
    public LiveDatabase(Country country, String url, String user, String password)
    {
        this(country, url, user, password, null);
    }
    public LiveDatabase(Country country, String url, String user, String password, String secret)
    {
        super(country, new SimpleDriverDataSource(Driver.getDriver(), url, user, password));

        if("sudo".equals(secret)) return;

        Arrays.stream(Thread.currentThread().getStackTrace()).forEach(ste -> {
            assert ste.getMethodName().toLowerCase().contains("junit") :
                    "LiveDatabase not to be used during testing without permission.";
        });
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
