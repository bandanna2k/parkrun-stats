package dnt.parkrun.stats.processors;


import dnt.parkrun.datastructures.Result;
import dnt.parkrun.datastructures.stats.EventDateCount;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/*
Max and Last attendance
 */
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

    public List<EventDateCount> getMaxAttendance(int courseId)
    {
        return courseIdToCourseRecord.get(courseId).getMaxAttendance();
    }

    public EventDateCount getLastAttendance(int courseId)
    {
        return courseIdToCourseRecord.get(courseId).getLastAttendance();
    }

    static class Record
    {
        private List<EventDateCount> max = new ArrayList<>();
        private EventDateCount last = null;

        public void onFinishCourse(Date date, int count)
        {
            onFinishCourseProcessMax(date, count);
            onFinishCourseProcessLast(date, count);
        }

        private void onFinishCourseProcessLast(Date date, int count)
        {
            last = new EventDateCount(date, count);
        }

        private void onFinishCourseProcessMax(Date date, int count)
        {
            if(max.isEmpty())
            {
                max.add(new EventDateCount(date, count));
                return;
            }
            if(count == max.getFirst().count)
            {
                max.add(new EventDateCount(date, count));
                return;
            }
            if(count > max.getFirst().count)
            {
                max.clear();
                max.add(new EventDateCount(date, count));
            }
        }

        public EventDateCount getLastAttendance()
        {
            return last;
        }

        public List<EventDateCount> getMaxAttendance()
        {
            return max;
        }
    }
}
