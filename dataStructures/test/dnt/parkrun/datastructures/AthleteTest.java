package dnt.parkrun.datastructures;

import org.junit.Test;

import static dnt.parkrun.datastructures.Athlete.NO_ATHLETE_ID;
import static org.assertj.core.api.Assertions.assertThat;

public class AthleteTest
{
    @Test
    public void testExtractIdFromAthleteSummaryLink()
    {
        assertThat(Athlete.fromAthleteSummaryLink("test", "https://www.parkrun.co.nz/parkrunner/414811/all/").athleteId)
                .isEqualTo(414811);
        assertThat(Athlete.fromAthleteSummaryLink("test", "https://www.parkrun.co.nz/parkrunner/all/").athleteId)
                .isEqualTo(NO_ATHLETE_ID);
        assertThat(Athlete.fromAthleteSummaryLink("test", "https://www.parkrun.co.nz/parkrunner/414811/").athleteId)
                .isEqualTo(414811);
        assertThat(Athlete.fromAthleteSummaryLink("test", "https://www.parkrun.co.nz/414811/all/").athleteId)
                .isEqualTo(NO_ATHLETE_ID);
        assertThat(Athlete.fromAthleteSummaryLink("test", null).athleteId)
                .isEqualTo(NO_ATHLETE_ID);
        assertThat(Athlete.fromAthleteSummaryLink("test", "").athleteId)
                .isEqualTo(NO_ATHLETE_ID);
    }

    @Test
    public void testExtractIdFromAthleteHistoryAtEventLink()
    {
        assertThat(Athlete.fromAthleteHistoryAtEventLink("test", "https://www.parkrun.co.nz/cornwall/results/athletehistory/?athleteNumber=320896").athleteId)
                .isEqualTo(320896);
        assertThat(Athlete.fromAthleteHistoryAtEventLink("test", "https://www.parkrun.co.nz/cornwall/results/athletehistory/?athleteNumber=").athleteId)
                .isEqualTo(NO_ATHLETE_ID);
        assertThat(Athlete.fromAthleteHistoryAtEventLink("test", "https://www.parkrun.co.nz/cornwall/results/athletehistory/?athleteNumber").athleteId)
                .isEqualTo(NO_ATHLETE_ID);
        assertThat(Athlete.fromAthleteHistoryAtEventLink("test", null).athleteId)
                .isEqualTo(NO_ATHLETE_ID);
        assertThat(Athlete.fromAthleteHistoryAtEventLink("test", "https://www.parkrun.us/colermountainbikepreserve/results/73/athletehistory/?athleteNumber=243802").athleteId)
                .isEqualTo(243802);
    }

    @Test
    public void testExtractIdFromAthleteAtCourseLink()
    {
        assertThat(Athlete.fromAthleteAtCourseLink("test", "https://www.parkrun.us/colermountainbikepreserve/parkrunner/9265263").athleteId)
                .isEqualTo(9265263);
        assertThat(Athlete.fromAthleteAtCourseLink("test", "https://www.parkrun.us/colermountainbikepreserve/parkrunner").athleteId)
                .isEqualTo(NO_ATHLETE_ID);
        assertThat(Athlete.fromAthleteAtCourseLink("test", "").athleteId)
                .isEqualTo(NO_ATHLETE_ID);
        assertThat(Athlete.fromAthleteAtCourseLink("test", null).athleteId)
                .isEqualTo(NO_ATHLETE_ID);
    }
}
