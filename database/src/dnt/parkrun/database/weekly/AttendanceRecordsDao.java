package dnt.parkrun.database.weekly;

import dnt.parkrun.common.DateConverter;
import dnt.parkrun.database.BaseDao;
import dnt.parkrun.datastructures.stats.AttendanceRecord;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;

import javax.sql.DataSource;
import java.util.Date;
import java.util.List;

import static dnt.parkrun.datastructures.Country.NZ;

public class AttendanceRecordsDao extends BaseDao
{
    private final Date date;

    private AttendanceRecordsDao(DataSource dataSource, Date date)
    {
        super(dataSource);
        this.date = date;
    }

    public static AttendanceRecordsDao getInstance(DataSource dataSource, Date date)
    {
        AttendanceRecordsDao attendanceRecordsDao = new AttendanceRecordsDao(dataSource, date);
        attendanceRecordsDao.generateAttendanceRecordTable();
        return attendanceRecordsDao;
    }
    String tableName()
    {
        return "attendance_records_for_region_" + DateConverter.formatDateForDbTable(date);
    }

    private void generateAttendanceRecordTable()
    {
        String sql =
                "create table if not exists " + tableName() + " as " +
                "select course_long_name, c.course_id, c.country_code, " +
                        "            max as record_event_finishers, ces.date as record_event_date, ces.event_number as record_event_number, " +
                        "            sub3.recent_event_finishers, sub3.recent_event_date, sub3.recent_event_number " +
                        "from " + courseTable() + " c " +
                        "left join " +
                        "(" +
                        "    select course_id, max(count) as max" +
                        "    from" +
                        "    (" +
                        "        select course_id, date, count(position) as count" +
                        "        from " + resultTable() +
                        "        group by course_id, date" +
                        "    ) as sub1" +
                        "    group by course_id" +
                        "    order by course_id asc" +
                        ") as sub2 on c.course_id = sub2.course_id " +
                        "left join " + courseEventSummaryTable() + " ces " +
                        "on c.course_id = ces.course_id " +
                        "and ces.finishers = sub2.max " +
                        "left join " +
                        "( " +
                        "    select ces.course_id, ces.event_number as recent_event_number, finishers as recent_event_finishers, recent_event_date " +
                        "    from " + courseEventSummaryTable() + " ces " +
                        "    join " +
                        "    ( " +
                        "        select course_id, max(date) as recent_event_date " +
                        "        from " + courseEventSummaryTable() +
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
