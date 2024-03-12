package dnt.parkrun.database;

import com.mysql.jdbc.Driver;
import dnt.parkrun.datastructures.Athlete;
import dnt.parkrun.datastructures.AthleteCourseSummary;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class AthleteCourseSummaryDaoTest
{
    private NamedParameterJdbcTemplate jdbc;
    private AthleteCourseSummaryDao acsDao;
    private AthleteDao athleteDao;

    @Before
    public void setUp() throws Exception
    {
        DataSource dataSource = new SimpleDriverDataSource(new Driver(),
                "jdbc:mysql://localhost/parkrun_stats_test", "test", "qa");

        this.athleteDao = new AthleteDao(dataSource);
        this.acsDao = new AthleteCourseSummaryDao(dataSource, new Date());

        jdbc = new NamedParameterJdbcTemplate(dataSource);
        jdbc.update("delete from " + acsDao.tableName, EmptySqlParameterSource.INSTANCE);
    }

    @Test
    public void shouldWriteAndRead()
    {
        Athlete athlete = Athlete.from("Bob Te WILLIGA", 12345);
        athleteDao.insert(athlete);
        acsDao.writeAthleteCourseSummary(
                new AthleteCourseSummary(athlete, "Elliðaárdalur", 20)
        );

        List<AthleteCourseSummary> athleteCourseSummaries = acsDao.getAthleteCourseSummaries();
        assertThat(athleteCourseSummaries).isNotEmpty();
        System.out.println(athleteCourseSummaries);
    }

    @Test
    public void shouldWriteAndReadMap()
    {
        Athlete athlete = Athlete.from("Bob Te WILLIGA", 12345);
        Athlete athlete2 = Athlete.from("Krusty Le CLOWN", 12346);
        athleteDao.insert(athlete);
        athleteDao.insert(athlete2);

        acsDao.writeAthleteCourseSummary(
                new AthleteCourseSummary(athlete, "Elliðaárdalur", 20)
        );
        acsDao.writeAthleteCourseSummary(
                new AthleteCourseSummary(athlete, "Cornwall Park", 2)
        );
        acsDao.writeAthleteCourseSummary(
                new AthleteCourseSummary(athlete2, "Elliðaárdalur", 1)
        );

        Map<Integer, List<AthleteCourseSummary>> acsMap = acsDao.getAthleteCourseSummariesMap();
        assertThat(acsMap).isNotEmpty();
        assertThat(acsMap.get(12345).size()).isEqualTo(2);
        assertThat(acsMap.get(12346).size()).isEqualTo(1);
        System.out.println(acsMap);
    }
}
