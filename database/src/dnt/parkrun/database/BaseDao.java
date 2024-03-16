package dnt.parkrun.database;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;

public abstract class BaseDao
{
    protected final NamedParameterJdbcTemplate jdbc;

    public BaseDao(DataSource statsDataSource)
    {
        jdbc = new NamedParameterJdbcTemplate(statsDataSource);
    }

    private static boolean test()
    {
        return null != System.getProperty("TEST") && Boolean.parseBoolean(System.getProperty("TEST"));
    }

    protected String courseEventSummaryTable()
    {
        return test() ? "parkrun_stats_test.course_event_summary" : "parkrun_stats.course_event_summary";
    }
    protected String courseTable()
    {
        return test() ? "parkrun_stats_test.course" : "parkrun_stats.course";
    }
    protected String athleteTable()
    {
        return test() ? "parkrun_stats_test.athlete" : "parkrun_stats.athlete";
    }
    protected String resultTable()
    {
        return test() ? "parkrun_stats_test.result" : "parkrun_stats.result";
    }
}
