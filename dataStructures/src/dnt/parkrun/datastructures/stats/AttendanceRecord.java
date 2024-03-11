package dnt.parkrun.datastructures.stats;

import java.sql.Date;

public class AttendanceRecord
{
    public final String courseLongName;
    public final String courseName;

    public final int recentEventFinishers;
    public final int recentEventNumber;
    public final Date recentEventDate;

    public final int recordEventFinishers;
    public final int recordEventNumber;
    public final Date recordEventDate;

    public int recentAttendanceDelta = 0;
    public int maxAttendanceDelta = 0;


    public AttendanceRecord(String courseLongName, String courseName,
                            int recentEventNumber, Date recentEventDate, int recentEventFinishers,
                            int recordEventNumber, Date recordEventDate, int recordEventFinishers)
    {
        this.courseLongName = courseLongName;
        this.courseName = courseName;
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
                "courseLongName='" + courseLongName + '\'' +
                ", courseName='" + courseName + '\'' +
                ", recentEventNumber=" + recentEventNumber +
                ", recentEventDate=" + recentEventDate +
                ", recentEventFinishers=" + recentEventFinishers +

                ", recordEventNumber=" + recordEventNumber +
                ", recordEventDate=" + recordEventDate +
                ", recordEventFinishers=" + recordEventFinishers +

                ", recentAttendanceDelta=" + recentAttendanceDelta +
                ", maxAttendanceDelta=" + maxAttendanceDelta +
                '}';
    }

}
