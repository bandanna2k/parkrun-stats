package dnt.parkrun.datastructures;

public class AthleteCourseSummary
{
    public final Course course;
    public final int countOfRuns;
    public final Athlete athlete;

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
