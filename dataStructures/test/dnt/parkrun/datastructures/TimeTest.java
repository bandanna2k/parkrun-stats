package dnt.parkrun.datastructures;

import org.junit.Test;

import static dnt.parkrun.datastructures.Time.NO_TIME_SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

public class TimeTest
{
    @Test
    public void testFromInt()
    {
        assertThat(Time.from(1).toString()).isEqualTo("00:01");
        assertThat(Time.from(61).toString()).isEqualTo("01:01");
        assertThat(Time.from(3601).toString()).isEqualTo("1:00:01");
        assertThat(Time.from(3661).toString()).isEqualTo("1:01:01");
    }

    @Test
    public void testTotalSeconds()
    {
        assertThat(Time.from(1).getTotalSeconds()).isEqualTo(1);
        assertThat(Time.from(61).getTotalSeconds()).isEqualTo(61);
        assertThat(Time.from(3601).getTotalSeconds()).isEqualTo(3601);
        assertThat(Time.from(3661).getTotalSeconds()).isEqualTo(3661);
    }

    @Test
    public void testFromString()
    {
        assertThat(Time.from(null).getTotalSeconds()).isEqualTo(NO_TIME_SECONDS);
        assertThat(Time.from("").getTotalSeconds()).isEqualTo(NO_TIME_SECONDS);
        assertThat(Time.from("1:02:03").getTotalSeconds()).isEqualTo(3600 + 120 + 3);
        assertThat(Time.from("1:19:50").getTotalSeconds()).isEqualTo(3600 + 1190);
        assertThat(Time.from("24:22:03").getTotalSeconds()).isEqualTo(87723);
        assertThat(Time.from("19:50").getTotalSeconds()).isEqualTo(1190);
        assertThat(Time.from(" 19:50").getTotalSeconds()).isEqualTo(1190);
        assertThat(Time.from("19:50 ").getTotalSeconds()).isEqualTo(1190);
        assertThat(Time.from(" 19:50 ").getTotalSeconds()).isEqualTo(1190);
        assertThat(Time.from("59:59").getTotalSeconds()).isEqualTo(3599);
        assertThat(Time.from("0:0").getTotalSeconds()).isEqualTo(NO_TIME_SECONDS);
    }

    @Test
    public void testToString()
    {
        assertThat(new Time(0, 0, 0).toString()).isEqualTo("00:00");
        assertThat(new Time(1, 0, 0).toString()).isEqualTo("1:00:00");
        assertThat(new Time(99, 59, 59).toString()).isEqualTo("99:59:59");
    }

    @Test
    public void testTimeValidation()
    {
        assertThrows(IllegalArgumentException.class, () -> new Time(-1, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> new Time(0, -1, 0));
        assertThrows(IllegalArgumentException.class, () -> new Time(-1, 0, -1));
        assertThrows(IllegalArgumentException.class, () -> new Time(100, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> new Time(0, 60, 0));
        assertThrows(IllegalArgumentException.class, () -> new Time(0, 0, 60));
    }
}
