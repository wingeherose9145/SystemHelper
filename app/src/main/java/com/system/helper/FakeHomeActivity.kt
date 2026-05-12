package com.system.helper

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class FakeHomeActivity : AppCompatActivity() {

    private var clickCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_fake_home)

        val title = findViewById<TextView>(R.id.titleText)

        title.setOnClickListener {

            clickCount++

            if (clickCount >= 3) {

                clickCount = 0

                startActivity(
                    Intent(
                        this,
                        HiddenVideoActivity::class.java
                    )
                )
            }
        }
    }
}
