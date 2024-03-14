package dnt.parkrun.datastructures;

import java.net.URL;

public class AthleteCourseSummary
{
    public final Course course;
    public final int countOfRuns;
    public final Athlete athlete;

    public AthleteCourseSummary(String athleteName, Course course, int countOfRuns, URL athleteAtEventUrl)
    {
        this(Athlete.fromAthleteAtCourseLink(athleteName, athleteAtEventUrl.toString()), course, countOfRuns);
    }

    public AthleteCourseSummary(Athlete athlete, Course course, int countOfRuns)
    {
        this.course = course;
        this.countOfRuns = countOfRuns;
        this.athlete = athlete;
    }

    @Override
    public String toString()
    {
        return "AthleteCourseSummary{" +
                "course=" + course +
                ", countOfRuns=" + countOfRuns +
                ", athlete=" + athlete +
                '}';
    }
}
