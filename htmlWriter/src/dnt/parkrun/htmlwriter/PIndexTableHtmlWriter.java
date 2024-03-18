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
        writer.writeCharacters("Region P-Index");
        endElement("th");

        startElement("th");
        //writer.writeCharacters("Region Next Max");
        endElement("th");

        startElement("th");
        writer.writeCharacters("Global P-Index");
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

        // Region P-Index
        startElement("td");
        writer.writeCharacters(String.valueOf(record.regionPIndex));
        endElement("td");

        // to next PIndex (Region Next Max)
        startElement("td");
        writer.writeCharacters(record.getRunsToNextPIndex() + " more to P" + (record.regionPIndex + 1));
        endElement("td");

        // Global P-Index
        startElement("td");
        writer.writeCharacters(String.valueOf(record.globalPIndex));
        endElement("td");

        endElement("tr");
    }

    public static class Record
    {
        public final Athlete athlete;
        public final int regionPIndex;
        public final int regionNextMax;
        public final int globalPIndex;

        public Record(Athlete athlete, int regionPIndex, int globalPIndex, int regionNextMax)
        {
            this.athlete = athlete;
            this.regionPIndex = regionPIndex;
            this.regionNextMax = regionNextMax;
            this.globalPIndex = globalPIndex;
        }

        public int getRunsToNextPIndex()
        {
            return (regionPIndex + 1) - regionNextMax;
        }

        @Override
        public String toString()
        {
            return "Record{" +
                    "athlete=" + athlete +
                    ", regionPIndex=" + regionPIndex +
                    ", regionNextMax=" + regionNextMax +
                    ", globalPIndex=" + globalPIndex +
                    '}';
        }
    }
}
