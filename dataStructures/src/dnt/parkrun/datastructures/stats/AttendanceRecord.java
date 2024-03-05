package dnt.parkrun.datastructures.stats;

import dnt.parkrun.common.DateConverter;

import java.sql.Date;

public class AttendanceRecord
{
    public final String courseLongName;
    public final String courseName;
    public final String maxAttendance;
    public final String recentAttendance;
    public final String date;

    public AttendanceRecord(String courseLongName, String courseName, String maxAttendance, String recentAttendance, Date date)
    {
        this(courseLongName, courseName, maxAttendance, recentAttendance, DateConverter.formatDateForHtml(date));
    }

    public AttendanceRecord(String courseLongName, String courseName, String maxAttendance, String recentAttendance, String date)
    {
        this.courseLongName = courseLongName;
        this.courseName = courseName;
        this.maxAttendance = maxAttendance;
        this.recentAttendance = recentAttendance;
        this.date = date;
    }

    @Override
    public String toString()
    {
        return "AttendanceRecord{" +
                "courseLongName='" + courseLongName + '\'' +
                ", courseName='" + courseName + '\'' +
                ", maxAttendance='" + maxAttendance + '\'' +
                ", recentAttendance='" + recentAttendance + '\'' +
                ", date='" + date + '\'' +
                '}';
    }
}
