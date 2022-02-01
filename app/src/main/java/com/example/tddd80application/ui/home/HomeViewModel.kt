package com.example.tddd80application.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.volley.Response
import com.example.tddd80application.R
import com.example.tddd80application.network.*
import com.example.tddd80application.responsedata.ApiMessage
import com.example.tddd80application.responsedata.Questions

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val context = getApplication<Application>().applicationContext

    private val _questions: MutableLiveData<Questions> = MutableLiveData()
    val questions: LiveData<Questions>
        get() = _questions

    private val _apiErrorMessage: MutableLiveData<ApiMessage> = MutableLiveData()
    val apiErrorMessage: LiveData<ApiMessage>
        get() = _apiErrorMessage

    fun fetchQuestions(accessToken: String) {

        var fetchQuestionsListener = Response.Listener<Questions> {
            response ->
            _questions.value = response
        }

        var fetchQuestionsErrorListener = Response.ErrorListener{
            error ->
            if (hasApiErrorMessage(error)) {
                _apiErrorMessage.value = getApiErrorMessage(error)
            } else {
                _apiErrorMessage.value = ApiMessage(getDefaultErrorMessage(error))
            }
        }

        var headers = HashMap<String, String>()
        headers["Authorization"] = accessToken

        var fetchQuestionsGsonRequest = GsonGetRequest<Questions>(context?.getString(R.string.base_url) + "questions", Questions::class.java, headers, fetchQuestionsListener, fetchQuestionsErrorListener)

        if (context != null) VolleySingleton.getInstance(context).addToRequestQueue(fetchQuestionsGsonRequest)
    }

}