package dnt.parkrun.database;

public abstract class DataSourceUrlBuilder
{
    public static String getDataSourceUrl(String database)
    {
        return String.format("jdbc:mysql://%s/%s",
                System.getProperty("parkrun_stats.mysql.host","localhost"),
                database);
    }
}
