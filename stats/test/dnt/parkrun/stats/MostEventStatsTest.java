package dnt.parkrun.stats;

import com.mysql.jdbc.Driver;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Date;

import static dnt.parkrun.database.DataSourceUrlBuilder.getDataSourceUrl;

public class MostEventStatsTest
{
    private NamedParameterJdbcTemplate jdbc;
    private MostEventStats stats;

    @Before
    public void setUp() throws Exception
    {
        MostEventStats.class.getClassLoader().setClassAssertionStatus(MostEventStats.class.getName(), false);

        DataSource dataSource = new SimpleDriverDataSource(new Driver(),
                getDataSourceUrl("parkrun_stats_test"), "test", "qa");

        stats = MostEventStats.newInstance(dataSource, dataSource, Date.from(Instant.EPOCH));

        jdbc = new NamedParameterJdbcTemplate(dataSource);
        jdbc.update("delete from athlete", EmptySqlParameterSource.INSTANCE);
        jdbc.update("delete from event_volunteer", EmptySqlParameterSource.INSTANCE);
        jdbc.update("delete from result", EmptySqlParameterSource.INSTANCE);
        jdbc.update("delete from course", EmptySqlParameterSource.INSTANCE);
        jdbc.update("delete from course_event_summary", EmptySqlParameterSource.INSTANCE);
        jdbc.update("delete from " + stats.attendanceRecordsDao.tableName(), EmptySqlParameterSource.INSTANCE);
    }

    @Test
    public void shouldRunStats() throws SQLException, XMLStreamException, IOException
    {
        stats.generateStats();
    }
}
