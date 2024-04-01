package dnt.parkrun.htmlwriter;

import dnt.parkrun.datastructures.Athlete;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.Closeable;

import static dnt.parkrun.common.UrlGenerator.generateAthleteEventSummaryUrl;
import static dnt.parkrun.datastructures.Country.NZ;

public class MostEventsTableHtmlWriter extends BaseWriter implements Closeable
{
    private final boolean extended;

    public MostEventsTableHtmlWriter(XMLStreamWriter writer, boolean extended) throws XMLStreamException
    {
        super(writer);
        this.extended = extended;

        startElement("details");
        startElement("summary");
        writer.writeCharacters(extended ? "Most Events (Extended)" : "Most Events");
        endElement("summary");

        startElement("table", "class", "sortable most-events");
        writeHeader(writer);
    }

    private void writeHeader(XMLStreamWriter writer) throws XMLStreamException
    {
        startElement("thead");
        startElement("tr");

        // Up arrows for max attendance
        startElement("th");
        endElement("th");

        // Name
        startElement("th");
        writer.writeCharacters("Name");
        endElement("th");

        startElement("th");
        writer.writeCharacters("Region Events");
        endElement("th");

        startElement("th");
        writer.writeCharacters("Total Region Runs");
        endElement("th");

        startElement("th");
        writer.writeCharacters("Worldwide Events");
        endElement("th");

        startElement("th");
        writer.writeCharacters("Total Runs");
        endElement("th");

        if(extended)
        {
            startElement("th");
            information("Regionnaire Count",
                    "Regionnaire is someone who has completed all parkruns in a region. " +
                            "This region being New Zealand. This count is how many times it has been achieved.");
            endElement("th");
        }

        if(extended)
        {
            startElement("th");
            information("Events Needed (Max)",
                    "Events needed to be regionnaire. (Maximum events this person has ever needed to become a regionnaire.");
            endElement("th");
        }

        if(extended)
        {
            // Chart
            startElement("th");
            endElement("th");
        }

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
            writer.writeCharacters("* Hover over position arrows to see weekly movement.");
            endElement("p");
            endElement("center");

            endElement("details");

        }
        catch (XMLStreamException e)
        {
            throw new RuntimeException(e);
        }
    }

    public void writeMostEventRecord(Record record) throws XMLStreamException
    {
       writer.writeStartElement("tr");

        // Up/Down
        writeTableDataWithDelta(record.positionDelta, record.isNewEntry);

        // Name
        startElement("td");
        writer.writeStartElement("a");
        writer.writeAttribute("href", generateAthleteEventSummaryUrl(NZ.baseUrl, record.athlete.athleteId).toString());
        writer.writeAttribute("target", String.valueOf(record.athlete.name));
        writer.writeCharacters(record.athlete.name);
        endElement("a");
        endElement("td");

        // Different region courses
        startElement("td");
        writer.writeCharacters(String.valueOf(record.differentRegionCourseCount));
        endElement("td");

        // Total region runs
        startElement("td");
        writer.writeCharacters(String.valueOf(record.totalRegionRuns));
        endElement("td");

        // Different courses
        startElement("td");
        writer.writeCharacters(String.valueOf(record.differentCourseCount));
        endElement("td");

        // Total region runs
        startElement("td");
        writer.writeCharacters(String.valueOf(record.totalRuns));
        endElement("td");

        if(extended)
        {
            // Regionnaire count
            startElement("td");
            writer.writeCharacters(String.valueOf(record.regionnaireCount));
            endElement("td");
        }

        if(extended)
        {
            // Runs needed
            startElement("td");
            writer.writeCharacters(record.runsNeeded);
            endElement("td");
        }

        if(extended)
        {
            startElement("td");
            startElement("span", "class", "click-me",
                    "onclick",
                    "dialog.showModal();" +
                            "setFirstRuns('" + record.athlete.name.replace("'", "\\'") + "'," + record.firstRuns + ");" +
                            "refreshStartDates();"
            );
            writer.writeCharacters("\uD83D\uDCC8");
            endElement("div");
            endElement("td");
        }

        endElement("tr");
    }

    public static class Record
    {
        public final Athlete athlete;
        public final int differentRegionCourseCount;
        public final int totalRegionRuns;
        public final int differentCourseCount;
        public final int totalRuns;
        public final String firstRuns;
        public final int regionnaireCount;

        public final int positionDelta;
        public final boolean isNewEntry;
        public final String runsNeeded;

        public Record(Athlete athlete,
                      int differentRegionCourseCount, int totalRegionRuns,
                      int differentCourseCount, int totalRuns,
                      int positionDelta, boolean isNewEntry,
                      String firstRuns, int regionnaireCount, String runsNeeded)
        {
            this.athlete = athlete;
            this.differentRegionCourseCount = differentRegionCourseCount;
            this.totalRegionRuns = totalRegionRuns;
            this.differentCourseCount = differentCourseCount;
            this.totalRuns = totalRuns;
            this.positionDelta = positionDelta;
            this.isNewEntry = isNewEntry;
            this.firstRuns = firstRuns;
            this.regionnaireCount = regionnaireCount;
            this.runsNeeded = runsNeeded;
        }

        @Override
        public String toString()
        {
            return "Record{" +
                    "athlete=" + athlete +
                    ", differentRegionCourseCount=" + differentRegionCourseCount +
                    ", totalRegionRuns=" + totalRegionRuns +
                    ", differentCourseCount=" + differentCourseCount +
                    ", totalRuns=" + totalRuns +
                    ", positionDelta=" + positionDelta +
                    ", firstRuns='" + firstRuns + '\'' +
                    ", regionnaireCount=" + regionnaireCount +
                    '}';
        }
    }
}
