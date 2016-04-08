package jp.shts.android.keyakifeed.api;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import jp.shts.android.keyakifeed.models2.Entries;
import jp.shts.android.keyakifeed.models2.Members;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.GsonConverterFactory;
import retrofit2.Retrofit;
import retrofit2.RxJavaCallAdapterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import rx.Observable;

public class KeyakiFeedApiClient {

    private static final String TAG = KeyakiFeedApiClient.class.getSimpleName();

    private static KeyakiFeedApiService apiService;

    public static Observable<Members> getAllMembers() {
        return getApiService().getAllMembers();
    }

    public static Observable<Entries> getAllEntries(int skip, int limit) {
        return getApiService().getAllEntries(skip, limit);
    }

    public static Observable<Entries> getMemberEntries(int memberId, int skip, int limit) {
        List<Integer> ids = new ArrayList<>();
        ids.add(memberId);
        return getApiService().getMemberEntries(ids, skip, limit);
    }

    public static Observable<Entries> getMemberEntries(List<Integer> memberIds, int skip, int limit) {
        return getApiService().getMemberEntries(memberIds, skip, limit);
    }

    public static void addFavorite(int memberId) {
        getApiService().changeFavoriteState(createFavoritePostBody(memberId, "incriment"));
    }

    public static void removeFavorite(int memberId) {
        getApiService().changeFavoriteState(createFavoritePostBody(memberId, "decriment"));
    }

    private static String createFavoritePostBody(int memberId, String action) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("member_id", String.valueOf(memberId));
            jsonObject.put("action", action);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    private static KeyakiFeedApiService getApiService() {
        if (apiService == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
//                    .addNetworkInterceptor(logging)
                    .build();
            Retrofit retrofit = new Retrofit.Builder()
                    .client(client)
                    .baseUrl("http://tk2-262-40775.vs.sakura.ne.jp/")
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            apiService = retrofit.create(KeyakiFeedApiService.class);
        }
        return apiService;
    }

    // http://tk2-262-40775.vs.sakura.ne.jp/members
    private interface KeyakiFeedApiService {
        @GET("/members")
        Observable<Members> getAllMembers();

        @GET("/entries")
        Observable<Entries> getAllEntries(@Query("skip") int skip, @Query("limit") int limit);

        @GET("/member/entries")
        Observable<Entries> getMemberEntries(
                @Query("id[]") List<Integer> ids, @Query("skip") int skip, @Query("limit") int limit);

        @POST("/favorite")
        Observable<Void> changeFavoriteState(@Body String json);
    }

}
