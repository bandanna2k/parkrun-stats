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

    protected void endElement(String tag) throws XMLStreamException
    {
        writer.writeEndElement();
        writer.writeCharacters("\n");
    }
}
