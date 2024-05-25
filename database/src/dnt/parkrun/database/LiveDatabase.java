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
            String methodName = ste.getMethodName();
            assert !methodName.toLowerCase().contains("junit") : "LiveDatabase not to be used during testing without permission. " + methodName;

            String className = ste.getClassName();
            assert !methodName.toLowerCase().contains("junit") : "LiveDatabase not to be used during testing without permission. " + className;
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
        return "parkrun_stats";
    }

    @Override
    public String getCountryDatabaseName()
    {
        return "parkrun_stats_" + country.name();
    }
}
