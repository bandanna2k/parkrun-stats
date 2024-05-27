package dnt.parkrun.datastructures.stats;

import java.util.List;

public class AttendanceRecord
{
    public final int courseId;
    public final EventDateCount recentEvent;
    public final List<EventDateCount> maxEvent;

    public String courseSmallTest;
    public int recentAttendanceDelta = 0;
    public int maxAttendanceDelta = 0;


    public AttendanceRecord(int courseId,
                            EventDateCount recentEvent,
                            List<EventDateCount> maxEvent)
    {
        this.courseId = courseId;
        this.recentEvent = recentEvent;
        this.maxEvent = maxEvent;
    }

    @Override
    public String toString()
    {
        return "AttendanceRecord{" +
                "courseId=" + courseId +
                ", recentEvent=" + recentEvent +
                ", maxEvent=" + maxEvent +
                ", courseSmallTest='" + courseSmallTest + '\'' +
                ", recentAttendanceDelta=" + recentAttendanceDelta +
                ", maxAttendanceDelta=" + maxAttendanceDelta +
                '}';
    }

}
