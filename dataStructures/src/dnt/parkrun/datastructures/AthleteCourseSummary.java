package dnt.parkrun.datastructures;

import java.net.URL;

public class AthleteCourseSummary
{
    public final String courseLongName;
    public final int countOfRuns;
    public final URL athleteAtEventUrl;
    public final Athlete athlete;

    public AthleteCourseSummary(String name, String courseLongName, int countOfRuns, URL athleteAtEventUrl)
    {
        this.courseLongName = courseLongName;
        this.countOfRuns = countOfRuns;
        this.athleteAtEventUrl = athleteAtEventUrl;
        this.athlete = Athlete.fromAthleteAtCourseLink(name, athleteAtEventUrl.toString());
    }

    @Override
    public String toString()
    {
        return "AthleteCourseSummary{" +
                "courseLongName='" + courseLongName + '\'' +
                ", countOfRuns=" + countOfRuns +
                ", athleteAtEventUrl=" + athleteAtEventUrl +
                ", athlete=" + athlete +
                '}';
    }
}
