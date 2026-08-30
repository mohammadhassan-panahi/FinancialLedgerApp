package com.example.util

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader

data class RssItem(
    val title: String,
    val link: String,
    val pubDate: String?,
    val description: String?
)

/** Parses standard RSS 2.0 XML (channel > item > title/link/pubDate/description). */
object RssParser {
    fun parse(xml: String): List<RssItem> {
        val items = mutableListOf<RssItem>()
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = false
        val parser = factory.newPullParser()
        parser.setInput(StringReader(xml))

        var eventType = parser.eventType
        var inItem = false
        var title: String? = null
        var link: String? = null
        var pubDate: String? = null
        var description: String? = null
        var currentTag: String? = null

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    currentTag = parser.name
                    if (currentTag.equals("item", ignoreCase = true)) {
                        inItem = true
                        title = null; link = null; pubDate = null; description = null
                    }
                }
                XmlPullParser.TEXT, XmlPullParser.CDSECT -> {
                    if (inItem) {
                        val text = parser.text?.trim()
                        if (!text.isNullOrEmpty()) {
                            when (currentTag?.lowercase()) {
                                "title" -> title = (title.orEmpty() + text)
                                "link" -> link = (link.orEmpty() + text)
                                "pubdate" -> pubDate = (pubDate.orEmpty() + text)
                                "description" -> description = (description.orEmpty() + text)
                            }
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (parser.name.equals("item", ignoreCase = true) && inItem) {
                        val finalTitle = title?.trim()
                        val finalLink = link?.trim()
                        if (!finalTitle.isNullOrEmpty() && !finalLink.isNullOrEmpty()) {
                            items.add(RssItem(finalTitle, finalLink, pubDate?.trim(), description?.trim()))
                        }
                        inItem = false
                    }
                    currentTag = null
                }
            }
            eventType = try { parser.next() } catch (e: Exception) { XmlPullParser.END_DOCUMENT }
        }
        return items
    }
}
