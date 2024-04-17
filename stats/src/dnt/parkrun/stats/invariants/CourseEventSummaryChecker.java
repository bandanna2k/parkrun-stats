package dnt.parkrun.stats.invariants;

import dnt.parkrun.common.UrlGenerator;
import dnt.parkrun.courseevent.Parser;
import dnt.parkrun.database.CourseDao;
import dnt.parkrun.database.CourseEventSummaryDao;
import dnt.parkrun.database.ResultDao;
import dnt.parkrun.datastructures.CourseEventSummary;
import dnt.parkrun.datastructures.CourseRepository;
import dnt.parkrun.datastructures.Result;
import dnt.parkrun.webpageprovider.WebpageProviderImpl;

import javax.sql.DataSource;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static dnt.parkrun.datastructures.Country.NZ;

public class CourseEventSummaryChecker
{
    private final Random random; // SEED
    private final UrlGenerator urlGenerator = new UrlGenerator(NZ.baseUrl);
    private final CourseEventSummaryDao courseEventSummaryDao;
    private final ResultDao resultDao;

    private final Set<String> courseIdToEventNumberToFix = new HashSet<>()
    {{
//            add("whanganuiriverbank167");
//            add("westernsprings346");
    }};
    private final List<String> errors = new ArrayList<>();
    private final int iterations;

    public CourseEventSummaryChecker(DataSource dataSource)
    {
        this(dataSource, 4);
    }

    public CourseEventSummaryChecker(DataSource dataSource, int iterations)
    {
        long time = System.currentTimeMillis();
        System.out.printf("Random seed for %s: %d%n", this.getClass().getSimpleName(), time);
        this.random = new Random(time);

        this.iterations = iterations;

        CourseRepository courseRepository = new CourseRepository();
        new CourseDao(dataSource, courseRepository);
        resultDao = new ResultDao(dataSource);
        courseEventSummaryDao = new CourseEventSummaryDao(dataSource, courseRepository);
    }

    public List<String> validate()
    {
        List<CourseEventSummary> courseEventSummaries = courseEventSummaryDao.getCourseEventSummaries();

        checkResultsFromThisMonth(courseEventSummaries);
        checkResultsFromLast60Days(courseEventSummaries);
        checkResultsFromLast350Days(courseEventSummaries);
        checkResults(courseEventSummaries);

        return errors;
    }

    private void checkResultsFromThisMonth(List<CourseEventSummary> courseEventSummaries)
    {
        Calendar firstOfTheMonthCalendar = Calendar.getInstance();
        firstOfTheMonthCalendar.set(Calendar.DAY_OF_MONTH, 1);
        Date firstOfTheMonth = Date.from(firstOfTheMonthCalendar.toInstant());
        List<CourseEventSummary> summariesForThisMonth = courseEventSummaries.stream()
                .filter(ces -> ces.date.after(firstOfTheMonth))
                .collect(Collectors.toList());
        checkResults(summariesForThisMonth);
    }

    private void checkResultsFromLast60Days(List<CourseEventSummary> courseEventSummaries)
    {
        Instant now = Instant.now();
        Instant pastTime = now.minus(60, ChronoUnit.DAYS);
        List<CourseEventSummary> summariesForThisMonth = courseEventSummaries.stream()
                .filter(ces -> ces.date.after(Date.from(pastTime)))
                .collect(Collectors.toList());
        checkResults(summariesForThisMonth);
    }

    private void checkResultsFromLast350Days(List<CourseEventSummary> courseEventSummaries)
    {
        Instant now = Instant.now();
        Instant pastTime = now.minus(350, ChronoUnit.DAYS);
        List<CourseEventSummary> summariesForThisMonth = courseEventSummaries.stream()
                .filter(ces -> ces.date.after(Date.from(pastTime)))
                .collect(Collectors.toList());
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
                resultDao.deleteResults(ces.course.courseId, ces.date);
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
