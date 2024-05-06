package dnt.parkrun.stats.invariants;

import com.mysql.jdbc.Driver;
import dnt.parkrun.common.UrlGenerator;
import dnt.parkrun.courseevent.Parser;
import dnt.parkrun.database.CourseDao;
import dnt.parkrun.database.CourseEventSummaryDao;
import dnt.parkrun.database.ResultDao;
import dnt.parkrun.datastructures.Course;
import dnt.parkrun.datastructures.CourseEventSummary;
import dnt.parkrun.datastructures.CourseRepository;
import dnt.parkrun.datastructures.Result;
import dnt.parkrun.webpageprovider.WebpageProviderImpl;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static dnt.parkrun.common.ParkrunDay.getParkrunDay;
import static dnt.parkrun.database.DataSourceUrlBuilder.getDataSourceUrl;
import static dnt.parkrun.datastructures.Country.NZ;

public class CourseEventSummaryChecker
{
    public static final int DEFAULT_ITERATION_COUNT = 1;

    private final Random random; // SEED
    private final UrlGenerator urlGenerator = new UrlGenerator(NZ.baseUrl);
    private final CourseEventSummaryDao courseEventSummaryDao;
    private final ResultDao resultDao;

    private final Set<String> courseIdToEventNumberToFix = new HashSet<>()
    {{
//            add("owairaka158");
//            add("westernsprings346");
    }};
    private final List<String> errors = new ArrayList<>();
    private final int iterations;
    private final CourseRepository courseRepository;
    private final List<Course> courses;

    public static void main(String[] args) throws SQLException
    {
        DataSource dataSource = new SimpleDriverDataSource(new Driver(),
                getDataSourceUrl("parkrun_stats"), "dao", "daoFractaldao");

//        CourseEventSummaryChecker checker = new CourseEventSummaryChecker(
//                dataSource, DEFAULT_ITERATION_COUNT, System.currentTimeMillis());
        CourseEventSummaryChecker checker = new CourseEventSummaryChecker(
                dataSource, DEFAULT_ITERATION_COUNT, 1714999522463L);

        List<String> errors = checker.validate();
        errors.forEach(error -> System.out.println("ERROR: " + error));
    }

    public CourseEventSummaryChecker(DataSource dataSource)
    {
        this(dataSource, 4, System.currentTimeMillis());
    }

    public CourseEventSummaryChecker(DataSource dataSource, int iterations, long seed)
    {
        System.out.printf("Random seed for %s: %d%n", this.getClass().getSimpleName(), seed);
        this.random = new Random(seed);

        this.iterations = iterations;

        courseRepository = new CourseRepository();
        new CourseDao(dataSource, courseRepository);
        courses = courseRepository.getCourses(NZ).stream().filter(course -> course.status == Course.Status.RUNNING).toList();
        resultDao = new ResultDao(dataSource);
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
                .collect(Collectors.toList());
        assert summariesForThisMonth.size() <= 1;
        if (summariesForThisMonth.isEmpty()) errors.add(String.format("ERROR: Recent run. No results for '%s' on %s%n", course.name, recentDate));
        checkResults(summariesForThisMonth);
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
                .collect(Collectors.toList());
        assert summariesForThisMonth.size() <= 1;
        if (summariesForThisMonth.isEmpty()) errors.add(String.format("ERROR: Last 4 weeks. No results for '%s' on %s%n", course.name, dateInThePast));
        checkResults(summariesForThisMonth);
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
                .collect(Collectors.toList());
        assert summariesForThisMonth.size() <= 1;
        if (summariesForThisMonth.isEmpty()) errors.add(String.format("ERROR: Last 8 weeks. No results for '%s' on %s%n", course.name, dateInThePast));
        checkResults(summariesForThisMonth);
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

        List<CourseEventSummary> summariesForThisMonth2 = courseEventSummaries.stream()
                .filter(ces ->
                {
                    if(ces.course.courseId != course.courseId) return false;
                    return true;
                })
                .collect(Collectors.toList());
        List<CourseEventSummary> summariesForThisMonth = courseEventSummaries.stream()
                .filter(ces ->
                {
                    if(ces.course.courseId != course.courseId) return false;
                    return ces.date.compareTo(dateInThePast) == 0;
                })
                .collect(Collectors.toList());
        assert summariesForThisMonth.size() <= 1;
        if (summariesForThisMonth.isEmpty()) errors.add(String.format("ERROR: Last 52 weeks. No results for '%s' on %s%n", course.name, dateInThePast));
        checkResults(summariesForThisMonth);
    }


    private void checkResults(List<CourseEventSummary> summaries)
    {
        if(summaries.isEmpty()) return;

        for (int i = 0; i < iterations; i++)
        {
            int randIndex = random.nextInt(summaries.size());
            CourseEventSummary ces = summaries.get(randIndex);

            List<Result> resultsFromDao = resultDao.getResults(ces.course.courseId, ces.date);
            List<Result> resultsFromWeb = getResultsFromWeb(ces);

            areResultsOk(ces, resultsFromDao, resultsFromWeb);

            String key = ces.course.name + ces.eventNumber;
            if (courseIdToEventNumberToFix.contains(key))
            {
                System.out.printf("INFO Fixing results for course: %s, date: %s %n", ces.course.name, ces.date);
                resultDao.delete(ces.course.courseId, ces.date);
                System.out.printf("WARNING Deleted results for course: %s, date: %s %n", ces.course.name, ces.date);
                resultsFromWeb.forEach(resultDao::insert);
                System.out.printf("INFO Results re-entered%n");
            }
        }
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
}
