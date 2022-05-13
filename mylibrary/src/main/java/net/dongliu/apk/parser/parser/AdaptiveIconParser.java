package net.dongliu.apk.parser.parser;

import net.dongliu.apk.parser.struct.xml.Attribute;
import net.dongliu.apk.parser.struct.xml.Attributes;
import net.dongliu.apk.parser.struct.xml.XmlCData;
import net.dongliu.apk.parser.struct.xml.XmlNamespaceEndTag;
import net.dongliu.apk.parser.struct.xml.XmlNamespaceStartTag;
import net.dongliu.apk.parser.struct.xml.XmlNodeEndTag;
import net.dongliu.apk.parser.struct.xml.XmlNodeStartTag;

/**
 * Parse adaptive icon xml file.
 *
 * @author Liu Dong dongliu@live.cn
 */
public class AdaptiveIconParser implements XmlStreamer {

    private String foreground;
    private String background;

    public String getForeground() {
        return this.foreground;
    }

    public String getBackground() {
        return this.background;
    }

    @Override
    public void onStartTag(final XmlNodeStartTag xmlNodeStartTag) {
        if (xmlNodeStartTag.getName().equals("background")) {
            this.background = this.getDrawable(xmlNodeStartTag);
        } else if (xmlNodeStartTag.getName().equals("foreground")) {
            this.foreground = this.getDrawable(xmlNodeStartTag);
        }
    }

    private String getDrawable(final XmlNodeStartTag xmlNodeStartTag) {
        final Attributes attributes = xmlNodeStartTag.getAttributes();
        for (final Attribute attribute : attributes.values()) {
            if (attribute.getName().equals("drawable")) {
                return attribute.getValue();
            }
        }
        return null;
    }

    @Override
    public void onEndTag(final XmlNodeEndTag xmlNodeEndTag) {

    }

    @Override
    public void onCData(final XmlCData xmlCData) {

    }

    @Override
    public void onNamespaceStart(final XmlNamespaceStartTag tag) {

    }

    @Override
    public void onNamespaceEnd(final XmlNamespaceEndTag tag) {

    }
}
