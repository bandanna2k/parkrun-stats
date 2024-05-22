package dnt.parkrun.athletecourseevents;


import dnt.jsoupwrapper.JsoupWrapper;
import dnt.parkrun.common.DateConverter;
import dnt.parkrun.datastructures.AgeCategory;
import dnt.parkrun.datastructures.Athlete;
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
import java.util.function.Consumer;

import static dnt.parkrun.common.UrlExtractor.extractCourseFromUrl;

public class Parser
{
    private final Document doc;
    private final Consumer<AthleteCourseEvent> consumer;
    private final Consumer<AgeCategory> consumerOfAgeCategory;

    private Parser(Document doc, Consumer<AthleteCourseEvent> consumer, Consumer<AgeCategory> consumerAgeCategory)
    {
        this.doc = doc;
        this.consumer = consumer;
        this.consumerOfAgeCategory = consumerAgeCategory;
    }

    public void parse()
    {
        Elements nameElements = doc.getElementsByTag("h2");
        String name = extractName(nameElements.text());
        int athleteId = extractAthleteId(nameElements.text());

        consumerOfAgeCategory.accept(getAgeCategory());

        Elements tableElements = doc.getElementsByTag("table");
        Element table = tableElements.get(2);

        List<Node> tableRows = table.childNodes().get(2).childNodes();
        int numRows = tableRows.size();

        for (int i = 0; i < numRows; i++)
        {
            Node row = tableRows.get(i);
            if (row instanceof Element)
            {
                String courseLink = row
                        .childNode(0)   // td
                        .childNode(0)   // a
                        .attr("href");
                String courseName = extractCourseFromUrl(courseLink);
//                System.out.println(courseName);
//                System.out.println(courseLink);

                Node dateNode = row
                        .childNode(0)   // td
                        .childNode(0)   // a
                        .childNode(0);
                Date date = DateConverter.parseWebsiteDate(dateNode.toString());
//                System.out.println(date);

                Node eventNumberNode = row
                        .childNode(1)   // td
                        .childNode(0)   // a
                        .childNode(0);
                int eventNumber = Integer.parseInt(eventNumberNode.toString());
//                System.out.println(eventNumber);

                Node positionNode = row
                        .childNode(2)   // td
                        .childNode(0);
                int position = Integer.parseInt(positionNode.toString());
//                System.out.println(position);

                Node timeNode = row
                        .childNode(3)   // td
                        .childNode(0);
                Time time = Time.from(timeNode.toString());
//                System.out.println(time);

                Athlete athlete = Athlete.from(name, athleteId);
                consumer.accept(new AthleteCourseEvent(athlete, courseName, date, eventNumber, position, time));
            }
        }
    }

    private AgeCategory getAgeCategory()
    {
        Elements ageCategoryElement = this.doc.select("p:contains(Most recent age category was)");
        try
        {
            String text = ageCategoryElement.text();
            int lastIndex = text.lastIndexOf(" ");
            String ageCategoryString = text.substring(lastIndex + 1);
            return AgeCategory.from(ageCategoryString);
        }
        catch (Exception ex)
        {
            System.out.printf("WARNING: Failed to get age category from text '%s'%n", ageCategoryElement);
            return null;
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
        private final JsoupWrapper jsoupWrapper = new JsoupWrapper.Builder().build();
        private Document doc;
        private Consumer<AthleteCourseEvent> consumer = ace -> {};
        private Consumer<AgeCategory> consumerAgeCategory = ac -> {};

        public Parser build() throws IOException
        {
            return new Parser(doc, consumer, consumerAgeCategory);
        }

        public Builder url(URL url) throws IOException
        {
            this.doc = jsoupWrapper.newDocument(url);
            return this;
        }

        public Builder file(File file) throws IOException
        {
            this.doc = jsoupWrapper.newDocument(file);
            return this;
        }

        public Builder onAgeCategory(Consumer<AgeCategory> consumerAgeCategory)
        {
            this.consumerAgeCategory = consumerAgeCategory;
            return this;
        }

        public Builder forEachAthleteCourseEvent(Consumer<AthleteCourseEvent> consumer)
        {
            this.consumer = consumer;
            return this;
        }
    }
}