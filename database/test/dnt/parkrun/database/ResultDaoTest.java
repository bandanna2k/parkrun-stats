package dnt.parkrun.database;

import dnt.parkrun.datastructures.Athlete;
import dnt.parkrun.datastructures.Result;
import dnt.parkrun.datastructures.Time;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ResultDaoTest extends BaseDaoTest
{
    private ResultDao resultDao;
    private AthleteDao athleteDao;

    @Before
    public void setUp() throws Exception
    {
        resultDao = new ResultDao(dataSource);
        athleteDao = new AthleteDao(dataSource);

        jdbc.update("delete from result", EmptySqlParameterSource.INSTANCE);
        jdbc.update("delete from athlete", EmptySqlParameterSource.INSTANCE);
    }

    @Test
    public void shouldInsertAndReturnResult()
    {
        Athlete athlete = Athlete.fromAthleteSummaryLink("Davey JONES", "https://www.parkrun.co.nz/parkrunner/902393/");
        athleteDao.insert(athlete);

        Result result = new Result(500, new Date(), 1, athlete, Time.from("1:30:02"));
        resultDao.insert(result);

        List<Result> results = resultDao.getResults();
        assertThat(results).isNotEmpty();
        System.out.println(results);
    }

    @Test
    public void shouldTableScan()
    {
        Athlete athlete = Athlete.fromAthleteSummaryLink("Davey JONES", "https://www.parkrun.co.nz/parkrunner/902393/");
        athleteDao.insert(athlete);

        Instant instant = Instant.EPOCH;
        for (int i = 0; i < 100; i++)
        {
            Time fastTime = Time.from("20:00");
            Time fastishTime = Time.from(fastTime.getTotalSeconds() + i);
            Result result = new Result(500, Date.from(instant), i, athlete, fastishTime);
            resultDao.insert(result);

            instant.plus(7, ChronoUnit.DAYS);
        }

        List<Result> list = new ArrayList<>();
        resultDao.tableScan(list::add);
        assertThat(list.size()).isEqualTo(100);
    }

    @Test
    public void shouldFetchFirstRunsJsonArrays()
    {
        Athlete athlete = Athlete.fromAthleteSummaryLink("Davey JONES", "https://www.parkrun.co.nz/parkrunner/902393/");
        athleteDao.insert(athlete);

        Instant instant = Instant.EPOCH.plus(7, ChronoUnit.DAYS);
        for (int i = 0; i < 10; i++)
        {
            Time fastTime = Time.from("20:00");
            Time fastishTime = Time.from(fastTime.getTotalSeconds() + i);
            Result result = new Result(500, Date.from(instant), i, athlete, fastishTime);
            resultDao.insert(result);

            instant.plus(7, ChronoUnit.DAYS);
        }

        String firstRuns = resultDao.getFirstRunsJsonArrays(athlete.athleteId);
        assertThat(firstRuns).isEqualTo("[[500],[604800]]");
    }
}
