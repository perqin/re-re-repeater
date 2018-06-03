package com.perqin.rererepeater

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.DocumentsContract
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var handler: Handler
    private lateinit var mediaPlayer: MediaPlayer
    private val resetToBeginRunnable = Runnable {
        playCutFab.setImageResource(R.drawable.ic_play_arrow_black_24dp)
        mediaPlayer.pause()
        mediaPlayer.seekTo(currentCutBegin)
    }
    private val updatePositionRunnable = object : Runnable {
        override fun run() {
            if (isMediaPlayerAvailable) {
                audioProgressBar.progress = mediaPlayer.currentPosition
            }
            handler.postDelayed(this, 100L)
        }
    }
    private var audioUri: Uri? = null
    private var isMediaPlayerAvailable = false
    @Suppress("DEPRECATION")
    private var progressDialog: ProgressDialog? = null
    private var currentCutBegin = 0
    private var currentCutEnd = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        handler = Handler()
        mediaPlayer = MediaPlayer()
        mediaPlayer.setAudioAttributes(AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                .build())
        mediaPlayer.setScreenOnWhilePlaying(true)
        mediaPlayer.setOnErrorListener { mp, what, extra ->
            Toast.makeText(this, "ERROR: $what, $extra", Toast.LENGTH_SHORT).show()
            hideProgressDialog()
            mp.reset()
            isMediaPlayerAvailable = false
            true
        }
        mediaPlayer.setOnPreparedListener { finishPreparingAudio() }
        mediaPlayer.setOnCompletionListener {
            handler.removeCallbacks(resetToBeginRunnable)
            resetToBeginRunnable.run()
        }

        playingAudioInfoCardView.setOnClickListener { pickAudioFile() }
        playCutFab.setOnClickListener { playOrCut() }
        playCutFab.setOnLongClickListener { startNextCut() }

        handler.post(updatePositionRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updatePositionRunnable)
        handler.removeCallbacks(resetToBeginRunnable)
        mediaPlayer.release()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when(requestCode) {
            REQUEST_PICK_AUDIO -> {
                if (resultCode == Activity.RESULT_OK) {
                    audioUri = data!!.data
                    startPreparingAudio()
                }
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun pickAudioFile() {
        startActivityForResult(Intent.createChooser(Intent().apply {
            action = Intent.ACTION_GET_CONTENT
            type = "audio/*"
        }, "TITLE"), REQUEST_PICK_AUDIO)
    }

    private fun startPreparingAudio() {
        showProgressDialog()
        mediaPlayer.reset()
        mediaPlayer.setDataSource(this, audioUri)
        mediaPlayer.prepareAsync()
    }

    private fun finishPreparingAudio() {
        hideProgressDialog()
        changeCutBegin(0)
        changeCutEnd(mediaPlayer.duration)
        println(audioUri)
        val cursor = contentResolver.query(audioUri, arrayOf(DocumentsContract.Document.COLUMN_DISPLAY_NAME), null, null, null)
        val title = if (cursor.moveToFirst()) {
            println(cursor.columnCount)
            for (i in 0..(cursor.columnCount - 1)) {
                println("Col[$i](${cursor.getColumnName(i)}) = ${cursor.getString(i)}")
            }
            cursor.getString(0)
        } else ""
        cursor.close()
        playingAudioTitleTextView.text = title
        audioProgressBar.min = 0
        audioProgressBar.max = mediaPlayer.duration
        isMediaPlayerAvailable = true
    }

    private fun showProgressDialog() {
        if (progressDialog != null) {
            hideProgressDialog()
        }
        @Suppress("DEPRECATION")
        progressDialog = ProgressDialog(this, R.style.AppTheme)
        @Suppress("DEPRECATION")
        progressDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progressDialog!!.show()
    }

    private fun hideProgressDialog() {
        if (progressDialog != null) {
            progressDialog!!.dismiss()
            progressDialog = null
        }
    }

    private fun playOrCut() {
        if (mediaPlayer.isPlaying) {
            // Cut!
            println("CUT")
            playCutFab.setImageResource(R.drawable.ic_play_arrow_black_24dp)
            changeCutEnd(mediaPlayer.currentPosition)
            mediaPlayer.pause()
            mediaPlayer.seekTo(currentCutBegin)
            // In case that the user pause (cut) before current repeating finish
            handler.removeCallbacks(resetToBeginRunnable)
            println("[$currentCutBegin, $currentCutEnd]")
        } else {
            println("PLAY")
            playCutFab.setImageResource(R.drawable.ic_cut_black_24dp)
            mediaPlayer.start()
            handler.postDelayed(resetToBeginRunnable, (currentCutEnd - mediaPlayer.currentPosition).toLong())
        }
    }

    private fun startNextCut(): Boolean {
        if (mediaPlayer.isPlaying) {
            return false
        }
        changeCutBegin(currentCutEnd)
        changeCutEnd(mediaPlayer.duration)
        playCutFab.setImageResource(R.drawable.ic_cut_black_24dp)
        mediaPlayer.seekTo(currentCutBegin)
        mediaPlayer.start()
        return true
    }

    private fun changeCutBegin(msec: Int) {
        currentCutBegin = msec
        audioProgressBar.durationStart = currentCutBegin
    }

    private fun changeCutEnd(msec: Int) {
        currentCutEnd = msec
        audioProgressBar.durationEnd = currentCutEnd
    }

    companion object {
        private const val REQUEST_PICK_AUDIO = 1
    }
}
