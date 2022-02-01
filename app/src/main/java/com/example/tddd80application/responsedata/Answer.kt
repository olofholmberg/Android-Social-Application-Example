package com.example.tddd80application.responsedata

data class Answer(val answer_id: String,
                  val answer_body: String,
                  val timestamp: String,
                  val author: User)