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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MainActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private val videoUris = mutableListOf<Uri>()
    private val displayNames = mutableListOf<String>()
    private lateinit var adapter: ArrayAdapter<String>

    private val requestPermission = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.all { it.value }) {
            loadAllVideos()
        } else {
            Toast.makeText(this, "权限被拒绝，无法读取视频", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        listView = findViewById(R.id.videoListView)
        val addButton = findViewById<Button>(R.id.addButton)

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, displayNames)
        listView.adapter = adapter

        addButton.text = "刷新视频列表"
        addButton.setOnClickListener { refreshOrLoadVideos() }

        // 点击播放
        listView.setOnItemClickListener { _, _, position, _ ->
            val intent = Intent(this, PlayerActivity::class.java).apply {
                putExtra("video_uri", videoUris[position].toString())
                putExtra("current_index", position)
                putStringArrayListExtra("video_list", ArrayList(videoUris.map { it.toString() }))
            }
            startActivity(intent)
        }

        // 长按删除
        listView.setOnItemLongClickListener { _, _, position, _ ->
            if (position >= 0 && position < videoUris.size) {
                AlertDialog.Builder(this)
                    .setTitle("删除")
                    .setMessage("从列表中移除？")
                    .setPositiveButton("移除") { _, _ ->
                        videoUris.removeAt(position)
                        displayNames.removeAt(position)
                        adapter.notifyDataSetChanged()
                        saveVideoList()
                    }
                    .setNegativeButton("取消", null)
                    .show()
            }
            true
        }

        // 启动时直接尝试恢复或加载
        loadSavedListOrRefresh()
    }

    private fun refreshOrLoadVideos() {
        if (hasPermission()) {
            loadAllVideos()
        } else {
            requestPermission()
        }
    }

    private fun hasPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Android 12 及以下不强制检查
        }
    }

    private fun requestPermission() {
        requestPermission.launch(arrayOf(Manifest.permission.READ_MEDIA_VIDEO))
    }

    private fun loadSavedListOrRefresh() {
        if (loadSavedVideoList()) {
            Toast.makeText(this, "已恢复 ${displayNames.size} 个视频", Toast.LENGTH_SHORT).show()
        } else {
            loadAllVideos()   // 直接尝试加载，不强制弹权限
        }
    }

    private fun loadAllVideos() {
        videoUris.clear()
        displayNames.clear()

        val projection = arrayOf(MediaStore.Video.Media._ID, MediaStore.Video.Media.DISPLAY_NAME)

        contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            "${MediaStore.Video.Media.DATE_ADDED} DESC"
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn) ?: "未知视频"
                val uri = Uri.withAppendedPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id.toString())

                videoUris.add(uri)
                displayNames.add(name)
            }
        }

        adapter.notifyDataSetChanged()
        saveVideoList()

        if (displayNames.isEmpty()) {
            Toast.makeText(this, "未找到视频文件", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "找到 ${displayNames.size} 个视频", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveVideoList() {
        val prefs = getSharedPreferences("video_list", MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString("uris", Gson().toJson(videoUris.map { it.toString() }))
        editor.putString("names", Gson().toJson(displayNames))
        editor.apply()
    }

    private fun loadSavedVideoList(): Boolean {
        val prefs = getSharedPreferences("video_list", MODE_PRIVATE)
        val uriJson = prefs.getString("uris", null) ?: return false
        val nameJson = prefs.getString("names", null) ?: return false

        try {
            val uriList: List<String> = Gson().fromJson(uriJson, object : TypeToken<List<String>>() {}.type)
            val nameList: List<String> = Gson().fromJson(nameJson, object : TypeToken<List<String>>() {}.type)

            videoUris.clear()
            displayNames.clear()
            uriList.forEach { videoUris.add(Uri.parse(it)) }
            displayNames.addAll(nameList)

            adapter.notifyDataSetChanged()
            return true
        } catch (e: Exception) {
            return false
        }
    }
}
