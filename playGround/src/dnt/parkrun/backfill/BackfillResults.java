package dnt.parkrun.backfill;

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
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import static dnt.parkrun.datastructures.Country.NZ;

@Deprecated
public class BackfillResults
{
    private final UrlGenerator urlGenerator = new UrlGenerator(NZ.baseUrl);

    public static void main(String[] args) throws IOException, SQLException
    {
        new BackfillResults().backfill();
    }
    public void backfill() throws SQLException, IOException
    {
        DataSource dataSource = new SimpleDriverDataSource(new Driver(),
                "jdbc:mysql://localhost/parkrun_stats", "dao", "daoFractaldao");

        CourseRepository courseRepository = new CourseRepository();
        new CourseDao(dataSource, courseRepository);

        ResultDao resultDao = new ResultDao(dataSource);

        courseRepository.getCourses(NZ).forEach(backfillCourse ->
        {
            List<CourseEventSummary> courseEventSummaries = new CourseEventSummaryDao(dataSource, courseRepository).getCourseEventSummaries()
                    .stream().filter(ces -> ces.course.courseId == backfillCourse.courseId).collect(Collectors.toList());

            int counter = 1;
            int size = courseEventSummaries.size();
            for (CourseEventSummary ces : courseEventSummaries)
            {
                System.out.printf("Downloading %d of %d ", counter++, size);

                if(backfillCourse.name.equals("anderson") && ces.eventNumber < 347) continue;

                Parser parser = new Parser.Builder(ces.course)
                        .webpageProvider(new WebpageProviderImpl(urlGenerator.generateCourseEventUrl(backfillCourse.name, ces.eventNumber)))
                        .forEachResult(r -> updateResult(resultDao, r))
                        .build();
                parser.parse();
            }
        });
    }

    private void updateResult(ResultDao resultDao, Result r)
    {
        resultDao.backfillUpdateResultWithAgeGroup(r.athlete, r.courseId, r.date, r.ageGroup, r.ageGrade);
    }
}
