package dnt.parkrun.htmlwriter;

import dnt.parkrun.datastructures.Athlete;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.Closeable;

import static dnt.parkrun.common.UrlGenerator.generateAthleteEventSummaryUrl;

public class PIndexTableHtmlWriter extends BaseWriter implements Closeable
{
    public PIndexTableHtmlWriter(XMLStreamWriter writer) throws XMLStreamException
    {
        super(writer);

        startElement("details");
        startElement("summary");
        writer.writeCharacters("(WORK IN PROGRESS) P-Index (New Zealand)");
        endElement("summary");

        startElement("table", "class", "sortable name-data");
        writeHeader(writer);
    }

    private void writeHeader(XMLStreamWriter writer) throws XMLStreamException
    {
        startElement("thead");
        startElement("tr");

        // Name
        startElement("th");
        writer.writeCharacters("Name");
        endElement("th");

        startElement("th");
        writer.writeCharacters("P-Index");
        endElement("th");

        startElement("th");
        writer.writeCharacters("Next max");
        endElement("th");

        endElement("tr");
        endElement("thead");
    }

    @Override
    public void close()
    {
        try
        {
            endElement("table");

            startElement("center");
            startElement("p");
            writer.writeCharacters("* Only contains parkrunners with pIndex of 5 within NZ parkruns.");
            endElement("p");
            endElement("center");

            endElement("details");

        }
        catch (XMLStreamException e)
        {
            throw new RuntimeException(e);
        }
    }

    public void writePIndexRecord(Record record) throws XMLStreamException
    {
       writer.writeStartElement("tr");

        // Name
        startElement("td");
        writer.writeStartElement("a");
        writer.writeAttribute("href", generateAthleteEventSummaryUrl("parkrun.co.nz", record.athlete.athleteId).toString());
        writer.writeAttribute("target", String.valueOf(record.athlete.name));
        writer.writeCharacters(record.athlete.name);
        endElement("a");
        endElement("td");

        // P-Index
        startElement("td");
        writer.writeCharacters(String.valueOf(record.pIndex));
        endElement("td");

        // Next max
        startElement("td");
        writer.writeCharacters(String.valueOf(record.nextMax));
        endElement("td");

        endElement("tr");
    }

    public static class Record
    {
        public final Athlete athlete;
        public final int pIndex;
        public final int nextMax;

        public Record(Athlete athlete, int pIndex, int nextMax)
        {
            this.athlete = athlete;
            this.pIndex = pIndex;
            this.nextMax = nextMax;
        }

        @Override
        public String toString()
        {
            return "Record{" +
                    "athlete=" + athlete +
                    ", pIndex=" + pIndex +
                    ", nextMax=" + nextMax +
                    '}';
        }
    }
}
