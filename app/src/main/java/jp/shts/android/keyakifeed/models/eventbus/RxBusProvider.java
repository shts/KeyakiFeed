package jp.shts.android.keyakifeed.models.eventbus;

/**
 */
public class RxBusProvider {

    private static final String TAG = RxBusProvider.class.getSimpleName();

    private static final RxBus BUS = new RxBus();

    private RxBusProvider() {
        // No instances.
    }

    public static RxBus getInstance() {
        return BUS;
    }
}
