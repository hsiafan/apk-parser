package net.dongliu.apk.parser.parser;

import net.dongliu.apk.parser.exception.ParserException;
import net.dongliu.apk.parser.struct.ChunkHeader;
import net.dongliu.apk.parser.struct.ChunkType;
import net.dongliu.apk.parser.struct.ResourceValue;
import net.dongliu.apk.parser.struct.StringPool;
import net.dongliu.apk.parser.struct.StringPoolHeader;
import net.dongliu.apk.parser.struct.resource.ResourceTable;
import net.dongliu.apk.parser.struct.xml.Attribute;
import net.dongliu.apk.parser.struct.xml.Attributes;
import net.dongliu.apk.parser.struct.xml.NullHeader;
import net.dongliu.apk.parser.struct.xml.XmlCData;
import net.dongliu.apk.parser.struct.xml.XmlHeader;
import net.dongliu.apk.parser.struct.xml.XmlNamespaceEndTag;
import net.dongliu.apk.parser.struct.xml.XmlNamespaceStartTag;
import net.dongliu.apk.parser.struct.xml.XmlNodeEndTag;
import net.dongliu.apk.parser.struct.xml.XmlNodeHeader;
import net.dongliu.apk.parser.struct.xml.XmlNodeStartTag;
import net.dongliu.apk.parser.struct.xml.XmlResourceMapHeader;
import net.dongliu.apk.parser.utils.Buffers;
import net.dongliu.apk.parser.utils.Locales;
import net.dongliu.apk.parser.utils.ParseUtils;
import net.dongliu.apk.parser.utils.Strings;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Android Binary XML format
 * see http://justanapplication.wordpress.com/category/android/android-binary-xml/
 *
 * @author dongliu
 */
public class BinaryXmlParser {

    /**
     * By default the data buffer Chunks is buffer little-endian byte order both at runtime and when stored buffer
     * files.
     */
    private final ByteOrder byteOrder = ByteOrder.LITTLE_ENDIAN;
    private StringPool stringPool;
    // some attribute name stored by resource id
    private String[] resourceMap;
    private final ByteBuffer buffer;
    private XmlStreamer xmlStreamer;
    private final ResourceTable resourceTable;
    /**
     * default locale.
     */
    private Locale locale = Locales.any;

    public BinaryXmlParser(final ByteBuffer buffer, final ResourceTable resourceTable) {
        this.buffer = buffer.duplicate();
        this.buffer.order(this.byteOrder);
        this.resourceTable = resourceTable;
    }

    /**
     * Parse binary xml.
     */
    public void parse() {
        final ChunkHeader firstChunkHeader = this.readChunkHeader();
        if (firstChunkHeader == null) {
            return;
        }

        switch (firstChunkHeader.getChunkType()) {
            case ChunkType.XML:
            case ChunkType.NULL:
                break;
            case ChunkType.STRING_POOL:
            default:
                // strange chunk header type, just skip this chunk header?
        }

        // read string pool chunk
        final ChunkHeader stringPoolChunkHeader = this.readChunkHeader();
        if (stringPoolChunkHeader == null) {
            return;
        }

        ParseUtils.checkChunkType(ChunkType.STRING_POOL, stringPoolChunkHeader.getChunkType());
        this.stringPool = ParseUtils.readStringPool(this.buffer, (StringPoolHeader) stringPoolChunkHeader);

        // read on chunk, check if it was an optional XMLResourceMap chunk
        ChunkHeader chunkHeader = this.readChunkHeader();
        if (chunkHeader == null) {
            return;
        }

        if (chunkHeader.getChunkType() == ChunkType.XML_RESOURCE_MAP) {
            final long[] resourceIds = this.readXmlResourceMap((XmlResourceMapHeader) chunkHeader);
            this.resourceMap = new String[resourceIds.length];
            for (int i = 0; i < resourceIds.length; i++) {
                this.resourceMap[i] = Attribute.AttrIds.getString(resourceIds[i]);
            }
            chunkHeader = this.readChunkHeader();
        }

        while (chunkHeader != null) {
                /*if (chunkHeader.chunkType == ChunkType.XML_END_NAMESPACE) {
                    break;
                }*/
            final long beginPos = this.buffer.position();
            switch (chunkHeader.getChunkType()) {
                case ChunkType.XML_END_NAMESPACE:
                    final XmlNamespaceEndTag xmlNamespaceEndTag = this.readXmlNamespaceEndTag();
                    this.xmlStreamer.onNamespaceEnd(xmlNamespaceEndTag);
                    break;
                case ChunkType.XML_START_NAMESPACE:
                    final XmlNamespaceStartTag namespaceStartTag = this.readXmlNamespaceStartTag();
                    this.xmlStreamer.onNamespaceStart(namespaceStartTag);
                    break;
                case ChunkType.XML_START_ELEMENT:
                    final XmlNodeStartTag xmlNodeStartTag = this.readXmlNodeStartTag();
                    break;
                case ChunkType.XML_END_ELEMENT:
                    final XmlNodeEndTag xmlNodeEndTag = this.readXmlNodeEndTag();
                    break;
                case ChunkType.XML_CDATA:
                    final XmlCData xmlCData = this.readXmlCData();
                    break;
                default:
                    if (chunkHeader.getChunkType() >= ChunkType.XML_FIRST_CHUNK &&
                            chunkHeader.getChunkType() <= ChunkType.XML_LAST_CHUNK) {
                        Buffers.skip(this.buffer, chunkHeader.getBodySize());
                    } else {
                        throw new ParserException("Unexpected chunk type:" + chunkHeader.getChunkType());
                    }
            }
            Buffers.position(this.buffer, beginPos + chunkHeader.getBodySize());
            chunkHeader = this.readChunkHeader();
        }
    }

