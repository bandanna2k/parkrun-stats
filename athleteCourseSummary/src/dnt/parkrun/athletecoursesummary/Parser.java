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

public class Parser
{
    private final Document doc;
    private final CourseRepository courseRepository;
    private final Consumer<AthleteCourseSummary> consumer;
    private final Consumer<String> courseNotFoundConsumer;

    private Parser(Document doc, CourseRepository courseRepository, Consumer<AthleteCourseSummary> consumer)
    {
        this.doc = doc;
        this.courseRepository = courseRepository;
        this.consumer = consumer;
        this.courseNotFoundConsumer = s -> System.out.println("WARNING Course not found: " + s);
    }

    public void parse()
    {
        Elements nameElements = doc.getElementsByTag("h2");
        String name = extractName(nameElements.text());
        int athleteId = extractAthleteId(nameElements.text());

        Elements summaries = doc.getElementById("event-summary").parents();
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
                String courseLongName = eventNode.toString();

                Node countNode = row
                        .childNode(1)   // td
                        .childNode(0);

                String athleteAtEvent = row.childNode(5)
                        .childNode(0)   // a;
                        .attr("href");


                Course course = courseRepository.getCourseFromLongName(courseLongName);
                if(course == null)
                {
                    courseNotFoundConsumer.accept(courseLongName + " (" + athleteAtEvent + ")");
                    course = new Course(Integer.MIN_VALUE, courseLongName, Country.UNKNOWN, courseLongName, Course.Status.STOPPED);
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
        int start = nameWithId.indexOf("(");
        int end = nameWithId.indexOf(")");
        String athleteId = nameWithId.substring(start + 2/*(A*/, end);
        return Integer.parseInt(athleteId);
    }


    public static class Builder
    {
        private Document doc;
        private Consumer<AthleteCourseSummary> consumer = es -> {};

        public Parser build(CourseRepository courseRepository)
        {
            return new Parser(doc, courseRepository, consumer);
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

        public Builder forEachAthleteCourseSummary(Consumer<AthleteCourseSummary> consumer)
        {
            this.consumer = consumer;
            return this;
        }
    }
}