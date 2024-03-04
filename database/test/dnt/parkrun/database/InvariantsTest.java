package dnt.parkrun.database;

import com.mysql.jdbc.Driver;
import dnt.parkrun.datastructures.Result;
import dnt.parkrun.datastructures.Time;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;
import java.util.List;

public class InvariantsTest
{
    private NamedParameterJdbcTemplate jdbc;

    @Before
    public void setUp() throws Exception
    {
        DataSource dataSource = new SimpleDriverDataSource(new Driver(),
                "jdbc:mysql://localhost/parkrun_stats", "dao", "daoFractaldao");

        jdbc = new NamedParameterJdbcTemplate(dataSource);
    }

    @Test
    public void shouldHaveNoZeroResult()
    {
        // TODO
    }

    @Test
    public void shouldHaveTimeSecondsConverter()
    {
        String s =
                "select course_name, event_number, position, time, time_seconds " +
                "from result " +
                "order by rand() " +
                "limit 100";
        List<Result> results = jdbc.query(s, EmptySqlParameterSource.INSTANCE, (rs, rowNum) ->
                new Result(
                        rs.getString("course_name") + "," + rs.getString("time") + "," + rs.getInt("time_seconds"),
                        rs.getInt("event_number"),
                        rs.getInt("position") ,
                        null,
                        null)
                );
        results.forEach(result -> {
            System.out.println(result);
            int indexOf = result.courseName.indexOf(",");
            int lastIndexOf = result.courseName.lastIndexOf(",");
            String time = result.courseName.substring(indexOf + 1, lastIndexOf);
            int timeSeconds = Integer.parseInt(result.courseName.substring(lastIndexOf + 1));
            Assertions.assertThat(timeSeconds).isEqualTo(Time.from(time).getTotalSeconds());
        });
    }
}
