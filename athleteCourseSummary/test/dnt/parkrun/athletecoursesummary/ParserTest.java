package dnt.parkrun.athletecoursesummary;


import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;

public class ParserTest
{
    @Test
    public void shouldParse() throws IOException
    {
        AtomicInteger counter = new AtomicInteger(0);

        URL resource = this.getClass().getResource("/example.athlete.course.summary.html");
        new Parser.Builder()
                .file(new File(resource.getFile()))
                .forEachAthleteCourseSummary(x ->
                {
                    counter.addAndGet(x.countOfRuns);
                    System.out.println(x);
                })
                .build()
                .parse();
        System.out.println("Total:" + counter.get());
    }


}