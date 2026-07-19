package com.system.helper

import android.content.pm.ActivityInfo
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import kotlin.random.Random

class PlayerActivity : AppCompatActivity() {

    private var player: ExoPlayer? = null
    private lateinit var playerView: PlayerView
    private lateinit var seekBar: SeekBar
    private lateinit var filenameText: TextView
    private lateinit var rewindButton: Button

    private lateinit var videoUris: ArrayList<String>
    private var currentIndex = 0
    private var retryCount = 0
    private var useSoftwareDecoder = false

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var gestureDetector: GestureDetector
    private val hideControlsHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)

        setContentView(R.layout.activity_player)

        playerView = findViewById(R.id.playerView)
        seekBar = findViewById(R.id.seekBar)
        filenameText = findViewById(R.id.filenameText)
        rewindButton = findViewById(R.id.rewindButton)

        rewindButton.setOnClickListener { rewind5Seconds() }

        videoUris = intent.getStringArrayListExtra("video_list") ?: arrayListOf()
        currentIndex = intent.getIntExtra("current_index", 0)

        if (videoUris.isEmpty()) {
            Toast.makeText(this, "没有视频可播放", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        shuffleAndRandomStart()
        setupGestureDetector()
        setupSeekBar()
        setupControls()

        playCurrentVideo()
    }

    private fun initPlayer() {
        player?.release()
        player = ExoPlayer.Builder(this).build().apply {
            playerView.player = this
            addListener(playerListener)
        }
    }

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(state: Int) {
            if (state == Player.STATE_ENDED) {
                playNextVideo()
            } else if (state == Player.STATE_READY) {
                showControlsTemporarily()
                retryCount = 0
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            if (isPlaying) hideControls() else showControls()
        }

        override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
            Toast.makeText(this@PlayerActivity, "播放错误: ${error.message}", Toast.LENGTH_SHORT).show()
            if (retryCount < 2) {
                retryCount++
                useSoftwareDecoder = true
                playCurrentVideo()
            } else {
                playNextVideo()
            }
        }
    }

    private fun playCurrentVideo() {
        try {
            initPlayer()

            val uri = Uri.parse(videoUris[currentIndex])
            val fileName = getFileNameFromUri(uri)
            filenameText.text = "正在播放: $fileName"
            
            setVideoOrientation(uri)

            player?.let {
                it.setMediaItem(MediaItem.fromUri(uri))
                it.prepare()
                it.play()
            }

            // 2秒后检查是否仍在播放（辅助判断黑屏）
            handler.postDelayed({
                if (player?.playbackState == Player.STATE_READY && player?.isPlaying == true) {
                    // 可在此扩展帧检测
                }
            }, 2000)

        } catch (e: Exception) {
            Toast.makeText(this, "加载失败: ${e.message}", Toast.LENGTH_SHORT).show()
            playNextVideo()
        }
    }

    private fun getFileNameFromUri(uri: Uri): String {
        return try {
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (cursor.moveToFirst()) cursor.getString(nameIndex) else uri.lastPathSegment ?: "未知视频"
            } ?: "未知视频"
        } catch (e: Exception) {
            uri.lastPathSegment ?: "未知视频"
        }
    }

    private fun setVideoOrientation(uri: Uri) {
        try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(this, uri)
            val width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull() ?: 0
            val height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull() ?: 0
            requestedOrientation = if (height > width) {
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            } else {
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }
            retriever.release()
        } catch (e: Exception) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    private fun shuffleAndRandomStart() {
        if (videoUris.size <= 1) return
        videoUris.shuffle(Random.Default)
        currentIndex = Random.nextInt(videoUris.size)
    }

    private fun playNextVideo() {
        currentIndex = (currentIndex + 1) % videoUris.size
        retryCount = 0
        useSoftwareDecoder = false
        playCurrentVideo()
    }

    private fun playPreviousVideo() {
        currentIndex = if (currentIndex > 0) currentIndex - 1 else videoUris.size - 1
        retryCount = 0
        useSoftwareDecoder = false
        playCurrentVideo()
    }

    private fun setupGestureDetector() {
        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
                if (kotlin.math.abs(velocityX) > 700) {
                    if (velocityX > 0) playPreviousVideo() else playNextVideo()
                    return true
                }
                return false
            }

            // 双击刷新当前视频（解决黑屏）
            override fun onDoubleTap(e: MotionEvent): Boolean {
                Toast.makeText(this@PlayerActivity, "正在刷新当前视频...", Toast.LENGTH_SHORT).show()
                retryCount = 0
                useSoftwareDecoder = true
                playCurrentVideo()
                return true
            }
        })

        playerView.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            false
        }
    }

    private fun setupControls() {
        hideControlsHandler.postDelayed({ hideControls() }, 3000)
    }

    private fun togglePlaybackAndControls() {
        player?.let {
            if (it.isPlaying) {
                it.pause()
                showControls()
            } else {
                it.play()
                hideControls()
            }
        }
    }

    private fun showControls() {
        filenameText.visibility = View.VISIBLE
        rewindButton.visibility = View.VISIBLE
        seekBar.visibility = View.VISIBLE
        hideControlsHandler.removeCallbacksAndMessages(null)
    }

    private fun hideControls() {
        filenameText.visibility = View.GONE
        rewindButton.visibility = View.GONE
        seekBar.visibility = View.GONE
    }

    private fun showControlsTemporarily() {
        showControls()
        hideControlsHandler.removeCallbacksAndMessages(null)
        hideControlsHandler.postDelayed({ if (player?.isPlaying == true) hideControls() }, 3000)
    }

    private fun setupSeekBar() {
        handler.post(object : Runnable {
            override fun run() {
                player?.let {
                    if (it.duration > 0 && seekBar.visibility == View.VISIBLE) {
                        seekBar.max = it.duration.toInt()
                        seekBar.progress = it.currentPosition.toInt()
                    }
                }
                handler.postDelayed(this, 500)
            }
        })

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) player?.seekTo(progress.toLong())
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun rewind5Seconds() {
        player?.let {
            if (it.duration > 0) {
                val newPosition = (it.currentPosition - 5000).coerceAtLeast(0)
                it.seekTo(newPosition)
                showControlsTemporarily()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        player?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        hideControlsHandler.removeCallbacksAndMessages(null)
        player?.release()
        player = null
    }
}
