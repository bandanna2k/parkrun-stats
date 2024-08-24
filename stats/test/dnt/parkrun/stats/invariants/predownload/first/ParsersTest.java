package dnt.parkrun.stats.invariants.predownload.first;

import dnt.parkrun.athletecoursesummary.Parser;
import dnt.parkrun.common.UrlGenerator;
import dnt.parkrun.database.CourseDao;
import dnt.parkrun.datastructures.*;
import dnt.parkrun.webpageprovider.WebpageProviderImpl;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dnt.parkrun.database.BaseDaoTest.TEST_DATABASE;
import static dnt.parkrun.datastructures.Athlete.NO_ATHLETE_ID;
import static dnt.parkrun.datastructures.Country.NZ;
import static org.assertj.core.api.Assertions.assertThat;

public class ParsersTest
{
    private static final Course CORNWALL = new Course(
            2, "cornwallpark", NZ, "Cornwall Park parkrun", Course.Status.RUNNING);
    private static final Course LOWER_HUTT = new Course(
            1, "lowerhutt", NZ, "Lower Hutt parkrun", Course.Status.RUNNING);

    private final Country country = NZ;

    private final UrlGenerator urlGenerator = new UrlGenerator(NZ.baseUrl);

    @Test
    public void testCourseSummaryParser()
    {
        List<CourseEventSummary> listOfCourseEvents = new ArrayList<>();
        dnt.parkrun.courseeventsummary.Parser parser = new dnt.parkrun.courseeventsummary.Parser.Builder()
                .webpageProvider(new WebpageProviderImpl(urlGenerator.generateCourseEventSummaryUrl(CORNWALL.name)))
                .forEachCourseEvent(listOfCourseEvents::add)
                .course(CORNWALL)
                .build();
        parser.parse();

        assertThat(listOfCourseEvents.size()).isGreaterThan(500);
    }

    @Test
    public void testCourseEventParser()
    {
        CourseRepository courseRepository = new CourseRepository();
        courseRepository.addCourse(LOWER_HUTT);

        List<Volunteer> listOfVolunteers = new ArrayList<>();
        List<Athlete> listOfAthletes = new ArrayList<>();
        dnt.parkrun.courseevent.Parser parser = new dnt.parkrun.courseevent.Parser.Builder(courseRepository)
                .webpageProvider(new WebpageProviderImpl(urlGenerator.generateCourseEventUrl(LOWER_HUTT.name, 1)))
                .forEachAthlete(listOfAthletes::add)
                .forEachVolunteer(listOfVolunteers::add)
                .build();
        parser.parse();
        {
            assertThat(listOfAthletes.size()).isGreaterThan(20);
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
            assertThat(listOfVolunteers.size()).isGreaterThan(2);
            listOfVolunteers.forEach(v ->
            {
                assertThat(v.athlete.name).isNotNull();
                assertThat(v.athlete.athleteId).isNotEqualTo(NO_ATHLETE_ID);
            });
        }
    }

    @Test
    public void testAthleteCourseSummaryParser()
    {
        CourseRepository courseRepository = new CourseRepository();
        new CourseDao(TEST_DATABASE, courseRepository);

        List<AthleteCourseSummary> list = new ArrayList<>();
        Map<String, Integer> volunteerTypeToCount = new HashMap<>();
        Parser parser = new Parser.Builder()
                .webpageProvider(new WebpageProviderImpl(urlGenerator.generateAthleteEventSummaryUrl(2147564)))
                .forEachVolunteerRecord(obj -> volunteerTypeToCount.put((String)obj[1], (int)obj[2]))
                .forEachAthleteCourseSummary(list::add)
                .build(courseRepository);
        parser.parse();

        volunteerTypeToCount.forEach((type, count) -> assertThat(count).isGreaterThan(0));
        Integer totalCredits = volunteerTypeToCount.get("Total Credits");
        System.out.printf("Count: %d, Total Credits: %d%n", volunteerTypeToCount.size(), totalCredits);
        assertThat(totalCredits).isNotNull();
        assertThat(list.size()).isGreaterThan(49);
    }
}
