package dnt.parkrun.stats;

import com.mysql.jdbc.Driver;
import dnt.parkrun.common.DateConverter;
import dnt.parkrun.courseeventsummary.Parser;
import dnt.parkrun.courses.reader.EventsJsonFileReader;
import dnt.parkrun.database.CourseDao;
import dnt.parkrun.datastructures.Country;
import dnt.parkrun.datastructures.Course;
import dnt.parkrun.datastructures.CourseEventSummary;
import dnt.parkrun.datastructures.CourseRepository;
import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static dnt.parkrun.common.UrlGenerator.generateCourseEventSummaryUrl;

public class HowManyResultsAreInTest
{
    private DataSource dataSource;

    @Before
    public void setUp() throws Exception
    {
        dataSource = new SimpleDriverDataSource(new Driver(),
                "jdbc:mysql://localhost/parkrun_stats", "stats", "statsfractalstats");
    }

    @Test
    public void areCourseUptoDate() throws IOException
    {
        CourseRepository courseRepository = new CourseRepository();
        new CourseDao(dataSource, courseRepository);

        Supplier<InputStream> supplier = () ->
        {
            try
            {
                return new URL("https://images.parkrun.com/events.json").openStream();
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        };

        Map<String, Course> upToDateCourses = new HashMap<>();
        SoftAssertions softly = new SoftAssertions();
        EventsJsonFileReader reader = new EventsJsonFileReader.Builder(supplier)
                .forEachCourse(course -> {
                    upToDateCourses.put(course.name, course);
                    softly.assertThat(courseRepository.getCourseFromName(course.name))
                            .describedAs("New course found " + course.country + "\t" + course.name)
                            .isNotNull();
                })
                .build();
        reader.read();

        courseRepository.addCourse(new Course(8888888, "Shenzhen", Country.UNKNOWN, "Shenzhen", Course.Status.STOPPED));

        courseRepository.forEachCourse(course -> {
            if(course.status != Course.Status.STOPPED)
            {
                softly.assertThat(upToDateCourses.get(course.name))
                        .describedAs("Existing course removed " + course.country + "\t" + course.name)
                        .isNotNull();
            }
        });
        softly.assertAll();
    }

    @Test
    public void howManyResultsAreInTest() throws IOException
    {
        CourseRepository courseRepository = new CourseRepository();
        CourseDao courseDao = new CourseDao(dataSource, courseRepository);

        for (Course course : courseDao.getCourses(Country.NZ))
        {
            List<CourseEventSummary> courseEventSummaries = new ArrayList<>();
            new Parser.Builder()
                    .course(course)
                    .forEachCourseEvent(courseEventSummaries::add)
                    .url(generateCourseEventSummaryUrl(course.country.baseUrl, course.name))
                    .build()
                    .parse();
            if(courseEventSummaries.isEmpty())
            {
                System.out.println("No results for " + course);
            }
            else
            {
                CourseEventSummary ces = courseEventSummaries.get(0);
                System.out.println(ces.finishers + "\t" + DateConverter.formatDateForHtml(ces.date) + "\t" + ces.course.longName);
            }
        }
    }
}
