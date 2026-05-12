package com.system.helper

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

class HiddenVideoActivity : AppCompatActivity() {

    private lateinit var listView: ListView

    private val videoList = mutableListOf(
        "movie1.mp4",
        "movie2.mp4"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_hidden_video)

        listView = findViewById(R.id.videoList)

        val addButton = findViewById<FloatingActionButton>(R.id.addButton)

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            videoList
        )

        listView.adapter = adapter

        addButton.setOnClickListener {

        }
    }
}
