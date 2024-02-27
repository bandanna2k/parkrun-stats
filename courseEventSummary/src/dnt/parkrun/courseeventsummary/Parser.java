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
import java.util.List;
import java.util.function.Consumer;

public class Parser
{
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

                Node date = row.childNode(1).childNode(0).childNode(0).childNode(0).childNode(0);
//                System.out.print(date);
//                System.out.print("\t");

                String firstMaleLink = row
                        .childNode(1) // td
                        .childNode(1) // div details
                        .childNode(0) // div 2 (male)
                        .childNode(0) // a
                        .attr("href");
                String firstMaleName = "1st man";
                Athlete maleFirstFinisher = Athlete.fromSummaryLink(firstMaleName, firstMaleLink);
//                System.out.print(maleFirstFinisher);
//                System.out.print("\t");

                String firstFemaleLink = row
                        .childNode(1) // td
                        .childNode(1) // div details
                        .childNode(1) // div 2 (female)
                        .childNode(0) // a
                        .attr("href");
                String firstFemaleName = "1st woman";
                Athlete femaleFirstFinisher = Athlete.fromSummaryLink(firstFemaleName, firstFemaleLink);
//                System.out.print(femaleFirstFinisher);
//                System.out.print("\t");

//                System.out.println();

                CourseEventSummary eventSummary = new CourseEventSummary(
                        course,
                        Integer.parseInt(eventNumber.toString()),
                        maleFirstFinisher,
                        femaleFirstFinisher);
                consumer.accept(eventSummary);
            }
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
