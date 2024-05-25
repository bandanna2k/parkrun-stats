package dnt.parkrun.stats.invariants.predownload.last;

import dnt.parkrun.common.DateConverter;
import dnt.parkrun.common.UrlGenerator;
import dnt.parkrun.courseeventsummary.Parser;
import dnt.parkrun.database.CourseDao;
import dnt.parkrun.database.Database;
import dnt.parkrun.database.LiveDatabase;
import dnt.parkrun.datastructures.Country;
import dnt.parkrun.datastructures.Course;
import dnt.parkrun.datastructures.CourseEventSummary;
import dnt.parkrun.datastructures.CourseRepository;
import dnt.parkrun.webpageprovider.WebpageProviderImpl;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runners.Parameterized;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static dnt.parkrun.database.DataSourceUrlBuilder.getDataSourceUrl;
import static dnt.parkrun.datastructures.Country.NZ;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

public class AreLatestRegionResultsInTest
{
    @Parameterized.Parameter(0)
    public Course course;

    @Parameterized.Parameters(name = "{0}")
    public static Object[] data()
    {
        final Country country = NZ;
        String needsToGetCoursesFromDatabase = "sudo";
        Database database = new LiveDatabase(country, getDataSourceUrl(), "stats", "4b0e7ff1", needsToGetCoursesFromDatabase);

        CourseRepository courseRepository = new CourseRepository();
        CourseDao courseDao = new CourseDao(database, courseRepository);

        return courseDao.getCourses(country).stream()
                .filter(c -> c.status == Course.Status.RUNNING)
                .toArray();
    }

    @Test
    public void howManyResultsAreInTest()
    {
        UrlGenerator urlGenerator = new UrlGenerator(NZ.baseUrl);

        List<CourseEventSummary> courseEventSummaries = new ArrayList<>();
        Parser parser = new Parser.Builder()
                .course(course)
                .forEachCourseEvent(courseEventSummaries::add)
                .webpageProvider(new WebpageProviderImpl(urlGenerator.generateCourseEventSummaryUrl(course.name)))
                .build();
        parser.parse();

        if(courseEventSummaries.isEmpty())
        {
            Assertions.fail("No results for " + course);
        }
        else
        {
            CourseEventSummary ces = courseEventSummaries.getFirst();
            assertTrue(ces.finishers > 0);
            assertThat(ces.date).isAfter(Date.from(Instant.now().minusSeconds(60 * 60 * 24 * 5)));
            assertThat(ces.firstMale).isNotEmpty();
            assertThat(ces.firstFemale).isNotEmpty();
            System.out.println(ces.finishers + "\t" + DateConverter.formatDateForHtml(ces.date) + "\t" + ces.course.longName);
        }
    }
}
