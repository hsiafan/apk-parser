package net.dongliu.apk.parser.parser;

import net.dongliu.apk.parser.bean.Constants.*;
import net.dongliu.apk.parser.bean.Locales;
import net.dongliu.apk.parser.exception.ParserException;
import net.dongliu.apk.parser.struct.ChunkHeader;
import net.dongliu.apk.parser.struct.ChunkType;
import net.dongliu.apk.parser.struct.StringPool;
import net.dongliu.apk.parser.struct.StringPoolHeader;
import net.dongliu.apk.parser.struct.resource.ResourceTable;
import net.dongliu.apk.parser.struct.xml.*;
import net.dongliu.apk.parser.utils.Buffers;
import net.dongliu.apk.parser.utils.ParseUtils;
import org.apache.commons.lang3.StringUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.Locale;

/**
 * Android Binary XML format
 * see http://justanapplication.wordpress.com/category/android/android-binary-xml/
 *
 * @author dongliu
 */
public class BinaryXmlParser {


    /**
     * By default the data buffer Chunks is buffer little-endian byte order both at runtime and when stored buffer files.
     */
    private ByteOrder byteOrder = ByteOrder.LITTLE_ENDIAN;
    private StringPool stringPool;
    private String[] resourceMap;
    private ByteBuffer buffer;
    private XmlStreamer xmlStreamer;
    private final ResourceTable resourceTable;
    /**
     * default locale.
     */
    private Locale locale = Locales.any;

    public BinaryXmlParser(ByteBuffer buffer, ResourceTable resourceTable) {
        this.buffer = buffer.duplicate();
        this.buffer.order(byteOrder);
        this.resourceTable = resourceTable;
    }

    /**
     * Parse binary xml.
     */
    public void parse() {
        ChunkHeader chunkHeader = readChunkHeader();
        if (chunkHeader.chunkType != ChunkType.XML) {
            //TODO: may be a plain xml file.
            return;
        }
        XmlHeader xmlHeader = (XmlHeader) chunkHeader;

        // read string pool chunk
        chunkHeader = readChunkHeader();
        ParseUtils.checkChunkType(ChunkType.STRING_POOL, chunkHeader.chunkType);
        stringPool = ParseUtils.readStringPool(buffer, (StringPoolHeader) chunkHeader);

        // read on chunk, check if it was an optional XMLResourceMap chunk
        chunkHeader = readChunkHeader();
        if (chunkHeader.chunkType == ChunkType.XML_RESOURCE_MAP) {
            long[] resourceIds = readXmlResourceMap((XmlResourceMapHeader) chunkHeader);
            resourceMap = new String[resourceIds.length];
            for (int i = 0; i < resourceIds.length; i++) {
                resourceMap[i] = Attribute.AttrIds.getString(resourceIds[i]);
            }
            chunkHeader = readChunkHeader();
        }

        while (chunkHeader != null) {
                /*if (chunkHeader.chunkType == ChunkType.XML_END_NAMESPACE) {
                    break;
                }*/
            long beginPos = buffer.position();
            switch (chunkHeader.chunkType) {
                case ChunkType.XML_END_NAMESPACE:
                    XmlNamespaceEndTag xmlNamespaceEndTag = readXmlNamespaceEndTag();
                    xmlStreamer.onNamespaceEnd(xmlNamespaceEndTag);
                    break;
                case ChunkType.XML_START_NAMESPACE:
                    XmlNamespaceStartTag namespaceStartTag = readXmlNamespaceStartTag();
                    xmlStreamer.onNamespaceStart(namespaceStartTag);
                    break;
                case ChunkType.XML_START_ELEMENT:
                    XmlNodeStartTag xmlNodeStartTag = readXmlNodeStartTag();
                    break;
                case ChunkType.XML_END_ELEMENT:
                    XmlNodeEndTag xmlNodeEndTag = readXmlNodeEndTag();
                    break;
                case ChunkType.XML_CDATA:
                    XmlCData xmlCData = readXmlCData();
                    break;
                default:
                    if (chunkHeader.chunkType >= ChunkType.XML_FIRST_CHUNK &&
                            chunkHeader.chunkType <= ChunkType.XML_LAST_CHUNK) {
                        Buffers.skip(buffer, (int) (chunkHeader.chunkSize - chunkHeader.headerSize));
                    } else {
                        throw new ParserException("Unexpected chunk type:" + chunkHeader.chunkType);
                    }
            }
            buffer.position((int) (beginPos + chunkHeader.chunkSize - chunkHeader.headerSize));
            chunkHeader = readChunkHeader();
        }
    }

