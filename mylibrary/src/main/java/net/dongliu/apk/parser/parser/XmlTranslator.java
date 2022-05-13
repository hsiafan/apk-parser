package net.dongliu.apk.parser.parser;

import net.dongliu.apk.parser.struct.xml.Attribute;
import net.dongliu.apk.parser.struct.xml.XmlCData;
import net.dongliu.apk.parser.struct.xml.XmlNamespaceEndTag;
import net.dongliu.apk.parser.struct.xml.XmlNamespaceStartTag;
import net.dongliu.apk.parser.struct.xml.XmlNodeEndTag;
import net.dongliu.apk.parser.struct.xml.XmlNodeStartTag;
import net.dongliu.apk.parser.utils.xml.XmlEscaper;

import java.util.List;

/**
 * trans to xml text when parse binary xml file.
 *
 * @author dongliu
 */
public class XmlTranslator implements XmlStreamer {
    private final StringBuilder sb;
    private int shift = 0;
    private final XmlNamespaces namespaces;
    private boolean isLastStartTag;

    public XmlTranslator() {
        this.sb = new StringBuilder();
        this.sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
        this.namespaces = new XmlNamespaces();
    }

    @Override
    public void onStartTag(final XmlNodeStartTag xmlNodeStartTag) {
        if (this.isLastStartTag) {
            this.sb.append(">\n");
        }
        this.appendShift(this.shift++);
        this.sb.append('<');
        if (xmlNodeStartTag.getNamespace() != null) {
            final String prefix = this.namespaces.getPrefixViaUri(xmlNodeStartTag.getNamespace());
            if (prefix != null) {
                this.sb.append(prefix).append(":");
            } else {
                this.sb.append(xmlNodeStartTag.getNamespace()).append(":");
            }
        }
        this.sb.append(xmlNodeStartTag.getName());

        final List<XmlNamespaces.XmlNamespace> nps = this.namespaces.consumeNameSpaces();
        if (!nps.isEmpty()) {
            for (final XmlNamespaces.XmlNamespace np : nps) {
                this.sb.append(" xmlns:").append(np.getPrefix()).append("=\"")
                        .append(np.getUri())
                        .append("\"");
            }
        }
        this.isLastStartTag = true;

        for (final Attribute attribute : xmlNodeStartTag.getAttributes().values()) {
            this.onAttribute(attribute);
        }
    }

    private void onAttribute(final Attribute attribute) {
        this.sb.append(" ");
        String namespace = this.namespaces.getPrefixViaUri(attribute.getNamespace());
        if (namespace == null) {
            namespace = attribute.getNamespace();
        }
        if (namespace != null && !namespace.isEmpty()) {
            this.sb.append(namespace).append(':');
        }
        final String escapedFinalValue = XmlEscaper.escapeXml10(attribute.getValue());
        this.sb.append(attribute.getName()).append('=').append('"')
                .append(escapedFinalValue).append('"');
    }

    @Override
    public void onEndTag(final XmlNodeEndTag xmlNodeEndTag) {
        --this.shift;
        if (this.isLastStartTag) {
            this.sb.append(" />\n");
        } else {
            this.appendShift(this.shift);
            this.sb.append("</");
            if (xmlNodeEndTag.getNamespace() != null) {
                String namespace = this.namespaces.getPrefixViaUri(xmlNodeEndTag.getNamespace());
                if (namespace == null) {
                    namespace = xmlNodeEndTag.getNamespace();
                }
                this.sb.append(namespace).append(":");
            }
            this.sb.append(xmlNodeEndTag.getName());
            this.sb.append(">\n");
        }
        this.isLastStartTag = false;
    }


    @Override
    public void onCData(final XmlCData xmlCData) {
        this.appendShift(this.shift);
        this.sb.append(xmlCData.getValue()).append('\n');
        this.isLastStartTag = false;
    }

    @Override
    public void onNamespaceStart(final XmlNamespaceStartTag tag) {
        this.namespaces.addNamespace(tag);
    }

    @Override
    public void onNamespaceEnd(final XmlNamespaceEndTag tag) {
        this.namespaces.removeNamespace(tag);
    }

    private void appendShift(final int shift) {
        for (int i = 0; i < shift; i++) {
            this.sb.append("\t");
        }
    }

    public String getXml() {
        return this.sb.toString();
    }
}
