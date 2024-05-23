package dnt.parkrun.database;

import dnt.parkrun.datastructures.Athlete;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class AthleteDao extends BaseDao
{
    private final String SQL_FOR_INSERT = STR."""
            insert into \{athleteTable()}
            (athlete_id, name)
            values
            (:athleteId, :name)
            on duplicate key
            update
                name = :name
            """;
    private final String SQL_FOR_SELECT = STR."""
        select * from \{athleteTable()} where athlete_id = :athleteId
    """;
    private final String SQL_FOR_MULTI_SELECT = STR."""
        select athlete_id, name from \{athleteTable()} where athlete_id in (:athleteIds)
    """;

    public AthleteDao(Database database)
    {
        super(database);
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
        return jdbc.queryForObject(SQL_FOR_SELECT,
                new MapSqlParameterSource("athleteId", athleteId),
                (rs, rowNum) ->
                        Athlete.from(
                                rs.getString("name"),
                                rs.getInt("athlete_id")
                        ));
    }

    // TODO: Write a test for me please.
    // TODO: Testing only surely.
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

    public Map<Integer, Athlete> getAthletes(List<Integer> athleteIds)
    {
        Map<Integer, Athlete> result = new TreeMap<>();
        jdbc.query(SQL_FOR_MULTI_SELECT,
                new MapSqlParameterSource("athleteIds", athleteIds.stream().map(String::valueOf).collect(Collectors.toList())),
                (rs, rowNum) ->
                {
                    int athleteId = rs.getInt("athlete_id");
                    return result.put(athleteId, Athlete.from(rs.getString("name"), athleteId));
                });
        return result;
    }
}
