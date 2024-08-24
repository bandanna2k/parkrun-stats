package dnt.parkrun.stats.invariants.predownload.first;

import dnt.parkrun.common.UrlGenerator;
import dnt.parkrun.courseevent.Parser;
import dnt.parkrun.database.CourseDao;
import dnt.parkrun.database.Database;
import dnt.parkrun.database.LiveDatabase;
import dnt.parkrun.datastructures.Country;
import dnt.parkrun.datastructures.Course;
import dnt.parkrun.datastructures.CourseRepository;
import dnt.parkrun.webpageprovider.WebpageProviderImpl;
import org.assertj.core.api.SoftAssertions;
import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static dnt.parkrun.database.DataSourceUrlBuilder.getDataSourceUrl;
import static dnt.parkrun.datastructures.Country.NZ;
import static dnt.parkrun.datastructures.Course.Status.RUNNING;

public class PendingCoursesTest
{
    private final Country country = NZ;

    @Test
    public void showPendingCoursesWithResults()
    {
        Database database = new LiveDatabase(country, getDataSourceUrl(), "stats", "4b0e7ff1", "sudo");
        CourseRepository courseRepository = new CourseRepository();
        CourseDao courseDao = new CourseDao(database, courseRepository);

        List<Course> pendingCourses = courseDao.getCourses(NZ).stream()
                .filter(c -> c.status == Course.Status.PENDING)
                .toList();

        Set<String> coursesToStart = new HashSet<>() {{
//            add("scarborough");
        }};

        UrlGenerator urlGenerator = new UrlGenerator(NZ.baseUrl);
        SoftAssertions softly = new SoftAssertions();
        pendingCourses.forEach(pc -> {
            try
            {
                if(coursesToStart.contains(pc.name))
                {
                    courseDao.setCourseStatus(pc.name, RUNNING);
                    System.out.printf("Course '%s' is set to %s%n", pc.name, RUNNING);
                }

                AtomicInteger volunteers = new AtomicInteger(0);
                AtomicInteger finishers = new AtomicInteger(0);
                Parser parser = new Parser.Builder(courseRepository)
                        .webpageProvider(new WebpageProviderImpl(urlGenerator.generateCourseEventUrl(pc.name, 1)))
                        .forEachResult(result -> finishers.incrementAndGet())
                        .forEachVolunteer(volunteer -> volunteers.incrementAndGet())
                        .build();
                parser.parse();

                System.out.printf("Course '%s' (%s), has results in for event number 1. Finishers %d, Volunteers %d%n",
                        pc.name, pc.longName, finishers.get(), volunteers.get());
                softly.assertThat(finishers.get() == 0 && volunteers.get() == 0)
                        .describedAs("Course %s has finishers and volunteers, but is set to pending?", pc.name)
                        .isTrue();
            }
            catch (Exception ex)
            {
                softly.fail(String.format("Exception for course '%s' %s", pc.name, ex.getMessage()));
            }
        });
        softly.assertAll();
    }
}
