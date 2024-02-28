package dnt.parkrun.datastructures;

import org.junit.Test;

import static dnt.parkrun.datastructures.Time.NO_TIME_SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

public class TimeTest
{
    @Test
    public void testFromString()
    {
        assertThat(Time.fromString(null).getTotalSeconds()).isEqualTo(NO_TIME_SECONDS);
        assertThat(Time.fromString("").getTotalSeconds()).isEqualTo(NO_TIME_SECONDS);
        assertThat(Time.fromString("1:02:03").getTotalSeconds()).isEqualTo(3600 + 120 + 3);
        assertThat(Time.fromString("1:19:50").getTotalSeconds()).isEqualTo(3600 + 1190);
        assertThat(Time.fromString("24:22:03").getTotalSeconds()).isEqualTo(87723);
        assertThat(Time.fromString("19:50").getTotalSeconds()).isEqualTo(1190);
        assertThat(Time.fromString(" 19:50").getTotalSeconds()).isEqualTo(1190);
        assertThat(Time.fromString("19:50 ").getTotalSeconds()).isEqualTo(1190);
        assertThat(Time.fromString(" 19:50 ").getTotalSeconds()).isEqualTo(1190);
        assertThat(Time.fromString("59:59").getTotalSeconds()).isEqualTo(3599);
        assertThat(Time.fromString("0:0").getTotalSeconds()).isEqualTo(NO_TIME_SECONDS);
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
