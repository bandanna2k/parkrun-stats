package dnt.parkrun.courseevent;

import dnt.parkrun.datastructures.Country;
import dnt.parkrun.datastructures.Course;
import dnt.parkrun.datastructures.Result;
import dnt.parkrun.filewebpageprovider.FileWebpageProvider;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@RunWith(Parameterized.class)
public class ParserTest
{
    private Course cornwall = new Course(27, "Cornwall", Country.NZ, "Cornwall", Course.Status.RUNNING);

    @Parameterized.Parameter(0)
    public String resource;

    @Parameterized.Parameters(name = "{0}")
    public static Object[] data() throws SQLException
    {
        return new Object[] {
                ("/example.event.with.unknowns.html"),
                ("/test/bushynewzealand/example.event.large.html"),
                ("/example.event.with.hour.plus.times.html"),
                ("/example.event.with.no.gender.html"),
                ("/example.event.with.assist.athlete.html"),
                ("/example.event.with.triple.dash.athlete.html"),
                ("/example.event.with.santa.claus.html"),
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
        List<Result> results = new ArrayList<>();
        Parser parser = new Parser.Builder(cornwall)
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
