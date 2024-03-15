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

    protected String athleteTable()
    {
        return test() ? "parkrun_stats_test.athlete" : "parkrun_stats.athlete";
    }
    protected String resultTable()
    {
        return test() ? "parkrun_stats_test.result" : "parkrun_stats.result";
    }
    private static boolean test()
    {
        return false;
    }
}
