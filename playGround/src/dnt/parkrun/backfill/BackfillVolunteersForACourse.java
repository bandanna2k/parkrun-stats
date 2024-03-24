package dnt.parkrun.backfill;

import com.mysql.jdbc.Driver;
import dnt.parkrun.common.UrlGenerator;
import dnt.parkrun.courseevent.Parser;
import dnt.parkrun.database.AthleteDao;
import dnt.parkrun.database.CourseDao;
import dnt.parkrun.database.CourseEventSummaryDao;
import dnt.parkrun.database.VolunteerDao;
import dnt.parkrun.datastructures.Course;
import dnt.parkrun.datastructures.CourseEventSummary;
import dnt.parkrun.datastructures.CourseRepository;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import static dnt.parkrun.datastructures.Country.NZ;

@Deprecated
public class BackfillVolunteersForACourse
{
    private NamedParameterJdbcTemplate jdbc;

    public static void main(String[] args) throws IOException, SQLException
    {
        new BackfillVolunteersForACourse().backfill();
    }
    public void backfill() throws SQLException, IOException
    {
        Course backfillCourse = new Course(44, "porirua", NZ, "Porirua parkrun", Course.Status.STOPPED);

        DataSource dataSource = new SimpleDriverDataSource(new Driver(),
                "jdbc:mysql://localhost/parkrun_stats", "dao", "daoFractaldao");
        jdbc = new NamedParameterJdbcTemplate(dataSource);

        AthleteDao athleteDao = new AthleteDao(dataSource);
        VolunteerDao volunteerDao = new VolunteerDao(dataSource);

        CourseRepository courseRepository = new CourseRepository();
        new CourseDao(dataSource, courseRepository);

        List<CourseEventSummary> courseEventSummaries = new CourseEventSummaryDao(dataSource, courseRepository).getCourseEventSummaries()
                .stream().filter(ces -> ces.course.courseId == backfillCourse.courseId).collect(Collectors.toList());

        int counter = 1;
        int size = courseEventSummaries.size();
        for (CourseEventSummary ces : courseEventSummaries)
        {
            System.out.printf("Downloading %d of %d ", counter++, size);
            Parser parser = new Parser.Builder(ces.course)
                    .url(UrlGenerator.generateCourseEventUrl(NZ.baseUrl, backfillCourse.name, ces.eventNumber))
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
