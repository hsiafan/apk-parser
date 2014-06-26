package net.dongliu.apk.parser.parser;

import net.dongliu.apk.parser.struct.ActivityInfo;
import net.dongliu.apk.parser.struct.resource.ResourceTable;
import net.dongliu.apk.parser.struct.xml.*;

import java.util.List;
import java.util.Locale;

/**
 * trans to xml text when parse binary xml file.
 *
 * @author dongliu
 */
public class XmlTranslator implements XmlStreamer {
    private StringBuilder sb;
    private int shift = 0;
    private XmlNamespaces namespaces;
    private boolean isLastStartTag;

    private Locale locale;
    private ResourceTable resourceTable;

    public XmlTranslator(ResourceTable resourceTable, Locale locale) {
        this.locale = locale;
        this.resourceTable = resourceTable;
        sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
        this.namespaces = new XmlNamespaces();
    }

    @Override
    public void onStartTag(XmlNodeStartTag xmlNodeStartTag) {
        if (isLastStartTag) {
            sb.append(">\n");
        }
        appendShift(shift++);
        sb.append('<');
        if (xmlNodeStartTag.namespace != null) {
            sb.append(xmlNodeStartTag.namespace).append(":");
        }
        sb.append(xmlNodeStartTag.name);

        List<XmlNamespaces.XmlNamespace> nps = namespaces.consumeNameSpaces();
        if (!nps.isEmpty()) {
            for (XmlNamespaces.XmlNamespace np : nps) {
                sb.append(" xmlns:").append(np.getPrefix()).append("=\"")
                        .append(np.getUri())
                        .append("\"");
            }
        }
        isLastStartTag = true;
    }

    @Override
    public void onEndTag(XmlNodeEndTag xmlNodeEndTag) {
        --shift;
        if (isLastStartTag) {
            sb.append(" />\n");
        } else {
            appendShift(shift);
            sb.append("</");
            if (xmlNodeEndTag.namespace != null) {
                sb.append(xmlNodeEndTag.namespace).append(":");
            }
            sb.append(xmlNodeEndTag.name);
            sb.append(">\n");
        }
        isLastStartTag = false;
    }

    @Override
    public void onAttribute(Attribute attribute) {
        sb.append(" ");
        String namespace = this.namespaces.getPrefixViaUri(attribute.namespace);
        if (namespace == null) {
            namespace = attribute.namespace;
        }
        if (namespace != null && !namespace.isEmpty()) {
            sb.append(namespace).append(':');
        }

        String value = attribute.toStringValue(resourceTable, locale);
        if (attribute.name.equals("screenOrientation")) {
            ActivityInfo.ScreenOrienTation screenOrienTation =
                    ActivityInfo.ScreenOrienTation.valueOf(Integer.parseInt(value));
            if (screenOrienTation != null) {
                value = screenOrienTation.toString();
            }
        } else if (attribute.name.equals("configChanges")) {
            List<ActivityInfo.ConfigChanges> configChangesList =
                    ActivityInfo.ConfigChanges.valuesOf(Integer.parseInt(value));
            if (!configChangesList.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (ActivityInfo.ConfigChanges c : configChangesList) {
                    sb.append(c.toString()).append('|');
                }
                sb.deleteCharAt(sb.length() - 1);
                value = sb.toString();
            }
        } else if (attribute.name.equals("windowSoftInputMode")) {
            List<ActivityInfo.WindowSoftInputMode> windowSoftInputModeList =
                    ActivityInfo.WindowSoftInputMode.valuesOf(Integer.parseInt(value));
            if (!windowSoftInputModeList.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (ActivityInfo.WindowSoftInputMode w : windowSoftInputModeList) {
                    sb.append(w.toString()).append('|');
                }
                sb.deleteCharAt(sb.length() - 1);
                value = sb.toString();
            }
        } else if (attribute.name.equals("launchMode")) {
            ActivityInfo.LaunchMode launchMode =
                    ActivityInfo.LaunchMode.valueOf(Integer.parseInt(value));
            if (launchMode != null) {
                value = launchMode.toString();
            }
        }
        sb.append(attribute.name).append('=').append('"')
                .append(value.replace("\"", "\\\"")).append('"');
    }

    @Override
    public void onCData(XmlCData xmlCData) {
        appendShift(shift);
        sb.append(xmlCData.toStringValue(resourceTable, locale)).append('\n');
        isLastStartTag = false;
    }

    @Override
    public void onNamespaceStart(XmlNamespaceStartTag tag) {
        this.namespaces.addNamespace(tag);
    }

    @Override
    public void onNamespaceEnd(XmlNamespaceEndTag tag) {
        this.namespaces.removeNamespace(tag);
    }

    private void appendShift(int shift) {
        for (int i = 0; i < shift; i++) {
            sb.append("\t");
        }
    }

    public String getXml() {
        return sb.toString();
    }
}
