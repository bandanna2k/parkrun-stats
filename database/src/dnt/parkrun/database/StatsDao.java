package dnt.parkrun.database;

import dnt.parkrun.common.DateConverter;
import dnt.parkrun.datastructures.Athlete;
import dnt.parkrun.datastructures.Country;
import dnt.parkrun.datastructures.Course;
import dnt.parkrun.datastructures.stats.AttendanceRecord;
import dnt.parkrun.datastructures.stats.RunsAtEvent;
import dnt.parkrun.datastructures.stats.VolunteersAtEvent;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import javax.sql.DataSource;
import java.util.Date;
import java.util.List;

import static dnt.parkrun.datastructures.Country.NZ;

public class StatsDao extends BaseDao
{
    private static final String MIN_DIFFERENT_REGION_COURSE_COUNT = "20";

    private final String differentCourseCountTableName;
    private final String attendanceRecordTableName;

    public StatsDao(DataSource dataSource, Date date)
    {
        super(dataSource);
        differentCourseCountTableName = "most_events_for_region_" + DateConverter.formatDateForDbTable(date);
        attendanceRecordTableName = "attendance_records_for_region_" + DateConverter.formatDateForDbTable(date);
    }

    public void generateDifferentCourseCountTable()
    {
        String sql =
                "create table if not exists " + differentCourseCountTableName + " as " +
                        "select a.name, a.athlete_id, " +
                        "sub1.count as different_region_course_count, sub2.count as total_region_runs, " +
                        "0 as different_course_count, 0 as total_runs, " +
                        "0 as p_index " +
                        "from " + athleteTable() + " a " +
                        "join   " +
                        "( " +
                        "    select athlete_id, count(course_id) as count " +
                        "    from (select distinct athlete_id, course_id from " + resultTable() + ") as sub1a " +
                        "    group by athlete_id " +
                        "    having count >= " + MIN_DIFFERENT_REGION_COURSE_COUNT +
                        "    order by count desc, athlete_id asc  " +
                        ") as sub1 on sub1.athlete_id = a.athlete_id " +
                        "join " +
                        "( " +
                        "    select athlete_id, count(concat) as count " +
                        "    from (select athlete_id, concat(athlete_id, '-', course_id, '-', date, '-', position) as concat from " + resultTable() + ") as sub2a " +
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
                        "different_course_count, total_runs," +
                        "p_index " +
                        "from " + differentCourseCountTableName;
        return jdbc.query(sql, EmptySqlParameterSource.INSTANCE, (rs, rowNum) ->
                new DifferentCourseCount(
                        rs.getString("name"),
                        rs.getInt("athlete_id"),
                        rs.getInt("different_region_course_count"),
                        rs.getInt("total_region_runs"),
                        rs.getInt("different_course_count"),
                        rs.getInt("total_runs"),
                        rs.getInt("p_index")
        ));
    }

    public void updateDifferentCourseRecord(int athleteId, int differentCourseCount, int totalRuns, int pIndex)
    {
        String sql = "update " + differentCourseCountTableName + " set " +
                "different_course_count = :differentCourseCount, " +
                "total_runs = :totalRuns, " +
                "p_index = :pIndex " +
                "where athlete_id = :athleteId";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("athleteId", athleteId)
                .addValue("differentCourseCount", differentCourseCount)
                .addValue("totalRuns", totalRuns)
                .addValue("pIndex", pIndex);
        jdbc.update(sql, params);
    }

    public List<RunsAtEvent> getTop10AtEvent(int courseId)
    {
        String sql =
                "select name, athlete_id, c.course_id, c.course_name, c.country_code, course_long_name, run_count\n" +
                        "from " + athleteTable() + "\n" +
                        "join\n" +
                        "(\n" +
                        "    select athlete_id, course_id, count(time_seconds) as run_count\n" +
                        "    from " + resultTable() + " r\n" +
                        "    group by athlete_id, course_id\n" +
                        "    having\n" +
                        "        athlete_id > 0\n" +
                        "        and course_id = :courseId\n" +
                        "    order by run_count desc\n" +
                        "    limit 20\n" +
                        ") as sub1 using (athlete_id)\n" +
                        "join " + courseTable() + " c using (course_id)";
        return jdbc.query(sql, new MapSqlParameterSource("courseId", courseId), (rs, rowNum) ->
        {
            Athlete athlete = Athlete.from(rs.getString("name"), rs.getInt("athlete_id"));
            Course course = new Course(
                    rs.getInt("course_id"),
                    rs.getString("course_name"),
                    Country.valueOf(rs.getInt("country_code")),
                    rs.getString("course_long_name"),
                    null);
            return new RunsAtEvent(
                    athlete,
                    course,
                    rs.getInt("run_count")
            );
        });
    }

