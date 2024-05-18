package dnt.parkrun.database;

import dnt.parkrun.datastructures.Country;

public abstract class DataSourceUrlBuilder
{
    public enum Type
    {
        PARKRUN_STATS("parkrun_stats"),
        WEEKLY_STATS("weekly_stats");

        public final String databaseName;

        Type(String databaseName)
        {
            this.databaseName = databaseName;
        }
    }

    public static String getDataSourceUrl(Type type, Country country)
    {
        return switch (type)
        {
            case PARKRUN_STATS, WEEKLY_STATS -> String.format("jdbc:mysql://%s/%s_%s",
                    System.getProperty("parkrun_stats.mysql.host", "localhost"),
                    type.databaseName,
                    country.name());
        };
    }

    public static String getTestDataSourceUrl()
    {
        return String.format("jdbc:mysql://%s/parkrun_stats_test", //_%s
                System.getProperty("parkrun_stats.mysql.host","localhost"));
    }
}
