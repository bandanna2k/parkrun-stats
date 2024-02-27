package dnt.datastructures;

import dnt.parkrun.datastructures.Time;
import org.junit.Test;

import static dnt.parkrun.datastructures.Time.NO_TIME_SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

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
}
