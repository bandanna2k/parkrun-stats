package dnt.parkrun.database.weekly;

import dnt.parkrun.common.DateConverter;
import dnt.parkrun.database.BaseDao;
import dnt.parkrun.database.Database;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import static dnt.parkrun.common.DateConverter.SEVEN_DAYS_IN_MILLIS;

public class PIndexDao extends BaseDao
{
    private final Date date;

    public PIndexDao(Database database, Date date)
    {
        super(database);
        this.date = date;
    }

    public static PIndexDao getInstance(Database database, Date date)
    {
        PIndexDao pIndexDao = new PIndexDao(database, date);
        pIndexDao.createTable();
        return pIndexDao;
    }

    private String getTableName(Date date)
    {
        return weeklyDatabaseName + ".p_index_" + DateConverter.formatDateForDbTable(date);
    }

    public void createTable()
    {
        String sql =
                "create table if not exists " + getTableName(date) + " ( " +
                        "    athlete_id             INT               NOT NULL," +
                        "    p_index                INT               NOT NULL," +
                        "    runs_needed_to_next    INT               NOT NULL," +
                        "    home_ratio             DECIMAL(6,4)      NOT NULL," +
                        " PRIMARY KEY (athlete_id) " +
                        ") DEFAULT CHARSET=utf8mb4";
        jdbc.update(sql, EmptySqlParameterSource.INSTANCE);
    }

    public void writePIndexRecord(PIndexRecord pIndexRecord)
    {
        String sql = "insert into " + getTableName(date) + " (" +
                "athlete_id, p_index, runs_needed_to_next, home_ratio" +
                ") values ( " +
                ":athleteId, :pIndex, :runsNeeded, :homeRatio" +
                ") on duplicate key " +
                "update p_index = :pIndex, runs_needed_to_next = :runsNeeded, home_ratio = :homeRatio";
        jdbc.update(sql, new MapSqlParameterSource()
                .addValue("athleteId", pIndexRecord.athleteId)
                .addValue("pIndex", pIndexRecord.pIndex)
                .addValue("runsNeeded", pIndexRecord.neededForNextPIndex)
                .addValue("homeRatio", pIndexRecord.homeRatio)
        );
    }

    public PIndexRecord getPIndexForAthlete(int athleteId)
    {
        String sql = "select * from " + getTableName(date) + " where athlete_id = :athleteId";
        return jdbc.queryForObject(sql, new MapSqlParameterSource("athleteId", athleteId), (rs, rowNum) ->
                new PIndexRecord(
                        rs.getInt("athlete_id"),
                        rs.getInt("p_index"),
                        rs.getInt("runs_needed_to_next"),
                        rs.getDouble("home_ratio")));
    }

    public List<PIndexRecord> getPIndexRecords()
    {
        return getPIndexRecords(new Date());
    }
    public List<PIndexRecord> getPIndexRecordsLastWeek()
    {
        Date lastWeek = new Date();
        lastWeek.setTime(date.getTime() - SEVEN_DAYS_IN_MILLIS);
        return getPIndexRecords(lastWeek);
    }
    public List<PIndexRecord> getPIndexRecords(Date date)
    {
        try
        {
            String sql = STR."""
            select *
            from \{getTableName(date)}
            order by p_index desc, runs_needed_to_next desc, athlete_id asc
            """;
            return jdbc.query(sql, EmptySqlParameterSource.INSTANCE, (rs, rowNum) -> new PIndexRecord(
                    rs.getInt("athlete_id"),
                    rs.getInt("p_index"),
                    rs.getInt("runs_needed_to_next"),
                    rs.getDouble("home_ratio")));
        }
        catch(BadSqlGrammarException ex)
        {
            if(ex.getMessage().matches(".*Table.*doesn't exist.*"))
            {
                System.out.println("WARNING " + ex.getMessage());
            }
            else
            {
                throw ex;
            }
        }
        return Collections.emptyList();
    }

    public static class PIndexRecord
    {
        public final int athleteId;
        public final int pIndex;
        public final int neededForNextPIndex;
        public final double homeRatio;
        public int positionDelta;

        public PIndexRecord(int athleteId, int pIndex, int neededForNextPIndex, double homeRatio)
        {
            this.athleteId = athleteId;
            this.pIndex = pIndex;
            this.neededForNextPIndex = neededForNextPIndex;
            this.homeRatio = homeRatio;
        }

        @Override
        public String toString()
        {
            return "PIndexRecord{" +
                    "athleteId=" + athleteId +
                    ", pIndex=" + pIndex +
                    ", neededForNextPIndex=" + neededForNextPIndex +
                    ", homeRatio=" + homeRatio +
                    ", positionDelta=" + positionDelta +
                    '}';
        }
    }
}
