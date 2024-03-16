package dnt.parkrun.stats;

import com.mysql.jdbc.Driver;
import dnt.parkrun.common.DateConverter;
import dnt.parkrun.courseeventsummary.Parser;
import dnt.parkrun.database.CourseDao;
import dnt.parkrun.datastructures.Country;
import dnt.parkrun.datastructures.Course;
import dnt.parkrun.datastructures.CourseEventSummary;
import dnt.parkrun.datastructures.CourseRepository;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static dnt.parkrun.common.UrlGenerator.generateCourseEventSummaryUrl;

public class HowManyResultsAreInTest
{
    @Ignore
    @Test
    public void howManyResultsAreInTest() throws SQLException, IOException
    {
        DataSource dataSource = new SimpleDriverDataSource(new Driver(),
                "jdbc:mysql://localhost/parkrun_stats", "stats", "statsfractalstats");

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
