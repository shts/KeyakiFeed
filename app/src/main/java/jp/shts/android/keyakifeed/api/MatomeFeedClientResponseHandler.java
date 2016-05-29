package jp.shts.android.keyakifeed.api;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;

import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.http.Header;

import java.io.ByteArrayInputStream;

import jp.shts.android.keyakifeed.entities.FeedItemList;
import jp.shts.android.keyakifeed.models.eventbus.BusHolder;

public class MatomeFeedClientResponseHandler extends AsyncHttpResponseHandler {

    private static final String TAG = MatomeFeedClientResponseHandler.class.getSimpleName();

    private static final HandlerThread HANDLER_THREAD = new HandlerThread("MatomeFeedClient-Thread");
    private static final Handler HANDLER;
    static {
        HANDLER_THREAD.start();
        HANDLER = new Handler(HANDLER_THREAD.getLooper());
    }

    private FeedItemList feedItemList = new FeedItemList();
    private int counter = 0;
    private final int limit;

    public MatomeFeedClientResponseHandler(int limit) {
        super(HANDLER.getLooper());
        this.limit = limit;
    }

    @Override
    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
        Log.v(TAG, "onSuccess! statusCode " + statusCode);
        countup();
        FeedItemList feedItemList = MatomeXmlParser.parse(new ByteArrayInputStream(responseBody));
        this.feedItemList.addAll(feedItemList);
    }

    @Override
    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
        Log.v(TAG, "onFailure! statusCode " + statusCode);
        countup();
    }

    private void countup() {
        counter++;
        if (limit <= counter) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    BusHolder.get().post(new MatomeFeedClient.GetMatomeFeedCallback(feedItemList, null));
                }
            });
        }
    }

}
