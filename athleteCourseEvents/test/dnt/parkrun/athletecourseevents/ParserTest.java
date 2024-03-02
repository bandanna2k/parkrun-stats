package dnt.parkrun.athletecourseevents;


import org.assertj.core.api.Assertions;
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

        URL resource = this.getClass().getResource("/example.athlete.event.html");
        new Parser.Builder()
                .file(new File(resource.getFile()))
                .forEachAthleteCourseEvent(x ->
                {
                    counter.incrementAndGet();
                    System.out.println(x);
                })
                .build()
                .parse();
        Assertions.assertThat(counter.get()).isEqualTo(1);
    }
}