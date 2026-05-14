package com.system.helper

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class HiddenVideoActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private val videoPaths = mutableListOf<String>()      // 真实播放路径
    private val displayNames = mutableListOf<String>()    // 显示的原始文件名
    private lateinit var adapter: ArrayAdapter<String>

    private val pickVideos = registerForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        uris?.forEach { saveVideo(it) }
        loadVideos()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hidden_video)

        // 适配你当前的 XML id
        listView = findViewById(R.id.videoListView)
        val addButton = findViewById<Button>(R.id.addButton)

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, displayNames)
        listView.adapter = adapter

        addButton.setOnClickListener {
            pickVideos.launch(arrayOf("video/*"))
        }

        listView.setOnItemClickListener { _, _, position, _ ->
            val intent = Intent(this, PlayerActivity::class.java)
            intent.putStringArrayListExtra("video_list", ArrayList(videoPaths))
            intent.putExtra("video_index", position)
            startActivity(intent)
        }

        loadVideos()
    }

    private fun loadVideos() {
        videoPaths.clear()
        displayNames.clear()

        val dir = File(filesDir, "hidden_videos")
        if (!dir.exists()) dir.mkdirs()

        dir.listFiles()?.sortedBy { it.name }?.forEach { file ->
            videoPaths.add(file.absolutePath)
            val name = if (file.name.contains("__")) {
                file.name.substringAfter("__")
            } else {
                file.name
            }
            displayNames.add(name)
        }
        adapter.notifyDataSetChanged()
    }

    private fun saveVideo(uri: Uri) {
        var originalName = "video.mp4"
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && idx >= 0) {
                originalName = cursor.getString(idx)
            }
        }

        val random = UUID.randomUUID().toString().replace("-", "").take(12)
        val finalName = "${random}__${originalName}"

        val dir = File(filesDir, "hidden_videos")
        if (!dir.exists()) dir.mkdirs()

        val outFile = File(dir, finalName)
        contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(outFile).use { output ->
                input.copyTo(output)
            }
        }
    }
}
