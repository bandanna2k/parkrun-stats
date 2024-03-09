package dnt.parkrun.datastructures.stats;

import java.sql.Date;

public class AttendanceRecord
{
    public final String courseLongName;
    public final String courseName;
    public final int recentAttendance;
    public final Date recentDate;
    public final int maxAttendance;
    public final Date maxDate;
    public int recentAttendanceDelta = 0;
    public int maxAttendanceDelta = 0;


    public AttendanceRecord(String courseLongName, String courseName,
                            Date recentDate, int recentAttendance,
                            Date maxDate, int maxAttendance)
    {
        this.courseLongName = courseLongName;
        this.courseName = courseName;
        this.recentAttendance = recentAttendance;
        this.recentDate = recentDate;
        this.maxAttendance = maxAttendance;
        this.maxDate = maxDate;
    }

    @Override
    public String toString()
    {
        return "AttendanceRecord{" +
                "courseLongName='" + courseLongName + '\'' +
                ", courseName='" + courseName + '\'' +
                ", recentAttendance=" + recentAttendance +
                ", recentDate=" + recentDate +
                ", maxAttendance=" + maxAttendance +
                ", maxDate=" + maxDate +
                ", recentAttendanceDelta=" + recentAttendanceDelta +
                ", maxAttendanceDelta=" + maxAttendanceDelta +
                '}';
    }
}
