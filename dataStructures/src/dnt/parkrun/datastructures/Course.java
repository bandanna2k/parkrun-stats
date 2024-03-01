package dnt.parkrun.datastructures;

import java.util.Objects;

public class Course
{
    public final String name;
    public final Country country;
    public final String longName;
    private final Status status;

    public Course(String name, Country country, String longName, Status status)
    {
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
        return country == course.country && Objects.equals(name, course.name);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(name, country);
    }

    public String getStatusDbCode()
    {
        return status.getStatusForDb();
    }

    public enum Status
    {
        RUNNING("R"),
        STOPPED("S");

        private final String dbCode;

        Status(String dbCode)
        {
            this.dbCode = dbCode;
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

        public Course build()
        {
            return new Course(name, country, longName, status);
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
                '}';
    }
}
