package dnt.parkrun.stats.invariants;

import dnt.parkrun.common.UrlGenerator;
import dnt.parkrun.courseevent.Parser;
import dnt.parkrun.database.*;
import dnt.parkrun.datastructures.*;
import dnt.parkrun.webpageprovider.WebpageProviderImpl;

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

    public static void main(String[] args)
    {
        Database database = new LiveDatabase(Country.NZ, getDataSourceUrl(), "dao", "0b851094", "sudo");

        RewriteEvent rewriteEvent = new RewriteEvent(database);
//        rewriteEvent.rewriteCourseEvent("otakiriver", 76); // David 1st place

        rewriteEvent.rewriteCourseEvent("araharakeke96");
        rewriteEvent.rewriteCourseEvent("broadpark106");
        rewriteEvent.rewriteCourseEvent("eastend275");
        rewriteEvent.rewriteCourseEvent("gisborne294");
        rewriteEvent.rewriteCourseEvent("hagley482");
        rewriteEvent.rewriteCourseEvent("invercargill301");
    }

    private void rewriteCourseEvent(String eventString)
    {
        int i = eventString.length() - 1;
        while(Character.isDigit(eventString.charAt(i)))
        {
            i--;
        }
        rewriteCourseEvent(eventString.substring(0, i+1), Integer.parseInt(eventString.substring(i + 1)));
    }

    public RewriteEvent(Database database)
    {
        urlGenerator = new UrlGenerator(database.country.baseUrl);

        courseRepository = new CourseRepository();
        new CourseDao(database, courseRepository);

        athleteDao = new AthleteDao(database);
        courseEventSummaryDao = new CourseEventSummaryDao(database, courseRepository);
        resultDao = new ResultDao(database);
        volunteerDao = new VolunteerDao(database);
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
        System.out.printf("INFO Fixing results for course: %s, date: %s, event number: %d %n",
                course.name, newCourseEventSummary.date, eventNumber);
        System.out.printf("INFO Course Event Summary %s%n", newCourseEventSummary);

        List<Result> newResults = new ArrayList<>();
        List<Athlete> newAthletes = new ArrayList<>();
        List<Volunteer> newVolunteers = new ArrayList<>();
        {
            Parser parser = new Parser.Builder(courseRepository)
                    .webpageProvider(new WebpageProviderImpl(urlGenerator.generateCourseEventUrl(course.name, eventNumber)))
                    .forEachResult(newResults::add)
                    .forEachAthlete(newAthletes::add)
                    .forEachVolunteer(newVolunteers::add)
                    .build();
            parser.parse();
        }
        {
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
        {
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
}
