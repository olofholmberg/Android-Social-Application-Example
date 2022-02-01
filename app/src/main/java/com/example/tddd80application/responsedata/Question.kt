package com.example.tddd80application.responsedata

import java.io.Serializable

data class Question(val question_id: String,
                    val question_title: String,
                    val question_body: String,
                    val timestamp: String,
                    val author: User,
                    val course: Course,
                    val likes: String,
                    val is_liking: String,
                    val answers: String): Serializable
