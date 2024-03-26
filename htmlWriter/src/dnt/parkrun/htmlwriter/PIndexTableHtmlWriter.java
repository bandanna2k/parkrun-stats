package dnt.parkrun.htmlwriter;

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

        startElement("table", "class", "sortable name-name-data");
        writeHeader(writer);
    }

    private void writeHeader(XMLStreamWriter writer) throws XMLStreamException
    {
        startElement("thead");
        startElement("tr");

        // Up/Down
        startElement("th");
        endElement("th");

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

            startElement("center");
            startElement("p");
            writer.writeCharacters("* parkrunners with pIndex of 5 or more within NZ parkruns.");
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
        startTableRowElement(record.isRegionalPIndexAthlete ? null : "#FFE9F0");

        writeTableDataWithDelta(record.positionDelta);

        // Name
        startElement("td");
        writer.writeStartElement("a");
        writer.writeAttribute("href", generateAthleteEventSummaryUrl(NZ.baseUrl, record.athlete.athleteId).toString());
        writer.writeAttribute("target", String.valueOf(record.athlete.name));
        writer.writeCharacters(record.athlete.name);
        endElement("a");
        endElement("td");

        // p-Index
        startElement("td");
        startElement("abbr", "title", "NZ p-Index " + record.regionPIndex.pIndex);
        writer.writeCharacters(String.valueOf(record.globalPIndex.pIndex));
        endElement("abbr");
        endElement("td");

        // to next PIndex (Region Next Max)
        startElement("td");
        writer.writeCharacters(record.globalPIndex.neededForNextPIndex + " more to P" + (record.globalPIndex.pIndex + 1));
        endElement("td");

        // Home parkrun Ratio
        startElement("td");
        writer.writeCharacters(String.format("%.3f", record.homeRatio));
        endElement("td");

        endElement("tr");
    }

    private void XstartTableElement(boolean isRegionalPIndexAthlete) throws XMLStreamException
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

    private void startTableRowElement(String colorCode) throws XMLStreamException
    {
        if(colorCode == null)
        {
            startElement("tr");
        }
        else
        {
            startElement("tr", "style", "background-color:" + colorCode);
        }
    }

    public static class Record
    {
        public final Athlete athlete;
        public final PIndex.Result globalPIndex;
        public final double homeRatio;
        public final PIndex.Result regionPIndex;
        public final boolean isRegionalPIndexAthlete;
        public int positionDelta;

        public Record(Athlete athlete, PIndex.Result globalPIndex, double homeRatio, PIndex.Result regionPIndex)
        {
            this(athlete, globalPIndex, homeRatio, regionPIndex, true);
        }

        public Record(Athlete athlete, PIndex.Result globalPIndex, double homeRatio, PIndex.Result regionPIndex, boolean isRegionalPIndexAthlete)
        {
            this.athlete = athlete;
            this.globalPIndex = globalPIndex;
            this.homeRatio = homeRatio;
            this.regionPIndex = regionPIndex;
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
