package dnt.parkrun.database;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

public class MostEventsDao extends BaseDao
{
    private static final int MIN_DIFFERENT_EVENTS = 20;

    public MostEventsDao(DataSource dataSource) throws SQLException
    {
        super(dataSource);
    }

    public List<Record> getMostEventsForRegion()
    {
        String sql = "select name, athlete_id, count(course_id) as count " +
                "from (select distinct athlete_id, course_id from " + resultTable() + ") as sub1 " +
                "join " + athleteTable() + " using (athlete_id) " +
                "group by athlete_id " +
                "having count >= :minDifferentEvents " +
                "order by count desc, athlete_id asc";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("minDifferentEvents", MIN_DIFFERENT_EVENTS);
        return jdbc.query(sql, params, (rs, rowNum) ->
                new Record(
                        rs.getString("name"),
                        rs.getInt("athlete_id"),
                        rs.getInt("count")
                ));
    }

    public static class Record
    {
        public final String name;
        public final int athleteId;
        public final int totalRegionEvents;

        public Record(String name, int athleteId, int totalRegionEvents)
        {
            this.name = name;
            this.athleteId = athleteId;
            this.totalRegionEvents = totalRegionEvents;
        }

        @Override
        public String toString()
        {
            return "Record{" +
                    "name='" + name + '\'' +
                    ", athleteId=" + athleteId +
                    ", totalRegionEvents=" + totalRegionEvents +
                    '}';
        }
    }
}
