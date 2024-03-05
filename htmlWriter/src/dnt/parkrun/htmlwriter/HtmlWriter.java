package dnt.parkrun.htmlwriter;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class HtmlWriter extends BaseWriter
{
    public static HtmlWriter newInstance(File file) throws IOException, XMLStreamException
    {
        FileOutputStream fos = new FileOutputStream(file);

        XMLStreamWriter writer = XMLOutputFactory
                .newInstance()
                .createXMLStreamWriter(
                        new OutputStreamWriter(fos, StandardCharsets.UTF_8));

        return new HtmlWriter(writer);
    }

    private HtmlWriter(XMLStreamWriter writer) throws XMLStreamException, IOException
    {
        super(writer);

        startHtml();
    }

    private void startHtml() throws XMLStreamException, IOException
    {
        writer.writeStartDocument();

        startElement("html");
        startElement("style");
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(
                this.getClass().getResourceAsStream("/css/most_events.css"))))
        {
            String line;
            while(null != (line = reader.readLine()))
            {
                writer.writeCharacters(line + "\n");
            }
        }
        endElement("style");

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
