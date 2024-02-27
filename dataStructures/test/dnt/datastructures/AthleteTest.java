package dnt.datastructures;

import dnt.parkrun.datastructures.Athlete;
import org.assertj.core.api.Assertions;
import org.junit.Test;

public class AthleteTest
{
    @Test
    public void testExtractIdFromSummaryLink()
    {
        Assertions.assertThat(Athlete.fromSummaryLink("test", "https://www.parkrun.co.nz/cornwall/results/athletehistory/?athleteNumber=320896").athleteId)
                .isEqualTo(320896);
        Assertions.assertThat(Athlete.fromSummaryLink("test", "https://www.parkrun.co.nz/cornwall/results/athletehistory/?athleteNumber=").athleteId)
                .isEqualTo(Athlete.NO_ATHLETE_ID);
        Assertions.assertThat(Athlete.fromSummaryLink("test", "https://www.parkrun.co.nz/cornwall/results/athletehistory/?athleteNumber").athleteId)
                .isEqualTo(Athlete.NO_ATHLETE_ID);
        Assertions.assertThat(Athlete.fromSummaryLink("test", null).athleteId)
                .isEqualTo(Athlete.NO_ATHLETE_ID);
    }

    @Test
    public void testExtractIdFromEventLink()
    {
        Assertions.assertThat(Athlete.fromEventLink("test", "https://www.parkrun.us/colermountainbikepreserve/parkrunner/9265263").athleteId)
                .isEqualTo(9265263);
        Assertions.assertThat(Athlete.fromEventLink("test", "https://www.parkrun.us/colermountainbikepreserve/parkrunner").athleteId)
                .isEqualTo(Athlete.NO_ATHLETE_ID);
        Assertions.assertThat(Athlete.fromEventLink("test", "").athleteId)
                .isEqualTo(Athlete.NO_ATHLETE_ID);
        Assertions.assertThat(Athlete.fromEventLink("test", null).athleteId)
                .isEqualTo(Athlete.NO_ATHLETE_ID);
    }
}
