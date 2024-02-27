package dnt.parkrun.mostevents;

import dnt.parkrun.courseeventsummary.Parser;
import dnt.parkrun.courses.Country;
import dnt.parkrun.courses.reader.EventsJsonFileReader;
import dnt.parkrun.datastructures.Athlete;
import dnt.parkrun.datastructures.Course;
import dnt.parkrun.datastructures.CourseEventSummary;
import dnt.parkrun.datastructures.Result;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class MostEvents
{
    private final UrlGenerator urlGenerator;
    private final Map<Long, Record> athleteIdToMostEventRecord = new HashMap<>();

    public MostEvents()
    {
        this.urlGenerator = new UrlGenerator();
    }

    public void collectMostEventRecords() throws IOException
    {
        System.out.println("* Collecting *");
        Map<String, Course> courseNameToCourse = new HashMap<>();
        Map<Integer, Country> countryCodeToCountry = new HashMap<>();
        InputStream inputStream = Course.class.getResourceAsStream("/events.json");
        dnt.parkrun.courses.reader.EventsJsonFileReader reader = new EventsJsonFileReader.Builder(() -> inputStream)
                .forEachCountry(c -> countryCodeToCountry.put(c.countryCode, c))
                .forEachCourse(c -> courseNameToCourse.put(c.name, c))
                .build();
        reader.read();

        System.out.println("* Filter courses *");
        Set<String> coursesToScan = new HashSet<>() {{ add("cornwall"); add("lowerhutt"); }};
        courseNameToCourse.keySet().removeIf(c -> !coursesToScan.contains(c));
        assert courseNameToCourse.size() == 2;

        System.out.println("* Get course summaries *");
        List<CourseEventSummary> courseEventSummaries = new ArrayList<>();
        for (Course course : courseNameToCourse.values())
        {
            System.out.printf("* Processing %s *\n", course);

            Country courseCountry = countryCodeToCountry.get(course.countryCode);
            Parser courseEventSummaryParser = new Parser.Builder()
                    .course(course)
                    .url(urlGenerator.generateCourseEventSummaryUrl(courseCountry.url, course.name))
                    .forEachCourseEvent(c -> courseEventSummaries.add(c))
                    .build();
            courseEventSummaryParser.parse();
        }

        System.out.println("* Get all course results (currently just 2) *");
        courseEventSummaries.removeIf(ces -> ces.eventNumber > 2);
        assert courseEventSummaries.size() == 2;

        System.out.println("* Get all results by all athletes *");
        for (CourseEventSummary ces : courseEventSummaries)
        {
            System.out.printf("* Processing %s *\n", ces);

            Country courseCountry = countryCodeToCountry.get(ces.course.countryCode);
            dnt.parkrun.courseevent.Parser parser = new dnt.parkrun.courseevent.Parser.Builder()
                    .courseName(ces.course.name)
                    .url(urlGenerator.generateCourseEventUrl(courseCountry.url, ces.course.name, ces.eventNumber))
                    .forEachResult(r -> processResult(r))
                    .build();
            parser.parse();
        }

        athleteIdToMostEventRecord.forEach((athleteId, record) -> {
            System.out.println(record);
        });
    }

    private void processResult(Result result)
    {
        athleteIdToMostEventRecord.compute(result.athlete.athleteId, (athleteId, record) ->
        {
            if(record == null)
            {
                record = new Record(result.athlete);
            }
            record.incrementTotalEventsCount();
            record.incrementDifferentEventsCount(result.courseName);
            return record;
        });
    }

    private class Record
    {
        private final Athlete athlete;
        private int totalEventsCount = 0;
        private Map<String, Integer> courseNameToCount = new HashMap<>();

        private Record(Athlete athlete)
        {
            this.athlete = athlete;
            this.totalEventsCount = totalEventsCount;
        }

        public int getTotalEventsCount()
        {
            return totalEventsCount;
        }

        public Athlete getAthlete()
        {
            return athlete;
        }

        public Record incrementTotalEventsCount()
        {
            totalEventsCount++;
            return this;
        }

        public Record incrementDifferentEventsCount(String courseName)
        {
            courseNameToCount.compute(courseName, (courseName1, count) -> count == null ? 1 : count + 1);
            return this;
        }

        @Override
        public String toString()
        {
            return "Record{" +
                    "athlete=" + athlete +
                    ", totalEventsCount=" + totalEventsCount +
                    ", courseNameToCount=" + courseNameToCount +
                    '}';
        }
    }
}
