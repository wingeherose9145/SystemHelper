package com.system.helper

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private val videoUris = mutableListOf<Uri>()           // 保存视频 Uri
    private val displayNames = mutableListOf<String>()     // 显示文件名
    private lateinit var adapter: ArrayAdapter<String>

    private val pickVideos = registerForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        uris?.forEach { uri ->
            videoUris.add(uri)
            val name = getFileNameFromUri(uri)
            displayNames.add(name)
        }
        adapter.notifyDataSetChanged()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        listView = findViewById(R.id.videoListView)
        val addButton = findViewById<Button>(R.id.addButton)

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, displayNames)
        listView.adapter = adapter

        addButton.setOnClickListener {
            pickVideos.launch(arrayOf("video/*"))
        }

        listView.setOnItemClickListener { _, _, position, _ ->
            val intent = Intent(this, PlayerActivity::class.java)
            intent.putExtra("video_uri", videoUris[position].toString())
            intent.putExtra("current_index", position)
            intent.putStringArrayListExtra("video_list", ArrayList(videoUris.map { it.toString() }))
            startActivity(intent)
        }
    }

    private fun getFileNameFromUri(uri: Uri): String {
        return contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex != -1) {
                cursor.getString(nameIndex)
            } else {
                "未知视频"
            }
        } ?: "未知视频"
    }
}
