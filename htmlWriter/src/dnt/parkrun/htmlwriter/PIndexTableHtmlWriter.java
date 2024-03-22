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
    public PIndexTableHtmlWriter(XMLStreamWriter writer, String title) throws XMLStreamException
    {
        super(writer);

        startElement("details", "style", "margin-left:10em");
        startElement("summary", "style", "font-size:24px");
        writer.writeCharacters(title);
        endElement("summary");

        startElement("p", "style", "margin-left:100px");
        writer.writeCharacters("p-Index table courtesy of ");
        startElement("a",
                "href", UrlGenerator.generateAthleteUrl(NZ.baseUrl, 4225353).toString(),
                "style", "color:inherit;text-decoration:none;");
        writer.writeCharacters("Dan Joe");
        endElement("a");
        endElement("p");

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

        startElement("th");
        writer.writeCharacters("Home Ratio");
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

//            startElement("center");
//            startElement("p");
//            writer.writeCharacters("* Only contains parkrunners with pIndex of 5 within NZ parkruns.");
//            endElement("p");
//            endElement("center");

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
        startTableElement(record.isRegionalPIndexAthlete);
        writer.writeStartElement("a");
        writer.writeAttribute("href", generateAthleteEventSummaryUrl("parkrun.co.nz", record.athlete.athleteId).toString());
        writer.writeAttribute("target", String.valueOf(record.athlete.name));
        writer.writeCharacters(record.athlete.name);
        endElement("a");
        endElement("td");

        // Region P-Index
        startTableElement(record.isRegionalPIndexAthlete);
        writer.writeCharacters(String.valueOf(record.globalPIndex.pIndex));
        endElement("td");

        // to next PIndex (Region Next Max)
        startTableElement(record.isRegionalPIndexAthlete);
        writer.writeCharacters(record.globalPIndex.neededForNextPIndex + " more to P" + (record.globalPIndex.pIndex + 1));
        endElement("td");

        // Home parkrun Ratio
        startTableElement(record.isRegionalPIndexAthlete);
        writer.writeCharacters(String.format("%.3f", record.homeRatio));
        endElement("td");

        endElement("tr");
    }

    private void startTableElement(boolean isRegionalPIndexAthlete) throws XMLStreamException
    {
        if(isRegionalPIndexAthlete)
        {
            startElement("td");
        }
        else
        {
            startElement("td", "style", "background-color:#FFE9F0");
        }
    }

    public static class Record
    {
        public final Athlete athlete;
        public final PIndex.Result globalPIndex;
        public final double homeRatio;
        public final boolean isRegionalPIndexAthlete;

        public Record(Athlete athlete, PIndex.Result globalPIndex, double homeRatio)
        {
            this(athlete, globalPIndex, homeRatio, true);
        }

        public Record(Athlete athlete, PIndex.Result globalPIndex, double homeRatio, boolean isRegionalPIndexAthlete)
        {
            this.athlete = athlete;
            this.globalPIndex = globalPIndex;
            this.homeRatio = homeRatio;
            this.isRegionalPIndexAthlete = isRegionalPIndexAthlete;
        }

        @Override
        public String toString()
        {
            return "Record{" +
                    "athlete=" + athlete +
                    ", globalPIndex=" + globalPIndex +
                    ", homeRatio=" + homeRatio +
                    ", isRegionalPIndexAthlete=" + isRegionalPIndexAthlete +
                    '}';
        }
    }
}
