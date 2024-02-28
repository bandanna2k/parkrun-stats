import com.mysql.jdbc.Driver;
import dnt.parkrun.datastructures.Athlete;
import dnt.parkrun.datastructures.Result;
import dnt.parkrun.datastructures.Time;
import dnt.parkrun.mostevents.dao.AthleteDao;
import dnt.parkrun.mostevents.dao.ResultDao;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ResultDaoTest
{
    private ResultDao resultDao;
    private AthleteDao athleteDao;
    private NamedParameterJdbcTemplate jdbc;

    @Before
    public void setUp() throws Exception
    {
        DataSource dataSource = new SimpleDriverDataSource(new Driver(),
                "jdbc:mysql://localhost", "dao", "daoFractaldao");
        resultDao = new ResultDao(dataSource);
        athleteDao = new AthleteDao(dataSource);

        jdbc = new NamedParameterJdbcTemplate(dataSource);
    }
    @After
    public void tearDown()
    {
        jdbc.update("delete from parkrun_stats.result", EmptySqlParameterSource.INSTANCE);
        jdbc.update("delete from parkrun_stats.athlete", EmptySqlParameterSource.INSTANCE);
    }

    @Test
    public void shouldInsertResult()
    {
        Athlete athlete = Athlete.fromAthleteSummaryLink("Davey JONES", "https://www.parkrun.co.nz/parkrunner/902393/");
        athleteDao.insert(athlete);

        Result result = new Result("test", 200, 1, athlete, Time.fromString("1:30:02"));
        resultDao.insert(result);

        List<Result> results = resultDao.getResults();
        assertThat(results).isNotEmpty();
        System.out.println(results);
    }
}
