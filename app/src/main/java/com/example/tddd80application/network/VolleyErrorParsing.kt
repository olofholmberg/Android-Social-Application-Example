package com.example.tddd80application.network

import com.android.volley.*
import com.android.volley.toolbox.HttpHeaderParser
import com.example.tddd80application.responsedata.ApiMessage
import com.google.gson.Gson
import org.json.JSONException
import org.json.JSONObject
import java.nio.charset.Charset

fun hasNoResponse(error: VolleyError): Boolean {
    when (error) {
        is TimeoutError -> return true
        is NoConnectionError -> return true
    }
    return false
}

/** For api specific error messages */

fun hasApiErrorMessage(error: VolleyError): Boolean {
    if (hasNoResponse(error)) return false
    val errorResponse = error.networkResponse
    val json = String(errorResponse?.data ?: ByteArray(0), Charset.forName(HttpHeaderParser.parseCharset(errorResponse?.headers)))
    val errorObj = try { JSONObject(json) } catch (e: JSONException) { null }
    when (errorObj?.has("msg")) {
        true -> return true
    }
    return false
}

fun getApiErrorMessage(error: VolleyError): ApiMessage {
    val gson = Gson()
    val errorResponse = error.networkResponse
    val json = String(errorResponse?.data ?: ByteArray(0), Charset.forName(HttpHeaderParser.parseCharset(errorResponse?.headers)))
    return gson.fromJson(json, ApiMessage::class.java)
}

/** For generic http error messages */

fun getDefaultErrorMessage(error: VolleyError): String {
    when (error) {
        is TimeoutError -> return "Connection Timed Out"
        is NoConnectionError -> return "No Connection To Server"
        is AuthFailureError -> return "Authentication Failed"
        is ServerError -> return "Server Side Error"
        is NetworkError -> return "Network Error While Performing Request"
        is ParseError -> return "Server Response Could Not Be Parsed"
    }
    return "Error While Performing Request"
}