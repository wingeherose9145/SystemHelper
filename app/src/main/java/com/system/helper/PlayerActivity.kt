package com.system.helper

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import java.io.File

class PlayerActivity : AppCompatActivity() {

    private lateinit var player: ExoPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_player)

        val playerView =
            findViewById<PlayerView>(
                R.id.playerView
            )

        player = ExoPlayer.Builder(this).build()

        playerView.player = player

        val videoPath =
            intent.getStringExtra("videoUri")

        if (videoPath != null) {

            val file = File(videoPath)

            val mediaItem =
                MediaItem.fromUri(
                    Uri.fromFile(file)
                )

            player.setMediaItem(mediaItem)

            player.prepare()

            player.play()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        player.release()
    }
}
