package dnt.parkrun.mostevents.dao;

import dnt.parkrun.datastructures.Athlete;
import dnt.parkrun.datastructures.Result;
import dnt.parkrun.datastructures.Time;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import java.util.List;

public class ResultDao
{
    private final NamedParameterJdbcOperations jdbc;

    public ResultDao(DataSource dataSource)
    {
        jdbc = new NamedParameterJdbcTemplate(dataSource);
    }

    public List<Result> getResults()
    {
        String sql = "select * from parkrun_stats.result right join parkrun_stats.athlete using (athlete_id)";
        List<Result> query = jdbc.query(sql, EmptySqlParameterSource.INSTANCE, (rs, rowNum) ->
        {
            return new Result(
                    rs.getString("course_name"),
                    rs.getInt("event_number"),
                    rs.getInt("position"),
                    Athlete.fromDao(
                            rs.getString("name"),
                            rs.getInt("athlete_id")
                    ),
                    Time.from(rs.getString("time"))     // TODO Needs converting to int
            );
        });
        return query;
    }

    public void insert(Result result)
    {
        String sql = "insert into parkrun_stats.result (" +
                "athlete_id, course_name, event_number, position, time, time_seconds" +
                ") values ( " +
                ":athleteId, :courseName, :eventNumber, :position, :time, :time_seconds" +
                ")";
        jdbc.update(sql, new MapSqlParameterSource()
                .addValue("athleteId", result.athlete.athleteId)
                .addValue("courseName", result.courseName)
                .addValue("eventNumber", result.eventNumber)
                .addValue("position", result.position)
                .addValue("time", result.time.toString())
                .addValue("time_seconds", result.time.getTotalSeconds())
        );
    }
}
