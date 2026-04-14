package com.ufrn.matrizdefocofsico.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class TaskRepository(context: Context) {

    private val prefs = context.getSharedPreferences("matrix_tasks", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun save(tasks: List<Task>) {
        prefs.edit().putString("tasks", gson.toJson(tasks)).apply()
    }

    fun load(): List<Task> {
        val json = prefs.getString("tasks", null) ?: return emptyList()
        val type = object : TypeToken<List<Task>>() {}.type
        return gson.fromJson(json, type)
    }
}
