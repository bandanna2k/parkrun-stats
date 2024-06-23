package dnt.parkrun.backfill;

import dnt.parkrun.common.UrlGenerator;
import dnt.parkrun.courseevent.Parser;
import dnt.parkrun.database.*;
import dnt.parkrun.datastructures.Country;
import dnt.parkrun.datastructures.Course;
import dnt.parkrun.datastructures.CourseEventSummary;
import dnt.parkrun.datastructures.CourseRepository;
import dnt.parkrun.webpageprovider.WebpageProviderImpl;

import java.util.List;

import static dnt.parkrun.database.DataSourceUrlBuilder.getDataSourceUrl;

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

    public static void main(String[] args)
    {
        new BackfillVolunteersForACourse(Country.valueOf(args[0])).backfill();
    }
    public void backfill()
    {
        assert country.countryCode == 65;
        Course backfillCourse = new Course(44, "porirua", country, "Porirua parkrun", Course.Status.STOPPED);

        Database database = new LiveDatabase(country, getDataSourceUrl(), "dao", "0b851094", "sudo");

        AthleteDao athleteDao = new AthleteDao(database);
        VolunteerDao volunteerDao = new VolunteerDao(database);

        CourseRepository courseRepository = new CourseRepository();
        new CourseDao(database, courseRepository);

        List<CourseEventSummary> courseEventSummaries = new CourseEventSummaryDao(database, courseRepository)
                .getCourseEventSummaries().stream()
                .filter(ces -> ces.course.courseId == backfillCourse.courseId)
                .toList();

        int counter = 1;
        int size = courseEventSummaries.size();
        for (CourseEventSummary ces : courseEventSummaries)
        {
            System.out.printf("Downloading %d of %d ", counter++, size);
            Parser parser = new Parser.Builder(courseRepository)
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
