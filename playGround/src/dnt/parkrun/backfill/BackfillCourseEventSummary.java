package dnt.parkrun.backfill;

import com.mysql.jdbc.Driver;
import dnt.parkrun.common.UrlGenerator;
import dnt.parkrun.courseeventsummary.Parser;
import dnt.parkrun.courses.reader.EventsJsonFileReader;
import dnt.parkrun.database.CourseEventSummaryDao;
import dnt.parkrun.datastructures.Course;
import dnt.parkrun.datastructures.CourseRepository;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

import static dnt.parkrun.datastructures.Country.NZ;

@Deprecated
public class BackfillCourseEventSummary
{
    public static void main(String[] args) throws IOException, SQLException
    {
        CourseRepository courseRepository = new CourseRepository();

        DataSource dataSource = new SimpleDriverDataSource(new Driver(),
                "jdbc:mysql://localhost/parkrun_stats", "dao", "daoFractaldao");

        System.out.println("* Adding courses *");
        InputStream inputStream = Course.class.getResourceAsStream("/events.json");
        dnt.parkrun.courses.reader.EventsJsonFileReader reader = new EventsJsonFileReader.Builder(() -> inputStream)
                .forEachCourse(course ->
                {
                    if(course.country == NZ)
                    {
                        courseRepository.addCourse(course);
                    }
                })
                .statusSupplier(() -> Course.Status.RUNNING)
                .build();
        reader.read();

        CourseEventSummaryDao courseEventSummaryDao = new CourseEventSummaryDao(dataSource, courseRepository);

        for (Course course : courseRepository.getCourses(NZ))
        {
            System.out.println("* Collecting summary for " + course + " *");
            Parser parser = new Parser.Builder()
                    .url(UrlGenerator.generateCourseEventSummaryUrl("parkrun.co.nz", course.name))
                    .forEachCourseEvent(ces ->
                    {
                        System.out.println(ces);
                        courseEventSummaryDao.backfillFinishers(course.name, ces.eventNumber, ces.finishers, ces.date);
                    })
                    .build();
            parser.parse();
        }
    }
}
