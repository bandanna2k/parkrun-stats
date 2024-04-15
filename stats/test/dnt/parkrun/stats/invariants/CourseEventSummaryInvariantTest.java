package dnt.parkrun.stats.invariants;

import com.mysql.jdbc.Driver;
import dnt.parkrun.common.UrlGenerator;
import dnt.parkrun.courseevent.Parser;
import dnt.parkrun.database.CourseDao;
import dnt.parkrun.database.CourseEventSummaryDao;
import dnt.parkrun.database.ResultDao;
import dnt.parkrun.datastructures.CourseEventSummary;
import dnt.parkrun.datastructures.CourseRepository;
import dnt.parkrun.datastructures.Result;
import dnt.parkrun.webpageprovider.WebpageProviderImpl;
import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static dnt.parkrun.datastructures.Country.NZ;

public class CourseEventSummaryInvariantTest
{
    private final SoftAssertions softly = new SoftAssertions();

    @Test
    public void checkCourseEventSummary() throws SQLException
    {
        DataSource dataSource = new SimpleDriverDataSource(new Driver(),
                "jdbc:mysql://localhost/parkrun_stats", "dao", "daoFractaldao");
        CourseEventSummaryChecker courseEventSummaryChecker = new CourseEventSummaryChecker(dataSource, softly);
        courseEventSummaryChecker.assertAll();
        softly.assertAll();
    }

    private static class CourseEventSummaryChecker
    {
        private final Random random = new Random(); // SEED
        private final UrlGenerator urlGenerator = new UrlGenerator(NZ.baseUrl);
        private final CourseEventSummaryDao courseEventSummaryDao;
        private final ResultDao resultDao;

        private final Set<String> courseIdToEventNumberToFix = new HashSet<>() {{
//            add("whanganuiriverbank167");
//            add("westernsprings346");
        }};
        private final SoftAssertions softly;
        private final int iterations;

        public CourseEventSummaryChecker(DataSource dataSource, SoftAssertions softly)
        {
            this(dataSource, softly, 4);
        }
        public CourseEventSummaryChecker(DataSource dataSource, SoftAssertions softly, int iterations)
        {
            this.softly = softly;
            this.iterations = iterations;

            CourseRepository courseRepository = new CourseRepository();
            new CourseDao(dataSource, courseRepository);
            resultDao = new ResultDao(dataSource);
            courseEventSummaryDao = new CourseEventSummaryDao(dataSource, courseRepository);
        }

        public void assertAll()
        {
            List<CourseEventSummary> courseEventSummaries = courseEventSummaryDao.getCourseEventSummaries();

            checkResultsFromThisMonth(courseEventSummaries);
            checkResultsFromLast60Days(courseEventSummaries);
            checkResultsFromLast350Days(courseEventSummaries);
            checkResults(courseEventSummaries);
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
            for (int i = 0; i < iterations; i++)
            {
                int randIndex = random.nextInt(summaries.size());
                CourseEventSummary ces = summaries.get(randIndex);

                List<Result> resultsFromDao = resultDao.getResults(ces.course.courseId, ces.date);
                List<Result> resultsFromWeb = getResultsFromWeb(ces);

                areResultsOk(ces, resultsFromDao, resultsFromWeb);

                String key = ces.course.name + ces.eventNumber;
                if(courseIdToEventNumberToFix.contains(key))
                {
                    System.out.printf("INFO Fixing results for course: %s, date: %s %n", ces.course.name, ces.date);
                    resultDao.deleteResults(ces.course.courseId, ces.date);
                    System.out.printf("WARNING Deleted results for course: %s, date: %s %n", ces.course.name, ces.date);
                    resultsFromWeb.forEach(resultDao::insert);
                    System.out.printf("INFO Results re-entered%n");
                }
            }
        }

        private List<Result> getResultsFromWeb(CourseEventSummary ces)
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
                softly.fail("List sizes do not match: A: %d, B: %d, CES: %s", daoItems.size(), webItems.size(), ces);
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
                softly.assertThat(daoItem.athlete.athleteId)
                        .describedAs("Athlete ID does not match. " + comparison.get())
                        .isEqualTo(webItem.athlete.athleteId);
                if(daoItem.time.getTotalSeconds() == 0 || webItem.time == null)
                {
                    softly.assertThat(daoItem.time.getTotalSeconds() == 0 && webItem.time == null)
                            .describedAs("Zero time does not match. " + comparison.get())
                            .isTrue();
                }
                else
                {
                    softly.assertThat(daoItem.time)
                            .describedAs("Time does not match. " + comparison.get())
                            .isEqualTo(webItem.time);
                }
                softly.assertThat(daoItem.ageGroup)
                        .describedAs("Age group does not match. " + comparison.get())
                        .isEqualTo(webItem.ageGroup);
                softly.assertThat(daoItem.ageGrade)
                        .describedAs("Age grade does not match. " + comparison.get())
                        .isEqualTo(webItem.ageGrade);
//            softly.assertThat(item1.ageGrade.ageGrade)
//                    .describedAs("Age grade too small. " + comparison.get() + item1)
//                    .matches(item -> item.equals(BigDecimal.ZERO) || item.doubleValue() < 2.0);
            }
        }
    }
}