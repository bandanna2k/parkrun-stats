package dnt.parkrun.stats;

import com.mysql.jdbc.Driver;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;
import java.util.List;

public class InvariantTest
{
    private DataSource dataSource;
    private NamedParameterJdbcTemplate jdbc;

    @Before
    public void setUp() throws Exception
    {
        dataSource = new SimpleDriverDataSource(new Driver(),
                "jdbc:mysql://localhost/parkrun_stats", "dao", "daoFractaldao");
        jdbc = new NamedParameterJdbcTemplate(dataSource);
    }

    @Test
    public void allVolunteersShouldHaveAnAthlete()
    {
        String sql = "select distinct athlete_id, course_id, name \n" +
                "from event_volunteer\n" +
                "left join athlete using (athlete_id)\n" +
                "where name is null;\n";
        List<Object[]> query = jdbc.query(sql, EmptySqlParameterSource.INSTANCE, (rs, rowNum) ->
        {
            return new Object[]{
                    rs.getInt("athlete_id"),
                    rs.getInt("course_id"),
                    rs.getString("name")
            };
        });
        Assertions.assertThat(query.size()).isEqualTo(0);
    }
}
