package dnt.parkrun.database;

public abstract class DataSourceUrlBuilder
{
    public static String getDataSourceUrl()
    {
        String host = System.getProperty("parkrun_stats.mysql.host", "localhost");
        return String.format("jdbc:mysql://%s", host);
    }

    public static String getTestDataSourceUrl()
    {
        return String.format("jdbc:mysql://%s/parkrun_stats_test", //_%s
                System.getProperty("parkrun_stats.mysql.host","localhost"));
    }
}
