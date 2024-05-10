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
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static dnt.parkrun.common.ParkrunDay.getParkrunDay;
import static dnt.parkrun.database.DataSourceUrlBuilder.getDataSourceUrl;
import static dnt.parkrun.datastructures.Country.NZ;

public class CourseEventSummaryChecker
{
    private final Random random; // SEED
    private final UrlGenerator urlGenerator = new UrlGenerator(NZ.baseUrl);
    private final CourseEventSummaryDao courseEventSummaryDao;
    private final ResultDao resultDao;

    private final List<String> errors = new ArrayList<>();
    private final CourseRepository courseRepository;
    private final List<Course> courses;
    private final VolunteerDao volunteerDao;
    private final AthleteDao athleteDao;

    public static void main(String[] args) throws SQLException
    {
        DataSource dataSource = new SimpleDriverDataSource(new Driver(),
                getDataSourceUrl("parkrun_stats"), "dao", "daoFractaldao");

        CourseEventSummaryChecker checker = new CourseEventSummaryChecker(
                dataSource, System.currentTimeMillis());
//        CourseEventSummaryChecker checker = new CourseEventSummaryChecker(
//                dataSource, 1715335259867L);
//
//        checker.rewriteCourseEvent("barrycurtis", 516);

        List<String> errors = checker.validate();
        errors.forEach(error -> System.out.println("ERROR: " + error));
    }

    public CourseEventSummaryChecker(DataSource dataSource, long seed)
    {
        System.out.printf("Random seed for %s: %d%n", this.getClass().getSimpleName(), seed);
        this.random = new Random(seed);

        courseRepository = new CourseRepository();
        new CourseDao(dataSource, courseRepository);
        courses = courseRepository.getCourses(NZ).stream().filter(course -> course.status == Course.Status.RUNNING).toList();
        resultDao = new ResultDao(dataSource);
        athleteDao = new AthleteDao(dataSource);
        volunteerDao = new VolunteerDao(dataSource);
        courseEventSummaryDao = new CourseEventSummaryDao(dataSource, courseRepository);
    }

    public List<String> validate()
    {
        List<CourseEventSummary> courseEventSummaries = courseEventSummaryDao.getCourseEventSummaries();

        checkResultsForRecentRun(courseEventSummaries);
        checkResultsForLast4Weeks(courseEventSummaries);
        checkResultsForLast8Weeks(courseEventSummaries);
        checkResultsForLast52Weeks(courseEventSummaries);
//        checkResults(courseEventSummaries);

        return errors;
    }

    private void checkResultsForRecentRun(List<CourseEventSummary> courseEventSummaries)
    {
        int courseIndex = random.nextInt(courses.size());
        Course course = courses.get(courseIndex);
        Date recentDate = getParkrunDay(new Date());

        List<CourseEventSummary> summariesForThisMonth = courseEventSummaries.stream()
                .filter(ces ->
                {
                    if(ces.course.courseId != course.courseId) return false;
                    return ces.date.compareTo(recentDate) == 0;
                })
                .toList();
        assert summariesForThisMonth.size() <= 1;
        if (summariesForThisMonth.isEmpty())
        {
            errors.add(String.format("ERROR: Recent run. No results for '%s' on %s%n", course.name, recentDate));
        }
        else
        {
            checkResults(summariesForThisMonth.getFirst());
        }
    }

    private void checkResultsForLast4Weeks(List<CourseEventSummary> courseEventSummaries)
    {
        int courseIndex = random.nextInt(courses.size());
        Course course = courses.get(courseIndex);

        int amountToSubtract = 7 * -(1 + random.nextInt(4));
        Date recentDate = getParkrunDay(new Date());
        Calendar calendarInThePast = Calendar.getInstance();
        calendarInThePast.setTime(recentDate);
        calendarInThePast.add(Calendar.DAY_OF_MONTH, amountToSubtract);

        Date dateInThePast = Date.from(calendarInThePast.toInstant());

        List<CourseEventSummary> summariesForThisMonth = courseEventSummaries.stream()
                .filter(ces ->
                {
                    if(ces.course.courseId != course.courseId) return false;
                    return ces.date.compareTo(dateInThePast) == 0;
                })
                .toList();
        assert summariesForThisMonth.size() <= 1;
        if (summariesForThisMonth.isEmpty())
        {
            errors.add(String.format("ERROR: Last 4 weeks. No results for '%s' on %s%n", course.name, dateInThePast));
        }
        else
        {
            checkResults(summariesForThisMonth.getFirst());
        }
    }

