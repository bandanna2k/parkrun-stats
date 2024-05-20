package dnt.parkrun.database.weekly;

import dnt.parkrun.common.DateConverter;
import dnt.parkrun.database.BaseDao;
import dnt.parkrun.datastructures.Country;
import dnt.parkrun.datastructures.stats.AttendanceRecord;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import javax.sql.DataSource;
import java.util.Date;
import java.util.List;

public class AttendanceRecordsDao extends BaseDao
{
    private final Country country;
    private final Date date;

    private AttendanceRecordsDao(Country country, DataSource dataSource, Date date)
    {
        super(country, dataSource);
        this.country = country;
        this.date = date;
    }

    public static AttendanceRecordsDao getInstance(Country country, DataSource dataSource, Date date)
    {
        AttendanceRecordsDao attendanceRecordsDao = new AttendanceRecordsDao(country, dataSource, date);
        attendanceRecordsDao.generateAttendanceRecordTable();
        return attendanceRecordsDao;
    }
    public String tableName()
    {
        return tableName(date);
    }
    public String tableName(Date date)
    {
        return weeklyDatabaseName + ".attendance_records_for_region_" + DateConverter.formatDateForDbTable(date);
    }

    private void generateAttendanceRecordTable()
    {
        /*
        Can get 2 dates for record attendance.
         */
        String sql = STR."""
                create table if not exists \{tableName()}
                select c.course_id, c.country_code,
                                    max as record_event_finishers, ces.date as record_event_date,
                                    sub3.recent_event_finishers, sub3.recent_event_date,
                                    average
                        from  \{courseTable()} c
                        left join
                        (
                            select course_id, max(count) as max
                            from
                            (
                                select course_id, date, count(position) as count
                                from \{resultTable()}
                                group by course_id, date
                            ) as sub1
                            group by course_id
                            order by course_id asc
                        ) as sub2 on c.course_id = sub2.course_id

                        left join
                        (
                            select course_id, avg(time_seconds) as average
                            from \{resultTable()}
                            group by course_id
                        ) as sub5 on c.course_id = sub5.course_id

                        left join  \{courseEventSummaryTable()} ces
                        on c.course_id = ces.course_id
                        and ces.finishers = sub2.max

                        left join
                        (
                            select ces.course_id, ces.event_number as recent_event_number, finishers as recent_event_finishers, recent_event_date
                            from \{courseEventSummaryTable()}  ces
                            join
                            (
                                select course_id, max(date) as recent_event_date
                                from \{courseEventSummaryTable()}
                                group by course_id
                            ) as sub4 on ces.course_id = sub4.course_id and ces.date = sub4.recent_event_date
                        ) as sub3 on c.course_id = sub3.course_id
                        where c.country_code = :countryCode;
                """;

        jdbc.update(sql, new MapSqlParameterSource("countryCode", country.getCountryCode()));
    }

    public List<AttendanceRecord> getAttendanceRecords(Date date)
    {
        String attendanceTableName = weeklyDatabaseName + ".attendance_records_for_region_" + DateConverter.formatDateForDbTable(date);
        String sql = STR."""
            select c.course_id,
                recent_ces.event_number as recent_event_number, recent_event_date, recent_event_finishers,
                record_ces.event_number as record_event_number, record_event_date, record_event_finishers
            from \{attendanceTableName} at
            join \{courseTable()} c using (course_id)
            join \{courseEventSummaryTable()} recent_ces on c.course_id = recent_ces.course_id and recent_ces.date = recent_event_date
            join \{courseEventSummaryTable()} record_ces on c.course_id = record_ces.course_id and record_ces.date = record_event_date
        """;
        return jdbc.query(sql, EmptySqlParameterSource.INSTANCE, (rs, rowNum) ->
                new AttendanceRecord(
                        rs.getInt("course_id"),
                        rs.getInt("recent_event_number"),
                        rs.getDate("recent_event_date"),
                        rs.getInt("recent_event_finishers"),
                        rs.getInt("record_event_number"),
                        rs.getDate("record_event_date"),
                        rs.getInt("record_event_finishers")));
    }
}