    public List<VolunteersAtEvent> getTop10VolunteersAtEvent(int courseId)
    {
        String sql =
                "select name, athlete_id, c.course_id, c.course_name, c.country_code, course_long_name, volunteer_count\n" +
                        "from " + athleteTable() + "\n" +
                        "join\n" +
                        "(\n" +
                        "    select athlete_id, course_id, count(date) as volunteer_count\n" + // ??? hopefully date
                        "    from " + volunteerTable() + " r\n" +
                        "    group by athlete_id, course_id\n" +
                        "    having\n" +
                        "        athlete_id > 0\n" +
                        "        and course_id = :courseId\n" +
                        "    order by volunteer_count desc\n" +
                        "    limit 20\n" +
                        ") as sub1 using (athlete_id)\n" +
                        "join " + courseTable() + " c using (course_id)";
        return jdbc.query(sql, new MapSqlParameterSource("courseId", courseId), (rs, rowNum) ->
        {
            Athlete athlete = Athlete.from(rs.getString("name"), rs.getInt("athlete_id"));
            Course course = new Course(
                    rs.getInt("course_id"),
                    rs.getString("course_name"),
                    Country.valueOf(rs.getInt("country_code")),
                    rs.getString("course_long_name"),
                    null);
            return new VolunteersAtEvent(
                    athlete,
                    course,
                    rs.getInt("volunteer_count")
            );
        });
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
        public int pIndex;

        public DifferentCourseCount(String name,
                                    int athleteId,
                                    int differentRegionCourseCount,
                                    int totalRegionRuns,
                                    int differentCourseCount,
                                    int totalRuns,
                                    int pIndex)
        {
            this.name = name;
            this.athleteId = athleteId;
            this.differentRegionCourseCount = differentRegionCourseCount;
            this.totalRegionRuns = totalRegionRuns;
            this.differentCourseCount = differentCourseCount;
            this.totalRuns = totalRuns;
            this.pIndex = pIndex;
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
                    ", pIndex=" + pIndex +
                    '}';
        }
    }

    public void generateAttendanceRecordTable()
    {
        String sql =
                "create table if not exists " + attendanceRecordTableName + " as " +
                "select course_long_name, c.course_id, c.country_code, " +
                        "            max as record_event_finishers, ces.date as record_event_date, ces.event_number as record_event_number, " +
                        "            sub3.recent_event_finishers, sub3.recent_event_date, sub3.recent_event_number " +
                        "from parkrun_stats.course c " +
                        "left join " +
                        "(" +
                        "    select course_id, max(count) as max" +
                        "    from" +
                        "    (" +
                        "        select course_id, date, count(position) as count" +
                        "        from parkrun_stats.result" +
                        "        group by course_id, date" +
                        "    ) as sub1" +
                        "    group by course_id" +
                        "    order by course_id asc" +
                        ") as sub2 on c.course_id = sub2.course_id " +
                        "left join parkrun_stats.course_event_summary ces " +
                        "on c.course_id = ces.course_id " +
                        "and ces.finishers = sub2.max " +
                        "left join " +
                        "( " +
                        "    select ces.course_id, ces.event_number as recent_event_number, finishers as recent_event_finishers, recent_event_date " +
                        "    from parkrun_stats.course_event_summary ces " +
                        "    join " +
                        "    ( " +
                        "        select course_id, max(date) as recent_event_date " +
                        "        from parkrun_stats.course_event_summary " +
                        "        group by course_id " +
                        "    ) as sub4 on ces.course_id = sub4.course_id and ces.date = sub4.recent_event_date " +
                        ") as sub3 on c.course_id = sub3.course_id " +
                        "where c.country_code = " + NZ.getCountryCode();

        jdbc.update(sql, EmptySqlParameterSource.INSTANCE);
    }

    public List<AttendanceRecord> getAttendanceRecords(Date date)
    {
        String attendanceTableName = "attendance_records_for_region_" + DateConverter.formatDateForDbTable(date);
        String sql = "select c.course_long_name, c.course_name, " +
                "recent_event_number, recent_event_date, recent_event_finishers, " +
                "record_event_number, record_event_date, record_event_finishers " +
                        "from " + attendanceTableName + " at " +
                "left join " + courseTable() + " c using (course_id)";
        return jdbc.query(sql, EmptySqlParameterSource.INSTANCE, (rs, rowNum) ->
                new AttendanceRecord(
                        rs.getString("course_long_name"),
                        rs.getString("course_name"),
                        rs.getInt("recent_event_number"),
                        rs.getDate("recent_event_date"),
                        rs.getInt("recent_event_finishers"),
                        rs.getInt("record_event_number"),
                        rs.getDate("record_event_date"),
                        rs.getInt("record_event_finishers")));
    }
}
