package dnt.parkrun.stats.invariants;

import com.mysql.jdbc.Driver;
import dnt.parkrun.athletecoursesummary.Parser;
import dnt.parkrun.common.UrlGenerator;
import dnt.parkrun.database.CourseDao;
import dnt.parkrun.datastructures.*;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static dnt.parkrun.datastructures.Country.NZ;

public class ParsersTest
{
    private static final Course CORNWALL = new Course(2, "cornwall", NZ, "Cornwall parkrun", Course.Status.RUNNING);

    @Test
    public void testCourseSummary() throws IOException
    {
        List<CourseEventSummary> listOfCourseEvents = new ArrayList<>();
        dnt.parkrun.courseeventsummary.Parser parser = new dnt.parkrun.courseeventsummary.Parser.Builder()
                .url(UrlGenerator.generateCourseEventSummaryUrl(NZ.baseUrl, CORNWALL.name))
                .forEachCourseEvent(listOfCourseEvents::add)
                .course(CORNWALL)
                .build();
        parser.parse();

        Assertions.assertThat(listOfCourseEvents.size()).isGreaterThan(500);
    }
    @Test
    public void testCourseEvent() throws IOException, SQLException
    {
        DataSource dataSource = new SimpleDriverDataSource(new Driver(),
                "jdbc:mysql://localhost/parkrun_stats", "dao", "daoFractaldao");
        CourseRepository courseRepository = new CourseRepository();
        new CourseDao(dataSource, courseRepository);

        List<Volunteer> listOfVolunteers = new ArrayList<>();
        List<Athlete> listOfAthletes = new ArrayList<>();
        dnt.parkrun.courseevent.Parser parser = new dnt.parkrun.courseevent.Parser.Builder(courseRepository.getCourse(CORNWALL.courseId))
                .url(UrlGenerator.generateCourseEventUrl(NZ.baseUrl, "cornwall", 1))
                .forEachAthlete(listOfAthletes::add)
                .forEachVolunteer(listOfVolunteers::add)
                .build();
        parser.parse();

        Assertions.assertThat(listOfVolunteers.size()).isGreaterThan(2);
        Assertions.assertThat(listOfAthletes.size()).isGreaterThan(20);
    }

    @Test
    public void testAthleteCourseSummary() throws SQLException
    {
        DataSource dataSource = new SimpleDriverDataSource(new Driver(),
                "jdbc:mysql://localhost/parkrun_stats", "dao", "daoFractaldao");
        CourseRepository courseRepository = new CourseRepository();
        new CourseDao(dataSource, courseRepository);

        List<AthleteCourseSummary> list = new ArrayList<>();
        Parser parser = new Parser.Builder()
                .url(UrlGenerator.generateAthleteEventSummaryUrl(NZ.baseUrl, 414811))
                .forEachAthleteCourseSummary(list::add)
                .build(courseRepository);
        parser.parse();

        Assertions.assertThat(list.size()).isGreaterThan(49);
    }
}
