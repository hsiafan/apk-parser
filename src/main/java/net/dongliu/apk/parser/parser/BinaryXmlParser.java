package net.dongliu.apk.parser.parser;

import net.dongliu.apk.parser.bean.Locales;
import net.dongliu.apk.parser.exception.ParserException;
import net.dongliu.apk.parser.io.TellableInputStream;
import net.dongliu.apk.parser.struct.*;
import net.dongliu.apk.parser.struct.xml.*;
import net.dongliu.apk.parser.utils.ParseUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

/**
 * Android Binary XML format
 * see http://justanapplication.wordpress.com/category/android/android-binary-xml/
 *
 * @author dongliu
 */
public class BinaryXmlParser {


    /**
     * By default the data in Chunks is in little-endian byte order both at runtime and when stored in files.
     */
    private ByteOrder byteOrder = ByteOrder.LITTLE;
    private StringPool stringPool;
    private String[] resourceMap;
    private TellableInputStream in;
    private long fileSize;
    private XmlStreamer xmlStreamer;

    /**
     * default locale.
     */
    private Locale locale = Locales.any;

    public BinaryXmlParser(InputStream in, long fileSize) {
        this.in = new TellableInputStream(in, byteOrder);
        this.fileSize = fileSize;
    }

    /**
     * Parse binary xml.
     *
     * @throws java.io.IOException
     */
    public void parse() throws IOException {
        try {
            ChunkHeader chunkHeader = readChunkHeader();
            if (chunkHeader.chunkType != ChunkType.XML) {
                //TODO: may be a plain xml file.
                return;
            }
            XmlHeader xmlHeader = (XmlHeader) chunkHeader;

            // read string pool chunk
            chunkHeader = readChunkHeader();
            ParseUtils.checkChunkType(ChunkType.STRING_POOL, chunkHeader.chunkType);
            stringPool = ParseUtils.readStringPool(in, (StringPoolHeader) chunkHeader);

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
                if (chunkHeader.chunkType == ChunkType.XML_END_NAMESPACE) {
                    break;
                }
                long beginPos = in.tell();
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
                            in.skip((int) (chunkHeader.chunkSize - chunkHeader.headerSize));
                        } else {
                            throw new ParserException("Unexpected chunk type:" + chunkHeader.chunkType);
                        }
                }
                in.advanceToPos(beginPos + chunkHeader.chunkSize - chunkHeader.headerSize);
                chunkHeader = readChunkHeader();
            }
        } finally {
            this.in.close();
        }
    }

    private XmlCData readXmlCData() throws IOException {
        XmlCData xmlCData = new XmlCData();
        int dataRef = in.readInt();
        if (dataRef > 0) {
            xmlCData.data = stringPool.get(dataRef);
        }
        xmlCData.typedData = ParseUtils.readResValue(in, stringPool);
        if (xmlStreamer != null) {
            xmlStreamer.onCData(xmlCData);
        }
        return xmlCData;
    }

    private XmlNodeEndTag readXmlNodeEndTag() throws IOException {
        XmlNodeEndTag xmlNodeEndTag = new XmlNodeEndTag();
        int nsRef = in.readInt();
        int nameRef = in.readInt();
        if (nsRef > 0) {
            xmlNodeEndTag.namespace = stringPool.get(nsRef);
        }
        xmlNodeEndTag.name = stringPool.get(nameRef);
        if (xmlStreamer != null) {
            xmlStreamer.onEndTag(xmlNodeEndTag);
        }
        return xmlNodeEndTag;
    }

    private XmlNodeStartTag readXmlNodeStartTag() throws IOException {
        int nsRef = in.readInt();
        int nameRef = in.readInt();
        XmlNodeStartTag xmlNodeStartTag = new XmlNodeStartTag();
        if (nsRef > 0) {
            xmlNodeStartTag.namespace = stringPool.get(nsRef);
        }
        xmlNodeStartTag.name = stringPool.get(nameRef);

        if (xmlStreamer != null) {
            xmlStreamer.onStartTag(xmlNodeStartTag);
        }

        // read attributes.
        // attributeStart and attributeSize are always 20 (0x14)
        int attributeStart = in.readUShort();
        int attributeSize = in.readUShort();
        int attributeCount = in.readUShort();
        int idIndex = in.readUShort();
        int classIndex = in.readUShort();
        int styleIndex = in.readUShort();

        // read attributes
        for (int count = 0; count < attributeCount; count++) {
            Attribute attribute = readAttribute();
            if (xmlStreamer != null) {
                xmlStreamer.onAttribute(attribute);
            }
        }

        return xmlNodeStartTag;
    }

    private Attribute readAttribute() throws IOException {
        int nsRef = in.readInt();
        int nameRef = in.readInt();
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

        int rawValueRef = in.readInt();
        if (rawValueRef > 0) {
            attribute.rawValue = stringPool.get(rawValueRef);
        }
        attribute.typedValue = ParseUtils.readResValue(in, stringPool,
                "style".equals(attribute.name) || "theme".equals(attribute.name));

        return attribute;
    }

    private XmlNamespaceStartTag readXmlNamespaceStartTag() throws IOException {
        int prefixRef = in.readInt();
        int uriRef = in.readInt();
        XmlNamespaceStartTag nameSpace = new XmlNamespaceStartTag();
        if (prefixRef > 0) {
            nameSpace.prefix = stringPool.get(prefixRef);
        }
        if (uriRef > 0) {
            nameSpace.uri = stringPool.get(uriRef);
        }
        return nameSpace;
    }

    private XmlNamespaceEndTag readXmlNamespaceEndTag() throws IOException {
        int prefixRef = in.readInt();
        int uriRef = in.readInt();
        XmlNamespaceEndTag nameSpace = new XmlNamespaceEndTag();
        if (prefixRef > 0) {
            nameSpace.prefix = stringPool.get(prefixRef);
        }
        if (uriRef > 0) {
            nameSpace.uri = stringPool.get(uriRef);
        }
        return nameSpace;
    }

    private long[] readXmlResourceMap(XmlResourceMapHeader chunkHeader) throws IOException {
        int count = (int) ((chunkHeader.chunkSize - chunkHeader.headerSize) / 4);
        long[] resourceIds = new long[count];
        for (int i = 0; i < count; i++) {
            resourceIds[i] = in.readUInt();
        }
        return resourceIds;
    }


    private ChunkHeader readChunkHeader() throws IOException {
        // finished
        if (in.tell() == this.fileSize) {
            return null;
        }

        long begin = in.tell();
        int chunkType = in.readUShort();
        int headerSize = in.readUShort();
        long chunkSize = in.readUInt();

        switch (chunkType) {
            case ChunkType.XML:
                return new XmlHeader(chunkType, headerSize, chunkSize);
            case ChunkType.STRING_POOL:
                StringPoolHeader stringPoolHeader = new StringPoolHeader(chunkType, headerSize, chunkSize);
                stringPoolHeader.stringCount = in.readUInt();
                stringPoolHeader.styleCount = in.readUInt();
                stringPoolHeader.flags = in.readUInt();
                stringPoolHeader.stringsStart = in.readUInt();
                stringPoolHeader.stylesStart = in.readUInt();
                in.advanceToPos(begin + headerSize);
                return stringPoolHeader;
            case ChunkType.XML_RESOURCE_MAP:
                in.advanceToPos(begin + headerSize);
                return new XmlResourceMapHeader(chunkType, headerSize, chunkSize);
            case ChunkType.XML_START_NAMESPACE:
            case ChunkType.XML_END_NAMESPACE:
            case ChunkType.XML_START_ELEMENT:
            case ChunkType.XML_END_ELEMENT:
            case ChunkType.XML_CDATA:
                XmlNodeHeader header = new XmlNodeHeader(chunkType, headerSize, chunkSize);
                header.lineNum = (int) in.readUInt();
                header.commentRef = (int) in.readUInt();
                in.advanceToPos(begin + headerSize);
                return header;
            case ChunkType.NULL:
                //in.advanceToPos(begin + headerSize);
                //in.skip((int) (chunkSize - headerSize));
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
