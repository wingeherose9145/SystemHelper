package com.system.helper

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.InputType
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

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

        val batteryText =
            findViewById<TextView>(
                R.id.batteryText
            )

        val tempText =
            findViewById<TextView>(
                R.id.tempText
            )

        val ramText =
            findViewById<TextView>(
                R.id.ramText
            )

        val cacheText =
            findViewById<TextView>(
                R.id.cacheText
            )

        val ramBar =
            findViewById<ProgressBar>(
                R.id.ramBar
            )

        val cleanButton =
            findViewById<Button>(
                R.id.cleanButton
            )
        val batterySaverButton =
            findViewById<Button>(
                R.id.batterySaverButton
            )

        val cpuBoostButton =
            findViewById<Button>(
                R.id.cpuBoostButton
            )

       val optimizeButton =
           findViewById<Button>(
               R.id.optimizeButton
            )
            
        val cacheLayout =
            findViewById<LinearLayout>(
                R.id.storageLayout
            )
            
        val ramUsed =
            Random.nextInt(4, 8)

        val ramPercent =
            Random.nextInt(45, 92)

        val temp =
            Random.nextInt(30, 41)

        val cache =
            Random.nextDouble(0.8, 3.2)

        val batteryStates =
            listOf(
                "Excellent",
                "Good",
                "Normal"
            )

        val randomTemp =
    (31..36).random()

val randomRam =
    (48..62).random() / 10.0

val randomCache =
    (12..24).random() / 10.0

tempText.text =
    "Temperature: ${randomTemp}°C"

ramText.text =
    "${randomRam}GB / 8GB Used"

cacheText.text =
    "Junk Cache: ${randomCache}GB"
        
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

        cleanButton.setOnClickListener {

            simulateCleaning()
        }
        cacheLayout.setOnClickListener {

            batterySaverButton.setOnClickListener {

                fakeOptimize(
                    "Battery Saver"
                )
            }

            cpuBoostButton.setOnClickListener {

                fakeOptimize(
                   "CPU Boost"
                )
            }

            optimizeButton.setOnClickListener {

              fakeOptimize(
                    "Smart Optimize"
                )
            }
            
            startActivity(
                Intent(
                    this,
                    StorageActivity::class.java
                )
            )
        }
    }
private fun fakeOptimize(title: String) {

    val loadingText =
        TextView(this)

    loadingText.text =
        "Optimizing..."

    loadingText.setPadding(
        60,
        40,
        60,
        40
    )

    val dialog =
        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(loadingText)
            .setCancelable(false)
            .create()

    dialog.show()

    handler.postDelayed({

        loadingText.text =
            "Analyzing System..."

    }, 1200)

    handler.postDelayed({

        loadingText.text =
            "Applying Optimization..."

    }, 2400)

    handler.postDelayed({

        dialog.dismiss()

        Toast.makeText(
            this,
            "Optimization Complete",
            Toast.LENGTH_SHORT
        ).show()

    }, 4000)
}


    private fun simulateCleaning() {

    val loadingText =
        TextView(this)

    loadingText.text =
        "Scanning..."

    loadingText.setPadding(
        60,
        40,
        60,
        40
    )

    val dialog =
        AlertDialog.Builder(this)
            .setTitle("System Cleaner")
            .setView(loadingText)
            .setCancelable(false)
            .create()

    dialog.show()

    handler.postDelayed({

        loadingText.text =
            "Cleaning Cache..."

    }, 1200)

    handler.postDelayed({

        loadingText.text =
            "Optimizing System..."

    }, 2500)

    handler.postDelayed({

        dialog.dismiss()

        Toast.makeText(
            this,
            "System Optimized",
            Toast.LENGTH_SHORT
        ).show()

    }, 4000)
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
            .setNegativeButton(
                "Cancel",
                null
            )
            .show()
    }
}
