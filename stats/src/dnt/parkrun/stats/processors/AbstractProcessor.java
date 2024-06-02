package dnt.parkrun.stats.processors;

import dnt.parkrun.database.ResultDao;
import dnt.parkrun.datastructures.Result;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractProcessor<T> implements ResultDao.ResultProcessor
{
    protected final Map<Integer, T> courseIdToCourseRecord = new HashMap<>();

//    private int prevCourseId = NO_COURSE_ID;
//    private Date prevDate = Date.from(Instant.EPOCH.minus(1, ChronoUnit.DAYS));
    private Record prevRecord;

    @Override
    public void visitInOrder(Result result)
    {
        if(prevRecord == null)
        {
            // First result, do nothing.
        }
        else if(prevRecord.courseId != result.courseId ||
                prevRecord.date.getTime() != result.date.getTime())
        {
            onFinishCourse();
            reset();
        }

        increment(result);

        prevRecord = new Record(result.courseId, result.date, result.eventNumber);
    }

    @Override
    public void onFinish()
    {
        onFinishCourse();
    }

    private void onFinishCourse()
    {
        T prevRecord = courseIdToCourseRecord.computeIfAbsent(this.prevRecord.courseId, courseId -> newRecord());
        onFinishCourse(this.prevRecord.date, this.prevRecord.eventNumber, prevRecord);
    }

    protected abstract void increment(Result result);
    protected abstract void reset();
    protected abstract T newRecord();
    protected abstract void onFinishCourse(Date date, int eventNumber, T record);

    private static class Record
    {
        final int courseId;
        final Date date;
        final int eventNumber;

        private Record(int courseId, Date date, int eventNumber)
        {
            this.courseId = courseId;
            this.date = date;
            this.eventNumber = eventNumber;
        }
    }
}
