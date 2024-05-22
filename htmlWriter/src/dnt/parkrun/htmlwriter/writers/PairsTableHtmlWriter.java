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
//        writer.writeAttribute("class", "sortable bold1 left1 hl2 hl3 hl6 hl7 right6 left7 tinypadding6 tinypadding7");

        writeHeader(writer);
    }

    private void writeHeader(XMLStreamWriter writer) throws XMLStreamException
    {
        startElement("thead");
        startElement("tr");


        endElement("tr");
        endElement("thead");
    }

    @Override
    public void close()
    {
        try
        {
            endElement("table");

//            startElement("center");
//            startElement("p");
//            writer.writeCharacters("* Hover over last event finisher to see weekly movement.");
//            endElement("p");
//            startElement("p");
//            writer.writeCharacters("* Hover over record attendance arrows to see difference since last record.");
//            endElement("p");
//            endElement("center");
        }
        catch (XMLStreamException e)
        {
            throw new RuntimeException(e);
        }
    }

    public void writeRecord(Athlete rowAthlete, int max, List<Integer> colAthletes) throws XMLStreamException
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

        for (Integer colAthlete : colAthletes)
        {
            double percentage = (double)100 * ((double)colAthlete / (double)max);
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

            writer.writeCharacters(String.valueOf(colAthlete));
            endElement("td");
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
            startElement("th", "style", "writing-mode: vertical-rl; transform:scale(-1);");
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
