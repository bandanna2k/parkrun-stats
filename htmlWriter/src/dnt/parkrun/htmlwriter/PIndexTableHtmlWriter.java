package dnt.parkrun.htmlwriter;

import dnt.parkrun.common.UrlGenerator;
import dnt.parkrun.datastructures.Athlete;
import dnt.parkrun.pindex.PIndex;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.Closeable;

public class PIndexTableHtmlWriter extends BaseWriter implements Closeable
{
    private final UrlGenerator urlGenerator;

    public PIndexTableHtmlWriter(XMLStreamWriter writer, UrlGenerator urlGenerator, String title) throws XMLStreamException
    {
        super(writer);
        this.urlGenerator = urlGenerator;

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
        information("Home Ratio",
                "The ratio of runs ran in a persons home parkrun NZ province. " +
                        "Home parkrun is the parkrun a person has attended the most. " +
                        "Must be in NZ, otherwise they are in the legacy table. " +
                        "E.g. For Bob who has run 11 runs, 10 in Invercargill and 1 in Wellington, " +
                        "Bob's home ratio will be 0.909");
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

        writeTableDataWithDelta(record.positionDelta, record.isNewEntry);

        // Name
        startElement("td");
        writer.writeStartElement("a");
        writer.writeAttribute("href", urlGenerator.generateAthleteEventSummaryUrl(record.athlete.athleteId).toString());
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

        public boolean isNewEntry;
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
                    ", regionPIndex=" + regionPIndex +
                    ", isRegionalPIndexAthlete=" + isRegionalPIndexAthlete +
                    ", positionDelta=" + positionDelta +
                    ", isNewEntry=" + isNewEntry +
                    '}';
        }
    }
}
