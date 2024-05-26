package dnt.parkrun.stats.processors;

import dnt.parkrun.database.ResultDao;
import dnt.parkrun.datastructures.Result;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static dnt.parkrun.datastructures.Course.NO_COURSE_ID;

public abstract class AbstractProcessor<T> implements ResultDao.ResultProcessor
{
    protected final Map<Integer, T> courseIdToCourseRecord = new HashMap<>();

    private int prevCourseId = NO_COURSE_ID;
    private Date prevDate = Date.from(Instant.EPOCH.minus(1, ChronoUnit.DAYS));

    @Override
    public void visitInOrder(Result result)
    {
        if(prevCourseId == NO_COURSE_ID)
        {
            // First result, do nothing.
        }
        else if(prevCourseId != result.courseId || prevDate.getTime() != result.date.getTime())
        {
            // Change of course or date. Maybe add max attendance
            onFinishCourse();

            reset();
        }

        increment(result);
        prevCourseId = result.courseId;
        prevDate = result.date;
    }

    @Override
    public void onFinish()
    {
        onFinishCourse();
    }

    void onFinishCourse()
    {
        T prevRecord = courseIdToCourseRecord.computeIfAbsent(prevCourseId, courseId -> newRecord());
        onFinishCourse(prevDate, prevRecord);
    }

    protected abstract void increment(Result result);
    protected abstract void reset();
    protected abstract T newRecord();
    protected abstract void onFinishCourse(Date date, T record);
}
