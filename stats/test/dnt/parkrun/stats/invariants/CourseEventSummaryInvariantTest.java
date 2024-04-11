package dnt.parkrun.stats.invariants;

import com.mysql.jdbc.Driver;
import dnt.parkrun.common.DateConverter;
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
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static dnt.parkrun.datastructures.Country.NZ;

public class CourseEventSummaryInvariantTest
{
    private static final int ITERATIONS = 1;

    private final UrlGenerator urlGenerator = new UrlGenerator(NZ.baseUrl);
    private final SoftAssertions softly = new SoftAssertions();

    @Test
    public void checkCourseEventSummary() throws SQLException
    {
        Random random = new Random(1);

        DataSource dataSource = new SimpleDriverDataSource(new Driver(),
                "jdbc:mysql://localhost/parkrun_stats", "dao", "daoFractaldao");
        CourseRepository courseRepository = new CourseRepository();

        CourseDao courseDao = new CourseDao(dataSource, courseRepository);
        ResultDao resultDao = new ResultDao(dataSource);
        CourseEventSummaryDao courseEventSummaryDao = new CourseEventSummaryDao(dataSource, courseRepository);

        List<CourseEventSummary> courseEventSummaries = courseEventSummaryDao.getCourseEventSummaries();
        Set<CourseEventSummary> done = new HashSet<>();

        Set<String> courseIdToEventNumberToFix = new HashSet<>() {{
//            add(38 + "-06/04/2024");
        }};

        // Get X from this month
        Calendar firstOfTheMonthCalendar = Calendar.getInstance();
        firstOfTheMonthCalendar.set(Calendar.DAY_OF_MONTH, 1);
        Date firstOfTheMonth = Date.from(firstOfTheMonthCalendar.toInstant());
        List<CourseEventSummary> summariesForThisMonth = courseEventSummaries.stream()
                .filter(ces -> ces.date.after(firstOfTheMonth))
                .collect(Collectors.toList());
        System.out.println(summariesForThisMonth.size() + " " + summariesForThisMonth);
        for (int i = 0; i < ITERATIONS; i++)
        {
            int randIndex = random.nextInt(summariesForThisMonth.size());
            CourseEventSummary ces = summariesForThisMonth.get(randIndex);

            List<Result> resultsFromDao = resultDao.getResults(ces.course.courseId, ces.date);
            List<Result> resultsFromWeb = getResultsFromWeb(ces);

            areResultsOk(ces, resultsFromDao, resultsFromWeb);

            String key = ces.course.courseId + "-" + DateConverter.formatDateForHtml(ces.date);
            if(courseIdToEventNumberToFix.contains(key))
            {
                System.out.printf("INFO Fixing results for course: %s, date: %s %n", ces.course.name, ces.date);
                resultDao.deleteResults(ces.course.courseId, ces.date);
                System.out.printf("WARNING Deleted results for course: %s, date: %s %n", ces.course.name, ces.date);
                resultsFromWeb.forEach(resultDao::insert);
                System.out.printf("INFO Results re-entered%n");
            }
        }

        softly.assertAll();
    }

    private void areResultsOk(CourseEventSummary ces, List<Result> list1, List<Result> list2)
    {
        if(list1.size() != list2.size())
        {
            softly.fail("List sizes do not match: A: %d, B: %d, CES: %s", list1.size(), list2.size(), ces);
            return;
        }

        for (int i = 0; i < list1.size(); i++)
        {
            Result item1 = list1.get(i);
            Result item2 = list2.get(i);

            Supplier<String> comparison = () -> String.format("A: %s, B: %s, CES: %s", item1, item2, ces);
            softly.assertThat(item1.athlete.athleteId)
                    .describedAs("Athlete ID does not match. " + comparison.get())
                    .isEqualTo(item2.athlete.athleteId);
            softly.assertThat(item1.time)
                    .describedAs("Time does not match. " + comparison.get())
                    .isEqualTo(item2.time);
            softly.assertThat(item1.ageGroup)
                    .describedAs("Age group does not match. " + comparison.get())
                    .isEqualTo(item2.ageGroup);
            softly.assertThat(item1.ageGrade)
                    .describedAs("Age grade does not match. " + comparison.get())
                    .isEqualTo(item2.ageGrade);
            softly.assertThat(item1.ageGrade.ageGrade)
                    .describedAs("Age grade too small. " + comparison.get() + item1)
                    .matches(item -> item.equals(BigDecimal.ZERO) || item.doubleValue() < 2.0);
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
}