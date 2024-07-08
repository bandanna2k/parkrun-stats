package dnt.parkrun.stats.invariants.predownload.last;

import dnt.parkrun.common.DateConverter;
import dnt.parkrun.common.UrlGenerator;
import dnt.parkrun.courseeventsummary.Parser;
import dnt.parkrun.database.CourseDao;
import dnt.parkrun.database.CourseEventSummaryDao;
import dnt.parkrun.database.Database;
import dnt.parkrun.database.LiveDatabase;
import dnt.parkrun.datastructures.Country;
import dnt.parkrun.datastructures.Course;
import dnt.parkrun.datastructures.CourseEventSummary;
import dnt.parkrun.datastructures.CourseRepository;
import dnt.parkrun.webpageprovider.WebpageProviderImpl;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.time.Instant;
import java.util.*;

import static dnt.parkrun.database.DataSourceUrlBuilder.getDataSourceUrl;
import static dnt.parkrun.datastructures.Country.NZ;

@RunWith(Parameterized.class)
public class LatestResultsAndEventHistoryTest
{
    private static final Country country = NZ;
    private static final Database database = new LiveDatabase(country, getDataSourceUrl(), "stats", "4b0e7ff1");
    private static final CourseRepository courseRepository = new CourseRepository();;
    private static final CourseDao courseDao = new CourseDao(database, courseRepository);
    private static final CourseEventSummaryDao courseEventSummaryDao = new CourseEventSummaryDao(database, courseRepository);

    @Parameterized.Parameter(0)
    public Course course;
    private final Map<Integer, CourseEventSummary> eventNumberToCourseEventSummary = new HashMap<>();

    @Parameterized.Parameters(name = "{0}")
    public static Object[] data()
    {
        return courseDao.getCourses(country).stream()
                .filter(c -> c.status == Course.Status.RUNNING)
                .toArray();
    }

    @Test
    public void howManyResultsAreInTest()
    {
        SoftAssertions softly = new SoftAssertions();
        UrlGenerator urlGenerator = new UrlGenerator(NZ.baseUrl);

        List<CourseEventSummary> courseEventSummaries = new ArrayList<>();
        Parser parser = new Parser.Builder()
                .course(course)
                .forEachCourseEvent(e ->
                {
                    eventNumberToCourseEventSummary.put(e.eventNumber, e);
                    courseEventSummaries.add(e);
                })
                .webpageProvider(new WebpageProviderImpl(urlGenerator.generateCourseEventSummaryUrl(course.name)))
                .build();
        parser.parse();

        if(courseEventSummaries.isEmpty())
        {
            Assertions.fail("No results for " + course);
        }
        else
        {
            CourseEventSummary ces1 = courseEventSummaries.getFirst();
            System.out.println(ces1.finishers + "\t" + DateConverter.formatDateForHtml(ces1.date) + "\t" + ces1.course.longName);
            softly.assertThat(ces1.finishers).isGreaterThan(0);
            softly.assertThat(ces1.volunteers).isGreaterThan(0);
            softly.assertThat(ces1.date).isAfter(Date.from(Instant.now().minusSeconds(60 * 60 * 24 * 5)));
            softly.assertThat(ces1.firstMale).isNotEmpty();
            softly.assertThat(ces1.firstFemale).isNotEmpty();

            List<CourseEventSummary> databaseCourseEventSummaries = courseEventSummaryDao.getCourseEventSummaries(course.courseId);
            databaseCourseEventSummaries.forEach(dces -> {
                String error = String.format("'%s%d'", dces.course.name, dces.eventNumber);
                int eventNumber = dces.eventNumber;
                CourseEventSummary liveCes =  eventNumberToCourseEventSummary.get(eventNumber);
                softly.assertThat(liveCes.finishers).describedAs(error + "(Finishers)").isEqualTo(dces.finishers);
                softly.assertThat(liveCes.volunteers).describedAs(error + "(Volunteers)").isEqualTo(dces.volunteers);
            });
            softly.assertAll();
        }
    }
}
