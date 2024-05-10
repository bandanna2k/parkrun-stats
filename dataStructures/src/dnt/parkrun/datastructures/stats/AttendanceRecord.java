package dnt.parkrun.datastructures.stats;

import java.sql.Date;

public class AttendanceRecord
{
    public final int courseId;
    public String courseSmallTest;

    public final int recentEventFinishers;
    public final int recentEventNumber;
    public final Date recentEventDate;

    public final int recordEventFinishers;
    public final int recordEventNumber;
    public final Date recordEventDate;

    public int recentAttendanceDelta = 0;
    public int maxAttendanceDelta = 0;


    public AttendanceRecord(int courseId,
                            int recentEventNumber, Date recentEventDate, int recentEventFinishers,
                            int recordEventNumber, Date recordEventDate, int recordEventFinishers)
    {
        this.courseId = courseId;
        this.recentEventNumber = recentEventNumber;
        this.recentEventFinishers = recentEventFinishers;
        this.recentEventDate = recentEventDate;
        this.recordEventNumber = recordEventNumber;
        this.recordEventFinishers = recordEventFinishers;
        this.recordEventDate = recordEventDate;
    }

    @Override
    public String toString()
    {
        return "AttendanceRecord{" +
                "courseId=" + courseId +
                ", courseSmallTest='" + courseSmallTest + '\'' +
                ", recentEventFinishers=" + recentEventFinishers +
                ", recentEventNumber=" + recentEventNumber +
                ", recentEventDate=" + recentEventDate +
                ", recordEventFinishers=" + recordEventFinishers +
                ", recordEventNumber=" + recordEventNumber +
                ", recordEventDate=" + recordEventDate +
                ", recentAttendanceDelta=" + recentAttendanceDelta +
                ", maxAttendanceDelta=" + maxAttendanceDelta +
                '}';
    }

}
