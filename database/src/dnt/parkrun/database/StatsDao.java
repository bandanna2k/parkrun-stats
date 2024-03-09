package dnt.parkrun.database;

import dnt.parkrun.common.DateConverter;
import dnt.parkrun.datastructures.stats.AttendanceRecord;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import java.util.Date;
import java.util.List;

public class StatsDao
{
    private static final String MIN_DIFFERENT_REGION_COURSE_COUNT = "20";

    private final NamedParameterJdbcTemplate jdbc;
    private final String differentCourseCountTableName;
    private final String attendanceRecordTableName;

    public StatsDao(DataSource dataSource, Date date)
    {
        jdbc = new NamedParameterJdbcTemplate(dataSource);
        differentCourseCountTableName = "most_events_for_region_" + DateConverter.formatDateForDbTable(date);
        attendanceRecordTableName = "attendance_records_for_region_" + DateConverter.formatDateForDbTable(date);
    }

    public void generateDifferentCourseCountTable()
    {
        String sql =
                "create table if not exists " + differentCourseCountTableName + " as " +
                        "select a.name, a.athlete_id, " +
                        "sub1.count as different_region_course_count, sub2.count as total_region_runs, " +
                        "0 as different_course_count, 0 as total_runs " +
                        "from athlete a " +
                        "join   " +
                        "( " +
                        "    select athlete_id, count(course_name) as count " +
                        "    from (select distinct athlete_id, course_name from result) as sub1a " +
                        "    group by athlete_id " +
                        "    having count >= " + MIN_DIFFERENT_REGION_COURSE_COUNT +
                        "    order by count desc, athlete_id asc  " +
                        ") as sub1 on sub1.athlete_id = a.athlete_id " +
                        "join " +
                        "( " +
                        "    select athlete_id, count(concat) as count " +
                        "    from (select athlete_id, concat(athlete_id, course_name, event_number, '-', position) as concat from result) as sub2a " +
                        "    group by athlete_id " +
                        "    order by count desc, athlete_id asc  " +
                        ") as sub2 on sub2.athlete_id = a.athlete_id " +
                        "where a.name is not null " +
                        "order by different_region_course_count desc, total_region_runs desc, a.athlete_id desc ";

        jdbc.update(sql, EmptySqlParameterSource.INSTANCE);
    }

    public List<DifferentCourseCount> getDifferentCourseCount(Date date)
    {
        String differentCourseCountTableName = "most_events_for_region_" + DateConverter.formatDateForDbTable(date);
        String sql =
                "select name, athlete_id, " +
                        "different_region_course_count, total_region_runs," +
                        "different_course_count, total_runs " +
                        "from " + differentCourseCountTableName;
        return jdbc.query(sql, EmptySqlParameterSource.INSTANCE, (rs, rowNum) ->
                new DifferentCourseCount(
                        rs.getString("name"),
                        rs.getInt("athlete_id"),
                        rs.getInt("different_region_course_count"),
                        rs.getInt("total_region_runs"),
                        rs.getInt("different_course_count"),
                        rs.getInt("total_runs")
                ));
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
        public final int differentRegionCourseCount;
        public final int totalRegionRuns;
        public final int differentCourseCount;
        public final int totalRuns;
        public int positionDelta = 0;

        public DifferentCourseCount(String name,
                                    int athleteId,
                                    int differentRegionCourseCount,
                                    int totalRegionRuns,
                                    int differentCourseCount,
                                    int totalRuns)
        {
            this.name = name;
            this.athleteId = athleteId;
            this.differentRegionCourseCount = differentRegionCourseCount;
            this.totalRegionRuns = totalRegionRuns;
            this.differentCourseCount = differentCourseCount;
            this.totalRuns = totalRuns;
        }

        @Override
        public String toString()
        {
            return "DifferentCourseCount{" +
                    "name='" + name + '\'' +
                    ", athleteId=" + athleteId +
                    ", differentRegionCourseCount=" + differentRegionCourseCount +
                    ", totalRegionRuns=" + totalRegionRuns +
                    ", differentCourseCount=" + differentCourseCount +
                    ", totalRuns=" + totalRuns +
                    ", positionDelta=" + positionDelta +
                    '}';
        }
    }

    public void generateAttendanceRecordTable()
    {
        String sql =
                "create table if not exists " + attendanceRecordTableName + " as " +
                "select course_long_name, c.course_name, " +
                        "            max, ces.date, " +
                        "            sub3.recent_event_finishers, sub3.recent_event_date " +
                        "from course c " +
                        "left join " +
                        "(" +
                        "    select course_name, max(count) as max" +
                        "    from" +
                        "    (" +
                        "        select course_name, event_number, count(position) as count" +
                        "        from result" +
                        "        group by course_name, event_number" +
                        "    ) as sub1" +
                        "    group by course_name" +
                        "    order by course_name asc" +
                        ") as sub2 on c.course_name = sub2.course_name " +
                        "left join course_event_summary ces " +
                        "on c.course_name = ces.course_name " +
                        "and ces.finishers = sub2.max " +
                        "left join " +
                        "( " +
                        "    select ces.course_name, finishers as recent_event_finishers, recent_event_date " +
                        "    from course_event_summary ces " +
                        "    join " +
                        "    ( " +
                        "        select course_name, max(date) as recent_event_date " +
                        "        from course_event_summary " +
                        "        group by course_name " +
                        "    ) as sub4 on ces.course_name = sub4.course_name and ces.date = sub4.recent_event_date " +
                        ") as sub3 on c.course_name = sub3.course_name;";

        jdbc.update(sql, EmptySqlParameterSource.INSTANCE);
    }

    public List<AttendanceRecord> getAttendanceRecords()
    {
        String sql = "select course_long_name, course_name, recent_event_date, recent_event_finishers, date, max " +
                        "from " + attendanceRecordTableName;
        return jdbc.query(sql, EmptySqlParameterSource.INSTANCE, (rs, rowNum) ->
                new AttendanceRecord(
                        rs.getString("course_long_name"),
                        rs.getString("course_name"),
                        rs.getDate("recent_event_date"),
                        String.valueOf(rs.getInt("recent_event_finishers")),
                        rs.getDate("date"),
                        String.valueOf(rs.getInt("max"))));
    }
}
