package jp.shts.android.keyakifeed.models;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

/**
 */
public class Download {

    private static final String TAG = Download.class.getSimpleName();

    @NonNull
    private final Subject<String, String> busSingle =
            new SerializedSubject<>(PublishSubject.<String>create());

    @NonNull
    private final Subject<List<String>, List<String>> busList =
            new SerializedSubject<>(PublishSubject.<List<String>>create());


    @NonNull
    public Observable<String> toSingleObservable() {
        return busSingle;
    }

    @NonNull
    public Observable<List<String>> toListObservable() {
        return busList;
    }

    public void download(@Nullable String url) {
//        public static void
    }

    public void download(@Nullable List<String> urlList) {

    }

}
