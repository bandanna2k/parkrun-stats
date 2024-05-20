package dnt.parkrun.backfill;

import com.mysql.jdbc.Driver;
import dnt.parkrun.common.UrlGenerator;
import dnt.parkrun.courseevent.Parser;
import dnt.parkrun.database.AthleteDao;
import dnt.parkrun.database.CourseDao;
import dnt.parkrun.database.CourseEventSummaryDao;
import dnt.parkrun.database.VolunteerDao;
import dnt.parkrun.datastructures.Country;
import dnt.parkrun.datastructures.Course;
import dnt.parkrun.datastructures.CourseEventSummary;
import dnt.parkrun.datastructures.CourseRepository;
import dnt.parkrun.webpageprovider.WebpageProviderImpl;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

@Deprecated(since = "Deprecated to discourage use")
public class BackfillVolunteersForACourse
{
    private final UrlGenerator urlGenerator;
    private final Country country;

    public BackfillVolunteersForACourse(Country country)
    {
        this.country = country;
        this.urlGenerator = new UrlGenerator(country.baseUrl);
    }

    public static void main(String[] args) throws SQLException
    {
        new BackfillVolunteersForACourse(Country.valueOf(args[0])).backfill();
    }
    public void backfill() throws SQLException
    {
        assert country.countryCode == 65;
        Course backfillCourse = new Course(44, "porirua", country, "Porirua parkrun", Course.Status.STOPPED);

        DataSource dataSource = new SimpleDriverDataSource(new Driver(),
                "jdbc:mysql://localhost/parkrun_stats", "dao", "daoFractaldao");
        NamedParameterJdbcTemplate jdbc = new NamedParameterJdbcTemplate(dataSource);

        AthleteDao athleteDao = new AthleteDao(dataSource);
        VolunteerDao volunteerDao = new VolunteerDao(country, dataSource);

        CourseRepository courseRepository = new CourseRepository();
        new CourseDao(dataSource, courseRepository);

        List<CourseEventSummary> courseEventSummaries = new CourseEventSummaryDao(country, dataSource, courseRepository)
                .getCourseEventSummaries().stream()
                .filter(ces -> ces.course.courseId == backfillCourse.courseId)
                .toList();

        int counter = 1;
        int size = courseEventSummaries.size();
        for (CourseEventSummary ces : courseEventSummaries)
        {
            System.out.printf("Downloading %d of %d ", counter++, size);
            Parser parser = new Parser.Builder(ces.course)
                    .webpageProvider(new WebpageProviderImpl(urlGenerator.generateCourseEventUrl(backfillCourse.name, ces.eventNumber)))
                    .forEachVolunteer(v ->
                    {
                        athleteDao.insert(v.athlete);
                        volunteerDao.insert(v);
                    })
                    .build();
            parser.parse();
        }
    }
}
