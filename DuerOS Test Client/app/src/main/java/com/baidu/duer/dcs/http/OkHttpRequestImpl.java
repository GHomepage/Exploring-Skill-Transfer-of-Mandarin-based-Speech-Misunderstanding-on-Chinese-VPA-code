
package com.baidu.duer.dcs.http;

import android.text.TextUtils;
import android.util.Log;

import com.baidu.dcs.okhttp3.RequestBody;
import com.baidu.duer.dcs.framework.message.DcsRequestBody;
import com.baidu.duer.dcs.framework.message.DcsStreamRequestBody;
import com.baidu.duer.dcs.http.callback.DcsCallback;
import com.baidu.duer.dcs.util.LogUtil;
import com.baidu.duer.dcs.util.ObjectMapperUtil;

import java.util.LinkedHashMap;
import java.util.Map;


public class OkHttpRequestImpl implements HttpRequestInterface {
    private static final String TAG = "OkHttpRequestImpl";
    private final DcsHttpManager dcsHttpManager;

    public OkHttpRequestImpl() {
        dcsHttpManager = DcsHttpManager.getInstance();
    }

    @Override
    public void doPostEventStringAsync(DcsRequestBody requestBody, DcsCallback dcsCallback) {
        if (TextUtils.isEmpty(HttpConfig.getAccessToken())) {
            Log.d(TAG, "doPostEventStringAsync-accessToken is null !");
            return;
        }
        String bodyJson = ObjectMapperUtil.instance().objectToJson(requestBody);
        Map<String, RequestBody> multiParts = new LinkedHashMap<>();
        multiParts.put(HttpConfig.Parameters.DATA_METADATA,
                RequestBody.create(OkHttpMediaType.MEDIA_JSON_TYPE, bodyJson));
        DcsHttpManager.post()
                .url(HttpConfig.getEventsUrl())
                .headers(HttpConfig.getDCSHeaders())
                .multiParts(multiParts)
                .tag(HttpConfig.HTTP_EVENT_TAG)
                .build()
                .execute(dcsCallback);
    }

    @Override
    public void doPostEventMultipartAsync(DcsRequestBody requestBody,
                                          DcsStreamRequestBody streamRequestBody,
                                          DcsCallback dcsCallback) {
        if (TextUtils.isEmpty(HttpConfig.getAccessToken())) {
            Log.d(TAG, "doPostEventMultipartAsync-accessToken is null !");
            return;
        }
        String bodyJson = ObjectMapperUtil.instance().objectToJson(requestBody);
        Log.d("time", "开始发语音");
        Map<String, RequestBody> multiParts = new LinkedHashMap<>();
        multiParts.put(HttpConfig.Parameters.DATA_METADATA,
                RequestBody.create(OkHttpMediaType.MEDIA_JSON_TYPE, bodyJson));
        multiParts.put(HttpConfig.Parameters.DATA_AUDIO, streamRequestBody);
        DcsHttpManager.post()
                .url(HttpConfig.getEventsUrl())
                .headers(HttpConfig.getDCSHeaders())
                .multiParts(multiParts)
                .tag(HttpConfig.HTTP_VOICE_TAG)
                .build()
                .execute(dcsCallback);
    }

    @Override
    public void doGetDirectivesAsync(DcsCallback dcsCallback) {
        LogUtil.d(TAG, "doGetDirectivesAsync");
        if (TextUtils.isEmpty(HttpConfig.getAccessToken())) {
            Log.d(TAG, "doGetDirectivesAsync-accessToken is null !");
            return;
        }
        long time = 7 * 24 * 60 * 60 * 1000L;
        DcsHttpManager.get()
                .url(HttpConfig.getDirectivesUrl())
                .headers(HttpConfig.getDCSHeaders())
                .tag(HttpConfig.HTTP_DIRECTIVES_TAG)
                .build()
                .connTimeOut(time)
                .readTimeOut(time)
                .execute(dcsCallback);
    }

    @Override
    public void doGetPingAsync(DcsRequestBody requestBody, DcsCallback dcsCallback) {
        LogUtil.d(TAG, "doGetPingAsync");
        if (TextUtils.isEmpty(HttpConfig.getAccessToken())) {
            Log.d(TAG, "doGetPingAsync-accessToken is null !");
            return;
        }
        DcsHttpManager.get()
                .url(HttpConfig.getPingUrl())
                .headers(HttpConfig.getDCSHeaders())
                .tag(HttpConfig.HTTP_PING_TAG)
                .build()
                .execute(dcsCallback);
    }

    @Override
    public void cancelRequest(Object requestTag) {
        dcsHttpManager.cancelTag(requestTag);
    }
}