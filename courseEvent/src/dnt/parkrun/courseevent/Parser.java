package dnt.parkrun.courseevent;

import dnt.jsoupwrapper.JsoupWrapper;
import dnt.parkrun.common.DateConverter;
import dnt.parkrun.datastructures.Athlete;
import dnt.parkrun.datastructures.Course;
import dnt.parkrun.datastructures.Result;
import dnt.parkrun.datastructures.Time;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class Parser
{
    private final Document doc;
    private final Course course;
    private final Consumer<Athlete> athleteConsumer;
    private final Consumer<Result> resultConsumer;

    public Parser(Document doc,
                  Course course,
                  Consumer<Athlete> athleteConsumer,
                  Consumer<Result> resultConsumer)
    {
        this.doc = doc;
        this.course = course;
        this.athleteConsumer = athleteConsumer;
        this.resultConsumer = resultConsumer;
    }

    public void parse()
    {
        Elements resultsHeader = doc.getElementsByClass("Results-header");
        Node eventNumberNode = resultsHeader.get(0)
                .childNode(1)   // h3
                .childNode(2)   // span
                .childNode(0);
        int eventNumber = Integer.parseInt(eventNumberNode.toString().replace("#", ""));

        Node dateNode = resultsHeader.get(0)
                .childNode(1)   // h3
                .childNode(0)   // span
                .childNode(0);
        Date date = DateConverter.parseWebsiteDate(dateNode.toString());

        Elements tableElements = doc.getElementsByClass("Results-table");

        Element firstTable = tableElements.get(0);

        List<Node> firstTableRows = firstTable.childNodes().get(1).childNodes();
        int numRows = firstTableRows.size();

        for (int i = 0; i < numRows; i++)
        {
            Node row = firstTableRows.get(i);
            if (row instanceof Element)
            {
                Node positionNode = row.childNode(0).childNode(0);
                int position = Integer.parseInt(String.valueOf(positionNode));

                String name = row.attr("data-name");
                Node athleteAtEventLink = row
                        .childNode(1)   // td
                        .childNode(0)  // a
                        .childNode(0);  // div
                Athlete athlete = Athlete.fromAthleteAtCourseLink(name, athleteAtEventLink.attr("href"));
                athleteConsumer.accept(athlete);

                Node timeDiv = row
                        .childNode(5)   // td
                        .childNode(0);   // div compact

                if(!timeDiv.childNodes().isEmpty())
                {
                    Node timeNode = row
                            .childNode(5)   // td
                            .childNode(0)   // div compact
                            .childNode(0);  //  value
                    Time time = Time.from(timeNode.toString());
                    resultConsumer.accept(new Result(course.courseId, date, position, athlete, time));
                }
                else
                {
                    resultConsumer.accept(new Result(course.courseId, date, position, athlete, Time.NO_TIME));
                }
            }
        }

        Elements thanksToTheVolunteers = doc.getElementsMatchingText("Thanks to the volunteers");
        Elements parents = thanksToTheVolunteers.parents();

        AtomicInteger counter = new AtomicInteger();
        List<Node> volunteers = parents.last().childNode(1).childNodes();
        volunteers.forEach(volunteerNode -> {
            Node volunteerAthleteNode = volunteerNode.firstChild();
            if(volunteerAthleteNode != null)
            {
                Athlete athlete = Athlete.fromAthleteAtCourseLink(volunteerAthleteNode.toString(), volunteerNode.toString());
                counter.incrementAndGet();
                System.out.println(athlete);
            }
        });
        System.out.println(counter);
    }

    public static class Builder
    {
        private Document doc;
        private Consumer<Athlete> athleteConsumer = r -> {};
        private Consumer<Result> resultConsumer = r -> {};
        private Course course;

        public Parser build()
        {
            return new Parser(doc, course, athleteConsumer, resultConsumer);
        }

        public Builder url(URL url) throws IOException
        {
            this.doc = JsoupWrapper.newDocument(url);
            return this;
        }

        public Builder file(File file)
        {
            this.doc = JsoupWrapper.newDocument(file);
            return this;
        }

        public Builder forEachAthlete(Consumer<Athlete> consumer)
        {
            this.athleteConsumer = consumer;
            return this;
        }

        public Builder forEachResult(Consumer<Result> consumer)
        {
            this.resultConsumer = consumer;
            return this;
        }

        public Builder course(Course course)
        {
            this.course = course;
            return this;
        }
    }
}
