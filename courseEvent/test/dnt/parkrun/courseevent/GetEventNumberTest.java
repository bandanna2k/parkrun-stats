package dnt.parkrun.courseevent;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class GetEventNumberTest
{
    @Test
    public void shouldGetEventNumber()
    {
        assertThat(Parser.getEventNumberFromUrl("https://www.parkrun.co.nz/taupo/results/345/")).isEqualTo(345);
        assertThat(Parser.getEventNumberFromUrl("https://www.parkrun.co.nz/taupo/results/")).isEqualTo(0);
        assertThat(Parser.getEventNumberFromUrl(null)).isEqualTo(0);
        assertThat(Parser.getEventNumberFromUrl("")).isEqualTo(0);
    }
}
