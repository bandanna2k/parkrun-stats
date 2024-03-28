package dnt.parkrun.htmlwriter;

import dnt.parkrun.datastructures.Athlete;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.Closeable;

import static dnt.parkrun.common.UrlGenerator.generateAthleteEventSummaryUrl;

public class MostEventsTableHtmlWriter extends BaseWriter implements Closeable
{
    public MostEventsTableHtmlWriter(XMLStreamWriter writer) throws XMLStreamException
    {
        super(writer);

        startElement("details");
        startElement("summary");
        writer.writeCharacters("Most Events (New Zealand)");
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

        startElement("th");
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
        startElement("td");
        if(record.positionDelta > 0)
        {
            startElement("font", "color", "green");
            startElement("abbr", "title", "+" + record.positionDelta);
            writer.writeCharacters("▲");
            endElement("abbr");
            endElement("font");
        }
        else if(record.positionDelta < 0)
        {
            startElement("font", "color", "red");
            startElement("abbr", "title", String.valueOf(record.positionDelta));
            writer.writeCharacters("▼");
            endElement("abbr");
            endElement("font");
        }
        endElement("td");

        // Name
        startElement("td");
        writer.writeStartElement("a");
        writer.writeAttribute("href", generateAthleteEventSummaryUrl("parkrun.co.nz", record.athlete.athleteId).toString());
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

        // Total region runs
        startElement("td");
        startElement("span", "onclick",
                "dialog.showModal();" +
                        "setFirstRuns('" + record.athlete.name.replace("'", "\\'") + "'," + record.firstRuns + ");" +
                        "refreshStartDates();"
        );
        writer.writeCharacters("\uD83D\uDCC8");
        endElement("div");
        endElement("td");

        endElement("tr");
    }

    public static class Record
    {
        public final Athlete athlete;
        public final int differentRegionCourseCount;
        public final int totalRegionRuns;
        public final int differentCourseCount;
        public final int totalRuns;
        public final int positionDelta;
        public final String firstRuns;

        public Record(Athlete athlete,
                      int differentRegionCourseCount, int totalRegionRuns,
                      int differentCourseCount, int totalRuns,
                      int positionDelta, String firstRuns)
        {
            this.athlete = athlete;
            this.differentRegionCourseCount = differentRegionCourseCount;
            this.totalRegionRuns = totalRegionRuns;
            this.differentCourseCount = differentCourseCount;
            this.totalRuns = totalRuns;
            this.positionDelta = positionDelta;
            this.firstRuns = firstRuns;
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
                    '}';
        }
    }
}
