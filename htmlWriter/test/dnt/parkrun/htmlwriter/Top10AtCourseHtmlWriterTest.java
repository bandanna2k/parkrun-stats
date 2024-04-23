package dnt.parkrun.htmlwriter;

import dnt.parkrun.datastructures.Athlete;
import dnt.parkrun.htmlwriter.writers.Top10AtCourseHtmlWriter;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class Top10AtCourseHtmlWriterTest
{
    private Athlete a1;
    private Athlete a2;
    private Athlete a3;

    @Before
    public void setUp() throws Exception
    {
        a1 = Athlete.from("A1", 1);
        a2 = Athlete.from("A2", 2);
        a3 = Athlete.from("A3", 3);
    }

    @Test
    public void testPercentageOnComparator()
    {
        List<Top10AtCourseHtmlWriter.Record> list = new ArrayList<>(List.of(
                new Top10AtCourseHtmlWriter.Record(a1, 100, 50.0d),
                new Top10AtCourseHtmlWriter.Record(a2, 100, 51.0d),
                new Top10AtCourseHtmlWriter.Record(a3, 100, 52.0d)
        ));
        list.sort(Top10AtCourseHtmlWriter.Record.COMPARATOR);

        assertThat(list.get(0).athlete.athleteId).isEqualTo(3);
        assertThat(list.get(1).athlete.athleteId).isEqualTo(2);
        assertThat(list.get(2).athlete.athleteId).isEqualTo(1);
    }

    @Test
    public void testRunCountOnComparator()
    {
        List<Top10AtCourseHtmlWriter.Record> list = new ArrayList<>(List.of(
                new Top10AtCourseHtmlWriter.Record(a1, 100, 100.0d),
                new Top10AtCourseHtmlWriter.Record(a2, 102, 100.0d),
                new Top10AtCourseHtmlWriter.Record(a3, 101, 100.0d)
        ));
        list.sort(Top10AtCourseHtmlWriter.Record.COMPARATOR);

        assertThat(list.get(0).athlete.athleteId).isEqualTo(2);
        assertThat(list.get(1).athlete.athleteId).isEqualTo(3);
        assertThat(list.get(2).athlete.athleteId).isEqualTo(1);
    }

    @Test
    public void testAthleteIdOnComparator()
    {
        List<Top10AtCourseHtmlWriter.Record> list = new ArrayList<>(List.of(
                new Top10AtCourseHtmlWriter.Record(a2, 102, 100.0d),
                new Top10AtCourseHtmlWriter.Record(a1, 102, 100.0d),
                new Top10AtCourseHtmlWriter.Record(a3, 102, 100.0d)
        ));
        list.sort(Top10AtCourseHtmlWriter.Record.COMPARATOR);

        assertThat(list.get(0).athlete.athleteId).isEqualTo(1);
        assertThat(list.get(1).athlete.athleteId).isEqualTo(2);
        assertThat(list.get(2).athlete.athleteId).isEqualTo(3);
    }
}
