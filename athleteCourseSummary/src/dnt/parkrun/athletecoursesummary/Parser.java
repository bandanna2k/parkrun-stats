package dnt.parkrun.athletecoursesummary;


import dnt.jsoupwrapper.JsoupWrapper;
import dnt.parkrun.datastructures.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.function.Consumer;

import static dnt.parkrun.common.UrlExtractor.extractCourseFromUrl;

public class Parser
{
    private final Document doc;
    private final CourseRepository courseRepository;
    private final Consumer<AthleteCourseSummary> consumer;
    private final Consumer<Course> courseNotFoundConsumer;
    private Athlete athlete;

    private Parser(Document doc,
                   CourseRepository courseRepository,
                   Consumer<AthleteCourseSummary> consumer,
                   Consumer<Course> courseNotFoundConsumer)
    {
        this.doc = doc;
        this.courseRepository = courseRepository;
        this.consumer = consumer;
        this.courseNotFoundConsumer = courseNotFoundConsumer;
    }

    public void parse()
    {
        Elements nameElements = doc.getElementsByTag("h2");
        String name = extractName(nameElements.text());
        int athleteId = extractAthleteId(nameElements.text());
        athlete = Athlete.from(name, athleteId);

        Element eventSummary = doc.getElementById("event-summary");
        if(eventSummary == null)
        {
            System.out.println("WARNING No event summary. Probable volunteer only.");
            return;
        }

        Elements summaries = eventSummary.parents();
        Elements tableElements = summaries.select("table");

        Element firstTable = tableElements.get(0);

        List<Node> firstTableRows = firstTable.childNodes().get(1).childNodes();
        int numRows = firstTableRows.size();

        for (int i = 0; i < numRows; i++)
        {
            Node row = firstTableRows.get(i);
            if (row instanceof Element)
            {
                Node eventNode = row
                        .childNode(0)   // td
                        .childNode(0)   // a
                        .childNode(0);
                String courseLongName = org.jsoup.parser.Parser.unescapeEntities(eventNode.toString(), false);

                Node countNode = row
                        .childNode(1)   // td
                        .childNode(0);

                String athleteAtEvent = row.childNode(5)
                        .childNode(0)   // a;
                        .attr("href");

                Course course = courseRepository.getCourseFromLongName(courseLongName);
                if(course == null)
                {
                    Course courseNotFound = new Course(
                            Course.NO_COURSE_ID,
                            extractCourseFromUrl(athleteAtEvent),
                            Country.findFromUrl(athleteAtEvent),
                            courseLongName,
                            Course.Status.STOPPED);
                    courseNotFoundConsumer.accept(courseNotFound);
                    //course = new Course(Integer.MIN_VALUE, courseLongName, Country.UNKNOWN, courseLongName, Course.Status.STOPPED);
                }
                Athlete athlete = Athlete.from(name, athleteId);
                int countOfRuns = Integer.parseInt(countNode.toString());
                AthleteCourseSummary acs = new AthleteCourseSummary(athlete, course, countOfRuns);
                consumer.accept(acs);
            }
        }
    }

    private static String extractName(String nameWithId)
    {
        int indexOf = nameWithId.indexOf("(");
        String nameUntrimmed = nameWithId.substring(0, indexOf);
        return nameUntrimmed.trim();
    }

    private static int extractAthleteId(String nameWithId)
    {
        int start = nameWithId.lastIndexOf("(");
        int end = nameWithId.indexOf(")", start);
        String athleteId = nameWithId.substring(start + 2/*(A*/, end);
        return Integer.parseInt(athleteId);
    }

    public Athlete getAthlete()
    {
        return athlete;
    }

    public static class Builder
    {
        private Document doc;
        private Consumer<AthleteCourseSummary> consumer = es -> {};
        private Consumer<Course> courseNotFoundConsumer = s -> System.out.println("WARNING Course not found: " + s);

        public Parser build(CourseRepository courseRepository)
        {
            return new Parser(doc, courseRepository, consumer, courseNotFoundConsumer);
        }

        public Builder url(URL url)
        {
            this.doc = JsoupWrapper.newDocument(url);
            return this;
        }

        public Builder file(File file) throws IOException
        {
            this.doc = JsoupWrapper.newDocument(file);
            return this;
        }

        public Builder courseNotFound(Consumer<Course> consumer)
        {
            this.courseNotFoundConsumer = consumer;
            return this;
        }

        public Builder forEachAthleteCourseSummary(Consumer<AthleteCourseSummary> consumer)
        {
            this.consumer = consumer;
            return this;
        }
    }
}