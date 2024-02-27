package dnt.parkrun.datastructures;

import java.net.URL;

public class Course
{
    public final String name;
    public final int countryCode;
    public final URL baseUrl;

    public Course(String name, int countryCode, URL baseUrl)
    {
        this.name = name;
        this.countryCode = countryCode;
        this.baseUrl = baseUrl;
    }

    @Override
    public String toString()
    {
        return "Course{" +
                "name='" + name + '\'' +
                ", countryCode=" + countryCode +
                ", baseUrl=" + baseUrl +
                '}';
    }

    public static class Builder
    {
        private int countryCode;
        private String name;
        private URL baseUrl;

        public Course build()
        {
            return new Course(name, countryCode, baseUrl);
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

        public Builder baseUrl(URL baseUrl)
        {
            this.baseUrl = baseUrl;
            return this;
        }
    }
}
