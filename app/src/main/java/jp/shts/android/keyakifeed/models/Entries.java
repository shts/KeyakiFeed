package jp.shts.android.keyakifeed.models;

import java.util.ArrayList;
import java.util.List;

public class Entries extends ArrayList<Entry> {

    private static final String TAG = Entries.class.getSimpleName();

    public List<String> getImageUrlList() {
        List<String> imageUrlList = new ArrayList<>();
        for (Entry e : this) {
            imageUrlList.addAll(e.getImageUrlList());
        }
        return imageUrlList;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Entries").append("\n");
        for (Entry entry : this) {
            sb.append(entry.toString()).append("\n");
        }
        return sb.toString();
    }
}
