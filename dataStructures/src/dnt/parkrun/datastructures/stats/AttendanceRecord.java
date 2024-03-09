package dnt.parkrun.datastructures.stats;

import dnt.parkrun.common.DateConverter;

import java.sql.Date;

public class AttendanceRecord
{
    public final String courseLongName;
    public final String courseName;
    public final String recentAttendance;
    public final String recentDate;
    public final String maxAttendance;
    public final String maxDate;
    public int recentAttendanceDelta = 0;

    public AttendanceRecord(String courseLongName, String courseName,
                            Date recentDate, String recentAttendance,
                            Date maxDate, String maxAttendance)
    {
        this(courseLongName, courseName,
                DateConverter.formatDateForHtml(recentDate), recentAttendance,
                DateConverter.formatDateForHtml(maxDate), maxAttendance);
    }

    public AttendanceRecord(String courseLongName, String courseName,
                            String recentDate, String recentAttendance,
                            String maxDate, String maxAttendance)
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
                ", recentAttendance='" + recentAttendance + '\'' +
                ", recentDate='" + recentDate + '\'' +
                ", maxAttendance='" + maxAttendance + '\'' +
                ", maxDate='" + maxDate + '\'' +
                ", recentAttendanceDelta=" + recentAttendanceDelta +
                '}';
    }
}
