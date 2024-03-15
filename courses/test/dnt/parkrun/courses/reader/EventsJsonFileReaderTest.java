package dnt.parkrun.courses.reader;

import dnt.parkrun.datastructures.Course;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.util.*;
import java.util.function.Supplier;

import static dnt.parkrun.datastructures.Country.NZ;
import static org.assertj.core.api.Assertions.assertThat;

public class EventsJsonFileReaderTest
{
    private final Supplier<InputStream> inputStreamSupplier = () ->
            EventsJsonFileReader.class.getResourceAsStream("/events.json");

    private List<Course> eventList = new ArrayList<>();

    private EventsJsonFileReader reader = new EventsJsonFileReader.Builder(inputStreamSupplier)
            .forEachCourse(course -> eventList.add(course))
            .build();

    @Before
    public void setUp() throws Exception
    {
        reader.read();
    }

    @Test
    public void shouldReadEvents()
    {
        assertThat(eventList.size()).isEqualTo(2496);
    }

    @Test
    public void shouldReadNzEvents()
    {
        assertThat(eventList.stream()
                .filter(course -> course.country == NZ)
                .count()).isEqualTo(65);
    }

    @Test
    public void hashShouldNotCollide()
    {
        Map<Long, Course> hashToCourse = new HashMap<>();
        eventList.forEach(course -> {
            long hash = UUID.nameUUIDFromBytes(course.name.getBytes()).getMostSignificantBits();
            hashToCourse.computeIfPresent(hash, (key, value) -> { throw new RuntimeException(); });
            hashToCourse.put(hash, course);
        });
        System.out.println(hashToCourse);
    }
}
