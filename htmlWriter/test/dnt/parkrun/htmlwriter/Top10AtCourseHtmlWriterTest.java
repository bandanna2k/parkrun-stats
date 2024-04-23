package dnt.parkrun.htmlwriter;

import dnt.parkrun.datastructures.Athlete;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class Top10AtCourseHtmlWriterTest
{
    private static Comparator<StatsRecord> COMPARATOR = (r1, r2) ->
    {
        if (r1.percentage() > r2.percentage()) return -1;
        if (r1.percentage() < r2.percentage()) return 1;
        if (r1.count() > r2.count()) return -1;
        if (r1.count() < r2.count()) return 1;
        if (r1.athlete().athleteId > r2.athlete().athleteId) return 1;
        if (r1.athlete().athleteId < r2.athlete().athleteId) return -1;
        return 0;
    };

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
        List<StatsRecord> list = new ArrayList<>(List.of(
                new StatsRecord().athlete(a1).count(100).percentage(50.0d),
                new StatsRecord().athlete(a2).count(100).percentage(51.0d),
                new StatsRecord().athlete(a3).count(100).percentage(52.0d)
        ));
        list.sort(COMPARATOR);

        assertThat(list.get(0).athlete().athleteId).isEqualTo(3);
        assertThat(list.get(1).athlete().athleteId).isEqualTo(2);
        assertThat(list.get(2).athlete().athleteId).isEqualTo(1);
    }

    @Test
    public void testRunCountOnComparator()
    {
        List<StatsRecord> list = new ArrayList<>(List.of(
                new StatsRecord().athlete(a1).count(100).percentage(100.0d),
                new StatsRecord().athlete(a2).count(102).percentage(100.0d),
                new StatsRecord().athlete(a3).count(101).percentage(100.0d)
        ));
        list.sort(COMPARATOR);

        assertThat(list.get(0).athlete().athleteId).isEqualTo(2);
        assertThat(list.get(1).athlete().athleteId).isEqualTo(3);
        assertThat(list.get(2).athlete().athleteId).isEqualTo(1);
    }

    @Test
    public void testAthleteIdOnComparator()
    {
        List<StatsRecord> list = new ArrayList<>(List.of(
                new StatsRecord().athlete(a2).count(102).percentage(100.0d),
                new StatsRecord().athlete(a1).count(102).percentage(100.0d),
                new StatsRecord().athlete(a3).count(102).percentage(100.0d)
        ));
        list.sort(COMPARATOR);

        assertThat(list.get(0).athlete().athleteId).isEqualTo(1);
        assertThat(list.get(1).athlete().athleteId).isEqualTo(2);
        assertThat(list.get(2).athlete().athleteId).isEqualTo(3);
    }
}
