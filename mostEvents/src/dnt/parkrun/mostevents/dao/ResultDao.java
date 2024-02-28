package dnt.parkrun.mostevents.dao;

import dnt.parkrun.datastructures.Result;
import dnt.parkrun.datastructures.Time;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

public class ResultDao
{

    private final NamedParameterJdbcOperations jdbc;

    public ResultDao() throws SQLException
    {
        DataSource ds = new SimpleDriverDataSource(new com.mysql.jdbc.Driver(),
                "jdbc:mysql://localhost", "dao", "daoFractaldao");
        jdbc = new NamedParameterJdbcTemplate(ds);
    }

    public List<Result> getResults()
    {
        List<Result> query = jdbc.query("select * from parkrun_stats.result", EmptySqlParameterSource.INSTANCE, (rs, rowNum) ->
        {
            return new Result(
                    rs.getString("course_name"),
                    rs.getInt("position"),
                    null,
                    Time.fromString(rs.getString("time"))
            );
        });
        return query;
    }
}
