package com.system.helper

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity

class PlayerActivity : AppCompatActivity() {

    private lateinit var controlsLayout: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_player)

        controlsLayout = findViewById(R.id.controlsLayout)

        val playPause = findViewById<ImageButton>(R.id.playPauseButton)

        val nextButton = findViewById<ImageButton>(R.id.nextButton)

        val prevButton = findViewById<ImageButton>(R.id.prevButton)

        val seekBar = findViewById<SeekBar>(R.id.seekBar)

        val rootView = findViewById<View>(R.id.rootLayout)

        rootView.setOnClickListener {

            if (controlsLayout.visibility == View.VISIBLE) {

                controlsLayout.visibility = View.GONE

            } else {

                controlsLayout.visibility = View.VISIBLE
            }
        }

        playPause.setOnClickListener {

        }

        nextButton.setOnClickListener {

        }

        prevButton.setOnClickListener {

        }
    }
}
