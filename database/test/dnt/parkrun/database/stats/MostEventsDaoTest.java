package dnt.parkrun.database.stats;

import com.mysql.jdbc.Driver;
import dnt.parkrun.database.*;
import dnt.parkrun.database.weekly.AttendanceRecordsDao;
import dnt.parkrun.datastructures.*;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static dnt.parkrun.datastructures.AgeCategory.SM25_29;
import static dnt.parkrun.datastructures.AgeCategory.SM30_34;
import static dnt.parkrun.datastructures.Country.NZ;
import static org.assertj.core.api.Assertions.assertThat;

public class MostEventsDaoTest extends BaseDaoTest
{
    private AttendanceRecordsDao dao;

    private final Date epoch = Date.from(Instant.EPOCH);
    private AthleteDao athleteDao;
    private CourseDao courseDao;
    private ResultDao resultDao;
    private CourseEventSummaryDao courseEventSummaryDao;

    @Before
    public void setUp() throws Exception
    {
        DataSource dataSource = new SimpleDriverDataSource(new Driver(),
                "jdbc:mysql://localhost/parkrun_stats_test", "test", "qa");

        jdbc.update("delete from athlete", EmptySqlParameterSource.INSTANCE);
        jdbc.update("delete from result", EmptySqlParameterSource.INSTANCE);
        jdbc.update("delete from course_event_summary", EmptySqlParameterSource.INSTANCE);
        jdbc.update("delete from course", EmptySqlParameterSource.INSTANCE);
        // TODO jdbc.update("drop table if exists " + MostEventsDao.getTableName(epoch), EmptySqlParameterSource.INSTANCE);

        CourseRepository courseRepository = new CourseRepository();
        courseDao = new CourseDao(country, dataSource, courseRepository);
        athleteDao = new AthleteDao(country, dataSource);
        resultDao = new ResultDao(country, dataSource);
        courseEventSummaryDao = new CourseEventSummaryDao(country, dataSource, courseRepository);
    }

    @Test
    public void shouldCreateAndIgnoreNextCreation()
    {
        Athlete girl = athleteDao.insert(Athlete.from("Camilia CROW", 10001));
        Athlete boy = athleteDao.insert(Athlete.from("Albert LYING", 10002));

        List<Course> courses = new ArrayList<>();
        for (int i = 0; i < 20; i++)
        {
            String name = "Course" + i;
            Course course = courseDao.insert(new Course(0, name, NZ, name, Course.Status.RUNNING));
            courses.add(course);
        }

        for (int i = 0; i < courses.size(); i++)
        {
            Course course = courses.get(i);
            Instant instant = Instant.EPOCH.plus(7L * i, ChronoUnit.DAYS);
            Date date = Date.from(instant);

            courseEventSummaryDao.insert(
                    new CourseEventSummary(course, 5, date, 2, Optional.of(boy), Optional.of(girl)));
            resultDao.insert(new Result(course.courseId, date,
                    1, girl, Time.from("22:22"), SM25_29, AgeGrade.newInstance(67.89 + i)));
            resultDao.insert(new Result(course.courseId, date,
                    2, boy, Time.from("22:23"), SM30_34, AgeGrade.newInstance(70.89 + i)));
        }

        MostEventsDao mostEventsDao = MostEventsDao.getOrCreate(country, dataSource, epoch);
        mostEventsDao.populateMostEventsTable();

        List<MostEventsDao.MostEventsRecord> mostEvents = mostEventsDao.getMostEvents();
        System.out.println(mostEvents);
        assertThat(mostEvents).isNotEmpty();
    }
}
