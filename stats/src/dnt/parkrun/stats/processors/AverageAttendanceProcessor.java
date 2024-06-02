package dnt.parkrun.stats.processors;


import dnt.parkrun.datastructures.Result;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AverageAttendanceProcessor extends AbstractProcessor<AverageAttendanceProcessor.Record>
{
    private int currentCount = 0;

    @Override
    protected void reset()
    {
        currentCount = 0;
    }

    @Override
    protected void increment(Result result)
    {
        currentCount++;
    }

    @Override
    protected Record newRecord()
    {
        return new Record();
    }

    @Override
    protected void onFinishCourse(Date date, int eventNumber, Record record)
    {
        record.onFinishCourseResults(currentCount);
        reset();
    }

    public double getAverageAttendance(int courseId)
    {
        return courseIdToCourseRecord.get(courseId).getAverageAttendance();
    }

    public double getRecentAverageAttendance(int courseId)
    {
        return courseIdToCourseRecord.get(courseId).getRecentAverageAttendance();
    }

    static class Record
    {
        double totalCount;
        double courseCount;

        List<Double> listOfXCounts = new ArrayList<>();
        double listCount;

        public void onFinishCourseResults(int currentCount)
        {
            totalCount += currentCount;
            courseCount++;

            listOfXCounts.add((double)currentCount);
            if(listOfXCounts.size() > 10)
            {
                listOfXCounts.removeFirst();
            }
        }

        public double getAverageAttendance()
        {
            return totalCount / courseCount;
        }

        public double getRecentAverageAttendance()
        {
            return listOfXCounts.stream().mapToDouble(d -> d).average().orElse(0.0);
        }
    }
}
