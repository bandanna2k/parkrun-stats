package dnt.parkrun.stats.processors;


import dnt.parkrun.datastructures.Result;
import dnt.parkrun.datastructures.Time;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AverageTimeProcessor extends AbstractProcessor<AverageTimeProcessor.Record>
{
    private static final int MOVING_AVERAGE = 10;

    private double currentTime = 0;
    private double currentCount = 0;

    @Override
    protected void reset()
    {
        currentTime = 0.0;
        currentCount = 0;
    }

    @Override
    protected void increment(Result result)
    {
        currentTime += result.time.getTotalSeconds();
        currentCount++;
    }

    @Override
    protected Record newRecord()
    {
        return new Record();
    }

    @Override
    protected void onFinishCourse(Date date, Record record)
    {
        record.onFinishCourseResults(currentCount, currentTime);
        reset();
    }

    public Time getAverageTime(int courseId)
    {
        return Time.from((int)courseIdToCourseRecord.get(courseId).getAverageTime());
    }

    public Time getRecentAverageTime(int courseId)
    {
        return Time.from((int)courseIdToCourseRecord.get(courseId).getRecentAverageTime());
    }

    static class Record
    {
        double totalTime;
        double courseCount;

        List<Double> listOfXTimes = new ArrayList<>();
        List<Double> listOfXCounts = new ArrayList<>();

        public void onFinishCourseResults(double currentCount, double currentTime)
        {
            totalTime += currentTime;
            courseCount += currentCount;

            listOfXTimes.add(currentTime);
            listOfXCounts.add(currentCount);
            if(listOfXTimes.size() > MOVING_AVERAGE)
            {
                listOfXTimes.removeFirst();
                listOfXCounts.removeFirst();
            }
        }

        public double getAverageTime()
        {
            return totalTime / courseCount;
        }

        public double getRecentAverageTime()
        {
            double sumOfTimes = listOfXTimes.stream().mapToDouble(d -> d).sum();
            double sumOfRunners = listOfXCounts.stream().mapToDouble(d -> d).sum();
            return sumOfTimes / sumOfRunners;
        }
    }
}
