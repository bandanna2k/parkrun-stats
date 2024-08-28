package dnt.parkrun.htmlwriter;

import dnt.parkrun.common.DateConverter;
import dnt.parkrun.datastructures.Country;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HtmlWriter extends BaseWriter
{
    private static final SimpleDateFormat sdfYear = new SimpleDateFormat("yyyy");
    private final Date date;
    private final File file;
    private final Country country;
    private final boolean writeHeaderAndFooter;

    public static HtmlWriter newInstance(Date date, Country country, String prefix) throws IOException, XMLStreamException
    {
        File file = new File(prefix + "_" + DateConverter.formatDateForDbTable(date) + ".html");

        XMLStreamWriter writer = XMLOutputFactory
                .newInstance()
                .createXMLStreamWriter(
                        new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8));
        return new HtmlWriter(writer, date, country, file);
    }

    public static HtmlWriter newInstance(Date date, Country country, String extraPath, String fileName) throws IOException, XMLStreamException
    {
        Path path = Path.of("parkrun_stats", country.name(), sdfYear.format(date), "time", extraPath, fileName);
        path.getParent().toFile().mkdirs();

        XMLStreamWriter writer = XMLOutputFactory
                .newInstance()
                .createXMLStreamWriter(
                        new OutputStreamWriter(new FileOutputStream(path.toFile()), StandardCharsets.UTF_8));
        return new HtmlWriter(writer, date, country, path.toFile(), false);
    }

    private HtmlWriter(XMLStreamWriter writer, Date date, Country country, File file) throws XMLStreamException, IOException
    {
        this(writer, date, country, file, true);
    }
    private HtmlWriter(XMLStreamWriter writer, Date date, Country country, File file, boolean writeHeaderAndFooter) throws XMLStreamException, IOException
    {
        super(writer);
        this.date = date;
        this.file = file;
        this.country = country;
        this.writeHeaderAndFooter = writeHeaderAndFooter;

        if(writeHeaderAndFooter) startHtml();
    }

    private void startHtml() throws XMLStreamException
    {
        writer.writeStartDocument();

        startElement("html");

        String title = country.countryName + " parkrun Most Events and Attendance Records for Events on and prior to " + DateConverter.formatDateForHtml(date);
        startElement("title");
        writer.writeCharacters(title);
        endElement("title");

        startElement("head");
        writer.writeCharacters("{{meta}}");
        endElement("head");

        startElement("script", "src", "https://www.kryogenix.org/code/browser/sorttable/sorttable.js");
        endElement("script");

        startElement("body");
    }

    @Override
    public void close() throws IOException
    {
        try
        {
            if(writeHeaderAndFooter)
            {
                endElement("body");
                endElement("html");
            }
            writer.writeEndDocument();
            writer.flush();
            writer.close();
        }
        catch (XMLStreamException e)
        {
            throw new RuntimeException(e);
        }
    }

    public File getFile()
    {
        return file;
    }
}
