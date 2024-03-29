package dnt.parkrun.datastructures;

import java.util.Objects;

public class Course
{
    public static final int NO_COURSE_ID = Integer.MIN_VALUE;

    public final String name;
    public final Country country;
    public final String longName;
    public final Status status;
    public final int courseId;

    public Course(int courseId, String name, Country country, String longName, Status status)
    {
        this.courseId = courseId;
        this.name = name;
        this.country = country;
        this.longName = longName;
        this.status = status;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        Course course = (Course) o;
        return courseId == course.courseId && Objects.equals(name, course.name) && Objects.equals(country, course.country) && Objects.equals(longName, course.longName) && status == course.status;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(name, country, longName, status, courseId);
    }

    public String getStatusDbCode()
    {
        return status.getStatusForDb();
    }

    public enum Status
    {
        PENDING("P"),
        RUNNING("R"),
        STOPPED("S");

        private final String dbCode;

        Status(String dbCode)
        {
            this.dbCode = dbCode;
        }

        public static Status fromDb(String status)
        {
            if(status.equals(PENDING.dbCode)) return PENDING;
            if(status.equals(RUNNING.dbCode)) return RUNNING;
            if(status.equals(STOPPED.dbCode)) return STOPPED;
            return null;
        }

        public String getStatusForDb()
        {
            return dbCode;
        }
    }

    public static class Builder
    {
        private Country country;
        private String name;
        private String longName;
        private Status status;
        private int courseId;

        public Course build()
        {
            return new Course(courseId, name, country, longName, status);
        }

        public Builder name(String name)
        {
            this.name = name;
            return this;
        }

        public Builder longName(String longName)
        {
            this.longName = longName;
            return this;
        }

        public Builder country(Country country)
        {
            this.country = country;
            return this;
        }

        public Builder status(Status status)
        {
            this.status = status;
            return this;
        }
    }

    @Override
    public String toString()
    {
        return "Course{" +
                "name='" + name + '\'' +
                ", country=" + country +
                ", longName='" + longName + '\'' +
                ", status=" + status +
                ", courseId=" + courseId +
                '}';
    }
}
