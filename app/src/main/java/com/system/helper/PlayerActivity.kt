package com.system.helper

import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import java.io.File

class PlayerActivity : AppCompatActivity() {

    private lateinit var player: ExoPlayer
    private lateinit var playerView: PlayerView
    private lateinit var seekBar: SeekBar
    private lateinit var topControls: LinearLayout

    private lateinit var videoList: ArrayList<String>
    private var currentIndex = 0

    private val handler = Handler(Looper.getMainLooper())
    private var isPortrait = false

    private val hideRunnable = Runnable {
        if (!player.isPlaying) return@Runnable
        topControls.visibility = View.GONE
        seekBar.visibility = View.GONE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)

            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

            supportActionBar?.hide()
            setContentView(R.layout.activity_player)

            // 关键：安全获取控件
            playerView = findViewById(R.id.playerView)
            topControls = findViewById(R.id.topControls)
            seekBar = findViewById(R.id.seekBar)

            val rotateButton = findViewById<ImageButton>(R.id.rotateButton)
            val prevButton = findViewById<ImageButton>(R.id.prevButton)
            val nextButton = findViewById<ImageButton>(R.id.nextButton)

            player = ExoPlayer.Builder(this).build()
            playerView.player = player
            playerView.useController = false

            videoList = intent.getStringArrayListExtra("video_list") ?: arrayListOf()
            currentIndex = intent.getIntExtra("video_index", 0)

            if (videoList.isEmpty() || currentIndex >= videoList.size) {
                Toast.makeText(this, "视频列表为空或索引错误", Toast.LENGTH_LONG).show()
                finish()
                return
            }

            player.addListener(object : Player.Listener {
                override fun onVideoSizeChanged(videoSize: VideoSize) {
                    requestedOrientation = if (videoSize.height > videoSize.width) {
                        isPortrait = true
                        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    } else {
                        isPortrait = false
                        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    }
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_ENDED) {
                        playNextVideo()
                    }
                }
            })

            playVideo()

            topControls.visibility = View.GONE
            seekBar.visibility = View.GONE

            playerView.setOnClickListener {
                if (player.isPlaying) {
                    player.pause()
                    topControls.visibility = View.VISIBLE
                    seekBar.visibility = View.VISIBLE
                } else {
                    player.play()
                    startAutoHide()
                }
            }

            rotateButton.setOnClickListener {
                isPortrait = !isPortrait
                requestedOrientation = if (isPortrait) {
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                } else {
                    ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                }
            }

            prevButton.setOnClickListener {
                if (currentIndex > 0) {
                    currentIndex--
                    playVideo()
                }
            }

            nextButton.setOnClickListener {
                if (currentIndex < videoList.size - 1) {
                    currentIndex++
                    playVideo()
                }
            }

            startSeekBarUpdate()

        } catch (e: Exception) {
            Toast.makeText(this, "播放器初始化失败: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
            finish()
        }
    }

    private fun playVideo() {
        try {
            val videoFile = File(videoList[currentIndex])
            if (!videoFile.exists() || videoFile.length() == 0L) {
                Toast.makeText(this, "视频文件不存在", Toast.LENGTH_LONG).show()
                finish()
                return
            }

            val mediaItem = MediaItem.fromUri(Uri.fromFile(videoFile))
            player.setMediaItem(mediaItem)
            player.prepare()
            player.play()

            startAutoHide()
        } catch (e: Exception) {
            Toast.makeText(this, "播放失败: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
            finish()
        }
    }

    private fun playNextVideo() {
        if (currentIndex < videoList.size - 1) {
            currentIndex++
            playVideo()
        }
    }

    private fun startAutoHide() {
        topControls.visibility = View.VISIBLE
        seekBar.visibility = View.VISIBLE
        handler.removeCallbacks(hideRunnable)
        handler.postDelayed(hideRunnable, 3000)
    }

    private fun startSeekBarUpdate() {
        handler.post(object : Runnable {
            override fun run() {
                if (player.duration > 0) {
                    seekBar.max = player.duration.toInt()
                    seekBar.progress = player.currentPosition.toInt()
                }
                handler.postDelayed(this, 500)
            }
        })

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) player.seekTo(progress.toLong())
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    override fun onPause() {
        super.onPause()
        if (::player.isInitialized) player.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::player.isInitialized) player.release()
    }
}
