package dnt.parkrun.courseevent;

import dnt.parkrun.datastructures.Course;
import dnt.parkrun.datastructures.CourseRepository;
import dnt.parkrun.datastructures.Result;
import dnt.parkrun.filewebpageprovider.FileWebpageProvider;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static dnt.parkrun.datastructures.Country.*;
import static dnt.parkrun.datastructures.Course.Status.RUNNING;

@RunWith(Parameterized.class)
public class ParserTest
{
    private Course cornwall = new Course(27, "Cornwall", NZ, "Cornwall", RUNNING);

    @Parameterized.Parameter(0)
    public String resource;

    @Parameterized.Parameters(name = "{0}")
    public static Object[] data()
    {
        return new Object[] {
                ("/example.event.with.unknowns.html"),
                ("/example.event.with.largest.run,count.ever.html"),
                ("/example.event.with.hour.plus.times.html"),
                ("/example.event.with.no.gender.html"),
                ("/example.event.with.assist.athlete.html"),
                ("/example.event.with.triple.dash.athlete.html"),
                ("/example.event.with.santa.claus.html"),
                ("/example.event.with.weird.results.html"),
                ("/example.event.with.blank.details.html"),
        };
    }

    @Test
    public void testExample()
    {
        parseResource(this.getClass().getResource(resource));
    }

    private void parseResource(URL resource)
    {
        CourseRepository courseRepository = new CourseRepository();
        courseRepository.addCourse(new Course(1, "colermountainbikepreserve", USA, "", RUNNING));
        courseRepository.addCourse(new Course(2, "bushy", UK, "", RUNNING));
        courseRepository.addCourse(new Course(3, "sunriseonsea", SOUTH_AFRICA, "", RUNNING));
        courseRepository.addCourse(new Course(4, "cornwall", NZ, "", RUNNING));
        courseRepository.addCourse(new Course(5, "blenheim", NZ, "", RUNNING));
        courseRepository.addCourse(new Course(6, "dunedin", NZ, "", RUNNING));
        courseRepository.addCourse(new Course(7, "hamiltonpark", NZ, "", RUNNING));
        courseRepository.addCourse(new Course(8, "barrycurtis", NZ, "", RUNNING));
        courseRepository.addCourse(new Course(9, "hamiltonlake", NZ, "", RUNNING));

        List<Result> results = new ArrayList<>();
        Parser parser = new Parser.Builder(courseRepository)
                .forEachAthlete(x -> System.out.println("Athlete: " + x))
                .forEachResult(results::add)
                .forEachVolunteer(x -> System.out.println("Volunteer: " + x))
                .webpageProvider(new FileWebpageProvider(new File(resource.getFile())))
                .build();
        parser.parse();

        results.forEach(r -> {
            Assertions.assertThat(r.ageGrade).isNotNull();
            Assertions.assertThat(r.ageCategory).isNotNull();
        });
    }
}
