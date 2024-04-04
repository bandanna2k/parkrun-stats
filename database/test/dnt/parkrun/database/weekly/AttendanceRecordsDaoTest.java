package dnt.parkrun.database.weekly;

import com.mysql.jdbc.Driver;
import dnt.parkrun.database.AthleteDao;
import dnt.parkrun.database.BaseDaoTest;
import dnt.parkrun.database.CourseDao;
import dnt.parkrun.database.CourseEventSummaryDao;
import dnt.parkrun.datastructures.*;
import dnt.parkrun.datastructures.stats.AttendanceRecord;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class AttendanceRecordsDaoTest extends BaseDaoTest
{
    private AttendanceRecordsDao attendanceRecordsDao;
    private AthleteDao athleteDao;
    private CourseDao courseDao;
    private CourseRepository courseRepository;
    private CourseEventSummaryDao courseEventSummaryDao;

    @Before
    public void setUp() throws Exception
    {
        DataSource dataSource = new SimpleDriverDataSource(new Driver(),
                "jdbc:mysql://localhost/parkrun_stats_test", "test", "qa");

        jdbc.update("delete from athlete", EmptySqlParameterSource.INSTANCE);
        jdbc.update("delete from course_event_summary", EmptySqlParameterSource.INSTANCE);
        jdbc.update("delete from course", EmptySqlParameterSource.INSTANCE);

        athleteDao = new AthleteDao(dataSource);
        courseRepository = new CourseRepository();
        courseDao = new CourseDao(dataSource, courseRepository);
        courseEventSummaryDao = new CourseEventSummaryDao(dataSource, courseRepository);
        attendanceRecordsDao = AttendanceRecordsDao.getInstance(dataSource, Date.from(Instant.EPOCH));
    }

    @Test
    public void shouldGetAttendanceRecords()
    {
        Athlete firstMan = Athlete.fromAthleteSummaryLink("Davey JONES", "https://www.parkrun.co.nz/parkrunner/902393/");
        Athlete firstWoman = Athlete.fromAthleteSummaryLink("Terry EVANS", "https://www.parkrun.co.nz/parkrunner/12345/");
        athleteDao.insert(firstWoman);
        athleteDao.insert(firstMan);

        Course course = courseDao.insert(new Course(9999, "cornwall", Country.NZ, null, Course.Status.RUNNING));

        courseEventSummaryDao.insert(new CourseEventSummary(
                course, 1, Date.from(Instant.EPOCH), 20, Optional.of(firstMan), Optional.of(firstWoman)));

        List<AttendanceRecord> attendanceRecords = attendanceRecordsDao.getAttendanceRecords(Date.from(Instant.EPOCH));
        Assertions.assertThat(attendanceRecords).isNotEmpty();
    }
}
