package dnt.parkrun.htmlwriter;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

public class HtmlWriter extends BaseWriter
{
    public static HtmlWriter newInstance(File file) throws FileNotFoundException, XMLStreamException
    {
        FileOutputStream fos = new FileOutputStream(file);

        XMLStreamWriter writer = XMLOutputFactory
                .newInstance()
                .createXMLStreamWriter(
                        new OutputStreamWriter(fos, StandardCharsets.UTF_8));

        return new HtmlWriter(writer);
    }

    private HtmlWriter(XMLStreamWriter writer) throws XMLStreamException
    {
        super(writer);

        startHtml();
    }

    private void startHtml() throws XMLStreamException
    {
        writer.writeStartDocument();

        startElement("html");
        startElement("style");
        writer.writeCharacters(
                "body { \n" +
                        "  background:#f2f2f2; \n" +
                        "  font:400 14px 'Montserrat', 'sans-serif', 'Arial';\n" +
                        "  padding:20px;\n" +
                        "}\n" +
                        "table { \n" +
                        "  border-spacing: 1; \n" +
                        "  border-collapse: collapse; \n" +
                        "  background:white;\n" +
                        "  border-radius:6px;\n" +
                        "  overflow:hidden;\n" +
                        "  max-width:800px; \n" +
                        "  width:100%;\n" +
                        "  margin:0 auto;\n" +
                        "  position:relative;\n" +
                        "}\n" +
                        "table caption { \n" +
                        "  font-size:48px;\n" +
                        "  padding:20px;\n" +
                        "}" +
                        "table a { \n" +
                        "  color:inherit;\n" +
                        "  text-decoration:none;\n" +
                        "}" +
                        "td,th { " +
                        "  padding:8px;\n" +
                        "  text-align:center;\n" +
                        "  white-space:nowrap;\n" +
                        "}\n" +
                        "td:first-child { " +
                        "  text-align:left;\n" +
                        "}\n" +
                        "thead tr { \n" +
                        "  height:60px;\n" +
                        "  color:#fff;\n" +
                        "  background:#3e3e77;\n" +
                        "  font-size:16px;\n" +
                        "}\n" +
                        "tbody tr { height:40px; border-bottom:1px solid #E3F1D5 ;\n" +
                        "}\n"
        );
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
