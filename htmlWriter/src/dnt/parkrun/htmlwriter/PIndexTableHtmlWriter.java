package dnt.parkrun.htmlwriter;

import dnt.parkrun.common.UrlGenerator;
import dnt.parkrun.datastructures.Athlete;
import dnt.parkrun.pindex.PIndex;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.Closeable;

import static dnt.parkrun.common.UrlGenerator.generateAthleteEventSummaryUrl;
import static dnt.parkrun.datastructures.Country.NZ;

public class PIndexTableHtmlWriter extends BaseWriter implements Closeable
{
    public PIndexTableHtmlWriter(XMLStreamWriter writer) throws XMLStreamException
    {
        super(writer);

        startElement("details", "style", "margin-left:10em");
        startElement("summary", "style", "font-size:24px");
        writer.writeCharacters("Legacy P-Index (New Zealand) - by ");
        startElement("a",
                "href", UrlGenerator.generateAthleteUrl(NZ.baseUrl, 12345).toString(),
                "style", "color:inherit;text-decoration:none;");
        writer.writeCharacters("Dan Joe");
        endElement("a");
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
        writer.writeCharacters("p-Index");
        endElement("th");

        startElement("th");
        writer.writeCharacters("Needed to Index Up");
        endElement("th");

//        startElement("th");
//        writer.writeCharacters("Global P-Index");
//        endElement("th");

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
        writer.writeCharacters(String.valueOf(record.globalPIndex.pIndex));
        endElement("td");

        // to next PIndex (Region Next Max)
        startElement("td");
        writer.writeCharacters(record.globalPIndex.neededForNextPIndex + " more to P" + (record.globalPIndex.pIndex + 1));
        endElement("td");

        // Global P-Index
//        startElement("td");
//        writer.writeCharacters(String.valueOf(record.globalPIndex.pIndex));
//        endElement("td");

        endElement("tr");
    }

    public static class Record
    {
        public final Athlete athlete;
        public final PIndex.Result regionPIndex;
        public final PIndex.Result globalPIndex;

        public Record(Athlete athlete, PIndex.Result regionPIndex, PIndex.Result globalPIndex)
        {
            this.athlete = athlete;
            this.regionPIndex = regionPIndex;
            this.globalPIndex = globalPIndex;
        }

        @Override
        public String toString()
        {
            return "Record{" +
                    "athlete=" + athlete +
                    ", regionPIndex=" + regionPIndex +
                    ", globalPIndex=" + globalPIndex +
                    '}';
        }
    }
}
