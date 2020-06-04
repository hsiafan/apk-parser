package com.lb.apkparserdemo.apk_info

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Xml
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import org.xmlpull.v1.XmlPullParser

object XmlDrawableParser {
//    https://stackoverflow.com/a/59023594/878126 https://stackoverflow.com/a/62114760/878126
    /**
     * Create a drawable from a binary XML byte array.
     *
     * @param context Any context.
     * @param binXml  Byte array containing the binary XML.
     * @return The drawable or null it couldn't be created.
     */
    @SuppressLint("PrivateApi", "DiscouragedPrivateApi")
    fun tryParseDrawable(context: Context, binXml: ByteArray): Drawable? {
        try {
            val xmlBlock = Class.forName("android.content.res.XmlBlock")
            val xmlBlockCtr = xmlBlock.getConstructor(ByteArray::class.java)
            val xmlParserNew = xmlBlock.getDeclaredMethod("newParser")
            xmlBlockCtr.isAccessible = true
            xmlParserNew.isAccessible = true
            var parser = xmlParserNew.invoke(xmlBlockCtr.newInstance(binXml)) as XmlPullParser
            return try {
                val attrs = Xml.asAttributeSet(parser)
                while (true)
                    if (parser.next() == XmlPullParser.START_TAG)
                        break
                VectorDrawableCompat.createFromXmlInner(context.resources, parser, attrs, null)
            } catch (e: Exception) {
                parser = xmlParserNew.invoke(xmlBlockCtr.newInstance(binXml)) as XmlPullParser
                Drawable.createFromXml(context.resources, parser)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}
