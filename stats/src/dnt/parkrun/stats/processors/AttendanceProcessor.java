package dnt.parkrun.stats.processors;


import dnt.parkrun.datastructures.Result;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AttendanceProcessor extends AbstractProcessor<AttendanceProcessor.Record>
{
    private int count = 0;

    @Override
    protected void reset()
    {
        count = 0;
    }

    @Override
    protected void increment(Result result)
    {
        count++;
    }

    @Override
    protected Record newRecord()
    {
        return new Record();
    }

    @Override
    protected void onFinishCourse(Date date, Record record)
    {
        record.onFinishCourse(date, count);
        reset();
    }

    public List<DateCount> getMaxAttendance(int courseId)
    {
        return courseIdToCourseRecord.get(courseId).getMaxAttendance();
    }

    public DateCount getLastAttendance(int courseId)
    {
        return courseIdToCourseRecord.get(courseId).getLastAttendance();
    }

    static class Record
    {
        private List<DateCount> max = new ArrayList<>();
        private DateCount last = null;

        public void onFinishCourse(Date date, int count)
        {
            onFinishCourseProcessMax(date, count);
            onFinishCourseProcessLast(date, count);
        }

        private void onFinishCourseProcessLast(Date date, int count)
        {
            last = new DateCount(date, count);
        }

        private void onFinishCourseProcessMax(Date date, int count)
        {
            if(max.isEmpty())
            {
                max.add(new DateCount(date, count));
                return;
            }
            if(count == max.getFirst().count)
            {
                max.add(new DateCount(date, count));
                return;
            }
            if(count > max.getFirst().count)
            {
                max.clear();
                max.add(new DateCount(date, count));
            }
        }

        public DateCount getLastAttendance()
        {
            return last;
        }

        public List<DateCount> getMaxAttendance()
        {
            return max;
        }
    }
}
