package dnt.parkrun.htmlwriter.writers;

import dnt.parkrun.common.UrlGenerator;
import dnt.parkrun.datastructures.Athlete;
import dnt.parkrun.htmlwriter.BaseWriter;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.Closeable;
import java.util.List;

public class PairsTableHtmlWriter extends BaseWriter implements Closeable
{
    private final UrlGenerator urlGenerator;

    public PairsTableHtmlWriter(XMLStreamWriter writer, UrlGenerator urlGenerator)
            throws XMLStreamException
    {
        super(writer);
        this.urlGenerator = urlGenerator;

        writer.writeStartElement("table");
    }

    @Override
    public void close()
    {
        try
        {
            endElement("table");
        }
        catch (XMLStreamException e)
        {
            throw new RuntimeException(e);
        }
    }

    public void writeRecord(Athlete rowAthlete,
                            int max,
                            List<Object[]> runsAndFriendshipKeys) throws XMLStreamException
    {
        writer.writeStartElement("tr");

        // Athlete
        startElement("td");
        writer.writeStartElement("a");
        writer.writeAttribute("href", urlGenerator.generateAthleteUrl(rowAthlete.athleteId).toString());
        writer.writeAttribute("target", String.valueOf(rowAthlete.athleteId));
        writer.writeCharacters(rowAthlete.name);
        endElement("a");
        endElement("td");

        for (Object[] runsAndFriendshipKey : runsAndFriendshipKeys)
        {
            int runs = (int)runsAndFriendshipKey[0];
            double percentage = (double)100 * ((double)runs / (double)max);
            if(percentage == 0) startElement("td", "class", "td_zero");
            else if(percentage < 1) startElement("td", "class", "td0");
            else if(percentage < 2) startElement("td", "class", "td1");
            else if(percentage < 4) startElement("td", "class", "td2");
            else if(percentage < 6) startElement("td", "class", "td3");
            else if(percentage < 9) startElement("td", "class", "td4");
            else if(percentage < 13) startElement("td", "class", "td5");
            else if(percentage < 25) startElement("td", "class", "td6");
            else if(percentage < 50) startElement("td", "class", "td7");
            else if(percentage < 75) startElement("td", "class", "td8");
            else                     startElement("td", "class", "td9");

            startElement("span", "class", "click-me",
                    "onclick", String.format("update('%s')", runsAndFriendshipKey[1]));
            writer.writeCharacters(String.valueOf(runs));
            endElement("span");
        }

        endElement("tr");
    }

    public void writeHeaderRecord(Athlete rowAthlete, List<Athlete> colAthletes) throws XMLStreamException
    {
//        startElement("colgroup");
//        for (int i = 0; i < colAthletes.size(); i++)
//        {
//            if((i % 2) == 0)
//            {
//                startElement("colgroup");
//                endElement("colgroup");
//            }
//            else
//            {
//                startElement("colgroup", "class", "colHighlight");
//                endElement("colgroup");
//            }
//        }
//        endElement("colgroup");
//
        writer.writeStartElement("thead");

        startElement("th");
        endElement("th");

        // Athlete
//        startElement("td");
//        writer.writeStartElement("a");
//        writer.writeAttribute("href", urlGenerator.generateAthleteUrl(rowAthlete.athleteId).toString());
//        writer.writeAttribute("target", String.valueOf(rowAthlete.athleteId));
//        writer.writeCharacters(rowAthlete.name);
//        endElement("a");
//        endElement("td");

        for (Athlete colAthlete : colAthletes)
        {
//            startElement("th", "style", "writing-mode: vertical-rl; transform:scale(-1);");
            startElement("th");
            writer.writeStartElement("a");
            writer.writeAttribute("href", urlGenerator.generateAthleteUrl(colAthlete.athleteId).toString());
            writer.writeAttribute("target", String.valueOf(colAthlete.athleteId));
            writer.writeCharacters(colAthlete.name);
            endElement("a");
            endElement("th");
        }

        endElement("thead");
    }
}
