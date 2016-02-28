package jp.shts.android.keyakifeed.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public abstract class ArrayRecyclerAdapter <T, VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<VH> implements Iterable<T> {

    private static final String TAG = ArrayRecyclerAdapter.class.getSimpleName();

    final Context context;
    final ArrayList<T> list;
    OnItemClickListener<T> onItemClickListener;
    OnItemLongClickListener<T> onItemLongClickListener;

    public interface OnItemClickListener<T> {
        void onItemClick(@NonNull View view, T item);
    }

    public interface OnItemLongClickListener<T> {
        boolean onItemLongClick(@NonNull View view, T item);
    }

    public ArrayRecyclerAdapter(@NonNull Context context) {
        this.context = context;
        this.list = new ArrayList<>();
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public T getItem(int position) {
        return list.get(position);
    }

    public void addItem(T item) {
        list.add(item);
    }

    public void addAll(Collection<T> items) {
        list.addAll(items);
    }

    public void addAll(int position, Collection<T> items) {
        list.addAll(position, items);
    }

    @UiThread
    public void addAllWithNotification(Collection<T> items) {
        int position = getItemCount();
        addAll(items);
        notifyItemInserted(position);
    }

    @UiThread
    public void reset(Collection<T> items) {
        clear();
        addAll(items);
        notifyDataSetChanged();
    }

    public Context getContext() {
        return context;
    }

    public void setOnItemClickListener(OnItemClickListener<T> listener) {
        onItemClickListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener<T> listener) {
        onItemLongClickListener = listener;
    }

    public void dispatchOnItemClick(View view, T item) {
        assert onItemClickListener != null;
        onItemClickListener.onItemClick(view, item);
    }

    public boolean dispatchOnItemLongClick(View view, T item) {
        assert onItemLongClickListener != null;
        return onItemLongClickListener.onItemLongClick(view, item);
    }

    public void clear() {
        list.clear();
    }

    @Override
    public Iterator<T> iterator() {
        return list.iterator();
    }
}
