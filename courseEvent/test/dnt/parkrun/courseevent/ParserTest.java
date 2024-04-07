package dnt.parkrun.courseevent;

import dnt.parkrun.datastructures.Country;
import dnt.parkrun.datastructures.Course;
import dnt.parkrun.filewebpageprovider.FileWebpageProvider;
import org.junit.Test;

import java.io.File;
import java.net.URL;

public class ParserTest
{
    private Course cornwall = new Course(27, "Cornwall", Country.NZ, "Cornwall", Course.Status.RUNNING);

    @Test
    public void shouldParseWithUnknowns()
    {
        URL resource = this.getClass().getResource("/example.event.with.unknowns.html");
        parseResource(resource);
    }

    @Test
    public void shouldParseWithHourPlusRunners()
    {
        URL resource = this.getClass().getResource("/example.event.with.hour.plus.times.html");
        parseResource(resource);
    }

    @Test
    public void shouldParseWithNoGender()
    {
        URL resource = this.getClass().getResource("/example.event.with.no.gender.html");
        parseResource(resource);
    }

    private void parseResource(URL resource)
    {
        Parser parser = new Parser.Builder(cornwall)
                .forEachAthlete(x -> System.out.println("Athlete: " + x))
                .forEachResult(x -> System.out.println("Result: " + x))
                .forEachVolunteer(x -> System.out.println("Volunteer: " + x))
                .webpageProvider(new FileWebpageProvider(new File(resource.getFile())))
                .build();
        parser.parse();
    }
}
