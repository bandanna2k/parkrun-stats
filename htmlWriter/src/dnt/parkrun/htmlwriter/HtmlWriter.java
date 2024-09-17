package dnt.parkrun.htmlwriter;

import dnt.parkrun.common.DateConverter;
import dnt.parkrun.datastructures.Country;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.*;
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

        XMLStreamWriter writer = newXmlStreamWriter(file);
        return new HtmlWriter(writer, date, country, file);
    }

    @Deprecated(since = "Please delete this. It is crap")
    public static HtmlWriter newInstance(Date date, Country country, String extraPath, String fileName) throws IOException, XMLStreamException
    {
        Path path = Path.of("parkrun_stats", country.name(), sdfYear.format(date), "time", extraPath, fileName);
        path.getParent().toFile().mkdirs();

        XMLStreamWriter writer = newXmlStreamWriter(path.toFile());
        return new HtmlWriter(writer, date, country, path.toFile(), false);
    }

    private static XMLStreamWriter newXmlStreamWriter(File file) throws FileNotFoundException, XMLStreamException
    {
        return XMLOutputFactory
                .newInstance()
                .createXMLStreamWriter(
                        new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8));
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
        }
        catch (XMLStreamException e)
        {
            throw new RuntimeException(e);
        }
        super.close();
    }

    public File getFile()
    {
        return file;
    }

    public BaseWriter createSubPageWriter(String fileString, String... pathStrings) throws XMLStreamException, FileNotFoundException
    {
        String pathString = String.join(File.separator, pathStrings);
        Path path = Path.of(pathString);
        path.toFile().mkdirs();
        if(path.toFile().exists() && path.toFile().isDirectory())
        {
            writer.writeStartElement("object");
            writer.writeAttribute("data", pathString + File.separator + fileString);
            writer.writeEndElement();

            File file = new File(path.toFile(), fileString);
            XMLStreamWriter xmlStreamWriter = newXmlStreamWriter(file);
            return new BaseWriter(xmlStreamWriter);
        }
        else
        {
            throw new RuntimeException("Failed to create sub page writer.");
        }
    }
}
