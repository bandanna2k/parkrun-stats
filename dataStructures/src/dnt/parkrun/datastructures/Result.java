package dnt.parkrun.datastructures;

import java.util.Date;

public class Result
{
    public final int courseId;
    public final Date date;
    public final int position;
    public final Athlete athlete;
    public final Time time;
    public final Gender gender;
    public final AgeGroup ageGroup;
    public final Integer ageGrade;

    public Result(int courseId, Date date, int position, Athlete athlete, Time time, Gender gender, AgeGroup ageGroup, Double ageGrade)
    {
        this.courseId = courseId;
        this.date = date;
        this.position = position;
        this.athlete = athlete;
        this.time = time;
        this.gender = gender;
        this.ageGroup = ageGroup;
        this.ageGrade = ageGrade == null ? null : (int)(ageGrade * 100.0d);
    }

    @Override
    public String toString()
    {
        return "Result{" +
                "courseId=" + courseId +
                ", date=" + date +
                ", position=" + position +
                ", athlete=" + athlete +
                ", time=" + time +
                ", gender=" + gender +
                ", ageGroup=" + ageGroup +
                ", ageGrade=" + ageGrade +
                '}';
    }
}
