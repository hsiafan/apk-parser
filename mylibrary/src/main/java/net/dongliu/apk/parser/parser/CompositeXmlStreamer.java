package net.dongliu.apk.parser.parser;

import net.dongliu.apk.parser.struct.xml.XmlCData;
import net.dongliu.apk.parser.struct.xml.XmlNamespaceEndTag;
import net.dongliu.apk.parser.struct.xml.XmlNamespaceStartTag;
import net.dongliu.apk.parser.struct.xml.XmlNodeEndTag;
import net.dongliu.apk.parser.struct.xml.XmlNodeStartTag;

/**
 * @author dongliu
 */
public class CompositeXmlStreamer implements XmlStreamer {

    public final XmlStreamer[] xmlStreamers;

    public CompositeXmlStreamer(final XmlStreamer... xmlStreamers) {
        this.xmlStreamers = xmlStreamers;
    }

    @Override
    public void onStartTag(final XmlNodeStartTag xmlNodeStartTag) {
        for (final XmlStreamer xmlStreamer : this.xmlStreamers) {
            xmlStreamer.onStartTag(xmlNodeStartTag);
        }
    }

    @Override
    public void onEndTag(final XmlNodeEndTag xmlNodeEndTag) {
        for (final XmlStreamer xmlStreamer : this.xmlStreamers) {
            xmlStreamer.onEndTag(xmlNodeEndTag);
        }
    }

    @Override
    public void onCData(final XmlCData xmlCData) {
        for (final XmlStreamer xmlStreamer : this.xmlStreamers) {
            xmlStreamer.onCData(xmlCData);
        }
    }

    @Override
    public void onNamespaceStart(final XmlNamespaceStartTag tag) {
        for (final XmlStreamer xmlStreamer : this.xmlStreamers) {
            xmlStreamer.onNamespaceStart(tag);
        }
    }

    @Override
    public void onNamespaceEnd(final XmlNamespaceEndTag tag) {
        for (final XmlStreamer xmlStreamer : this.xmlStreamers) {
            xmlStreamer.onNamespaceEnd(tag);
        }
    }
}
