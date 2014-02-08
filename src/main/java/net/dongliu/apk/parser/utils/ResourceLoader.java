package net.dongliu.apk.parser.utils;

import net.dongliu.apk.parser.struct.xml.Attribute;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;

/**
 * methods for load resources.
 *
 * @author dongliu
 */
public class ResourceLoader {


    public static String[] loadSystemAttrIds() {
        InputStream in = ResourceLoader.class.getResourceAsStream("/resource_values.xml");
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            final String[] ids =
                    new String[Attribute.AttrIds.MAX_ID - Attribute.AttrIds.ID_START + 1];
            DefaultHandler dh = new DefaultHandler() {
                @Override
                public void characters(char ch[], int start, int length) throws SAXException {
                }

                @Override
                public void endElement(String uri, String localName, String qName)
                        throws SAXException {
                }

                @Override
                public void startElement(String uri, String localName, String qName,
                                         Attributes attributes) throws SAXException {
                    if (!qName.equals("public")) {
                        return;
                    }
                    String type = attributes.getValue("type");
                    if (type == null) {
                        return;
                    }
                    if (type.equals("attr")) {
                        //attr ids.
                        String idStr = attributes.getValue("id");
                        String name = attributes.getValue("name");
                        if (idStr.startsWith("0x")) {
                            idStr = idStr.substring(2);
                        }
                        int id = Integer.parseInt(idStr, 16);
                        ids[id - Attribute.AttrIds.ID_START] = name;
                    }
                }
            };
            parser.parse(in, dh);
            return ids;
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                in.close();
            } catch (IOException ignored) {
            }
        }
    }
}
