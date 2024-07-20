package dnt.parkrun.htmlwriter.writers;

import dnt.parkrun.common.UrlGenerator;
import dnt.parkrun.datastructures.Country;
import dnt.parkrun.htmlwriter.BaseWriter;
import dnt.parkrun.htmlwriter.MostEventsRecord;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.Closeable;

public class MostEventsTableHtmlWriter extends BaseWriter implements Closeable
{
    private final Country country;
    private final UrlGenerator urlGenerator;
    private final boolean extended;

    public MostEventsTableHtmlWriter(XMLStreamWriter writer, Country country, boolean extended)
            throws XMLStreamException
    {
        super(writer);
        this.extended = extended;
        this.country = country;
        this.urlGenerator = new UrlGenerator(country.baseUrl);

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

        startElement("th", "class", "dt");
        writer.writeCharacters("Worldwide Events");
        endElement("th");

        startElement("th", "class", "dt");
        writer.writeCharacters("Total Runs");
        endElement("th");

        if(extended)
        {
            // Inaugural runs
            startElement("th", "class", "dt");
            writer.writeCharacters("Inaugural Runs");
            endElement("th");
        }

        if(extended)
        {
            startElement("th");
            information("Regionnaire Count",
                    "Regionnaire is someone who has completed all parkruns in a region. " +
                            "This region being " + country.countryName + ". This count is how many times it has been achieved.");
            endElement("th");
        }

        if(extended)
        {
            startElement("th", "class", "dt");
            information("Events Needed (Max)",
                    "Events needed to be regionnaire. " +
                            "(Maximum running events this person has ever needed to become a regionnaire.)");
            endElement("th");
        }

        if(extended)
        {
            // Chart
            startElement("th", "class", "dt");
            endElement("th");
            startElement("th", "class", "dt");
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
            writer.writeCharacters("* For runners that have run at 20 or more locations.");
            endElement("p");
            startElement("p");
            writer.writeCharacters("* Hover over position arrows to see weekly movement.");
            endElement("p");
            endElement("center");
        }
        catch (XMLStreamException e)
        {
            throw new RuntimeException(e);
        }
    }

    public void writeMostEventRecord(MostEventsRecord mostEventsRecord) throws XMLStreamException
    {
       writer.writeStartElement("tr");

        // Up/Down
        writeTableDataWithDelta(mostEventsRecord.positionDelta, mostEventsRecord.isNewEntry);

        // Name
        startElement("td");
        writer.writeStartElement("a");
        writer.writeAttribute("href", urlGenerator.generateAthleteEventSummaryUrl(mostEventsRecord.athlete.athleteId).toString());
        writer.writeAttribute("target", String.valueOf(mostEventsRecord.athlete.name));
        writer.writeCharacters(mostEventsRecord.athlete.name);
        endElement("a");
        endElement("td");

        // Different region courses
        startElement("td");
        writer.writeCharacters(String.valueOf(mostEventsRecord.differentRegionCourseCount));
        endElement("td");

        // Total region runs
        startElement("td");
        writer.writeCharacters(String.valueOf(mostEventsRecord.totalRegionRuns));
        endElement("td");

        // Different worldwide courses (desktop)
        startElement("td", "class", "dt");
        writer.writeCharacters(String.valueOf(mostEventsRecord.differentGlobalCourseCount));
        endElement("td");

        // Total worldwide runs (desktop)
        startElement("td", "class", "dt");
        writer.writeCharacters(String.valueOf(mostEventsRecord.totalGlobalRuns));
        endElement("td");

        if(extended)
        {
            // Inaugural runs
            startElement("td", "class", "dt");
            writer.writeCharacters(String.valueOf(mostEventsRecord.inauguralRuns));
            endElement("td");
        }

        if(extended)
        {
            // Regionnaire count
            startElement("td");
            writer.writeCharacters(String.valueOf(mostEventsRecord.regionnaireCount));
            endElement("td");
        }

        if(extended)
        {
            // Max courses needed
            startElement("td", "class", "dt");
            writer.writeCharacters(mostEventsRecord.runsNeeded);
            endElement("td");
        }

        if(extended)
        {
            // Charts
            startElement("td", "class", "dt");
            startElement("span", "class", "click-me",
                    "onclick",
                    "dialog.showModal();" +
                            "setFirstRuns('" + mostEventsRecord.athlete.name.replace("'", "\\'") + "'," + mostEventsRecord.firstRuns + ");" +
                            "refreshStartDates();"
            );
            writer.writeCharacters("\uD83D\uDCC8");
            endElement("span");
            endElement("td");

            startElement("td", "class", "dt");
//            startElement("span", "class", "click-me",
//                    "onclick", "downloadChart()");
//            writer.writeCharacters("\uD83D\uDDBC");
//            endElement("span");
            endElement("td");
        }

        endElement("tr");
    }

}
