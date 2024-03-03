package dnt.parkrun.database;

import dnt.parkrun.common.DateConverter;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import java.util.Date;

public class StatsProducerDao
{
    private final NamedParameterJdbcTemplate jdbc;

    public StatsProducerDao(DataSource dataSource)
    {
        jdbc = new NamedParameterJdbcTemplate(dataSource);
    }

    public void produceStatsForDate(Date date)
    {
        String tableName = "most_events_for_region_" + DateConverter.formatDateForDbTable(date);
        System.out.println(tableName);
        String sql =
                "create table if not exists " + tableName + " as " +
                "select name, count(course_name) as count " +
                "from (select distinct athlete_id, course_name from parkrun_stats.result) as sub1 " +
                "join athlete using (athlete_id) " +
                "group by athlete_id " +
                "having count >= 40 and name is not null " +
                "order by count desc, athlete_id asc ";
        jdbc.update(sql, EmptySqlParameterSource.INSTANCE);
    }
}
