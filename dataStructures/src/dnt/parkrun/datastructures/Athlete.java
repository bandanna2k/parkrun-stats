package dnt.parkrun.datastructures;

public class Athlete
{
    public static final int NO_ATHLETE_ID = Integer.MIN_VALUE;
    public static final Athlete NO_ATHLETE = Athlete.fromDao(null, NO_ATHLETE_ID);

    public final String name;
    public final int athleteId;

    private Athlete(String name, int athleteId)
    {
        if(athleteId == NO_ATHLETE_ID)
        {
            this.name = null;
            this.athleteId = NO_ATHLETE_ID;
        }
        else
        {
            this.name = name;
            this.athleteId = athleteId;
        }
    }

    @Override
    public String toString()
    {
        return "Athlete{" +
                "name='" + name + '\'' +
                ", athleteId=" + athleteId +
                '}';
    }

    /*
        https://www.parkrun.co.nz/parkrunner/414811/all/
     */
    public static Athlete fromAthleteSummaryLink(String name, String link)
    {
        return new Athlete(name, extractIdFromSummaryLink(link));
    }

    /*
        https://www.parkrun.co.nz/cornwall/results/athletehistory/?athleteNumber=320896
     */

    public static Athlete fromAthleteHistoryAtEventLink(String name, String link)
    {
        return new Athlete(name, extractIdFromAthleteHistoryAtEventLink(link));
    }
    /*
        https://www.parkrun.us/colermountainbikepreserve/parkrunner/9265263
     */
    public static Athlete fromAthleteAtCourseLink(String name, String link)
    {
        return new Athlete(name, extractIdFromAthleteAtCourseLink(link));
    }

    public static Athlete fromDao(String name, int athleteId)
    {
        return new Athlete(name, athleteId);
    }

    static int extractIdFromAthleteHistoryAtEventLink(String href)
    {
        if(href == null)
        {
            return NO_ATHLETE_ID;
        }
        int lastIndexOf = href.lastIndexOf("=");
        if (lastIndexOf >= 0)
        {
            try
            {
                return Integer.parseInt(href.substring(lastIndexOf + 1));
            }
            catch (NumberFormatException ex)
            {
                return NO_ATHLETE_ID;
            }
        }
        return NO_ATHLETE_ID;
    }

    static int extractIdFromAthleteAtCourseLink(String href)
    {
        if(href == null)
        {
            return NO_ATHLETE_ID;
        }
        int lastIndexOf = href.lastIndexOf("/");
        if (lastIndexOf >= 0)
        {
            try
            {
                return Integer.parseInt(href.substring(lastIndexOf + 1));
            }
            catch (NumberFormatException ex)
            {
                return NO_ATHLETE_ID;
            }
        }
        return NO_ATHLETE_ID;
    }

    /*
            https://www.parkrun.co.nz/parkrunner/414811/all/
     */
    private static int extractIdFromSummaryLink(String href)
    {
        if(href == null)
        {
            return NO_ATHLETE_ID;
        }

        try
        {
            String parkrunner = "parkrunner/";
            int indexOfParkrunner = href.indexOf(parkrunner);
            String href2 = href.substring(indexOfParkrunner + parkrunner.length());
            String href3 = href2.replace("all", "").replace("/", "");
            return Integer.parseInt(href3);
        }
        catch (NumberFormatException | StringIndexOutOfBoundsException ex)
        {
            return NO_ATHLETE_ID;
        }
    }
}