    private void checkResultsForLast8Weeks(List<CourseEventSummary> courseEventSummaries)
    {
        int courseIndex = random.nextInt(courses.size());
        Course course = courses.get(courseIndex);

        int amountToSubtract = 7 * -(1 + random.nextInt(8));
        Date recentDate = getParkrunDay(new Date());
        Calendar calendarInThePast = Calendar.getInstance();
        calendarInThePast.setTime(recentDate);
        calendarInThePast.add(Calendar.DAY_OF_MONTH, amountToSubtract);

        Date dateInThePast = Date.from(calendarInThePast.toInstant());

        List<CourseEventSummary> summariesForThisMonth = courseEventSummaries.stream()
                .filter(ces ->
                {
                    if(ces.course.courseId != course.courseId) return false;
                    return ces.date.compareTo(dateInThePast) == 0;
                })
                .toList();
        assert summariesForThisMonth.size() <= 1;
        if (summariesForThisMonth.isEmpty())
        {
            errors.add(String.format("ERROR: Last 8 weeks. No results for '%s' on %s%n", course.name, dateInThePast));
        }
        else
        {
            checkResults(summariesForThisMonth.getFirst());
        }
    }

    private void checkResultsForLast52Weeks(List<CourseEventSummary> courseEventSummaries)
    {
        int courseIndex = random.nextInt(courses.size());
        Course course = courses.get(courseIndex);

        int amountToSubtract = 7 * -(1 + random.nextInt(52));
        Date recentDate = getParkrunDay(new Date());
        Calendar calendarInThePast = Calendar.getInstance();
        calendarInThePast.setTime(recentDate);
        calendarInThePast.add(Calendar.DAY_OF_MONTH, amountToSubtract);

        Date dateInThePast = Date.from(calendarInThePast.toInstant());

        List<CourseEventSummary> summariesForThisMonth = courseEventSummaries.stream()
                .filter(ces ->
                {
                    if(ces.course.courseId != course.courseId) return false;
                    return ces.date.compareTo(dateInThePast) == 0;
                })
                .toList();
        assert summariesForThisMonth.size() <= 1;
        if (summariesForThisMonth.isEmpty())
        {
            errors.add(String.format("ERROR: Last 52 weeks. No results for '%s' on %s%n", course.name, dateInThePast));
        }
        else
        {
            checkResults(summariesForThisMonth.getFirst());
        }
    }

    private void checkResults(CourseEventSummary summary)
    {
        List<Result> resultsFromDao = resultDao.getResults(summary.course.courseId, summary.date);
        List<Result> resultsFromWeb = getResultsFromWeb(summary);

        areResultsOk(summary, resultsFromDao, resultsFromWeb);
    }



    protected List<Result> getResultsFromWeb(CourseEventSummary ces)
    {
        List<Result> results = new ArrayList<>();
        Parser parser = new Parser.Builder(ces.course)
                .webpageProvider(new WebpageProviderImpl(urlGenerator.generateCourseEventUrl(ces.course.name, ces.eventNumber)))
                .forEachResult(results::add)
                .build();
        parser.parse();
        return results;
    }

    private void areResultsOk(CourseEventSummary ces, List<Result> daoItems, List<Result> webItems)
    {
        if (daoItems.size() != webItems.size())
        {
            errors.add(String.format("List sizes do not match: A: %d, B: %d, CES: %s", daoItems.size(), webItems.size(), ces));
            return;
        }

        for (int i = 0; i < daoItems.size(); i++)
        {
            Result daoItem = daoItems.get(i);
            Result webItem = webItems.get(i);

            Supplier<String> comparison = () -> String.format("%n" +
                    "A: %s%n" +
                    "B: %s%n" +
                    "CES: %s%n%n", daoItem, webItem, ces);
            if (daoItem.athlete.athleteId != webItem.athlete.athleteId)
            {
                errors.add("Athlete ID does not match. " + comparison.get());
            }
            if (daoItem.time.getTotalSeconds() == 0 || webItem.time == null)
            {
                if (daoItem.time.getTotalSeconds() != 0 || webItem.time != null)
                {
                    errors.add("Zero times are not correct. Web should be null. DAO should be zero. " + comparison.get());
                }
            }
            else
            {
                if (daoItem.time.getTotalSeconds() != webItem.time.getTotalSeconds())
                {
                    errors.add("Time does not match. " + comparison.get());
                }
            }
            if (daoItem.ageCategory != webItem.ageCategory)
            {
                errors.add("Age group does not match. " + comparison.get());
            }
            if (daoItem.ageGrade.ageGrade != webItem.ageGrade.ageGrade)
            {
                errors.add("Age grade does not match. " + comparison.get());
            }
//            softly.assertThat(item1.ageGrade.ageGrade)
//                    .describedAs("Age grade too small. " + comparison.get() + item1)
//                    .matches(item -> item.equals(BigDecimal.ZERO) || item.doubleValue() < 2.0);
        }
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
