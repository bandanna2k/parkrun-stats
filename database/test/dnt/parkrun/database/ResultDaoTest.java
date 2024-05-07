package dnt.parkrun.database;

import dnt.parkrun.datastructures.AgeGrade;
import dnt.parkrun.datastructures.Athlete;
import dnt.parkrun.datastructures.Result;
import dnt.parkrun.datastructures.Time;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static dnt.parkrun.datastructures.AgeCategory.*;
import static java.time.Instant.EPOCH;
import static java.time.temporal.ChronoUnit.DAYS;
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

        Result result = new Result(500, EPOCH_PLUS_7, 1, athlete, Time.from("1:30:02"), VW45_49, AgeGrade.newInstance(68.49));
        resultDao.insert(result);

        Result resultNull = new Result(501, EPOCH_PLUS_14, 1, athlete, null, VW45_49, AgeGrade.newInstance(68.49));
        resultDao.insert(resultNull);

        List<Result> results = resultDao.getResults();
        assertThat(results).isNotEmpty();
        System.out.println(results);
    }

    @Test
    public void shouldDeleteResults() throws InterruptedException
    {
        athleteDao.insert(janeDoe);
        athleteDao.insert(johnDoe);
        athleteDao.insert(juniorDoeAdere);
        {
            Result result1 = new Result(501, EPOCH_PLUS_7, 1, johnDoe, Time.from("1:31:02"), VM45_49, AgeGrade.newInstance(68.49));
            resultDao.insert(result1);
        }
        {
            Result result1 = new Result(502, EPOCH_PLUS_14, 1, johnDoe, Time.from("1:32:02"), VM45_49, AgeGrade.newInstance(68.49));
            Result result2 = new Result(502, EPOCH_PLUS_14, 2, janeDoe, Time.from("1:32:03"), VW45_49, AgeGrade.newInstance(68.50));
            resultDao.insert(result1);
            resultDao.insert(result2);
        }
        {
            Result result1 = new Result(503, EPOCH_PLUS_21, 1, johnDoe, Time.from("1:32:02"), VM45_49, AgeGrade.newInstance(68.49));
            Result result2 = new Result(503, EPOCH_PLUS_21, 2, janeDoe, Time.from("1:32:03"), VW45_49, AgeGrade.newInstance(68.50));
            Result result3 = new Result(503, EPOCH_PLUS_21, 3, juniorDoeAdere, Time.from("1:32:04"), JM11_14, AgeGrade.newInstance(68.51));
            resultDao.insert(result1);
            resultDao.insert(result2);
            resultDao.insert(result3);
        }
        {
            List<Result> results = resultDao.getResults();
            assertThat(results.size()).isEqualTo(6);
        }
        {
            resultDao.delete(502, EPOCH_PLUS_14);
            List<Result> results = resultDao.getResults();
            assertThat(results.size()).isEqualTo(4);
        }
        {
            resultDao.delete(501, EPOCH_PLUS_7);
            List<Result> results = resultDao.getResults();
            assertThat(results.size()).isEqualTo(3);
        }
        {
            resultDao.delete(503, EPOCH_PLUS_21);
            List<Result> results = resultDao.getResults();
            assertThat(results.size()).isEqualTo(0);
        }
    }

    @Test
    public void shouldSerialiseDeserialiseAgeGrade()
    {
        Athlete athlete = Athlete.fromAthleteSummaryLink("Davey JONES", "https://www.parkrun.co.nz/parkrunner/902393/");
        athleteDao.insert(athlete);

        Instant epoch = EPOCH;
        resultDao.insert(new Result(
                500, EPOCH_PLUS_7, 1, athlete, Time.from("1:30:02"), VW45_49,
                AgeGrade.newInstanceAssisted()));
        resultDao.insert(new Result(
                500, EPOCH_PLUS_14, 2, athlete, Time.from("1:31:03"), VW45_49,
                AgeGrade.newInstanceNoAgeGrade()));
        resultDao.insert(new Result(
                500, EPOCH_PLUS_21, 3, athlete, Time.from("1:32:04"), VW45_49,
                AgeGrade.newInstance(68.49)));
        resultDao.insert(new Result(
                500, EPOCH_PLUS_28, 4, athlete, Time.from("1:32:05"), VW45_49,
                AgeGrade.newInstance(68.48)));

        List<Result> results = resultDao.getResults();
        assertThat(results).isNotEmpty();

        assertThat(results.get(0).ageGrade.assisted).isEqualTo(true);
        assertThat(results.get(0).ageGrade.ageGrade).isEqualTo(-1.0);
        assertThat(results.get(1).ageGrade.assisted).isEqualTo(false);
        assertThat(results.get(1).ageGrade.ageGrade).isEqualTo(0.0);
        assertThat(results.get(2).ageGrade.assisted).isEqualTo(false);
        assertThat(results.get(2).ageGrade.ageGrade).isEqualTo(68.49);
        assertThat(results.get(3).ageGrade.assisted).isEqualTo(false);
        assertThat(results.get(3).ageGrade.ageGrade).isEqualTo(68.48);
    }

    @Test
    public void shouldTableScan()
    {
        Athlete athlete = Athlete.fromAthleteSummaryLink("Davey JONES", "https://www.parkrun.co.nz/parkrunner/902393/");
        athleteDao.insert(athlete);

        Instant instant = EPOCH;
        for (int i = 0; i < 100; i++)
        {
            Time fastTime = Time.from("20:00");
            Time fastishTime = Time.from(fastTime.getTotalSeconds() + i);
            Result result = new Result(500, Date.from(instant), i, athlete, fastishTime, JM11_14, AgeGrade.newInstance(66.0));
            resultDao.insert(result);

            instant.plus(7, DAYS);
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

        Instant instant = EPOCH.plus(7, DAYS);
        for (int i = 0; i < 10; i++)
        {
            Time fastTime = Time.from("20:00");
            Time fastishTime = Time.from(fastTime.getTotalSeconds() + i);
            Result result = new Result(500, Date.from(instant), i, athlete, fastishTime, SM25_29, AgeGrade.newInstance(66.6));
            resultDao.insert(result);

            instant.plus(7, DAYS);
        }

        String firstRuns = resultDao.getFirstRunsJsonArrays(athlete.athleteId);
        assertThat(firstRuns).isEqualTo("[[500],[604800]]");
    }
}
