package dnt.parkrun.mostevents.dao;

import dnt.parkrun.datastructures.Athlete;
import dnt.parkrun.datastructures.Result;
import dnt.parkrun.datastructures.Time;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

public class AthleteDao
{

    private final NamedParameterJdbcOperations jdbc;

    public AthleteDao(DataSource dataSource) throws SQLException
    {
        jdbc = new NamedParameterJdbcTemplate(dataSource);
    }

    public List<Result> getResults()
    {
        List<Result> query = jdbc.query("select * from parkrun_stats.result", EmptySqlParameterSource.INSTANCE, (rs, rowNum) ->
        {
            return new Result(
                    rs.getString("course_name"),
                    rs.getInt("event_number"),
                    rs.getInt("position"),
                    null,
                    Time.fromString(rs.getString("time"))
            );
        });
        return query;
    }

    public void insert(Athlete athlete)
    {
        try
        {
            String sql = "insert into parkrun_stats.athlete (" +
                    "athlete_id, name" +
                    ") values ( " +
                    ":athleteId, :name" +
                    ")";
            jdbc.update(sql, new MapSqlParameterSource()
                    .addValue("athleteId", athlete.athleteId)
                    .addValue("name", athlete.name)
            );
        }
        catch (DuplicateKeyException ex)
        {
            System.out.println("WARN: Duplicate key for athlete: " + athlete);
        }
    }

    public Athlete getAthlete(long athleteId)
    {
        Athlete athlete = jdbc.queryForObject("select * from parkrun_stats.athlete",
                new MapSqlParameterSource("athleteId", athleteId),
                (rs, rowNum) ->
                        Athlete.fromDao(
                                rs.getString("name"),
                                rs.getLong("athlete_id")
                        ));
        return athlete;
    }
}