    private XmlCData readXmlCData() {
        XmlCData xmlCData = new XmlCData();
        int dataRef = buffer.getInt();
        if (dataRef > 0) {
            xmlCData.data = stringPool.get(dataRef);
        }
        xmlCData.typedData = ParseUtils.readResValue(buffer, stringPool);
        if (xmlStreamer != null) {
            //TODO: to know more about cdata. some cdata appears buffer xml tags
//            String value = xmlCData.toStringValue(resourceTable, locale);
//            xmlCData.setValue(value);
//            xmlStreamer.onCData(xmlCData);
        }
        return xmlCData;
    }

    private XmlNodeEndTag readXmlNodeEndTag() {
        XmlNodeEndTag xmlNodeEndTag = new XmlNodeEndTag();
        int nsRef = buffer.getInt();
        int nameRef = buffer.getInt();
        if (nsRef > 0) {
            xmlNodeEndTag.namespace = stringPool.get(nsRef);
        }
        xmlNodeEndTag.name = stringPool.get(nameRef);
        if (xmlStreamer != null) {
            xmlStreamer.onEndTag(xmlNodeEndTag);
        }
        return xmlNodeEndTag;
    }

    private XmlNodeStartTag readXmlNodeStartTag() {
        int nsRef = buffer.getInt();
        int nameRef = buffer.getInt();
        XmlNodeStartTag xmlNodeStartTag = new XmlNodeStartTag();
        if (nsRef > 0) {
            xmlNodeStartTag.namespace = stringPool.get(nsRef);
        }
        xmlNodeStartTag.name = stringPool.get(nameRef);

        // read attributes.
        // attributeStart and attributeSize are always 20 (0x14)
        int attributeStart = Buffers.readUShort(buffer);
        int attributeSize = Buffers.readUShort(buffer);
        int attributeCount = Buffers.readUShort(buffer);
        int idIndex = Buffers.readUShort(buffer);
        int classIndex = Buffers.readUShort(buffer);
        int styleIndex = Buffers.readUShort(buffer);

        // read attributes
        Attributes attributes = new Attributes(attributeCount);
        for (int count = 0; count < attributeCount; count++) {
            Attribute attribute = readAttribute();
            if (xmlStreamer != null) {
                String value = attribute.toStringValue(resourceTable, locale);
                if (StringUtils.isNumeric(value)) {
                    value = getFinalValueAsString(attribute.name, value);
                }
                attribute.setValue(value);
                attributes.set(count, attribute);
            }
        }
        xmlNodeStartTag.setAttributes(attributes);

        if (xmlStreamer != null) {
            xmlStreamer.onStartTag(xmlNodeStartTag);
        }

        return xmlNodeStartTag;
    }

    //trans int attr value to string
    private String getFinalValueAsString(String attributeName, String value) {
        int intValue = Integer.valueOf(value);
        String realValue = value;
        switch (attributeName) {
            case "screenOrientation":
                ScreenOrientation screenOrientation =
                        ScreenOrientation.valueOf(intValue);
                if (screenOrientation != null) {
                    realValue = screenOrientation.name();
                }
                break;
            case "configChanges":
                List<ConfigChanges> configChangesList =
                        ConfigChanges.valuesOf(intValue);
                if (!configChangesList.isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    for (ConfigChanges c : configChangesList) {
                        sb.append(c.name()).append('|');
                    }
                    sb.deleteCharAt(sb.length() - 1);
                    realValue = sb.toString();
                }
                break;
            case "windowSoftInputMode":
                List<WindowSoftInputMode> windowSoftInputModeList =
                        WindowSoftInputMode.valuesOf(intValue);
                if (!windowSoftInputModeList.isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    for (WindowSoftInputMode w : windowSoftInputModeList) {
                        sb.append(w.name()).append('|');
                    }
                    sb.deleteCharAt(sb.length() - 1);
                    realValue = sb.toString();
                }
                break;
            case "launchMode":
                LaunchMode launchMode = LaunchMode.valueOf(intValue);
                if (launchMode != null) {
                    realValue = launchMode.name();
                }
                break;
            case "installLocation":
                InstallLocation installLocation = InstallLocation.valueOf(intValue);
                if (installLocation != null) {
                    realValue = installLocation.name();
                }
                break;
            case "protectionLevel":
                List<ProtectionLevel> protectionLevelList = ProtectionLevel.valueOf(intValue);
                StringBuilder sb = new StringBuilder();
                if (protectionLevelList != null) {
                    for (ProtectionLevel protectionLevel : protectionLevelList) {
                        sb.append(protectionLevel.name()).append('|');
                    }
                    sb.deleteCharAt(sb.length() - 1);
                    realValue = sb.toString();
                }
                break;
        }
        return realValue;
    }

