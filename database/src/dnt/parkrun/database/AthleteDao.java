package dnt.parkrun.database;

import dnt.parkrun.datastructures.Athlete;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AthleteDao
{

    public static final String SQL_FOR_INSERT = "insert into athlete (" +
            "athlete_id, name" +
            ") values ( " +
            ":athleteId, :name" +
            ") on duplicate key " +
            "update " +
            "name = :name";
    private final NamedParameterJdbcOperations jdbc;

    public AthleteDao(DataSource dataSource)
    {
        jdbc = new NamedParameterJdbcTemplate(dataSource);
    }

    public Athlete insert(Athlete athlete)
    {
        jdbc.update(SQL_FOR_INSERT, new MapSqlParameterSource()
                .addValue("athleteId", athlete.athleteId)
                .addValue("name", athlete.name)
        );
        return getAthlete(athlete.athleteId);
    }

    public void insert(List<Athlete> athletes)
    {
        jdbc.batchUpdate(SQL_FOR_INSERT, athletes.stream().map(athlete -> new MapSqlParameterSource()
                .addValue("athleteId", athlete.athleteId)
                .addValue("name", athlete.name)).toArray(MapSqlParameterSource[]::new));
    }

    public Athlete getAthlete(int athleteId)
    {
        return jdbc.queryForObject("select * from athlete where athlete_id = :athleteId",
                new MapSqlParameterSource("athleteId", athleteId),
                (rs, rowNum) ->
                        Athlete.from(
                                rs.getString("name"),
                                rs.getInt("athlete_id")
                        ));
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
