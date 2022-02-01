package com.example.tddd80application.network

import com.android.volley.NetworkResponse
import com.android.volley.ParseError
import com.android.volley.Response
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.JsonRequest
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset

/**
 * Make a GET request and return a parsed object from JSON.
 *
 * @param url URL of the request to make
 * @param type is the type of the object to be returned
 * @param listener is the listener for the right answer
 * @param errorListener  is the listener for the wrong answer
 */
class GsonPostRequest<T>(
        url: String?,
        body: String?,
        private val clazz: Class<T>,
        private val headers: MutableMap<String, String>?,
        private val listener: Response.Listener<T>,
        errorListener: Response.ErrorListener?
) : JsonRequest<T>(Method.POST, url, body, listener, errorListener) {

    private val gson = Gson()

    override fun getHeaders(): MutableMap<String, String> = headers ?: super.getHeaders()

    override fun deliverResponse(response: T) = listener.onResponse(response)

    override fun parseNetworkResponse(response: NetworkResponse?): Response<T> {
        return try {
            val json = String(
                    response?.data ?: ByteArray(0),
                    Charset.forName(HttpHeaderParser.parseCharset(response?.headers)))
            Response.success(
                    gson.fromJson<Any>(json, clazz),
                    HttpHeaderParser.parseCacheHeaders(response)
            ) as Response<T>
        } catch (e: UnsupportedEncodingException) {
            Response.error(ParseError(e))
        } catch (e: JsonSyntaxException) {
            Response.error(ParseError(e))
        }
    }
}