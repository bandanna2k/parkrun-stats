package dnt.parkrun.datastructures;

import java.net.URL;

public class AthleteCourseSummary
{
    public final String courseLongName;
    public final int countOfRuns;
    public final Athlete athlete;

    public AthleteCourseSummary(String athleteName, String courseLongName, int countOfRuns, URL athleteAtEventUrl)
    {
        this(Athlete.fromAthleteAtCourseLink(athleteName, athleteAtEventUrl.toString()), courseLongName, countOfRuns);
    }

    public AthleteCourseSummary(Athlete athlete, String courseLongName, int countOfRuns)
    {
        this.courseLongName = courseLongName;
        this.countOfRuns = countOfRuns;
        this.athlete = athlete;
    }

    @Override
    public String toString()
    {
        return "AthleteCourseSummary{" +
                "courseLongName='" + courseLongName + '\'' +
                ", countOfRuns=" + countOfRuns +
                ", athlete=" + athlete +
                '}';
    }
}
