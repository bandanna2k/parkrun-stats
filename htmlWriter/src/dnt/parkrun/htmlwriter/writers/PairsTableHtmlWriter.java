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

    public void writeRecord(Athlete rowAthlete, List<Integer> colAthletes) throws XMLStreamException
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
            startElement("td");
            writer.writeCharacters(String.valueOf(colAthlete));
            endElement("td");
        }

        endElement("tr");
    }

    public void writeHeaderRecord(Athlete rowAthlete, List<Athlete> colAthletes) throws XMLStreamException
    {
        writer.writeStartElement("tr");

        startElement("td");
        endElement("td");

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
            startElement("td", "style", "writing-mode: vertical-rl; transform:scale(-1);");
            writer.writeStartElement("a");
            writer.writeAttribute("href", urlGenerator.generateAthleteUrl(colAthlete.athleteId).toString());
            writer.writeAttribute("target", String.valueOf(colAthlete.athleteId));
            writer.writeCharacters(colAthlete.name);
            endElement("a");
            endElement("td");
        }

        endElement("tr");
    }
}
