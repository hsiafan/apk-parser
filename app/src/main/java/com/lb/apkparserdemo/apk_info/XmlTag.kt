package com.lb.apkparserdemo.apk_info

import android.content.Context
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * an xml tag , includes its name, value and attributes
 * @param tagName the name of the xml tag . for example : <a>b</a> . the name of the tag is "a"
 */
class XmlTag(val tagName: String) {
    /** a hashmap of all of the tag attributes. example: <a c="d" e="f">b</a> . attributes: {{"c"="d"},{"e"="f"}}     */
    @JvmField
    var tagAttributes: HashMap<String, String>? = null

    /**list of inner text and xml tags*/
    @JvmField
    var innerTagsAndContent: ArrayList<Any>? = null

    companion object {
        @JvmStatic
        fun getXmlFromString(input: String): XmlTag? {
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = true
            val xpp = factory.newPullParser()
            xpp.setInput(StringReader(input))
            return getXmlRootTagOfXmlPullParser(
                xpp
            )
        }

        @JvmStatic
        fun getXmlRootTagOfXmlPullParser(xmlParser: XmlPullParser): XmlTag? {
            var currentTag: XmlTag? = null
            var rootTag: XmlTag? = null
            val tagsStack = Stack<XmlTag>()
            xmlParser.next()
            var eventType = xmlParser.eventType
            var doneParsing = false
            while (eventType != XmlPullParser.END_DOCUMENT && !doneParsing) {
                when (eventType) {
                    XmlPullParser.START_DOCUMENT -> {
                    }
                    XmlPullParser.START_TAG -> {
                        val xmlTagName = xmlParser.name
                        currentTag = XmlTag(xmlTagName)
                        if (tagsStack.isEmpty())
                            rootTag = currentTag
                        tagsStack.push(currentTag)
                        val numberOfAttributes = xmlParser.attributeCount
                        if (numberOfAttributes > 0) {
                            val attributes = HashMap<String, String>(numberOfAttributes)
                            for (i in 0 until numberOfAttributes) {
                                val attrName = xmlParser.getAttributeName(i)
                                val attrValue = xmlParser.getAttributeValue(i)
                                attributes[attrName] = attrValue
                            }
                            currentTag.tagAttributes = attributes
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        currentTag = tagsStack.pop()
                        if (!tagsStack.isEmpty()) {
                            val parentTag = tagsStack.peek()
                            parentTag.addInnerXmlTag(currentTag)
                            currentTag = parentTag
                        } else
                            doneParsing = true
                    }
                    XmlPullParser.TEXT -> {
                        val innerText = xmlParser.text
                        currentTag?.addInnerText(innerText)
                    }
                }
                eventType = xmlParser.next()
            }
            return rootTag
        }

        /**returns the root xml tag of the given xml resourceId , or null if not succeeded . */
        fun getXmlRootTagOfXmlFileResourceId(context: Context, xmlFileResourceId: Int): XmlTag? {
            val res = context.resources
            val xmlParser = res.getXml(xmlFileResourceId)
            return getXmlRootTagOfXmlPullParser(
                xmlParser
            )
        }
    }

    fun addInnerXmlTag(tag: XmlTag) {
        if (innerTagsAndContent == null)
            innerTagsAndContent = ArrayList()
        innerTagsAndContent!!.add(tag)
    }

    fun addInnerText(str: String) {
        if (innerTagsAndContent == null)
            innerTagsAndContent = ArrayList()
        innerTagsAndContent!!.add(str)
    }

    /**formats the xmlTag back to its string format,including its inner tags     */
    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("<").append(tagName)
        val numberOfAttributes = if (tagAttributes != null) tagAttributes!!.size else 0
        if (numberOfAttributes != 0)
            for ((key, value) in tagAttributes!!)
                sb.append(" ").append(key).append("=\"").append(value).append("\"")
        val numberOfInnerContent =
            if (innerTagsAndContent != null) innerTagsAndContent!!.size else 0
        if (numberOfInnerContent == 0)
            sb.append("/>")
        else {
            sb.append(">")
            for (innerItem in innerTagsAndContent!!)
                sb.append(innerItem.toString())
            sb.append("</").append(tagName).append(">")
        }
        return sb.toString()
    }

}
