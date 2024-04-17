package dnt.parkrun.stats.speed;

import dnt.parkrun.datastructures.*;

import java.util.Date;

public class StatsRecord
{
    private Athlete athlete;
    private Time time;
    private AgeGrade ageGrade;
    private Date date;
    private AgeGroup ageGroup;
    private Course course;
    private int eventNumber;
    private int position;
    private int courseId;
    private Result result;

    StatsRecord athlete(Athlete athlete)
    {
        this.athlete = athlete;
        return this;
    }
    Athlete athlete() { return athlete; }

    public StatsRecord time(Time time)
    {
        this.time = time;
        return this;
    }
    Time time() { return time; }

    public StatsRecord ageGrade(AgeGrade ageGrade)
    {
        this.ageGrade = ageGrade;
        return this;
    }
    AgeGrade ageGrade() { return ageGrade; }

    public StatsRecord date(Date date)
    {
        this.date = date;
        return this;
    }
    public Date date() { return date; }

    public StatsRecord ageGroup(AgeGroup ageGroup)
    {
        this.ageGroup = ageGroup;
        return this;
    }
    public AgeGroup ageGroup() { return ageGroup; }

    public StatsRecord eventNumber(int eventNumber)
    {
        this.eventNumber = eventNumber;
        return this;
    }
    public int eventNumber() { return eventNumber; }

    public StatsRecord course(Course course)
    {
        this.course = course;
        return this;
    }
    public Course course() { return course; }

    public StatsRecord position(int position)
    {
        this.position = position;
        return this;
    }
    public int position() { return position; }

    public StatsRecord courseId(int courseId)
    {
        this.courseId = courseId;
        return this;
    }
    public int courseId() { return courseId; }

    public StatsRecord result(Result result)
    {
        this.result = result;
//        time(result.time);
//        date(result.date);
//        ageGrade(result.ageGrade);
//        ageGroup(result.ageGroup);
//        athlete(result.athlete);
//        courseId(result.courseId);
//        position(result.position);
        return this;
    }
    public Result result() { return result; }
}
