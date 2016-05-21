package jp.shts.android.keyakifeed.entities;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class FeedItemList extends ArrayList<FeedItem> {

    private static final String TAG = FeedItemList.class.getSimpleName();
    private final DateComparator comparator = new DateComparator(DateComparator.DESC);

    public synchronized void sort() {
        // java.lang.ArrayIndexOutOfBoundsException
        try {
            Collections.sort(this, comparator);
        } catch (ArrayIndexOutOfBoundsException e) {
            Log.e(TAG, "failed to sort", e);
        }
    }

    private static class DateComparator implements Comparator<FeedItem> {
        public static final int ASC = 1;    //昇順
        public static final int DESC = -1;    //降順
        private int sort = ASC;    //デフォルトは昇順
        public DateComparator(int sort) {
            this.sort = sort;
        }
        @Override
        public int compare(FeedItem item0, FeedItem item1) {
            return item0.getDate().compareTo(item1.getDate()) * sort;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (FeedItem feedItem : this) {
            sb.append("item { ").append("\n")
                    .append(feedItem.toString())
                    .append("\n").append(" } ").append("\n");
        }
        return sb.toString();
    }
}
