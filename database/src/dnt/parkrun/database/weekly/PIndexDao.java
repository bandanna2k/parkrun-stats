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
    private final Date date;

    public PIndexDao(DataSource statsDataSource, Date date)
    {
        super(statsDataSource);
        this.date = date;
        createTable();
    }

    private static String getTableName(Date date)
    {
        return "p_index_" + DateConverter.formatDateForDbTable(date);
    }

    public void createTable()
    {
        String sql =
                "create table if not exists " + getTableName(date) + " ( " +
                        "    athlete_id             INT               NOT NULL," +
                        "    p_index                INT               NOT NULL," +
                        "    runs_needed_to_next    INT               NOT NULL," +
                        " PRIMARY KEY (athlete_id) " +
                        ") DEFAULT CHARSET=utf8mb4";
        jdbc.update(sql, EmptySqlParameterSource.INSTANCE);
    }

    public void writePIndexRecord(PIndexRecord pIndexRecord)
    {
        String sql = "insert into " + getTableName(date) + " (" +
                "athlete_id, p_index, runs_needed_to_next" +
                ") values ( " +
                ":athleteId, :pIndex, :runsNeeded" +
                ") on duplicate key " +
                "update p_index = :pIndex, runs_needed_to_next = :runsNeeded";
        jdbc.update(sql, new MapSqlParameterSource()
                .addValue("athleteId", pIndexRecord.athleteId)
                .addValue("pIndex", pIndexRecord.pIndex)
                .addValue("runsNeeded", pIndexRecord.neededForNextPIndex)
        );
    }

    public PIndex.Result getPIndexForAthlete(int athleteId)
    {
        String sql = "select * from " + getTableName(date) + " where athlete_id = :athleteId";
        return jdbc.queryForObject(sql, new MapSqlParameterSource("athleteId", athleteId), (rs, rowNum) ->
                new PIndex.Result(rs.getInt("p_index"), rs.getInt("runs_needed_to_next")));
    }

    public static class PIndexRecord
    {
        public final int athleteId;
        public final int pIndex;
        public final int neededForNextPIndex;
        public int positionDelta;

        public PIndexRecord(int athleteId, int pIndex, int neededForNextPIndex)
        {
            this.athleteId = athleteId;
            this.pIndex = pIndex;
            this.neededForNextPIndex = neededForNextPIndex;
        }

        @Override
        public String toString()
        {
            return "PIndexRecord{" +
                    "athleteId=" + athleteId +
                    ", pIndex=" + pIndex +
                    ", neededForNextPIndex=" + neededForNextPIndex +
                    '}';
        }
    }
}
