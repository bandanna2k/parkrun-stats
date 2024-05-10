package dnt.parkrun.stats.processors;

import dnt.parkrun.database.ResultDao;
import dnt.parkrun.datastructures.Result;
import dnt.parkrun.datastructures.stats.EventDateCount;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static dnt.parkrun.datastructures.Course.NO_COURSE_ID;

public class MaxAttendanceProcessor implements ResultDao.ResultProcessor
{
    private final Map<Integer, CourseRecord> courseIdToCourseRecord = new HashMap<>();

    private int currentCourseCount = 0;
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

            currentCourseCount = 0;
        }

        currentCourseCount++;
        prevCourseId = result.courseId;
        prevDate = result.date;
    }

    @Override
    public void onFinishCourse()
    {
        CourseRecord currentCourseRecord = courseIdToCourseRecord.computeIfAbsent(prevCourseId, courseId -> new CourseRecord());
        currentCourseRecord.maybeAddMaxAttendance(new EventDateCount(prevDate, currentCourseCount));
    }

    public List<EventDateCount> getMaxAttendancesOverAllEvents(int courseId)
    {
        return courseIdToCourseRecord.get(courseId).getMaxAttendances();
    }

    private static class CourseRecord
    {
        private final List<EventDateCount> maxAttendances = new ArrayList<>();

        public boolean maybeAddMaxAttendance(EventDateCount maybeMaxAttendance)
        {
            if(maxAttendances.isEmpty())
            {
//                System.out.println("Added empty " + maybeMaxAttendance);
                maxAttendances.add(maybeMaxAttendance);
                return true;
            }
            else if(maybeMaxAttendance.count == maxAttendances.getFirst().count)
            {
//                System.out.println("Added = " + maybeMaxAttendance);
                maxAttendances.add(maybeMaxAttendance);
                return true;
            }
            else if(maybeMaxAttendance.count > maxAttendances.getFirst().count)
            {
//                System.out.println("Added > " + maybeMaxAttendance);
                maxAttendances.clear();
                maxAttendances.add(maybeMaxAttendance);
                return true;
            }
            return false;
        }

        public List<EventDateCount> getMaxAttendances()
        {
            return maxAttendances;
        }
    }
}
