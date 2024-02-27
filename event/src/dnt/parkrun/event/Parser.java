package dnt.parkrun.event;

import dnt.parkrun.datastructures.Athlete;
import dnt.parkrun.datastructures.CourseEventSummary;
import dnt.parkrun.datastructures.Result;
import dnt.parkrun.datastructures.Time;
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
    private final Consumer<Result> consumer;

    public Parser(Document doc, Consumer<Result> consumer)
    {
        this.doc = doc;
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
                Node positionNode = row.childNode(0).childNode(0);
                int position = Integer.parseInt(String.valueOf(positionNode));

                Node athleteAtEventLink = row
                        .childNode(1)   // td
                        .childNode(0)  // a
                        .childNode(0);  // div
                Athlete athlete = Athlete.fromEventLink(athleteAtEventLink.attr("href"));

                Node timeDiv = row
                        .childNode(5)   // td
                        .childNode(0);   // div compact
                if(!timeDiv.childNodes().isEmpty())
                {
                    Node timeNode = row
                            .childNode(5)   // td
                            .childNode(0)   // div compact
                            .childNode(0);  // value
                    Time time = Time.fromString(timeNode.toString());
                    consumer.accept(new Result(position, athlete, time));
                }
                else
                {
                    consumer.accept(new Result(position, athlete, Time.NO_TIME));
                }
            }
        }
    }

    public static class Builder
    {
        private Document doc;
        private Consumer<Result> resultConsumer = r -> {};

        public Parser build() throws IOException
        {
            return new Parser(doc, resultConsumer);
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

        public Builder forEachResult(Consumer<Result> resultConsumer)
        {
            this.resultConsumer = resultConsumer;
            return this;
        }
    }
}
