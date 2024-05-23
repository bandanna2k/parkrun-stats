package dnt.parkrun.database.weekly;

import dnt.parkrun.database.AthleteDao;
import dnt.parkrun.database.BaseDaoTest;
import dnt.parkrun.database.CourseTestBuilder;
import dnt.parkrun.datastructures.Athlete;
import dnt.parkrun.datastructures.AthleteCourseSummary;
import dnt.parkrun.datastructures.Course;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class AthleteCourseSummaryDaoTest extends BaseDaoTest
{
    private AthleteCourseSummaryDao acsDao;
    private AthleteDao athleteDao;

    @Before
    public void setUp() throws Exception
    {
        this.athleteDao = new AthleteDao(TEST_DATABASE);
        this.acsDao = AthleteCourseSummaryDao.getInstance(TEST_DATABASE, new Date());
        jdbc.update("delete from " + acsDao.tableName(), EmptySqlParameterSource.INSTANCE);
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

        Course course1 = new CourseTestBuilder().courseId(9001).build();
        Course course2 = new CourseTestBuilder().courseId(9002).build();
        Course course3 = new CourseTestBuilder().courseId(9003).build();

        acsDao.writeAthleteCourseSummary(
                new AthleteCourseSummary(athlete, course1, 20)
        );
        acsDao.writeAthleteCourseSummary(
                new AthleteCourseSummary(athlete, course2, 2)
        );
        acsDao.writeAthleteCourseSummary(
                new AthleteCourseSummary(athlete2, course3, 1)
        );

        List<Object[]> actualSummaries = acsDao.getAthleteCourseSummaries();
        assertThat(actualSummaries.size()).isEqualTo(3);

        Object[] cornwallAcs = actualSummaries.get(1);
        assertThat(cornwallAcs[0]).isEqualTo("Bob Te WILLIGA");
        assertThat(cornwallAcs[1]).isEqualTo(12345);
        assertThat(cornwallAcs[2]).isEqualTo(9002);
        assertThat(cornwallAcs[3]).isEqualTo(2);
    }
}
