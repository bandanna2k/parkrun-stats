package dnt.parkrun.stats.processors;

import dnt.parkrun.database.ResultDao;
import dnt.parkrun.datastructures.Athlete;
import dnt.parkrun.datastructures.Result;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AverageProcessorOld implements ResultDao.ResultProcessor
{
    private final Map<Integer, AverageRecord> courseIdToAverageRecord = new HashMap<>();

    private Result prevResult;// = new Result(
            //NO_COURSE_ID, Date.from(EPOCH), 0, null, Time.from(0), UNKNOWN, AgeGrade.newInstanceNoAgeGrade())

    @Override
    public void visitInOrder(Result result)
    {
        AverageRecord averageRecord = courseIdToAverageRecord.computeIfAbsent(result.courseId, integer -> new AverageRecord());
        if(prevResult == null)
        {
            averageRecord.addResult(result, true);
        }
        else
        {
            averageRecord.addResult(result, result.courseId != prevResult.courseId);
        }

        prevResult = result;
    }

    @Override
    public void onFinishCourse()
    {

    }

    public double getAverageAttendanceForAllEvents(int courseId)
    {
        return courseIdToAverageRecord.get(courseId).getAverageAttendanceForAllEvents();
    }

    public double getAverageTimeForAllEvents(int courseId)
    {
        return courseIdToAverageRecord.get(courseId).getAverageTimeForAllEvents();
    }

    public double getAverageAttendanceForRecentEvents(int courseId)
    {
        return courseIdToAverageRecord.get(courseId).getAverageAttendanceForRecentEvents();
    }

    public double getAverageTimeForRecentEvents(int courseId)
    {
        return courseIdToAverageRecord.get(courseId).getAverageTimeForRecentEvents();
    }

    private static class AverageRecord
    {
        public static final int MOVING_AVERAGE_COUNT = 10;
        double currentCourseCount = 0.0;
        double currentCourseSumOfTimes = 0.0;

        List<Double> listOfAverageTimes = new ArrayList<>();
        List<Double> listOfAttendances = new ArrayList<>();

        List<Double> listOfRecentAverageTimes = new ArrayList<>();
        List<Double> listOfRecentAttendances = new ArrayList<>();

        public void addResult(Result result, boolean isNextCourse)
        {
            if(isNextCourse)
            {
                finalise();
            }

            if(result.athlete.athleteId == Athlete.NO_ATHLETE_ID) return;

            currentCourseCount++;
            currentCourseSumOfTimes += result.time.getTotalSeconds();
        }

        private void finalise()
        {
            if(currentCourseCount != 0)
            {
                double averageTimesForCourse = currentCourseSumOfTimes / currentCourseCount;

                listOfAttendances.add(currentCourseCount);
                listOfAverageTimes.add(averageTimesForCourse);

                listOfRecentAttendances.add(currentCourseCount);
                listOfRecentAverageTimes.add(averageTimesForCourse);
                if(listOfRecentAttendances.size() > MOVING_AVERAGE_COUNT) listOfRecentAttendances.removeFirst();
                if(listOfRecentAverageTimes.size() > MOVING_AVERAGE_COUNT) listOfRecentAverageTimes.removeFirst();
            }

            // Reset
            currentCourseCount = 0;
            currentCourseSumOfTimes = 0;
        }

        double getAverageTimeForAllEvents()
        {
            finalise();
            return listOfAverageTimes.stream().mapToDouble(d -> d).average().orElse(0.0);
        }

        double getAverageAttendanceForAllEvents()
        {
            finalise();
            return listOfAttendances.stream().mapToDouble(d -> d).average().orElse(0.0);
        }

        double getAverageTimeForRecentEvents()
        {
            finalise();
            return listOfRecentAverageTimes.stream().mapToDouble(d -> d).average().orElse(0.0);
        }

        double getAverageAttendanceForRecentEvents()
        {
            finalise();
            return listOfRecentAttendances.stream().mapToDouble(d -> d).average().orElse(0.0);
        }
    }
}
