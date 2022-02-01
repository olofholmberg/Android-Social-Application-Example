package com.example.tddd80application.responsedata

import java.io.Serializable

data class Course(
        val course_code: String,
        val course_id: String,
        val course_name: String
): Serializable