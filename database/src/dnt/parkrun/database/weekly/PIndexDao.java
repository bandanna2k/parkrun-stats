package dnt.parkrun.database.weekly;

import dnt.parkrun.common.DateConverter;
import dnt.parkrun.database.BaseDao;
import dnt.parkrun.pindex.PIndex;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import javax.sql.DataSource;
import java.util.Date;

public class PIndexDao extends BaseDao
{
    final String tableName;

    public PIndexDao(DataSource statsDataSource, Date date)
    {
        super(statsDataSource);
        tableName = "p_index_" + DateConverter.formatDateForDbTable(date);
        createTable();
    }

    public void createTable()
    {
        String sql =
                "create table if not exists " + tableName + " ( " +
                        "    athlete_id             INT               NOT NULL," +
                        "    p_index                INT               NOT NULL," +
                        "    runs_needed_to_next    INT               NOT NULL," +
                        " PRIMARY KEY (athlete_id) " +
                        ") DEFAULT CHARSET=utf8mb4";
        jdbc.update(sql, EmptySqlParameterSource.INSTANCE);
    }

    public void writePIndexRecord(int athleteId, PIndex.Result result)
    {
        String sql = "insert into " + tableName + " (" +
                "athlete_id, p_index, runs_needed_to_next" +
                ") values ( " +
                ":athleteId, :pIndex, :runsNeeded" +
                ") on duplicate key " +
                "update p_index = :pIndex, runs_needed_to_next = :runsNeeded";
        jdbc.update(sql, new MapSqlParameterSource()
                .addValue("athleteId", athleteId)
                .addValue("pIndex", result.pIndex)
                .addValue("runsNeeded", result.neededForNextPIndex)
        );
    }

    public PIndex.Result getPIndexForAthlete(int athleteId)
    {
        String sql = "select * from " + tableName + " where athlete_id = :athleteId";
        return jdbc.queryForObject(sql, new MapSqlParameterSource("athleteId", athleteId), (rs, rowNum) ->
                new PIndex.Result(rs.getInt("p_index"), rs.getInt("runs_needed_to_next")));
    }
}
