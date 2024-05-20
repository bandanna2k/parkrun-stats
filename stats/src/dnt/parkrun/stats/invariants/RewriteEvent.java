package dnt.parkrun.stats.invariants;

import com.mysql.jdbc.Driver;
import dnt.parkrun.common.UrlGenerator;
import dnt.parkrun.courseevent.Parser;
import dnt.parkrun.database.*;
import dnt.parkrun.datastructures.*;
import dnt.parkrun.webpageprovider.WebpageProviderImpl;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static dnt.parkrun.database.DataSourceUrlBuilder.getDataSourceUrl;

public class RewriteEvent
{
    private final ResultDao resultDao;
    private final CourseRepository courseRepository;
    private final VolunteerDao volunteerDao;
    private final UrlGenerator urlGenerator;
    private final CourseEventSummaryDao courseEventSummaryDao;
    private final AthleteDao athleteDao;

    public static void main(String[] args) throws SQLException
    {
        Country country = Country.valueOf(args[0]);
        DataSource dataSource = new SimpleDriverDataSource(new Driver(), getDataSourceUrl(), "dao", "0b851094");

        RewriteEvent rewriteEvent = new RewriteEvent(country, dataSource);
//        rewriteEvent.rewriteCourseEvent("blenheim", 368);
        rewriteEvent.rewriteCourseEvent("owairaka", 160);
    }

    public RewriteEvent(Country country, DataSource dataSource)
    {
        urlGenerator = new UrlGenerator(country.baseUrl);

        courseRepository = new CourseRepository();
        new CourseDao(country, dataSource, courseRepository);

        athleteDao = new AthleteDao(country, dataSource);
        courseEventSummaryDao = new CourseEventSummaryDao(country, dataSource, courseRepository);
        resultDao = new ResultDao(country, dataSource);
        volunteerDao = new VolunteerDao(country, dataSource);
    }

    private void rewriteCourseEvent(String courseName, int eventNumber)
    {
        Course course = courseRepository.getCourseFromName(courseName);

        // Get course event summary from web
        AtomicReference<CourseEventSummary> maybeCourseEventSummary = new AtomicReference<>();
        {
            dnt.parkrun.courseeventsummary.Parser parser = new dnt.parkrun.courseeventsummary.Parser.Builder()
                    .webpageProvider(new WebpageProviderImpl(urlGenerator.generateCourseEventSummaryUrl(course.name)))
                    .course(course)
                    .forEachCourseEvent(ces ->
                    {
                        if(eventNumber == ces.eventNumber) maybeCourseEventSummary.set(ces);
                    })
                    .build();
            parser.parse();
        }
        assert maybeCourseEventSummary.get() != null;
        CourseEventSummary newCourseEventSummary = maybeCourseEventSummary.get();

        {
            System.out.printf("INFO Fixing results for course: %s, date: %s, event number: %d %n",
                    course.name, newCourseEventSummary.date, eventNumber);

            System.out.print("WARNING Deleting results ... ");
            resultDao.delete(course.courseId, newCourseEventSummary.date);
            System.out.printf("Done%n");

            System.out.print("WARNING Deleting volunteers ... ");
            volunteerDao.delete(course.courseId, newCourseEventSummary.date);
            System.out.printf("Done%n");

            System.out.print("WARNING Deleting course event summary ... ");
            courseEventSummaryDao.delete(course.courseId, newCourseEventSummary.date);
            System.out.printf("Done%n");
        }

        List<Result> newResults = new ArrayList<>();
        List<Athlete> newAthletes = new ArrayList<>();
        List<Volunteer> newVolunteers = new ArrayList<>();
        {
            Parser parser = new Parser.Builder(course)
                    .webpageProvider(new WebpageProviderImpl(urlGenerator.generateCourseEventUrl(course.name, eventNumber)))
                    .forEachResult(newResults::add)
                    .forEachAthlete(e ->
                    {
//                      System.out.println("A " + e);
                        newAthletes.add(e);
                    })
                    .forEachVolunteer(e1 ->
                    {
//                        System.out.println("V " + e1);
                        newVolunteers.add(e1);
                    })
                    .build();
            parser.parse();
        }

        System.out.print("INFO Re-entering course event summary ... ");
        courseEventSummaryDao.insert(maybeCourseEventSummary.get());
        System.out.printf("Done%n");

        System.out.print("INFO Re-entering athletes ... ");
        athleteDao.insert(newAthletes);
        System.out.printf("Done%n");

        System.out.print("INFO Re-entering volunteers ... ");
        volunteerDao.insert(newVolunteers);
        System.out.printf("Done%n");

        System.out.print("INFO Re-entering results ... ");
        resultDao.insert(newResults);
        System.out.printf("Done%n");

    }
}
