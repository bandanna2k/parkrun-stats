package dnt.parkrun.backfill;

import com.mysql.jdbc.Driver;
import dnt.parkrun.common.DateConverter;
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
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static dnt.parkrun.datastructures.Country.NZ;

@Deprecated
public class BackfillResults
{
    private final UrlGenerator urlGenerator = new UrlGenerator(NZ.baseUrl);

    public static void main(String[] args) throws IOException, SQLException
    {
        new BackfillResults().backfill1();
    }

    private void backfill2() throws SQLException
    {
        DataSource dataSource = new SimpleDriverDataSource(new Driver(),
                "jdbc:mysql://localhost/parkrun_stats", "dao", "daoFractaldao");

        CourseRepository courseRepository = new CourseRepository();
        new CourseDao(dataSource, courseRepository);

        ResultDao resultDao = new ResultDao(dataSource);
        CourseEventSummaryDao courseEventSummaryDao = new CourseEventSummaryDao(dataSource, courseRepository);

        Set<String> courseIdAndDateSet = new HashSet<>();
        resultDao.tableScan(r ->
        {
            if(r.ageGrade == null && r.ageGroup == null)
            {
                courseIdAndDateSet.add(r.courseId + "-" + DateConverter.formatDateForHtml(r.date));
            }
        });

        assert !courseIdAndDateSet.contains("0-null"); // Assert fails here. Why does table scan give a result with 0 courseId

        Map<String, Integer> courseIdAndDateToEventNumber = new HashMap<>();
        courseEventSummaryDao.getCourseEventSummaries().forEach(ces -> {
            String courseIdAndDate = ces.course.courseId + "-" + DateConverter.formatDateForHtml(ces.date);
            courseIdAndDateToEventNumber.put(courseIdAndDate, ces.eventNumber);
        });

        assert courseIdAndDateToEventNumber.get("0-null") == null;

        courseIdAndDateSet.forEach(courseIdAndDate ->
        {
            assert courseIdAndDateToEventNumber.get(courseIdAndDate) != null : courseIdAndDate;
            int eventNumber = courseIdAndDateToEventNumber.get(courseIdAndDate);

            System.out.println(courseIdAndDate + "-" + eventNumber);

            int courseId = Integer.parseInt(courseIdAndDate.substring(0, courseIdAndDate.indexOf('-')));
            Course backfillCourse = courseRepository.getCourse(courseId);
            Parser parser = new Parser.Builder(backfillCourse)
                    .webpageProvider(new WebpageProviderImpl(
                            urlGenerator.generateCourseEventUrl(backfillCourse.name, eventNumber)))
                    .forEachResult(result -> updateResult(resultDao, result))
                    .build();
            parser.parse();
        });
    }

    public void backfill1() throws SQLException, IOException
    {
        DataSource dataSource = new SimpleDriverDataSource(new Driver(),
                "jdbc:mysql://localhost/parkrun_stats", "dao", "daoFractaldao");

        CourseRepository courseRepository = new CourseRepository();
        new CourseDao(dataSource, courseRepository);

        ResultDao resultDao = new ResultDao(dataSource);

        for (Course backfillCourse : courseRepository.getCourses(NZ))
        {
            List<CourseEventSummary> courseEventSummaries = new CourseEventSummaryDao(dataSource, courseRepository).getCourseEventSummaries()
                    .stream().filter(ces -> ces.course.courseId == backfillCourse.courseId).collect(Collectors.toList());

            if (backfillCourse.name.startsWith("a")) continue;
            if (backfillCourse.name.startsWith("b")) continue;
            if (backfillCourse.name.startsWith("c")) continue;
            if (backfillCourse.name.startsWith("d")) continue;
            if (backfillCourse.name.startsWith("e")) continue;
            if (backfillCourse.name.startsWith("f")) continue;
//            if (backfillCourse.name.startsWith("g")) continue;
//            if (backfillCourse.name.startsWith("h")) continue;
//            if (backfillCourse.name.startsWith("i")) continue;
//            if (backfillCourse.name.startsWith("j")) continue;
//            if (backfillCourse.name.startsWith("k")) continue;
//            if (backfillCourse.name.startsWith("l")) continue;
//            if (backfillCourse.name.startsWith("m")) continue;
//            if (backfillCourse.name.startsWith("n")) continue;
//            if (backfillCourse.name.startsWith("o")) continue;
//            if (backfillCourse.name.startsWith("p")) continue;
//            if (backfillCourse.name.startsWith("q")) continue;
//            if (backfillCourse.name.startsWith("r")) continue;
//            if (backfillCourse.name.startsWith("s")) continue;
//            if (backfillCourse.name.startsWith("t")) continue;
//            if (backfillCourse.name.startsWith("u")) continue;
//            if (backfillCourse.name.startsWith("v")) continue;
//            if (backfillCourse.name.startsWith("cambridge")) continue;
//            if (backfillCourse.name.startsWith("western")) continue;
//            if (backfillCourse.name.startsWith("whakatane")) continue;
//            if (backfillCourse.name.startsWith("whanganui")) continue;

            int counter = 1;
            int size = courseEventSummaries.size();

            for (CourseEventSummary ces : courseEventSummaries)
            {
                System.out.printf("Downloading %d of %d%n", counter++, size);

                if (backfillCourse.name.equals("dunedin") && ces.eventNumber < 118) continue;

                Parser parser = new Parser.Builder(ces.course)
                        .webpageProvider(new WebpageProviderImpl(urlGenerator.generateCourseEventUrl(backfillCourse.name, ces.eventNumber)))
                        .forEachResult(r -> updateResult(resultDao, r))
                        .build();
                parser.parse();
            }
        }
    }

    private void updateResult(ResultDao resultDao, Result r)
    {
        resultDao.backfillUpdateResultWithAgeGroup(r);
    }
}
