package com.example.tddd80application.ui.askquestion

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.volley.Response
import com.example.tddd80application.R
import com.example.tddd80application.network.*
import com.example.tddd80application.requestdata.RequestQuestionData
import com.example.tddd80application.responsedata.ApiMessage
import com.example.tddd80application.responsedata.Courses
import com.google.gson.Gson

class AskQuestionViewModel(application: Application) : AndroidViewModel(application) {

    private val context = getApplication<Application>().applicationContext

    private val _availableCourses: MutableLiveData<Courses> = MutableLiveData()
    val availableCourses: LiveData<Courses>
        get() = _availableCourses

    private val _askQuestionMessage: MutableLiveData<ApiMessage> = MutableLiveData()
    val askQuestionMessage: LiveData<ApiMessage>
        get() = _askQuestionMessage

    private val _apiErrorMessage: MutableLiveData<ApiMessage> = MutableLiveData()
    val apiErrorMessage: LiveData<ApiMessage>
        get() = _apiErrorMessage

    fun fetchAvailableCourses(accessToken: String) {

        val fetchAvailableCoursesListener = Response.Listener<Courses> {
            response ->
            _availableCourses.value = response
        }

        val fetchAvailableCoursesErrorListener = Response.ErrorListener{
            error ->
            if (hasApiErrorMessage(error)) {
                _apiErrorMessage.value = getApiErrorMessage(error)
            } else {
                _apiErrorMessage.value = ApiMessage(getDefaultErrorMessage(error))
            }
        }

        val headers = HashMap<String, String>()
        headers["Authorization"] = accessToken

        val fetchAvailableCoursesGsonRequest = GsonGetRequest<Courses>(context?.getString(R.string.base_url) + "courses", Courses::class.java, headers, fetchAvailableCoursesListener, fetchAvailableCoursesErrorListener)

        if (context != null) VolleySingleton.getInstance(context).addToRequestQueue(fetchAvailableCoursesGsonRequest)

    }

    fun postQuestion(accessToken: String, questionTitle: String, courseCode: String, questionDescription: String) {

        val requestQuestionData = RequestQuestionData(questionTitle, courseCode, questionDescription)
        val askQuestionJsonBody = Gson().toJson(requestQuestionData)

        val askQuestionListener = Response.Listener<ApiMessage> {
            response ->
            _askQuestionMessage.value = response
        }

        val askQuestionErrorListener = Response.ErrorListener{
            error ->
            if (hasApiErrorMessage(error)) {
                _apiErrorMessage.value = getApiErrorMessage(error)
            } else {
                _apiErrorMessage.value = ApiMessage(getDefaultErrorMessage(error))
            }
        }

        val headers = HashMap<String, String>()
        headers["Authorization"] = accessToken

        val askQuestionGsonRequest = GsonPostRequest<ApiMessage>(context?.getString(R.string.base_url) + "questions", askQuestionJsonBody, ApiMessage::class.java, headers, askQuestionListener, askQuestionErrorListener)

        if (context != null) VolleySingleton.getInstance(context).addToRequestQueue(askQuestionGsonRequest)

    }

}