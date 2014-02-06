package net.dongliu.apk.parser.parser;

/**
 * interface for parser xmlfile.
 *
 * @author dongliu
 */
public interface XmlStreamReader {

    void onStartTagStart(String name);

    void onStartTagEnd(String name);

    void onEndTag(String name);

    void onAttribute(String name, String value);
}
