package com.baka3k.mixaudio.sample

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.baka3k.soundutilities.concat.SoundConcat
import com.baka3k.soundutilities.mixer.SoundMixer
import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis

class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "MainActivity"
        private val PERMISSIONS_REQUIRED = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        private const val PERMISSIONS_REQUEST_CODE = 10
    }

    private lateinit var buttonTestMixAudio: Button
    private lateinit var buttonConcat: Button
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var job: Job? = null
    private val exceptionHandler = CoroutineExceptionHandler { _, e ->
        Log.e(TAG, "exception ${e.message}", e)
    }
    private val onClickListener = View.OnClickListener {
        if (it.id == R.id.buttonTestMixAudio) {
            mixAudio()
        } else if (it.id == R.id.buttonConcat) {
            concatAudio()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        buttonConcat = findViewById(R.id.buttonConcat)
        buttonTestMixAudio = findViewById(R.id.buttonTestMixAudio)
        buttonTestMixAudio.setOnClickListener(onClickListener)
        buttonConcat.setOnClickListener(onClickListener)
    }

    private fun mixAudio() {
        job?.cancel()
        job = coroutineScope.launch(exceptionHandler) {
            val timeMixer = measureTimeMillis {
                val soundMixer = SoundMixer(applicationContext.cacheDir)
                val inputFile1 = "/storage/emulated/0/Download/A.mp3"
                val inputFile2 = "/storage/emulated/0/Download/B.mp3"
                val outPutFileMixed = "/storage/emulated/0/Download/mixed.wav"
                val duration = 15000
                soundMixer.mix(
                    inputFile1 = inputFile1,
                    inputFile2 = inputFile2,
                    outPutMixing = outPutFileMixed,
                    duration = duration
                )
            }
            Log.d(TAG, "Mixed time spent $timeMixer")
        }
    }

    private fun concatAudio() {
        job?.cancel()
        job = coroutineScope.launch(exceptionHandler) {
            val timeConcat = measureTimeMillis {
                val soundConcat = SoundConcat(applicationContext.cacheDir)
                val inputFile1 = "/storage/emulated/0/Download/A.mp3"
                val inputFile2 = "/storage/emulated/0/Download/B.mp3"
                val outPutFileConcated = "/storage/emulated/0/Download/concat.wav"
                soundConcat.concatMP3File(
                    inputFile1 = inputFile1,
                    inputFile2 = inputFile2,
                    outputPath = outPutFileConcated
                )
            }
            Log.d(TAG, "Concat time spent $timeConcat")
        }
    }

    private fun hasPermission(): Boolean {
        return PERMISSIONS_REQUIRED.all {
            ContextCompat.checkSelfPermission(
                applicationContext,
                it
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onStart() {
        super.onStart()
        checkStoragePermission()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkStoragePermission() {
        if (!hasPermission()) {
            requestPermissions(
                PERMISSIONS_REQUIRED,
                PERMISSIONS_REQUEST_CODE
            )
        }
    }

    override fun onDestroy() {
        coroutineScope.cancel()
        super.onDestroy()
    }
}