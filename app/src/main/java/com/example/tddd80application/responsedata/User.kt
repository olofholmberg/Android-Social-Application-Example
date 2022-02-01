package com.example.tddd80application.responsedata

import java.io.Serializable

data class User(
        val email: String,
        val user_id: String,
        val username: String
): Serializable