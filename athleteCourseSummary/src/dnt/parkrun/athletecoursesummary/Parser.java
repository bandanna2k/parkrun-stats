package dnt.parkrun.athletecoursesummary;


import dnt.parkrun.datastructures.AthleteCourseSummary;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.function.Consumer;

public class Parser
{
    private final Document doc;
    private final Consumer<AthleteCourseSummary> consumer;

    private Parser(Document doc, Consumer<AthleteCourseSummary> consumer)
    {
        this.doc = doc;
        this.consumer = consumer;
    }

    public void parse() throws MalformedURLException
    {
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

                Node countNode = row
                        .childNode(1)   // td
                        .childNode(0);

                String athleteAtEvent = row.childNode(5)
                        .childNode(0)   // a;
                        .attr("href");

                consumer.accept(new AthleteCourseSummary(eventNode.toString(), Integer.parseInt(countNode.toString()), new URL(athleteAtEvent)));
            }
        }
    }


    public static class Builder
    {
        private Document doc;
        private Consumer<AthleteCourseSummary> consumer = es -> {};

        public Parser build() throws IOException
        {
            return new Parser(doc, consumer);
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

        public Builder forEachAthleteCourseSummary(Consumer<AthleteCourseSummary> consumer)
        {
            this.consumer = consumer;
            return this;
        }
    }
}