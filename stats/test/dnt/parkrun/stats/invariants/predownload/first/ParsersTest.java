package dnt.parkrun.stats.invariants.predownload.first;

import com.mysql.jdbc.Driver;
import dnt.parkrun.athletecoursesummary.Parser;
import dnt.parkrun.common.UrlGenerator;
import dnt.parkrun.database.CourseDao;
import dnt.parkrun.datastructures.*;
import dnt.parkrun.webpageprovider.WebpageProviderImpl;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static dnt.parkrun.database.DataSourceUrlBuilder.Type.PARKRUN_STATS;
import static dnt.parkrun.database.DataSourceUrlBuilder.getDataSourceUrl;
import static dnt.parkrun.datastructures.Athlete.NO_ATHLETE_ID;
import static dnt.parkrun.datastructures.Country.NZ;

public class ParsersTest
{
    private static final Course CORNWALL = new Course(
            2, "cornwallpark", NZ, "Cornwall Park parkrun", Course.Status.RUNNING);
    private static final Course LOWER_HUTT = new Course(
            1, "lowerhutt", NZ, "Lower Hutt parkrun", Course.Status.RUNNING);

    private final Country country = NZ;

    private final UrlGenerator urlGenerator = new UrlGenerator(NZ.baseUrl);

    @Test
    public void testCourseSummary() throws IOException
    {
        List<CourseEventSummary> listOfCourseEvents = new ArrayList<>();
        dnt.parkrun.courseeventsummary.Parser parser = new dnt.parkrun.courseeventsummary.Parser.Builder()
                .webpageProvider(new WebpageProviderImpl(urlGenerator.generateCourseEventSummaryUrl(CORNWALL.name)))
                .forEachCourseEvent(listOfCourseEvents::add)
                .course(CORNWALL)
                .build();
        parser.parse();

        Assertions.assertThat(listOfCourseEvents.size()).isGreaterThan(500);
    }

    @Test
    public void testCourseEvent() throws SQLException
    {
        List<Volunteer> listOfVolunteers = new ArrayList<>();
        List<Athlete> listOfAthletes = new ArrayList<>();
        dnt.parkrun.courseevent.Parser parser = new dnt.parkrun.courseevent.Parser.Builder(LOWER_HUTT)
                .webpageProvider(new WebpageProviderImpl(urlGenerator.generateCourseEventUrl(LOWER_HUTT.name, 1)))
                .forEachAthlete(listOfAthletes::add)
                .forEachVolunteer(listOfVolunteers::add)
                .build();
        parser.parse();
        {
            Assertions.assertThat(listOfAthletes.size()).isGreaterThan(20);
            int noIds = listOfAthletes.stream().filter(a -> a.athleteId == NO_ATHLETE_ID).toList().size();
            int unknowns = listOfAthletes.stream().filter(a -> a.name == null).toList().size();
            int knowns = listOfAthletes.stream().filter(a -> a.athleteId != NO_ATHLETE_ID).toList().size();
//            System.out.println("noIds: " + noIds);
//            System.out.println("unknowns: " + unknowns);
//            System.out.println("knowns: " + knowns);
            assert knowns > noIds : "knowns > noIds";
            assert knowns > unknowns : "knowns > unknowns";
        }
        {
            Assertions.assertThat(listOfVolunteers.size()).isGreaterThan(2);
            listOfVolunteers.forEach(v ->
            {
                Assertions.assertThat(v.athlete.name).isNotNull();
                Assertions.assertThat(v.athlete.athleteId).isNotEqualTo(NO_ATHLETE_ID);
            });
        }
    }

    @Test
    public void testAthleteCourseSummary() throws SQLException
    {
        DataSource dataSource = new SimpleDriverDataSource(new Driver(),
                getDataSourceUrl(PARKRUN_STATS, country), "stats", "4b0e7ff1");
        CourseRepository courseRepository = new CourseRepository();
        new CourseDao(country, dataSource, courseRepository);

        List<AthleteCourseSummary> list = new ArrayList<>();
        Parser parser = new Parser.Builder()
                .url(urlGenerator.generateAthleteEventSummaryUrl(414811))
                .forEachAthleteCourseSummary(list::add)
                .build(courseRepository);
        parser.parse();

        Assertions.assertThat(list.size()).isGreaterThan(49);
    }
}
