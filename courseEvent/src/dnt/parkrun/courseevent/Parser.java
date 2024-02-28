package dnt.parkrun.courseevent;

import dnt.parkrun.datastructures.Athlete;
import dnt.parkrun.datastructures.Result;
import dnt.parkrun.datastructures.Time;
import org.jsoup.Connection;
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
    private final String courseName;
    private final Consumer<Athlete> athleteConsumer;
    private final Consumer<Result> resultConsumer;

    public Parser(Document doc,
                  String courseName,
                  Consumer<Athlete> athleteConsumer,
                  Consumer<Result> resultConsumer)
    {
        this.doc = doc;
        this.courseName = courseName;
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
                            .childNode(0);  // value
                    Time time = Time.fromString(timeNode.toString());
                    resultConsumer.accept(new Result(courseName, eventNumber, position, athlete, time));
                }
                else
                {
                    resultConsumer.accept(new Result(courseName, eventNumber, position, athlete, Time.NO_TIME));
                }
            }
        }
    }

    public static class Builder
    {
        private Document doc;
        private Consumer<Athlete> athleteConsumer = r -> {};
        private Consumer<Result> resultConsumer = r -> {};
        private String courseName;

        public Parser build() throws IOException
        {
            return new Parser(doc, courseName, athleteConsumer, resultConsumer);
        }

        public Builder url(URL url) throws IOException
        {
            Connection connection = Jsoup.connect(url.toString()).followRedirects(false).timeout(5000);
            this.doc = connection.get();
            return this;
        }

        public Builder file(File file) throws IOException
        {
            this.doc = Jsoup.parse(file);
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

        public Builder courseName(String name)
        {
            this.courseName = name;
            return this;
        }
    }
}
