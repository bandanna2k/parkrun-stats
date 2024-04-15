package dnt.parkrun.database.weekly;

import com.mysql.jdbc.Driver;
import dnt.parkrun.database.*;
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

import static dnt.parkrun.datastructures.AgeGrade.newInstance;
import static dnt.parkrun.datastructures.AgeGroup.SM20_24;
import static dnt.parkrun.datastructures.AgeGroup.SM25_29;

public class AttendanceRecordsDaoTest extends BaseDaoTest
{
    private AthleteDao athleteDao;
    private CourseDao courseDao;
    private CourseRepository courseRepository;
    private CourseEventSummaryDao courseEventSummaryDao;
    private ResultDao resultDao;
    private Date date = Date.from(Instant.EPOCH);

    @Before
    public void setUp() throws Exception
    {
        DataSource dataSource = new SimpleDriverDataSource(new Driver(),
                "jdbc:mysql://localhost/parkrun_stats_test", "test", "qa");

        jdbc.update("delete from athlete", EmptySqlParameterSource.INSTANCE);
        jdbc.update("delete from result", EmptySqlParameterSource.INSTANCE);
        jdbc.update("delete from course_event_summary", EmptySqlParameterSource.INSTANCE);
        jdbc.update("delete from course", EmptySqlParameterSource.INSTANCE);
        jdbc.update("drop table if exists " + AttendanceRecordsDao.tableName(date), EmptySqlParameterSource.INSTANCE);

        athleteDao = new AthleteDao(dataSource);
        courseRepository = new CourseRepository();
        courseDao = new CourseDao(dataSource, courseRepository);
        resultDao = new ResultDao(dataSource);
        courseEventSummaryDao = new CourseEventSummaryDao(dataSource, courseRepository);
    }

    @Test
    public void shouldGetAttendanceRecords()
    {
        Course course = courseDao.insert(CORNWALL);

        Athlete firstMan = Athlete.fromAthleteSummaryLink("Davey JONES", "https://www.parkrun.co.nz/parkrunner/902393/");
        Athlete firstWoman = Athlete.fromAthleteSummaryLink("Terry EVANS", "https://www.parkrun.co.nz/parkrunner/12345/");
        athleteDao.insert(firstWoman);
        athleteDao.insert(firstMan);
        resultDao.insert(new Result(course.courseId, date, 1, firstMan, Time.from("21:00"), SM25_29, newInstance("50.01%")));
        resultDao.insert(new Result(course.courseId, date, 2, firstWoman, Time.from("21:01"), SM20_24, newInstance("51.01%")));

        courseEventSummaryDao.insert(new CourseEventSummary(
                course, 1, Date.from(Instant.EPOCH), 2, Optional.of(firstMan), Optional.of(firstWoman)));

        AttendanceRecordsDao attendanceRecordsDao = AttendanceRecordsDao.getInstance(dataSource, date);
        List<AttendanceRecord> attendanceRecords = attendanceRecordsDao.getAttendanceRecords(Date.from(Instant.EPOCH));
        Assertions.assertThat(attendanceRecords).isNotEmpty();
    }
}
