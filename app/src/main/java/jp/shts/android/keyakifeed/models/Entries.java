package jp.shts.android.keyakifeed.models;

import java.util.ArrayList;

public class Entries extends ArrayList<Entry> {

    private static final String TAG = Entries.class.getSimpleName();

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
