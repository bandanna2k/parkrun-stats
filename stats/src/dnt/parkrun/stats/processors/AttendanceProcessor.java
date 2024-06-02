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
    protected void onFinishCourse(Date date, int eventNumber, Record record)
    {
        record.onFinishCourse(date, eventNumber, count);
        reset();
    }

    public List<EventDateCount> getMaxAttendance(int courseId)
    {
        return courseIdToCourseRecord.get(courseId).getMaxAttendance();
    }
    public int getMaxDifference(int courseId)
    {
        return courseIdToCourseRecord.get(courseId).maxDelta;
    }

    public EventDateCount getLastAttendance(int courseId)
    {
        return courseIdToCourseRecord.get(courseId).getLastAttendance();
    }
    public int getLastDifference(int courseId)
    {
        return courseIdToCourseRecord.get(courseId).lastDelta;
    }


    static class Record
    {
        private List<EventDateCount> prevMax = new ArrayList<>();
        private List<EventDateCount> max = new ArrayList<>();
        private EventDateCount prev = null;
        private EventDateCount last = null;
        private int lastDelta = 0;
        private int maxDelta = 0;

        public void onFinishCourse(Date date, int eventNumber, int count)
        {
            onFinishCourseProcessMax(date, eventNumber, count);
            onFinishCourseProcessLast(date, eventNumber, count);
        }

        private void onFinishCourseProcessLast(Date date, int eventNumber, int count)
        {
            prev = last;
            last = new EventDateCount(date, count, eventNumber);
            if(prev != null) lastDelta = last.count - prev.count;
        }

        private void onFinishCourseProcessMax(Date date, int eventNumber, int count)
        {
            if(max.isEmpty())
            {
                // First event
                EventDateCount newEventDateCount = new EventDateCount(date, eventNumber, count);
                prevMax.add(newEventDateCount);
                max.add(newEventDateCount);
                return;
            }
            if(count == max.getFirst().count)
            {
                // Equalling the record
                max.add(new EventDateCount(date, eventNumber, count));
                return;
            }
            if(count > max.getFirst().count)
            {
                // New event record
                prevMax.clear();
                prevMax.addAll(max);

                maxDelta = count - prevMax.getFirst().count;
                max.clear();
                max.add(new EventDateCount(date, eventNumber, count));
                return;
            }
            maxDelta = 0;
        }

        public EventDateCount getLastAttendance()
        {
            return last;
        }

        public EventDateCount getLastMinusOneAttendance()
        {
            return prev;
        }

        public List<EventDateCount> getMaxAttendance()
        {
            return max;
        }

        public List<EventDateCount> getPrevMaxAttendance()
        {
            return prevMax;
        }
    }
}
