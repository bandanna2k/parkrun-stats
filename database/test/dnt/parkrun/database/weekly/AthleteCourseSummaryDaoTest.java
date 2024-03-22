package dnt.parkrun.database.weekly;

import com.mysql.jdbc.Driver;
import dnt.parkrun.database.AthleteDao;
import dnt.parkrun.database.BaseDaoTest;
import dnt.parkrun.datastructures.Athlete;
import dnt.parkrun.datastructures.AthleteCourseSummary;
import dnt.parkrun.datastructures.Course;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;
import java.util.Date;
import java.util.List;

import static dnt.parkrun.datastructures.Country.UNKNOWN;
import static org.assertj.core.api.Assertions.assertThat;

public class AthleteCourseSummaryDaoTest extends BaseDaoTest
{
    public static final Course ELLIÐAÁRDALUR =
            new Course(9999, "Elliðaárdalur", UNKNOWN, "Elliðaárdalur", Course.Status.RUNNING);
    public static final Course CORNWALL =
            new Course(9998, "Cornwall Park", UNKNOWN, "Cornwall Park", Course.Status.RUNNING);

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
                new AthleteCourseSummary(athlete, ELLIÐAÁRDALUR, 20)
        );

        List<Object[]> athleteCourseSummaries = acsDao.getAthleteCourseSummaries();
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
                new AthleteCourseSummary(athlete, ELLIÐAÁRDALUR, 20)
        );
        acsDao.writeAthleteCourseSummary(
                new AthleteCourseSummary(athlete, CORNWALL, 2)
        );
        acsDao.writeAthleteCourseSummary(
                new AthleteCourseSummary(athlete2, ELLIÐAÁRDALUR, 1)
        );

        List<Object[]> actualSummaries = acsDao.getAthleteCourseSummariesMap();
        assertThat(actualSummaries).isNotEmpty();

        System.out.println(actualSummaries);
    }
}
