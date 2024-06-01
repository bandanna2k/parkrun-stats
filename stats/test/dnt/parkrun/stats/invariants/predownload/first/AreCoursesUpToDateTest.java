package dnt.parkrun.stats.invariants.predownload.first;

import dnt.parkrun.courses.reader.EventsJsonFileReader;
import dnt.parkrun.database.CourseDao;
import dnt.parkrun.database.Database;
import dnt.parkrun.database.LiveDatabase;
import dnt.parkrun.datastructures.Country;
import dnt.parkrun.datastructures.Course;
import dnt.parkrun.datastructures.CourseRepository;
import org.assertj.core.api.SoftAssertions;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static dnt.parkrun.database.DataSourceUrlBuilder.getDataSourceUrl;
import static dnt.parkrun.datastructures.Country.NZ;

public class AreCoursesUpToDateTest
{
    private final Country country = NZ;

    @Test
    public void areCoursesUpToDate() throws IOException
    {
        boolean disableDatabaseRuns = false;
        boolean addNewCourses = false;

        String permissionToInsertRecords = "sudo";
        Database database = new LiveDatabase(country, getDataSourceUrl(), "dao", "0b851094", permissionToInsertRecords);
        CourseRepository courseRepository = new CourseRepository();
        CourseDao courseDao = new CourseDao(database, courseRepository);

        Supplier<InputStream> supplier = () ->
        {
            try
            {
                return URI.create("https://images.parkrun.com/events.json").toURL().openStream();
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        };

        Map<String, Course> upToDateCourses = new HashMap<>();
        SoftAssertions softly = new SoftAssertions();
        EventsJsonFileReader reader = new EventsJsonFileReader.Builder(supplier)
                .forEachCourse(course ->
                {
                    upToDateCourses.put(course.name, course);

                    Course repositoryCourse = courseRepository.getCourseFromName(course.name);
                    if (repositoryCourse == null)
                    {
                        final String warning;
                        if (course.country == NZ)
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
                        if (addNewCourses)
                        {
                            courseDao.insert(course);
                            System.out.println("Adding new course " + course);
                        }
                    }
                })
                .build();
        reader.read();

        courseRepository.forEachCourse(course ->
        {
            if (course.status != Course.Status.STOPPED)
            {
                Course upToDateCourse = upToDateCourses.get(course.name);
                if (upToDateCourse == null)
                {
                    softly.fail("Existing course now removed from json file " + course.country + "\t" + course.name);

                    if (disableDatabaseRuns)
                    {
                        courseDao.setCourseStatus(course.name, Course.Status.STOPPED);
                        System.out.println("Set status to STOPPED for " + course);
                    }
                }
            }
        });
        softly.assertAll();
    }
}