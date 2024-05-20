package dnt.parkrun.backfill;

import com.mysql.jdbc.Driver;
import dnt.parkrun.common.UrlGenerator;
import dnt.parkrun.courseeventsummary.Parser;
import dnt.parkrun.courses.reader.EventsJsonFileReader;
import dnt.parkrun.database.CourseEventSummaryDao;
import dnt.parkrun.datastructures.Country;
import dnt.parkrun.datastructures.Course;
import dnt.parkrun.datastructures.CourseRepository;
import dnt.parkrun.webpageprovider.WebpageProviderImpl;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Date;

@Deprecated(since = "Deprecated to discourage use.")
public class BackfillCourseEventSummary
{
    private final UrlGenerator urlGenerator;
    private final Country country;

    private NamedParameterJdbcTemplate jdbc;

    public BackfillCourseEventSummary(Country country)
    {
        this.country = country;
        this.urlGenerator = new UrlGenerator(country.baseUrl);
    }

    public static void main(String[] args) throws IOException, SQLException
    {
        new BackfillCourseEventSummary(Country.valueOf(args[0])).backfill();
    }
    public void backfill() throws SQLException, IOException
    {
        CourseRepository courseRepository = new CourseRepository();

        DataSource dataSource = new SimpleDriverDataSource(new Driver(),
                "jdbc:mysql://localhost/parkrun_stats", "dao", "daoFractaldao");
        jdbc = new NamedParameterJdbcTemplate(dataSource);

        System.out.println("* Adding courses *");
        InputStream inputStream = Course.class.getResourceAsStream("/events.json");
        dnt.parkrun.courses.reader.EventsJsonFileReader reader = new EventsJsonFileReader.Builder(() -> inputStream)
                .forEachCourse(course ->
                {
                    if(course.country == country)
                    {
                        courseRepository.addCourse(course);
                    }
                })
                .statusSupplier(() -> Course.Status.RUNNING)
                .build();
        reader.read();

        CourseEventSummaryDao courseEventSummaryDao = new CourseEventSummaryDao(country, dataSource, courseRepository);

        for (Course course : courseRepository.getCourses(country))
        {
            System.out.println("* Collecting summary for " + course + " *");
            Parser parser = new Parser.Builder()
                    .webpageProvider(new WebpageProviderImpl(urlGenerator.generateCourseEventSummaryUrl(course.name)))
                    .forEachCourseEvent(ces ->
                    {
                        System.out.println(ces);
                        backfillFinishers(course.name, ces.eventNumber, ces.finishers, ces.date);
                    })
                    .build();
            parser.parse();
        }
    }

    public void backfillFinishers(String name, int eventNumber, int finishers, Date date)
    {
        int update = jdbc.update("update course_event_summary " +
                        "set date = :date " +
                        "where course_name = :courseName and " +
                        " event_number = :eventNumber and " +
                        " date is null",
                new MapSqlParameterSource()
                        .addValue("courseName", name)
                        .addValue("eventNumber", eventNumber)
                        .addValue("date", date)
                        .addValue("finishers", finishers));
    }
}
