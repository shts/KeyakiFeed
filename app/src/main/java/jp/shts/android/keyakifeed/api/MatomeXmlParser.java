package jp.shts.android.keyakifeed.api;

import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;

import jp.shts.android.keyakifeed.entities.FeedItem;
import jp.shts.android.keyakifeed.entities.FeedItemList;

public class MatomeXmlParser {

    private static final String TAG = MatomeXmlParser.class.getSimpleName();

    public static FeedItemList parse(InputStream data) {
        final FeedItemList feedItemList = new FeedItemList();
        FeedItem feedItem = new FeedItem();
        boolean channelFlag = false;

        String tag;
        final XmlPullParser parser = Xml.newPullParser();
        try {
            parser.setInput(data, "UTF-8");
            int eventType = parser.getEventType();
            String siteTitle = null, siteDescription = null, siteUrl = null;
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        tag = parser.getName();
                        if ("channel".equals(tag)) {
                            channelFlag = true;
                        } else if ("item".equals(tag)) {
                            feedItem = new FeedItem();
                            feedItem.siteTitle = siteTitle;
                            feedItem.siteDescription = siteDescription;
                            feedItem.siteUrl = siteUrl;

                        } else if ("title".equals(tag)) {
                            if (channelFlag) {
                                siteTitle = parser.nextText();
                            } else {
                                feedItem.title = parser.nextText();
                            }
                        } else if ("description".equals(tag)) {
                            if (channelFlag) {
                                siteDescription = parser.nextText();
                            } else {
                                feedItem.description = parser.nextText();
                            }
                        } else if ("link".equals(tag)) {
                            if (channelFlag) {
                                String url = parser.nextText();
                                if (!TextUtils.isEmpty(url)) {
                                    siteUrl = url;
                                }
                            } else {
                                feedItem.url = parser.nextText();
                            }
                        } else if ("date".equals(tag)) {
                            feedItem.date = parser.nextText();
                        } else if ("subject".equals(tag)) {
                            feedItem.subject = parser.nextText();
                        } else if ("encoded".equals(tag)) {
                            feedItem.content = parser.nextText();
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        tag = parser.getName();
                        if ("channel".equals(tag)) {
                            channelFlag = false;
                        } else if ("item".equals(tag)) {
                            feedItemList.add(feedItem);
                        }
                        break;
                }
                eventType = parser.next();
            }
        } catch (XmlPullParserException e) {
            Log.e(TAG, "XmlPullParserException", e);
        } catch (IOException e) {
            Log.e(TAG, "IOException", e);
        }
        return feedItemList;
    }

}
