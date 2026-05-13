package com.system.helper

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.InputType
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class FakeHomeActivity : AppCompatActivity() {

    private var clickCount = 0

    private val handler = Handler()

    private val hiddenPassword = "9527"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_fake_home)

        val titleText =
            findViewById<TextView>(
                R.id.titleText
            )

        titleText.setOnClickListener {

            clickCount++

            if (clickCount == 3) {

                clickCount = 0

                showPasswordDialog()
            }

            handler.removeCallbacksAndMessages(null)

            handler.postDelayed({

                clickCount = 0

            }, 1000)
        }

        Toast.makeText(
            this,
            "System Status Normal",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun showPasswordDialog() {

        val input = EditText(this)

        input.inputType =
            InputType.TYPE_CLASS_NUMBER or
            InputType.TYPE_NUMBER_VARIATION_PASSWORD

        AlertDialog.Builder(this)
            .setTitle("Security Verification")
            .setMessage("Enter Access Code")
            .setView(input)
            .setPositiveButton("Unlock") { _, _ ->

                val password =
                    input.text.toString()

                if (password == hiddenPassword) {

                    startActivity(
                        Intent(
                            this,
                            HiddenVideoActivity::class.java
                        )
                    )

                } else {

                    Toast.makeText(
                        this,
                        "Invalid Code",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
