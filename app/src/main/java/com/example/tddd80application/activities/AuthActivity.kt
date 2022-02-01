package com.example.tddd80application.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.android.volley.Response
import com.example.tddd80application.R
import com.example.tddd80application.data.UserPreferences
import com.example.tddd80application.extensions.startNewActivity
import com.example.tddd80application.network.*
import com.example.tddd80application.responsedata.Token
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AuthActivity : AppCompatActivity() {

    private lateinit var userPreferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        userPreferences = UserPreferences(this@AuthActivity)

        /**
         * If we have a token, attempt to refresh it before starting the app.
         * IMPORTANT: Read Access Token by using first() instead of observeOnce() to
         * prevent issues with removing observer and doing other tasks simultaneously.
         */
        lifecycleScope.launch {
            val accessToken = userPreferences.accessToken.first()
            if (accessToken != null && accessToken.isNotEmpty()) {
                refreshToken(accessToken)
            }
        }
    }

    /**
     * Sends a refresh_token request to the API. If successful the user is logged in automatically
     * with the new token that was received.
     */
    private fun refreshToken(accessToken: String) {

        var tokenRefreshListener = Response.Listener<Token> { response ->
            when(response.token_type) {
                "Bearer" -> lifecycleScope.launch {
                    userPreferences.saveAccessToken(response.token_type + " " + response.access_token)
                    this@AuthActivity.startNewActivity(MainActivity::class.java)
                }
            }
        }

        var tokenRefreshErrorListener = Response.ErrorListener{ error ->
            if (hasApiErrorMessage(error)) {
                Toast.makeText(this@AuthActivity, getApiErrorMessage(error).msg , Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this@AuthActivity, getDefaultErrorMessage(error) , Toast.LENGTH_LONG).show()
            }
        }

        var headers = HashMap<String, String>()
        headers["Authorization"] = accessToken

        var tokenRefreshGsonRequest = GsonPostRequest<Token>(getString(R.string.base_url) + "refresh_token", null, Token::class.java, headers, tokenRefreshListener, tokenRefreshErrorListener)

        VolleySingleton.getInstance(this@AuthActivity).addToRequestQueue(tokenRefreshGsonRequest)
    }
}