    private XmlCData readXmlCData() {
        final XmlCData xmlCData = new XmlCData();
        final int dataRef = this.buffer.getInt();
        if (dataRef > 0) {
            xmlCData.setData(this.stringPool.get(dataRef));
        }
        xmlCData.setTypedData(ParseUtils.readResValue(this.buffer, this.stringPool));
        //noinspection StatementWithEmptyBody
        if (this.xmlStreamer != null) {
            //TODO: to know more about cdata. some cdata appears buffer xml tags
//            String value = xmlCData.toStringValue(resourceTable, locale);
//            xmlCData.setValue(value);
//            xmlStreamer.onCData(xmlCData);
        }
        return xmlCData;
    }

    private XmlNodeEndTag readXmlNodeEndTag() {
        final XmlNodeEndTag xmlNodeEndTag = new XmlNodeEndTag();
        final int nsRef = this.buffer.getInt();
        final int nameRef = this.buffer.getInt();
        if (nsRef > 0) {
            xmlNodeEndTag.setNamespace(this.stringPool.get(nsRef));
        }
        xmlNodeEndTag.setName(this.stringPool.get(nameRef));
        if (this.xmlStreamer != null) {
            this.xmlStreamer.onEndTag(xmlNodeEndTag);
        }
        return xmlNodeEndTag;
    }

    private XmlNodeStartTag readXmlNodeStartTag() {
        final int nsRef = this.buffer.getInt();
        final int nameRef = this.buffer.getInt();
        final XmlNodeStartTag xmlNodeStartTag = new XmlNodeStartTag();
        if (nsRef > 0) {
            xmlNodeStartTag.setNamespace(this.stringPool.get(nsRef));
        }
        xmlNodeStartTag.setName(this.stringPool.get(nameRef));

        // read attributes.
        // attributeStart and attributeSize are always 20 (0x14)
        final int attributeStart = Buffers.readUShort(this.buffer);
        final int attributeSize = Buffers.readUShort(this.buffer);
        final int attributeCount = Buffers.readUShort(this.buffer);
        final int idIndex = Buffers.readUShort(this.buffer);
        final int classIndex = Buffers.readUShort(this.buffer);
        final int styleIndex = Buffers.readUShort(this.buffer);

        // read attributes
        final Attributes attributes = new Attributes(attributeCount);
        for (int count = 0; count < attributeCount; count++) {
            final Attribute attribute = this.readAttribute();
            if (this.xmlStreamer != null) {
                String value = attribute.toStringValue(this.resourceTable, this.locale);
                if (BinaryXmlParser.intAttributes.contains(attribute.getName()) && Strings.isNumeric(value)) {
                    try {
                        value = this.getFinalValueAsString(attribute.getName(), value);
                    } catch (final Exception ignore) {
                    }
                }
                attribute.setValue(value);
                attributes.set(count, attribute);
            }
        }
        xmlNodeStartTag.setAttributes(attributes);

        if (this.xmlStreamer != null) {
            this.xmlStreamer.onStartTag(xmlNodeStartTag);
        }

        return xmlNodeStartTag;
    }

    private static final Set<String> intAttributes = new HashSet<>(
            Arrays.asList("screenOrientation", "configChanges", "windowSoftInputMode",
                    "launchMode", "installLocation", "protectionLevel"));

    //trans int attr value to string
    private String getFinalValueAsString(final String attributeName, final String str) {
        final int value = Integer.parseInt(str);
        switch (attributeName) {
            case "screenOrientation":
                return AttributeValues.getScreenOrientation(value);
            case "configChanges":
                return AttributeValues.getConfigChanges(value);
            case "windowSoftInputMode":
                return AttributeValues.getWindowSoftInputMode(value);
            case "launchMode":
                return AttributeValues.getLaunchMode(value);
            case "installLocation":
                return AttributeValues.getInstallLocation(value);
            case "protectionLevel":
                return AttributeValues.getProtectionLevel(value);
            default:
                return str;
        }
    }

