package dnt.parkrun.stats.invariants.predownload.last;

import dnt.parkrun.common.UrlGenerator;
import dnt.parkrun.datastructures.Country;
import dnt.parkrun.datastructures.Course;
import dnt.parkrun.datastructures.CourseRepository;
import dnt.parkrun.webpageprovider.WebpageProviderImpl;
import org.assertj.core.api.SoftAssertions;
import org.junit.Test;

import java.util.Date;

import static dnt.parkrun.common.DateConverter.ONE_DAY_IN_MILLIS;

public class AreLastResultsInFromCanadaTest
{
    @Test
    public void areLastResultsIn()
    {
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(areResultsIn(new Course(12345, "shawniganhills", Country.CANADA, "shawniganhills", Course.Status.RUNNING)))
                .describedAs("shawniganhills results not in")
                .isEqualTo(true);
        softly.assertThat(areResultsIn(new Course(12346, "cloverpoint", Country.CANADA, "cloverpoint", Course.Status.RUNNING)))
                .describedAs("cloverpoint results not in")
                .isEqualTo(true);
        softly.assertThat(areResultsIn(new Course(12347, "ambleside", Country.CANADA, "ambleside", Course.Status.RUNNING)))
                .describedAs("ambleside results not in")
                .isEqualTo(true);
        softly.assertAll();
    }
    public boolean areResultsIn(Course course)
    {
        CourseRepository courseRepository = new CourseRepository();
        courseRepository.addCourse(course);

        UrlGenerator urlGenerator = new UrlGenerator(Country.CANADA.baseUrl);
        dnt.parkrun.courseevent.Parser parser = new dnt.parkrun.courseevent.Parser.Builder(courseRepository)
                .webpageProvider(new WebpageProviderImpl(urlGenerator.generateCourseLatestResultsUrl(course.name)))
                .build();
        parser.parse();

        Date dateFromMostWesterlyParkrun = parser.getDate();
        Date todayMinus5Days = new Date();
        todayMinus5Days.setTime(todayMinus5Days.getTime() - (5 * ONE_DAY_IN_MILLIS));
        return dateFromMostWesterlyParkrun.after(todayMinus5Days);
    }
}
