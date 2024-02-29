package dnt.parkrun.courseeventsummary;

import dnt.parkrun.datastructures.Athlete;
import dnt.parkrun.datastructures.Course;
import dnt.parkrun.datastructures.CourseEventSummary;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

public class Parser
{
    private static final SimpleDateFormat WEBSITE_DATE_PARSER = new SimpleDateFormat("dd/MM/yyyy");

    private final Document doc;
    private final Course course;
    private final Consumer<CourseEventSummary> consumer;

    private Parser(Document doc, Course course, Consumer<CourseEventSummary> consumer)
    {
        this.doc = doc;
        this.course = course;
        this.consumer = consumer;
    }

    public void parse()
    {
        Elements tableElements = doc.getElementsByClass("Results-table");

        Element firstTable = tableElements.get(0);

        List<Node> firstTableRows = firstTable.childNodes().get(1).childNodes();
        int numRows = firstTableRows.size();

        for (int i = 0; i < numRows; i++)
        {
            Node row = firstTableRows.get(i);
            if (row instanceof Element)
            {
                Node eventNumber = row.childNode(0).childNode(0).childNode(0);
//                System.out.print(position);
//                System.out.print("\t");

                Node dateNode = row.childNode(1).childNode(0).childNode(0).childNode(0).childNode(0);
//                System.out.print(date);
//                System.out.print("\t");
                Date date = parseWebsiteDate(dateNode.toString());

                Athlete maleFirstFinisher = null;
                try
                {
                    String firstMaleLink = row
                            .childNode(1) // td
                            .childNode(1) // div details
                            .childNode(0) // div 2 (male)
                            .childNode(0) // a
                            .attr("href");
                    Node firstMaleName = row
                            .childNode(1) // td
                            .childNode(1) // div details
                            .childNode(0) // div 2 (male)
                            .childNode(0) // a
                            .childNode(0); // a value;
                    maleFirstFinisher = Athlete.fromAthleteHistoryAtEventLink(firstMaleName.toString(), firstMaleLink);
                }
                catch (Exception ex)
                {
                    System.out.printf("WARN: No first male finisher. Course: %s, Event: %s\n", course, eventNumber);
                }
//                System.out.print(maleFirstFinisher);
//                System.out.print("\t");

                Athlete femaleFirstFinisher = null;
                try
                {
                    String firstFemaleLink = row
                            .childNode(1) // td
                            .childNode(1) // div details
                            .childNode(1) // div 2 (female)
                            .childNode(0) // a
                            .attr("href");
                    Node firstFemaleName = row
                            .childNode(1) // td
                            .childNode(1) // div details
                            .childNode(1) // div 2 (female)
                            .childNode(0) // a
                            .childNode(0); // a value;
                    femaleFirstFinisher = Athlete.fromAthleteHistoryAtEventLink(firstFemaleName.toString(), firstFemaleLink);
                }
                catch(Exception ex)
                {
                    System.out.printf("WARN: No first female finisher. Course: %s, Event: %s\n", course, eventNumber);
                }
//                System.out.print(femaleFirstFinisher);
//                System.out.print("\t");

//                System.out.println();

                CourseEventSummary eventSummary = new CourseEventSummary(
                        course,
                        Integer.parseInt(eventNumber.toString()),
                        date,
                        maleFirstFinisher,
                        femaleFirstFinisher);
                consumer.accept(eventSummary);
            }
        }
    }

    private static Date parseWebsiteDate(String date)
    {
        try
        {
            return WEBSITE_DATE_PARSER.parse(date);
        }
        catch (ParseException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static class Builder
    {
        private Document doc;
        private Consumer<CourseEventSummary> consumer = ehr -> {};
        private Course course;

        public Parser build() throws IOException
        {
            return new Parser(doc, course, consumer);
        }

        public Builder url(URL url) throws IOException
        {
            this.doc = Jsoup.parse(url, 5000);
            return this;
        }

        public Builder file(File file) throws IOException
        {
            this.doc = Jsoup.parse(file);
            return this;
        }

        public Builder forEachCourseEvent(Consumer<CourseEventSummary> eventHistoryRecordConsumer)
        {
            this.consumer = eventHistoryRecordConsumer;
            return this;
        }

        public Builder course(Course course)
        {
            this.course = course;
            return this;
        }
    }
}