    private Attribute readAttribute() {
        final int nsRef = this.buffer.getInt();
        final int nameRef = this.buffer.getInt();
        final Attribute attribute = new Attribute();
        if (nsRef > 0) {
            final String namespace = this.stringPool.get(nsRef);
            //TODO fix this part in a better way. Workaround for this: https://github.com/hsiafan/apk-parser/issues/122
            if (!namespace.equals("http://schemas.android.com/apk/res/android"))
                attribute.setNamespace(namespace);
        }

        attribute.setName(this.stringPool.get(nameRef));
        if (attribute.getName().isEmpty() && this.resourceMap != null && nameRef < this.resourceMap.length) {
            // some processed apk file make the string pool value empty, if it is a xmlmap attr.
            attribute.setName(this.resourceMap[nameRef]);
            //TODO: how to get the namespace of attribute
        }

        final int rawValueRef = this.buffer.getInt();
        if (rawValueRef > 0) {
            attribute.setRawValue(this.stringPool.get(rawValueRef));
        }
        final ResourceValue resValue = ParseUtils.readResValue(this.buffer, this.stringPool);
        attribute.setTypedValue(resValue);

        return attribute;
    }

    private XmlNamespaceStartTag readXmlNamespaceStartTag() {
        final int prefixRef = this.buffer.getInt();
        final int uriRef = this.buffer.getInt();
        final XmlNamespaceStartTag nameSpace = new XmlNamespaceStartTag();
        if (prefixRef > 0) {
            nameSpace.setPrefix(this.stringPool.get(prefixRef));
        }
        if (uriRef > 0) {
            nameSpace.setUri(this.stringPool.get(uriRef));
        }
        return nameSpace;
    }

    private XmlNamespaceEndTag readXmlNamespaceEndTag() {
        final int prefixRef = this.buffer.getInt();
        final int uriRef = this.buffer.getInt();
        final XmlNamespaceEndTag nameSpace = new XmlNamespaceEndTag();
        if (prefixRef > 0) {
            nameSpace.setPrefix(this.stringPool.get(prefixRef));
        }
        if (uriRef > 0) {
            nameSpace.setUri(this.stringPool.get(uriRef));
        }
        return nameSpace;
    }

    private long[] readXmlResourceMap(final XmlResourceMapHeader chunkHeader) {
        final int count = chunkHeader.getBodySize() / 4;
        final long[] resourceIds = new long[count];
        for (int i = 0; i < count; i++) {
            resourceIds[i] = Buffers.readUInt(this.buffer);
        }
        return resourceIds;
    }


    private ChunkHeader readChunkHeader() {
        // finished
        if (!this.buffer.hasRemaining()) {
            return null;
        }

        final long begin = this.buffer.position();
        final int chunkType = Buffers.readUShort(this.buffer);
        final int headerSize = Buffers.readUShort(this.buffer);
        final long chunkSize = Buffers.readUInt(this.buffer);

        switch (chunkType) {
            case ChunkType.XML:
                return new XmlHeader(chunkType, headerSize, chunkSize);
            case ChunkType.STRING_POOL:
                final StringPoolHeader stringPoolHeader = new StringPoolHeader(headerSize, chunkSize);
                stringPoolHeader.setStringCount(Buffers.readUInt(this.buffer));
                stringPoolHeader.setStyleCount(Buffers.readUInt(this.buffer));
                stringPoolHeader.setFlags(Buffers.readUInt(this.buffer));
                stringPoolHeader.setStringsStart(Buffers.readUInt(this.buffer));
                stringPoolHeader.setStylesStart(Buffers.readUInt(this.buffer));
                Buffers.position(this.buffer, begin + headerSize);
                return stringPoolHeader;
            case ChunkType.XML_RESOURCE_MAP:
                Buffers.position(this.buffer, begin + headerSize);
                return new XmlResourceMapHeader(chunkType, headerSize, chunkSize);
            case ChunkType.XML_START_NAMESPACE:
            case ChunkType.XML_END_NAMESPACE:
            case ChunkType.XML_START_ELEMENT:
            case ChunkType.XML_END_ELEMENT:
            case ChunkType.XML_CDATA:
                final XmlNodeHeader header = new XmlNodeHeader(chunkType, headerSize, chunkSize);
                header.setLineNum((int) Buffers.readUInt(this.buffer));
                header.setCommentRef((int) Buffers.readUInt(this.buffer));
                Buffers.position(this.buffer, begin + headerSize);
                return header;
            case ChunkType.NULL:
                return new NullHeader(chunkType, headerSize, chunkSize);
            default:
                throw new ParserException("Unexpected chunk type:" + chunkType);
        }
    }

    public void setLocale(final Locale locale) {
        if (locale != null) {
            this.locale = locale;
        }
    }

    public Locale getLocale() {
        return this.locale;
    }

    public XmlStreamer getXmlStreamer() {
        return this.xmlStreamer;
    }

    public void setXmlStreamer(final XmlStreamer xmlStreamer) {
        this.xmlStreamer = xmlStreamer;
    }
}
