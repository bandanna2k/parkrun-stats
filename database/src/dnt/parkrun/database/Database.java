package dnt.parkrun.database;

import dnt.parkrun.datastructures.Country;

import javax.sql.DataSource;

public abstract class Database
{
    public final Country country;
    public final DataSource dataSource;

    public Database(Country country, DataSource dataSource)
    {
        this.country = country;
        this.dataSource = dataSource;
    }

    public abstract String getWeeklyDatabaseName();
    public abstract String getGlobalDatabaseName();
    public abstract String getCountryDatabaseName();
}
