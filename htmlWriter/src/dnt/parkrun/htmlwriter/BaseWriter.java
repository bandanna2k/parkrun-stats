package dnt.parkrun.htmlwriter;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.Closeable;

public abstract class BaseWriter implements Closeable
{
    public final XMLStreamWriter writer;

    public BaseWriter(XMLStreamWriter writer)
    {
        this.writer = writer;
    }

    protected void startElement(String tag) throws XMLStreamException
    {
        writer.writeStartElement(tag);
        writer.writeCharacters("\n");
    }

    protected void startElement(String tag, String attribute, String attributeValue) throws XMLStreamException
    {
        writer.writeStartElement(tag);
        writer.writeAttribute(attribute, attributeValue);
        writer.writeCharacters("\n");
    }

    protected void startElement(String tag,
                                String attribute,
                                String attributeValue,
                                String attribute2,
                                String attributeValue2) throws XMLStreamException
    {
        writer.writeStartElement(tag);
        writer.writeAttribute(attribute, attributeValue);
        writer.writeAttribute(attribute2, attributeValue2);
        writer.writeCharacters("\n");
    }

    protected void endElement(String tag) throws XMLStreamException
    {
        writer.writeEndElement();
        writer.writeCharacters("\n");
    }

    protected void information(String text, String hoverText) throws XMLStreamException
    {
        startElement("abbr",
                "style", "border-bottom: 1px dotted;",
                "title", hoverText);
        writer.writeCharacters(text);
        endElement("abbr");
    }

    protected void startDetails() throws XMLStreamException
    {
        startElement("details", "name", "details1");
    }
    protected void endDetails() throws XMLStreamException
    {
        endElement("details");
    }
    protected void startSubDetails() throws XMLStreamException
    {
        startElement("details", "name", "details2");
    }
    protected void endSubDetails() throws XMLStreamException
    {
        endElement("details");
    }

    protected void writeTableDataWithDelta(int delta, boolean isNewEntry) throws XMLStreamException
    {
        // Up/Down
        startElement("th", "class", "dt");
        if(isNewEntry)
        {
            startElement("font", "color", "orange");
            startElement("abbr", "title", "New Entry");
            writer.writeCharacters("★");
            endElement("abbr");
            endElement("font");
        }
        else
        {
            if (delta > 0)
            {
                startElement("font", "color", "green");
                startElement("abbr", "title", "+" + delta);
                writer.writeCharacters("▲");
                endElement("abbr");
                endElement("font");
            }
            else if (delta < 0)
            {
                startElement("font", "color", "red");
                startElement("abbr", "title", String.valueOf(delta));
                writer.writeCharacters("▼");
                endElement("abbr");
                endElement("font");
            }
        }
        endElement("td");
    }
}
