package dnt.parkrun.datastructures;

import java.util.Objects;

public class Course
{
    public final String name;
    public final int countryCode;

    public Course(String name, int countryCode)
    {
        this.name = name;
        this.countryCode = countryCode;
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
        return countryCode == course.countryCode && Objects.equals(name, course.name);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(name, countryCode);
    }

    public static class Builder
    {

        private int countryCode;
        private String name;
        public Course build()
        {
            return new Course(name, countryCode);
        }

        public Builder name(String name)
        {
            this.name = name;
            return this;
        }

        public Builder countryCode(int countryCode)
        {
            this.countryCode = countryCode;
            return this;
        }
    }

    @Override
    public String toString()
    {
        return "Course{" +
                "name='" + name + '\'' +
                ", countryCode=" + countryCode +
                '}';
    }
}
