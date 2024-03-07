package dnt.parkrun.htmlwriter;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class HtmlWriter extends BaseWriter
{
    private final OutputStreamWriter rawWriter;

    public static HtmlWriter newInstance(File file) throws IOException, XMLStreamException
    {
        FileOutputStream fos = new FileOutputStream(file);

        OutputStreamWriter rawWriter = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
        XMLStreamWriter writer = XMLOutputFactory
                .newInstance()
                .createXMLStreamWriter(
                        rawWriter);

        return new HtmlWriter(writer, rawWriter);
    }

    private HtmlWriter(XMLStreamWriter writer, OutputStreamWriter rawWriter) throws XMLStreamException, IOException
    {
        super(writer);
        this.rawWriter = rawWriter;

        startHtml();
    }

    private void startHtml() throws XMLStreamException, IOException
    {
        writer.writeStartDocument();

        startElement("html");
        writer.writeStartElement("style");
        try(BufferedReader reader1 = new BufferedReader(new InputStreamReader(
                this.getClass().getResourceAsStream("/css/most_events.css"))))
        {
            String line1;
            while(null != (line1 = reader1.readLine()))
            {
                writer.writeCharacters(line1 + "\n");
            }
        }
        endElement("style");

        writer.writeStartElement("script");
        writer.writeAttribute("src", "https://www.kryogenix.org/code/browser/sorttable/sorttable.js");
        endElement("script");

        startElement("body");
    }

    @Override
    public void close()
    {
        try
        {
            endElement("body");
            endElement("html");

            writer.writeEndDocument();
            writer.close();
        }
        catch (XMLStreamException e)
        {
            throw new RuntimeException(e);
        }
    }

}
