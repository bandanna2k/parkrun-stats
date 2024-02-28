import com.mysql.jdbc.Driver;
import dnt.parkrun.datastructures.Athlete;
import dnt.parkrun.datastructures.Result;
import dnt.parkrun.datastructures.Time;
import dnt.parkrun.mostevents.dao.ResultDao;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;

public class ResultDaoTest
{
    private ResultDao resultDao;

    public ResultDaoTest()
    {

    }

    @Before
    public void setUp() throws Exception
    {
        DataSource dataSource = new SimpleDriverDataSource(new Driver(),
                "jdbc:mysql://localhost", "dao", "daoFractaldao");
        resultDao = new ResultDao(dataSource);

        NamedParameterJdbcTemplate jdbc = new NamedParameterJdbcTemplate(dataSource);
        jdbc.update("delete from parkrun_stats.result", EmptySqlParameterSource.INSTANCE);
    }

    @Test
    public void shouldInsertResult()
    {
        Athlete athlete = Athlete.fromAthleteHistoryAtEventLink("Davey JONES", "https://www.parkrun.co.nz/parkrunner/902393/");
        Result result = new Result("test", 1, athlete, Time.fromString("1:30:02"));
        resultDao.insert(result);

        assertThat(resultDao.getResults()).isNotEmpty();
    }
}
