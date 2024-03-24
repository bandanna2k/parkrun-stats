package dnt.parkrun.stats.invariants;

import dnt.parkrun.courses.reader.EventsJsonFileReader;
import dnt.parkrun.datastructures.Country;
import dnt.parkrun.datastructures.Course;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static dnt.parkrun.region.Region.isSameNzRegion;
import static org.junit.Assert.assertTrue;

public class ProvinceTest
{
    @Test
    public void allNzCoursesShouldHaveAProvince() throws IOException
    {
        List<Course> courses = new ArrayList<>();
        Supplier<InputStream> inputStreamSupplier = () ->
                EventsJsonFileReader.class.getResourceAsStream("/events.json");
        EventsJsonFileReader reader = new EventsJsonFileReader.Builder(inputStreamSupplier)
                .forEachCourse(courses::add)
                .build();
        reader.read();
        courses.stream().filter(c -> c.country == Country.NZ).forEach(c -> {
            assertTrue("Bad course " + c, isSameNzRegion(c, c));
        });

    }
}
