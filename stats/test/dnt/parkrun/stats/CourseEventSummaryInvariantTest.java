package dnt.parkrun.stats;

import dnt.parkrun.database.*;
import dnt.parkrun.datastructures.*;
import dnt.parkrun.stats.invariants.CourseEventSummaryChecker;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static dnt.parkrun.datastructures.AgeCategory.*;
import static org.assertj.core.api.Assertions.assertThat;

public class CourseEventSummaryInvariantTest extends BaseDaoTest
{
    private CourseEventSummaryDao courseEventSummaryDao;
    private final Date date = Date.from(Instant.EPOCH);
    private Course course;
    private Athlete girl;
    private Athlete boy;
    private ResultDao resultDao;

    @Before
    public void setUp() throws Exception
    {
        jdbc.update("delete from athlete", EmptySqlParameterSource.INSTANCE);
        jdbc.update("delete from course", EmptySqlParameterSource.INSTANCE);
        jdbc.update("delete from course_event_summary", EmptySqlParameterSource.INSTANCE);
        jdbc.update("delete from result", EmptySqlParameterSource.INSTANCE);

        CourseRepository courseRepository = new CourseRepository();
        CourseDao courseDao = new CourseDao(TEST_DATABASE, courseRepository);
        courseEventSummaryDao = new CourseEventSummaryDao(TEST_DATABASE, courseRepository);
        resultDao = new ResultDao(TEST_DATABASE);
        AthleteDao athleteDao = new AthleteDao(TEST_DATABASE);

        course = courseDao.insert(CORNWALL);
        girl = athleteDao.insert(Athlete.from("Camilia CROW", 10001));
        boy = athleteDao.insert(Athlete.from("Albert LYING", 10002));

        courseEventSummaryDao.insert(
                new CourseEventSummary(course, 5, date, 22, 2, Optional.of(boy), Optional.of(girl)));
        resultDao.insert(new Result(course.courseId, date, 1,
                1, girl, Time.from("22:22"), SM25_29, AgeGrade.newInstance(67.89)));
        resultDao.insert(new Result(course.courseId, date, 1,
                2, boy, Time.from("22:23"), SM30_34, AgeGrade.newInstance(70.89)));
    }

    @Test
    public void checkHappyPath()
    {
        CourseEventSummaryChecker checker = new Stub(TEST_DATABASE,
                List.of(
                        new Result(course.courseId, date, 1,  1, girl, Time.from("22:22"), SM25_29, AgeGrade.newInstance(67.89)),
                        new Result(course.courseId, date, 1, 2, boy, Time.from("22:23"), SM30_34, AgeGrade.newInstance(70.89))
                ));
        List<String> validate = checker.validate();
        assertThat(validate.size()).isEqualTo(0);
    }

    @Test
    public void checkResultSetsOfDifferentSize()
    {
        CourseEventSummaryChecker checker = new Stub(TEST_DATABASE,
                List.of(
                        new Result(course.courseId, date, 1, 1, girl, Time.from("22:22"), SM25_29, AgeGrade.newInstance(67.89))
                ));
        List<String> validate = checker.validate();
        assertThat(validate.size()).isEqualTo(1);
        assertThat(validate.getFirst()).contains("List sizes do not match");
    }

    @Test
    public void checkAgeGrade()
    {
        CourseEventSummaryChecker checker = new Stub(TEST_DATABASE,
                List.of(
                        new Result(course.courseId, date, 1, 1, girl, Time.from("22:22"), SM25_29, AgeGrade.newInstance(67.89)),
                        new Result(course.courseId, date, 1, 2, boy, Time.from("22:23"), SM30_34, AgeGrade.newInstance(67.89))
                ));
        List<String> validate = checker.validate();
        assertThat(validate.size()).isEqualTo(1);
        assertThat(validate.getFirst()).contains("Age grade does not match");
    }

    @Test
    public void checkAgeGroup()
    {
        CourseEventSummaryChecker checker = new Stub(TEST_DATABASE,
                List.of(
                        new Result(course.courseId, date, 1, 1, girl, Time.from("22:22"), SM25_29, AgeGrade.newInstance(67.89)),
                        new Result(course.courseId, date, 1, 2, boy, Time.from("22:23"), SM25_29, AgeGrade.newInstance(70.89))
                ));
        List<String> validate = checker.validate();
        assertThat(validate.size()).isEqualTo(1);
        assertThat(validate.getFirst()).contains("Age group does not match");
    }

    @Test
    public void checkAthlete()
    {
        CourseEventSummaryChecker checker = new Stub(TEST_DATABASE,
                List.of(
                        new Result(course.courseId, date, 1, 1, boy, Time.from("22:22"), SM25_29, AgeGrade.newInstance(67.89)),
                        new Result(course.courseId, date, 1, 2, boy, Time.from("22:23"), SM30_34, AgeGrade.newInstance(70.89))
                ));
        List<String> validate = checker.validate();
        assertThat(validate.size()).isEqualTo(1);
        assertThat(validate.getFirst()).contains("Athlete ID does not match");
    }

    @Test
    public void checkTimeDoesNotMatch()
    {
        CourseEventSummaryChecker checker = new Stub(TEST_DATABASE,
                List.of(
                        new Result(course.courseId, date, 1, 1, girl, Time.from("22:20"), SM25_29, AgeGrade.newInstance(67.89)),
                        new Result(course.courseId, date, 1, 2, boy, Time.from("22:23"), SM30_34, AgeGrade.newInstance(70.89))
                ));
        List<String> validate = checker.validate();
        assertThat(validate.size()).isEqualTo(1);
        assertThat(validate.getFirst()).contains("Time does not match");
    }

    @Test
    public void checkZeroTimesAreGood()
    {
        jdbc.update("delete from result", EmptySqlParameterSource.INSTANCE);
        resultDao.insert(new Result(course.courseId, date, 1,
                1, Athlete.NO_ATHLETE, null, UNKNOWN, AgeGrade.newInstanceNoAgeGrade()));

        CourseEventSummaryChecker checker = new Stub(TEST_DATABASE,
                List.of(
                        new Result(course.courseId, date, 1, 1, Athlete.NO_ATHLETE, null, UNKNOWN, AgeGrade.newInstanceNoAgeGrade())
                ));
        List<String> validate = checker.validate();
        assertThat(validate.size()).isEqualTo(0);
    }

    @Test
    public void checkZeroTimesAreBad()
    {
        jdbc.update("delete from result", EmptySqlParameterSource.INSTANCE);
        resultDao.insert(new Result(course.courseId, date, 1,
                1, Athlete.NO_ATHLETE, null, UNKNOWN, AgeGrade.newInstanceNoAgeGrade()));

        CourseEventSummaryChecker checker = new Stub(TEST_DATABASE,
                List.of(
                        new Result(course.courseId, date, 1, 1, Athlete.NO_ATHLETE, Time.NO_TIME, UNKNOWN, AgeGrade.newInstanceNoAgeGrade())
                ));
        List<String> validate = checker.validate();
        assertThat(validate.size()).isEqualTo(1);
        assertThat(validate.getFirst()).contains("Zero times are not correct. Web should be null. DAO should be zero.");
    }

    private static class Stub extends CourseEventSummaryChecker
    {
        private final List<Result> stubResults;

        public Stub(Database database, List<Result> stubResults)
        {
            super(database, 1L);
            this.stubResults = stubResults;
        }

        @Override
        protected List<Result> getResultsFromWeb(CourseEventSummary ces)
        {
            return stubResults;
        }
    }
}