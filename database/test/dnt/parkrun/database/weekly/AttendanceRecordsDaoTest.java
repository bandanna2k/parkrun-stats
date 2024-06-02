package dnt.parkrun.database.weekly;

import dnt.parkrun.database.*;
import dnt.parkrun.datastructures.*;
import dnt.parkrun.datastructures.stats.AttendanceRecord;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static dnt.parkrun.datastructures.AgeCategory.SM20_24;
import static dnt.parkrun.datastructures.AgeCategory.SM25_29;
import static dnt.parkrun.datastructures.AgeGrade.newInstance;
import static java.time.temporal.ChronoUnit.DAYS;

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
        jdbc.update("delete from athlete", EmptySqlParameterSource.INSTANCE);
        jdbc.update("delete from result", EmptySqlParameterSource.INSTANCE);
        jdbc.update("delete from course_event_summary", EmptySqlParameterSource.INSTANCE);
        jdbc.update("delete from course", EmptySqlParameterSource.INSTANCE);
        // TODO jdbc.update("drop table if exists " + AttendanceRecordsDao.tableName(date), EmptySqlParameterSource.INSTANCE);

        athleteDao = new AthleteDao(TEST_DATABASE);
        courseRepository = new CourseRepository();
        courseDao = new CourseDao(TEST_DATABASE, courseRepository);
        resultDao = new ResultDao(TEST_DATABASE);
        courseEventSummaryDao = new CourseEventSummaryDao(TEST_DATABASE, courseRepository);
    }

    @Test
    public void shouldGetAttendanceRecords()
    {
        Course course = courseDao.insert(CORNWALL);

        Athlete firstMan = Athlete.fromAthleteSummaryLink("Davey JONES", "https://www.parkrun.co.nz/parkrunner/902393/");
        Athlete firstWoman = Athlete.fromAthleteSummaryLink("Terry EVANS", "https://www.parkrun.co.nz/parkrunner/12345/");
        athleteDao.insert(firstWoman);
        athleteDao.insert(firstMan);
        resultDao.insert(new Result(course.courseId, date, 1, 1, firstMan, Time.from("21:00"), SM25_29, newInstance("50.01%")));
        resultDao.insert(new Result(course.courseId, date, 1, 2, firstWoman, Time.from("21:01"), SM20_24, newInstance("51.01%")));

        courseEventSummaryDao.insert(new CourseEventSummary(
                course, 1, Date.from(Instant.EPOCH), 2, Optional.of(firstMan), Optional.of(firstWoman)));

        AttendanceRecordsDao attendanceRecordsDao = AttendanceRecordsDao.getInstance(TEST_DATABASE, date);
        List<AttendanceRecord> attendanceRecords = attendanceRecordsDao.getAttendanceRecords(Date.from(Instant.EPOCH));
        Assertions.assertThat(attendanceRecords).isNotEmpty();
    }

    @Test
    public void shouldGetAttendanceRecordsForMultipleDates()
    {
        Date run1 = Date.from(Instant.EPOCH);
        Date run2 = Date.from(Instant.EPOCH.plus(7, DAYS));

        Course course = courseDao.insert(CORNWALL);
        Athlete firstMan = Athlete.fromAthleteSummaryLink("Davey JONES", "https://www.parkrun.co.nz/parkrunner/902393/");
        Athlete firstWoman = Athlete.fromAthleteSummaryLink("Terry EVANS", "https://www.parkrun.co.nz/parkrunner/12345/");
        athleteDao.insert(firstWoman);
        athleteDao.insert(firstMan);
        {
            resultDao.insert(new Result(course.courseId, run1, 1, 1, firstMan, Time.from("21:00"), SM25_29, newInstance("50.01%")));
            resultDao.insert(new Result(course.courseId, run1, 1, 2, firstWoman, Time.from("21:01"), SM20_24, newInstance("51.01%")));
        }
        {
            resultDao.insert(new Result(course.courseId, run2, 1, 1, firstMan, Time.from("21:00"), SM25_29, newInstance("50.01%")));
            resultDao.insert(new Result(course.courseId, run2, 1, 2, firstWoman, Time.from("21:01"), SM20_24, newInstance("51.01%")));
        }
        courseEventSummaryDao.insert(new CourseEventSummary(
                course, 1, run1, 2, Optional.of(firstMan), Optional.of(firstWoman)));
        courseEventSummaryDao.insert(new CourseEventSummary(
                course, 1, run2, 2, Optional.of(firstMan), Optional.of(firstWoman)));

        AttendanceRecordsDao attendanceRecordsDao = AttendanceRecordsDao.getInstance(TEST_DATABASE, date);
        List<AttendanceRecord> attendanceRecords = attendanceRecordsDao.getAttendanceRecords(Date.from(Instant.EPOCH));
        Assertions.assertThat(attendanceRecords.size()).isEqualTo(2);
        attendanceRecords.forEach(System.out::println);
    }
}
