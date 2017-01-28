package jp.shts.android.keyakifeed.api;

import android.support.annotation.CheckResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import jp.shts.android.keyakifeed.BuildConfig;
import jp.shts.android.keyakifeed.models.Entries;
import jp.shts.android.keyakifeed.models.Entry;
import jp.shts.android.keyakifeed.models.Matome;
import jp.shts.android.keyakifeed.models.Member;
import jp.shts.android.keyakifeed.models.Members;
import jp.shts.android.keyakifeed.models.Reports;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.GsonConverterFactory;
import retrofit2.Retrofit;
import retrofit2.RxJavaCallAdapterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

public class KeyakiFeedApiClient {

    private static final String TAG = KeyakiFeedApiClient.class.getSimpleName();

    private static KeyakiFeedApiService apiService;

    @CheckResult
    public static Observable<Members> getAllMembers() {
        return getApiService().getAllMembers();
    }

    @CheckResult
    public static Observable<Member> getMember(int id) {
        return getApiService().getMember(id);
    }

    @CheckResult
    public static Observable<Entries> getAllEntries(int skip, int limit) {
        return getApiService().getAllEntries(skip, limit);
    }

    @CheckResult
    public static Observable<Entry> getEntry(int entryId) {
        return getApiService().getEntry(entryId);
    }

    @CheckResult
    public static Observable<Reports> getAllReports(int skip, int limit) {
        return getApiService().getAllReports(skip, limit);
    }

    @CheckResult
    public static Observable<Entries> getMemberEntries(int memberId, int skip, int limit) {
        List<Integer> ids = new ArrayList<>();
        ids.add(memberId);
        return getApiService().getMemberEntries(ids, skip, limit);
    }

    @CheckResult
    public static Observable<Entries> getMemberEntries(List<Integer> memberIds, int skip, int limit) {
        return getApiService().getMemberEntries(memberIds, skip, limit);
    }

    @CheckResult
    public static Observable<Void> addFavorite(int memberId) {
        return getApiService().changeFavoriteState(createFavoritePostBody(memberId, "incriment"));
    }

    @CheckResult
    public static Observable<Void> removeFavorite(int memberId) {
        return getApiService().changeFavoriteState(createFavoritePostBody(memberId, "decriment"));
    }

    @CheckResult
    public static Observable<List<Matome>> getMatomeFeeds() {
        return getApiService().getMatomeFeeds();
    }

    @CheckResult
    public static Observable<Void> registrationId(String regId) {
        return getApiService().registrationId(createRegistrationPostBody(regId));
    }

    @CheckResult
    public static Observable<Void> unregistrationId(String regId) {
        return getApiService().unregistrationId(createRegistrationPostBody(regId));
    }

    private static HashMap<String, String> createRegistrationPostBody(String regId) {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("reg_id", regId);
        return hashMap;
    }

    private static HashMap<String, String> createFavoritePostBody(int memberId, String action) {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("member_id", String.valueOf(memberId));
        hashMap.put("action", action);
        return hashMap;
    }

    private static synchronized KeyakiFeedApiService getApiService() {
        if (apiService == null) {
            Retrofit retrofit = new Retrofit.Builder()
                    .client(createOkHttpClient())
                    .baseUrl(BuildConfig.BASE_URL)
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            apiService = retrofit.create(KeyakiFeedApiService.class);
        }
        return apiService;
    }

    private static OkHttpClient createOkHttpClient() {
        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            return new OkHttpClient.Builder()
                    .addNetworkInterceptor(logging)
                    .build();
        } else {
            return new OkHttpClient.Builder().build();
        }
    }

    private interface KeyakiFeedApiService {

        @GET("/members")
        Observable<Members> getAllMembers();

        @GET("/entries")
        Observable<Entries> getAllEntries(@Query("skip") int skip, @Query("limit") int limit);

        @GET("/entries/{id}")
        Observable<Entry> getEntry(@Path("id") int id);

        @GET("/reports")
        Observable<Reports> getAllReports(@Query("skip") int skip, @Query("limit") int limit);

        @GET("/members/{id}")
        Observable<Member> getMember(@Path("id") int id);

        @GET("/member/entries")
        Observable<Entries> getMemberEntries(
                @Query("ids[]") List<Integer> ids, @Query("skip") int skip, @Query("limit") int limit);

        @Headers({
                "Accept: application/json",
                "Content-type: application/json"
        })
        @POST("/favorite")
        Observable<Void> changeFavoriteState(@Body HashMap<String, String> body);

        @GET("/matomes")
        Observable<List<Matome>> getMatomeFeeds();

        @Headers({
                "Accept: application/json",
                "Content-type: application/json"
        })
        @POST("/registration")
        Observable<Void> registrationId(@Body HashMap<String, String> body);

        @Headers({
                "Accept: application/json",
                "Content-type: application/json"
        })
        @POST("/unregistration")
        Observable<Void> unregistrationId(@Body HashMap<String, String> body);
    }

}
