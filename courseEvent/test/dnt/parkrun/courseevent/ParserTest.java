package dnt.parkrun.courseevent;

import dnt.parkrun.datastructures.Country;
import dnt.parkrun.datastructures.Course;
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
        Parser parser = new Parser.Builder(cornwall)
                .forEachAthlete(x -> System.out.println("Athlete: " + x))
                .forEachResult(x -> System.out.println("Result: " + x))
                .forEachVolunteer(x -> System.out.println("Volunteer: " + x))
                .file(new File(resource.getFile()))
                .build();
        parser.parse();
    }

    @Test
    public void shouldParseWithHourPlusRunners()
    {
        URL resource = this.getClass().getResource("/example.event.with.hour.plus.times.html");
        Parser parser = new Parser.Builder(cornwall)
                .forEachAthlete(x -> System.out.println("Athlete: " + x))
                .forEachResult(x -> System.out.println("Result: " + x))
                .forEachVolunteer(x -> System.out.println("Volunteer: " + x))
                .file(new File(resource.getFile()))
                .build();
        parser.parse();
    }
}
