package dnt.parkrun.database;

import dnt.parkrun.datastructures.*;

import java.time.Instant;
import java.util.Date;

import static dnt.parkrun.datastructures.Course.NO_COURSE_ID;

public class ResultTestBuilder
{
    private int courseId = NO_COURSE_ID;
    private Date date = Date.from(Instant.now());
    private int position = 1;
    private Athlete athlete = new AthleteTestBuilder().build();
    private Time time = Time.from(20 * 60); // 20:00
    private AgeCategory ageCategory = AgeCategory.SM30_34;
    private AgeGrade ageGrade = AgeGrade.newInstance(60.0d);

    public Result build()
    {
        return new Result(courseId, date, position, athlete, time, ageCategory, ageGrade);
    }

    public ResultTestBuilder athlete(Athlete athlete)
    {
        this.athlete = athlete;
        return this;
    }

    public ResultTestBuilder date(Date date)
    {
        this.date = date;
        return this;
    }

    public ResultTestBuilder courseId(int courseId)
    {
        this.courseId = courseId;
        return this;
    }
}
