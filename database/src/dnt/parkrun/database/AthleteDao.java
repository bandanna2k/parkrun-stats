package dnt.parkrun.database;

import dnt.parkrun.datastructures.Athlete;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class AthleteDao
{

    private final NamedParameterJdbcOperations jdbc;

    public AthleteDao(DataSource dataSource) throws SQLException
    {
        jdbc = new NamedParameterJdbcTemplate(dataSource);
    }

    public void insert(Athlete athlete)
    {
        String sql = "insert into athlete (" +
                "athlete_id, name" +
                ") values ( " +
                ":athleteId, :name" +
                ") on duplicate key " +
                "update " +
                "name = :name";
        jdbc.update(sql, new MapSqlParameterSource()
                .addValue("athleteId", athlete.athleteId)
                .addValue("name", athlete.name)
        );
    }

    public Athlete getAthlete(int athleteId)
    {
        Athlete athlete = jdbc.queryForObject("select * from athlete",
                new MapSqlParameterSource("athleteId", athleteId),
                (rs, rowNum) ->
                        Athlete.from(
                                rs.getString("name"),
                                rs.getInt("athlete_id")
                        ));
        return athlete;
    }

    // TODO: Write a test for me please.
    public Map<Integer, Athlete> getAllAthletes()
    {
        Map<Integer, Athlete> result = new HashMap<>();
        jdbc.query("select * from athlete",
                EmptySqlParameterSource.INSTANCE,
                (rs, rowNum) ->
                {
                    int athleteId = rs.getInt("athlete_id");
                    return result.put(athleteId, Athlete.from(rs.getString("name"), athleteId));
                });
        return result;
    }
}
