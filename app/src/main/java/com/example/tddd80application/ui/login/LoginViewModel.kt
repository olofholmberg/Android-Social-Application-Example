package com.example.tddd80application.ui.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.volley.Response
import com.example.tddd80application.R
import com.example.tddd80application.network.*
import com.example.tddd80application.requestdata.RequestLoginData
import com.example.tddd80application.responsedata.ApiMessage
import com.example.tddd80application.responsedata.Token
import com.google.gson.Gson

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val context = getApplication<Application>().applicationContext

    private val _accessToken: MutableLiveData<Token> = MutableLiveData()
    val accessToken: LiveData<Token>
        get() = _accessToken

    private val _apiErrorMessage: MutableLiveData<ApiMessage> = MutableLiveData()
    val apiErrorMessage: LiveData<ApiMessage>
        get() = _apiErrorMessage

    fun login(email: String, password: String) {

        var requestLoginData = RequestLoginData(email, password)
        var loginJsonBody = Gson().toJson(requestLoginData)

        var loginListener = Response.Listener<Token> {
            response ->
            _accessToken.value = response
        }

        var loginErrorListener = Response.ErrorListener{
            error ->
            if (hasApiErrorMessage(error)) {
                _apiErrorMessage.value = getApiErrorMessage(error)
            } else {
                _apiErrorMessage.value = ApiMessage(getDefaultErrorMessage(error))
            }
        }

        var loginGsonRequest = GsonPostRequest<Token>(context?.getString(R.string.base_url) + "login", loginJsonBody, Token::class.java, null, loginListener, loginErrorListener)

        if (context != null) VolleySingleton.getInstance(context).addToRequestQueue(loginGsonRequest)
    }
}