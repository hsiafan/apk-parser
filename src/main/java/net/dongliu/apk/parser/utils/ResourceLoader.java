package net.dongliu.apk.parser.utils;

import net.dongliu.apk.parser.struct.xml.Attribute;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * methods for load resources.
 *
 * @author dongliu
 */
public class ResourceLoader {


    public static Map<Integer, String> loadSystemAttrIds() {
        InputStream in = ResourceLoader.class.getResourceAsStream("/r_values.xml");
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            final Map<Integer, String> map = new HashMap<Integer, String>();
            DefaultHandler dh = new DefaultHandler() {
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
                        map.put(id, name);
                    }
                }
            };
            parser.parse(in, dh);
            return map;
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

    public static Map<Integer, String> loadSystemStyles() {
        InputStream in = ResourceLoader.class.getResourceAsStream("/r_styles.conf");
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        Map<Integer, String> map = new HashMap<Integer, String>();
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("#")) {
                    continue;
                }
                String[] items = line.split("=");
                int id = Integer.parseInt(items[1].trim().substring(2), 16);
                String name = items[0].trim();
                map.put(id, name);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                in.close();
            } catch (IOException ignored) {
            }
        }
        return map;
    }

    public static void main(String[] args) {
        loadSystemStyles();
    }
}