    private Attribute readAttribute() {
        int nsRef = buffer.getInt();
        int nameRef = buffer.getInt();
        Attribute attribute = new Attribute();
        if (nsRef > 0) {
            attribute.namespace = stringPool.get(nsRef);
        }

        attribute.name = stringPool.get(nameRef);
        if (attribute.name.isEmpty() && resourceMap != null && nameRef < resourceMap.length) {
            // some processed apk file make the string pool value empty, if it is a xmlmap attr.
            attribute.name = resourceMap[nameRef];
            //TODO: how to get the namespace of attribute
        }

        int rawValueRef = buffer.getInt();
        if (rawValueRef > 0) {
            attribute.rawValue = stringPool.get(rawValueRef);
        }
        attribute.typedValue = ParseUtils.readResValue(buffer, stringPool,
                "style".equals(attribute.name) || "theme".equals(attribute.name));

        return attribute;
    }

    private XmlNamespaceStartTag readXmlNamespaceStartTag() {
        int prefixRef = buffer.getInt();
        int uriRef = buffer.getInt();
        XmlNamespaceStartTag nameSpace = new XmlNamespaceStartTag();
        if (prefixRef > 0) {
            nameSpace.prefix = stringPool.get(prefixRef);
        }
        if (uriRef > 0) {
            nameSpace.uri = stringPool.get(uriRef);
        }
        return nameSpace;
    }

    private XmlNamespaceEndTag readXmlNamespaceEndTag() {
        int prefixRef = buffer.getInt();
        int uriRef = buffer.getInt();
        XmlNamespaceEndTag nameSpace = new XmlNamespaceEndTag();
        if (prefixRef > 0) {
            nameSpace.prefix = stringPool.get(prefixRef);
        }
        if (uriRef > 0) {
            nameSpace.uri = stringPool.get(uriRef);
        }
        return nameSpace;
    }

    private long[] readXmlResourceMap(XmlResourceMapHeader chunkHeader) {
        int count = (int) ((chunkHeader.chunkSize - chunkHeader.headerSize) / 4);
        long[] resourceIds = new long[count];
        for (int i = 0; i < count; i++) {
            resourceIds[i] = Buffers.readUInt(buffer);
        }
        return resourceIds;
    }


    private ChunkHeader readChunkHeader() {
        // finished
        if (!buffer.hasRemaining()) {
            return null;
        }

        long begin = buffer.position();
        int chunkType = Buffers.readUShort(buffer);
        int headerSize = Buffers.readUShort(buffer);
        long chunkSize = Buffers.readUInt(buffer);

        switch (chunkType) {
            case ChunkType.XML:
                return new XmlHeader(chunkType, headerSize, chunkSize);
            case ChunkType.STRING_POOL:
                StringPoolHeader stringPoolHeader = new StringPoolHeader(chunkType, headerSize, chunkSize);
                stringPoolHeader.stringCount = Buffers.readUInt(buffer);
                stringPoolHeader.styleCount = Buffers.readUInt(buffer);
                stringPoolHeader.flags = Buffers.readUInt(buffer);
                stringPoolHeader.stringsStart = Buffers.readUInt(buffer);
                stringPoolHeader.stylesStart = Buffers.readUInt(buffer);
                buffer.position((int) (begin + headerSize));
                return stringPoolHeader;
            case ChunkType.XML_RESOURCE_MAP:
                buffer.position((int) (begin + headerSize));
                return new XmlResourceMapHeader(chunkType, headerSize, chunkSize);
            case ChunkType.XML_START_NAMESPACE:
            case ChunkType.XML_END_NAMESPACE:
            case ChunkType.XML_START_ELEMENT:
            case ChunkType.XML_END_ELEMENT:
            case ChunkType.XML_CDATA:
                XmlNodeHeader header = new XmlNodeHeader(chunkType, headerSize, chunkSize);
                header.lineNum = (int) Buffers.readUInt(buffer);
                header.commentRef = (int) Buffers.readUInt(buffer);
                buffer.position((int) (begin + headerSize));
                return header;
            case ChunkType.NULL:
                //buffer.advanceTo(begin + headerSize);
                //buffer.skip((int) (chunkSize - headerSize));
            default:
                throw new ParserException("Unexpected chunk type:" + chunkType);
        }
    }

    public void setLocale(Locale locale) {
        if (locale != null) {
            this.locale = locale;
        }
    }

    public Locale getLocale() {
        return locale;
    }

    public XmlStreamer getXmlStreamer() {
        return xmlStreamer;
    }

    public void setXmlStreamer(XmlStreamer xmlStreamer) {
        this.xmlStreamer = xmlStreamer;
    }
}
