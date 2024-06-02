package dnt.parkrun.datastructures;

import java.util.Date;

public class Result
{
    public final int courseId;
    public final Date date;
    public final int eventNumber;
    public final int position;
    public final Athlete athlete;
    public final Time time;
    public final AgeCategory ageCategory;
    public final AgeGrade ageGrade;

    public Result(int courseId,
                  Date date,
                  int eventNumber,
                  int position,
                  Athlete athlete,
                  Time time,
                  AgeCategory ageCategory,
                  AgeGrade ageGrade)
    {
        this.courseId = courseId;
        this.date = date;
        this.eventNumber = eventNumber;
        this.position = position;
        this.athlete = athlete;
        this.time = time;
        this.ageCategory = ageCategory;
        this.ageGrade = ageGrade;
    }

    @Override
    public String toString()
    {
        return "Result{" +
                "courseId=" + courseId +
                ", date=" + date +
                ", eventNumber=" + eventNumber +
                ", position=" + position +
                ", athlete=" + athlete +
                ", time=" + time +
                ", ageCategory=" + ageCategory +
                ", ageGrade=" + ageGrade +
                '}';
    }
}
