package dnt.parkrun.datastructures;

import java.net.URL;

public class AthleteCourseSummary
{
    public final String course;
    public final int countOfRuns;
    public final URL athleteAtEventUrl;
    public final Athlete athlete;

    public AthleteCourseSummary(String name, String course, int countOfRuns, URL athleteAtEventUrl)
    {
        this.course = course;
        this.countOfRuns = countOfRuns;
        this.athleteAtEventUrl = athleteAtEventUrl;
        this.athlete = Athlete.fromAthleteAtCourseLink(name, athleteAtEventUrl.toString());
    }

    @Override
    public String toString()
    {
        return "AthleteAtEventSummary{" +
                "course='" + course + '\'' +
                ", countOfRuns=" + countOfRuns +
                ", athleteAtEventUrl=" + athleteAtEventUrl +
                ", athlete=" + athlete +
                '}';
    }
}
