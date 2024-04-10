package dnt.parkrun.database;

import dnt.parkrun.datastructures.*;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

        Result result = new Result(500, new Date(EPOCH.plus(7, DAYS).getEpochSecond()), 1, athlete, Time.from("1:30:02"), AgeGroup.VW45_49, AgeGrade.newInstance(71.09));
        resultDao.insert(result);

        Result resultNull = new Result(501, new Date(EPOCH.plus(14, DAYS).getEpochSecond()), 1, athlete, null, AgeGroup.VW45_49, AgeGrade.newInstance(71.09));
        resultDao.insert(resultNull);

        List<Result> results = resultDao.getResults();
        assertThat(results).isNotEmpty();
        System.out.println(results);
    }

    @Test
    public void shouldSerialiseDeserialiseAgeGrade()
    {
        Athlete athlete = Athlete.fromAthleteSummaryLink("Davey JONES", "https://www.parkrun.co.nz/parkrunner/902393/");
        athleteDao.insert(athlete);

        Instant epoch = EPOCH;
        resultDao.insert(new Result(
                500, new Date(epoch.plus(7, DAYS).getEpochSecond()), 1, athlete, Time.from("1:30:02"), AgeGroup.VW45_49,
                AgeGrade.newInstanceAssisted()));
        resultDao.insert(new Result(
                500, new Date(epoch.plus(14, DAYS).getEpochSecond()), 2, athlete, Time.from("1:31:03"), AgeGroup.VW45_49,
                AgeGrade.newInstanceNoAgeGrade()));
        resultDao.insert(new Result(
                500, new Date(epoch.plus(21, DAYS).getEpochSecond()), 3, athlete, Time.from("1:32:04"), AgeGroup.VW45_49,
                AgeGrade.newInstance(71.09)));

        List<Result> results = resultDao.getResults();
        assertThat(results).isNotEmpty();

        assertThat(results.get(0).ageGrade.assisted).isEqualTo(true);
        assertThat(results.get(0).ageGrade.ageGrade).isEqualTo(-1);
        assertThat(results.get(1).ageGrade.assisted).isEqualTo(false);
        assertThat(results.get(1).ageGrade.ageGrade).isEqualTo(0);
        assertThat(results.get(2).ageGrade.assisted).isEqualTo(false);
        assertThat(results.get(2).ageGrade.ageGrade).isEqualTo(71.09);
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
            Result result = new Result(500, Date.from(instant), i, athlete, fastishTime, AgeGroup.JM11_14, AgeGrade.newInstance(66.0));
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
            Result result = new Result(500, Date.from(instant), i, athlete, fastishTime, AgeGroup.SM25_29, AgeGrade.newInstance(66.6));
            resultDao.insert(result);

            instant.plus(7, DAYS);
        }

        String firstRuns = resultDao.getFirstRunsJsonArrays(athlete.athleteId);
        assertThat(firstRuns).isEqualTo("[[500],[604800]]");
    }
}
