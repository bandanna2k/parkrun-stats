package dnt.parkrun.stats.invariants;

import com.mysql.jdbc.Driver;
import dnt.parkrun.common.DateConverter;
import dnt.parkrun.common.UrlGenerator;
import dnt.parkrun.courseeventsummary.Parser;
import dnt.parkrun.courses.reader.EventsJsonFileReader;
import dnt.parkrun.database.CourseDao;
import dnt.parkrun.datastructures.Country;
import dnt.parkrun.datastructures.Course;
import dnt.parkrun.datastructures.CourseEventSummary;
import dnt.parkrun.datastructures.CourseRepository;
import dnt.parkrun.webpageprovider.WebpageProviderImpl;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.SQLException;
import java.time.Instant;
import java.util.*;
import java.util.function.Supplier;

import static dnt.parkrun.common.DateConverter.ONE_DAY_IN_MILLIS;
import static dnt.parkrun.datastructures.Country.NZ;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(Enclosed.class)
public class HowYouDoingTest
{
    @RunWith(Parameterized.class)
    public static class AreResultsIn
    {
        @Parameterized.Parameter(0)
        public Course course;

        @Parameterized.Parameters(name = "{0}")
        public static Object[] data() throws SQLException
        {
            DataSource dataSource = new SimpleDriverDataSource(new Driver(),
                    "jdbc:mysql://localhost/parkrun_stats", "dao", "daoFractaldao");
            CourseRepository courseRepository = new CourseRepository();
            CourseDao courseDao = new CourseDao(dataSource, courseRepository);

            return courseDao.getCourses(NZ).stream()
                    .filter(c -> c.status == Course.Status.RUNNING)
                    .toArray();
        }

        @Test
        public void howManyResultsAreInTest() throws IOException
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
                CourseEventSummary ces = courseEventSummaries.get(0);
                assertTrue(ces.finishers > 0);
                assertThat(ces.date).isAfter(Date.from(Instant.now().minusSeconds(60 * 60 * 24 * 5)));
                assertThat(ces.firstMale).isNotEmpty();
                assertThat(ces.firstFemale).isNotEmpty();
                System.out.println(ces.finishers + "\t" + DateConverter.formatDateForHtml(ces.date) + "\t" + ces.course.longName);
            }
        }
    }

    public static class NewCourseTest
    {
        @Test
        public void areCoursesUpToDate() throws IOException, SQLException
        {
            DataSource dataSource = new SimpleDriverDataSource(new Driver(),
                    "jdbc:mysql://localhost/parkrun_stats", "dao", "daoFractaldao");
            CourseRepository courseRepository = new CourseRepository();
            CourseDao courseDao = new CourseDao(dataSource, courseRepository);

            boolean disableDatabaseRuns = false;
            boolean addNewCourses = false;
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

                        Course repositoryCourse = courseRepository.getCourseFromName(course.name);
                        if(repositoryCourse == null)
                        {
                            final String warning;
                            if(course.country == NZ)
                            {
                                warning = STR."""
                                        *** New course found \{course.country}  \{course.name}  \{course.longName} ***
                                        INSERT INTO course (
                                        course_id, course_name, course_long_name, country_code, country, status
                                        ) VALUES (
                                        ID, '\{course.name}', '\{course.longName}', \{course.country.countryCode}, 'NZ', 'P'
                                        );
                                        """;
                            }
                            else
                            {
                                warning = STR."*** New course found \{course.country}  \{course.name}  \{course.longName} ***";
                            }
                            softly.fail(warning);
                            if(addNewCourses)
                            {
                                courseDao.insert(course);
                                System.out.println("Adding new course " + course);
                            }
                        }
                    })
                    .build();
            reader.read();

            courseRepository.forEachCourse(course -> {
                if(course.status != Course.Status.STOPPED)
                {
                    Course upToDateCourse = upToDateCourses.get(course.name);
                    if(upToDateCourse == null)
                    {
                        softly.fail("Existing course now removed " + course.country + "\t" + course.name);

                        if(disableDatabaseRuns)
                        {
                            courseDao.setCourseStatus(course.name, Course.Status.STOPPED);
                            System.out.println("Set status to STOPPED for " + course);
                        }
                    }
                }
            });
            softly.assertAll();
        }

        @Test
        public void areLastResultsIn() throws IOException
        {
            SoftAssertions softly = new SoftAssertions();
            softly.assertThat(areResultsIn(new Course(12345, "shawniganhills", Country.CANADA, "shawniganhills", Course.Status.RUNNING)))
                    .describedAs("shawniganhills results not in")
                    .isEqualTo(true);
            softly.assertThat(areResultsIn(new Course(12346, "cloverpoint", Country.CANADA, "cloverpoint", Course.Status.RUNNING)))
                    .describedAs("cloverpoint results not in")
                    .isEqualTo(true);
            softly.assertThat(areResultsIn(new Course(12347, "ambleside", Country.CANADA, "ambleside", Course.Status.RUNNING)))
                    .describedAs("ambleside results not in")
                    .isEqualTo(true);
            softly.assertAll();
        }
        public boolean areResultsIn(Course course) throws IOException
        {
            UrlGenerator urlGenerator = new UrlGenerator(Country.CANADA.baseUrl);
            dnt.parkrun.courseevent.Parser parser = new dnt.parkrun.courseevent.Parser.Builder(course)
                    .webpageProvider(new WebpageProviderImpl(urlGenerator.generateCourseLatestResultsUrl(course.name)))
                    .build();
            parser.parse();

            Date dateFromMostWesterlyParkrun = parser.getDate();
            Date todayMinus5Days = new Date();
            todayMinus5Days.setTime(todayMinus5Days.getTime() - (5 * ONE_DAY_IN_MILLIS));
            return dateFromMostWesterlyParkrun.after(todayMinus5Days);
        }
    }
}
