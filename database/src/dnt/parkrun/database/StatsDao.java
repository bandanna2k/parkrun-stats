package dnt.parkrun.database;

import dnt.parkrun.common.DateConverter;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import java.util.Date;
import java.util.List;

public class StatsDao
{
    public static final String MIN_DIFFERENT_REGION_COURSE_COUNT = "30";
    private final NamedParameterJdbcTemplate jdbc;
    private final String differentCourseCountTableName;

    public StatsDao(DataSource dataSource, Date date)
    {
        jdbc = new NamedParameterJdbcTemplate(dataSource);
        differentCourseCountTableName = "most_events_for_region_" + DateConverter.formatDateForDbTable(date);
    }

    public void generateDifferentCourseCountTable()
    {
        String sql =    "create table if not exists " + differentCourseCountTableName + " as " +
                        "select name, athlete_id, count(course_name) as different_region_course_count, 0 as different_course_count, 0 as total_runs " +
                        "from (select distinct athlete_id, course_name from parkrun_stats.result) as sub1 " +
                        "join athlete using (athlete_id) " +
                        "group by athlete_id " +
                        "having different_region_course_count >= " + MIN_DIFFERENT_REGION_COURSE_COUNT + " and name is not null " +
                        "order by different_region_course_count desc, athlete_id asc ";
        jdbc.update(sql, EmptySqlParameterSource.INSTANCE);
    }

    public List<DifferentCourseCount> getDifferentCourseCount()
    {
        String sql =
                "select name, athlete_id, different_region_course_count from " + differentCourseCountTableName;
        return jdbc.query(sql, EmptySqlParameterSource.INSTANCE, (rs, rowNum) ->
                new DifferentCourseCount(
                        rs.getString("name"),
                        rs.getInt("athlete_id"),
                        rs.getInt("different_region_course_count")));
    }

    public void updateDifferentCourseRecord(int athleteId, int differentCourseCount, int totalRuns)
    {
        String sql = "update " + differentCourseCountTableName + " set " +
                "different_course_count = :differentCourseCount, " +
                "total_runs = :totalRuns " +
                "where athlete_id = :athleteId";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("athleteId", athleteId)
                .addValue("differentCourseCount", differentCourseCount)
                .addValue("totalRuns", totalRuns);
        jdbc.update(sql, params);
    }

    public static class DifferentCourseCount
    {
        public final String name;
        public final int athleteId;
        public final int differentEvents;

        public DifferentCourseCount(String name, int athleteId, int differentEvents)
        {
            this.name = name;
            this.athleteId = athleteId;
            this.differentEvents = differentEvents;
        }

        @Override
        public String toString()
        {
            return "DifferentCourseCount{" +
                    "name='" + name + '\'' +
                    ", athleteId=" + athleteId +
                    ", differentEvents=" + differentEvents +
                    '}';
        }
    }
}
