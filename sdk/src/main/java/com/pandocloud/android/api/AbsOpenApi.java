package com.pandocloud.android.api;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;

import android.content.Context;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.pandocloud.android.utils.LogUtils;

public class AbsOpenApi {

    public static final int CODE_SUCCESS = 0;

    public static boolean DEBUG = false;

    private static String API_SERVER_URL = "https://api.pandocloud.com";
	
	public static final String MINE_TYPE_JSON = "application/json;charset=UTF-8";
	
	protected static AsyncHttpClient httpClient = new AsyncHttpClient(true, 80, 443);
	
	public static final int INNER_ERROR_CODE = -200;
	
	
	public static String getApiServerUrl() {
		return API_SERVER_URL;
	}
	
	private static void post(Context context, String url, List<Header> headers, HttpEntity entity, String contentType, AsyncHttpResponseHandler responseHandler) {
		if (headers == null) {
			headers = new ArrayList<Header>();
		}
		if (DEBUG) {
			LogUtils.d("url: " + url);
			LogUtils.d("head: " + headers.toString());
		}
		headers.add(new BasicHeader(HTTP.CONTENT_TYPE, MINE_TYPE_JSON));
		httpClient.post(context, url, headers.toArray(new Header[headers.size()]), entity, contentType, responseHandler);
	}
	
	public static void post(Context context, String url, List<Header> headers, String jsonBody, AsyncHttpResponseHandler responseHandler) throws UnsupportedEncodingException {
		if (jsonBody == null) {
			jsonBody = "";
		} 
		StringEntity entity = new StringEntity(jsonBody, "UTF-8");
		entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, MINE_TYPE_JSON));
		post(context, url, headers, entity, MINE_TYPE_JSON, responseHandler);
	}
	
	public static void post(Context context, String url, String jsonBody, AsyncHttpResponseHandler responseHandler) throws UnsupportedEncodingException {
		post(context, url, null, jsonBody, responseHandler);
	}
	
	public static void post(Context context, String url, String jsonBody, HashMap<String, String> headers, AsyncHttpResponseHandler responseHandler) throws UnsupportedEncodingException {
		List<Header> headersList = null;
		if (headers != null && !headers.isEmpty()) {
			headersList = new ArrayList<Header>();
			
			@SuppressWarnings("rawtypes")
			Iterator iterator = headers.entrySet().iterator();
			
			while(iterator.hasNext()) {
				@SuppressWarnings("rawtypes")
				Entry entry = (Entry) iterator.next();
				String key = (String) entry.getKey();
				String value = (String) entry.getValue();
				headersList.add(new BasicHeader(key, value));
			}
		}
		post(context, url, headersList, jsonBody, responseHandler);
	}
	
	public static void get(Context context, String url, List<Header> headers, RequestParams params, AsyncHttpResponseHandler responseHandler) {
		if (headers == null) {
			headers = new ArrayList<Header>();
		}
		headers.add(new BasicHeader(HTTP.CONTENT_TYPE, MINE_TYPE_JSON));
		if (DEBUG) {
			LogUtils.d("url: " + url);
			LogUtils.d("head: " + headers.toString());
		}
		httpClient.get(context, url, headers.toArray(new Header[headers.size()]), params, responseHandler);
	}
	
	
	private static void put(Context context, String url, List<Header> headers, HttpEntity entity, AsyncHttpResponseHandler responseHandler) {
		if (headers == null) {
			headers = new ArrayList<Header>();
		}
		if (DEBUG) {
			LogUtils.d("url: " + url);
			LogUtils.d("head: " + headers.toString());
		}
		headers.add(new BasicHeader(HTTP.CONTENT_TYPE, MINE_TYPE_JSON));
		httpClient.put(context, url, headers.toArray(new Header[headers.size()]), entity, MINE_TYPE_JSON, responseHandler);
	}
	
	public static void put(Context context, String url, List<Header> headers, String jsonBody, AsyncHttpResponseHandler responseHandler) throws UnsupportedEncodingException {
		if (jsonBody == null) {
			jsonBody = "";
		}
		if (headers == null) {
			headers = new ArrayList<Header>();
		}
		headers.add(new BasicHeader(HTTP.CONTENT_TYPE, MINE_TYPE_JSON));
		StringEntity entity = new StringEntity(jsonBody, "UTF-8");
		entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, MINE_TYPE_JSON));
		put(context, url, headers, entity, responseHandler);
	}
	
	public static void delete(Context context, String url, List<Header> headers, RequestParams params, AsyncHttpResponseHandler responseHandler) {
		if (headers == null) {
			headers = new ArrayList<Header>();
		}
		headers.add(new BasicHeader(HTTP.CONTENT_TYPE, MINE_TYPE_JSON));
		httpClient.delete(context, url, headers.toArray(new Header[headers.size()]), params, responseHandler);
	}
	
	public static void cancel(Context context, boolean mayInterruptIfRunning) {
		httpClient.cancelRequests(context, mayInterruptIfRunning);
	}
	
	public static void cancelAllRequests(boolean cancel) {
		httpClient.cancelAllRequests(cancel);
	}
}
