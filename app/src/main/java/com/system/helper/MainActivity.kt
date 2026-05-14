package com.system.helper

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private val videoUris = mutableListOf<Uri>()
    private val displayNames = mutableListOf<String>()
    private lateinit var adapter: ArrayAdapter<String>

    // 请求权限
    private val requestPermission = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.all { it.value }) {
            loadAllVideos()
        } else {
            Toast.makeText(this, "需要存储权限才能读取视频", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        listView = findViewById(R.id.videoListView)
        val addButton = findViewById<Button>(R.id.addButton)

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, displayNames)
        listView.adapter = adapter

        // 隐藏或修改“添加”按钮（可选完全移除）
        addButton.text = "刷新列表"
        addButton.setOnClickListener { loadAllVideos() }

        // 点击播放
        listView.setOnItemClickListener { _, _, position, _ ->
            val intent = Intent(this, PlayerActivity::class.java).apply {
                putExtra("video_uri", videoUris[position].toString())
                putExtra("current_index", position)
                putStringArrayListExtra("video_list", ArrayList(videoUris.map { it.toString() }))
            }
            startActivity(intent)
        }

        // 长按删除（从列表中移除，非删除文件）
        listView.setOnItemLongClickListener { _, _, position, _ ->
            // ... 原有删除逻辑不变
            true
        }

        // 启动时自动加载
        checkAndRequestPermissions()
    }

    private fun checkAndRequestPermissions() {
        val permissions = mutableListOf<String>()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            permissions.add(Manifest.permission.READ_MEDIA_VIDEO)
        } else {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (permissions.any { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }) {
            requestPermission.launch(permissions.toTypedArray())
        } else {
            loadAllVideos()
        }
    }

    private fun loadAllVideos() {
        videoUris.clear()
        displayNames.clear()

        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DATA
        )

        val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"

        contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn) ?: "未知视频"

                val contentUri = Uri.withAppendedPath(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    id.toString()
                )

                videoUris.add(contentUri)
                displayNames.add(name)
            }
        }

        adapter.notifyDataSetChanged()

        if (displayNames.isEmpty()) {
            Toast.makeText(this, "未找到视频文件", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "加载了 ${displayNames.size} 个视频", Toast.LENGTH_SHORT).show()
        }
    }
}
