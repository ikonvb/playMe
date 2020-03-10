package com.bulyginkonstantin.playme.activities

import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.widget.SeekBar
import android.widget.SeekBar.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bulyginkonstantin.playme.R
import com.bulyginkonstantin.playme.adapters.ItemClicked
import com.bulyginkonstantin.playme.adapters.MusicAdapter
import com.bulyginkonstantin.playme.data.Music
import kotlinx.android.synthetic.main.activity_music_player.*
import java.util.concurrent.TimeUnit

private const val REQUEST_CODE_EXTERNAL_STORAGE = 1

class MusicPlayerActivity : AppCompatActivity(), ItemClicked {

    private lateinit var songList: MutableList<Music>
    private lateinit var adapter: MusicAdapter
    private var currentPosition = 5
    private var isPlaying = false
    private var mediaPlayer: MediaPlayer = MediaPlayer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_music_player)
        songList = mutableListOf()

        if (Build.VERSION.SDK_INT >= 23) {
            checkPermissions()
            fabPlay.setOnClickListener {
                play(currentPosition)
            }

            fabNext.setOnClickListener {
                mediaPlayer.reset()
                isPlaying = false
                if (currentPosition < songList.size - 1) {
                    currentPosition++
                }

                play(currentPosition)
            }

            fabPrevious.setOnClickListener {
                mediaPlayer.reset()
                isPlaying = false
                if (currentPosition > 0) {
                    currentPosition--
                }
                play(currentPosition)
            }

            seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        mediaPlayer.seekTo(progress * 1000)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                }
            })
        }
    }

    private fun play(pos: Int) {
        if (!isPlaying) {
            fabPlay.setImageDrawable(resources.getDrawable(R.drawable.ic_stop, null))
            isPlaying = true
            mediaPlayer.apply {
                setAudioAttributes(AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build())
                setDataSource(this@MusicPlayerActivity, Uri.parse(songList[pos].songUri))
                prepare()
                start()
            }

            val handler = Handler()
            this@MusicPlayerActivity.runOnUiThread(object : Runnable {
                override fun run() {
                    val playerPosition  = mediaPlayer.currentPosition / 1000
                    val totalDuration = mediaPlayer.duration / 1000
                    seekBar.max = totalDuration
                    seekBar.progress = playerPosition
                    handler.postDelayed(this, 1000)
                    passTv.text = timerFormat(playerPosition.toLong())
                    remainTv.text = timerFormat((totalDuration - playerPosition).toLong())
                }
            })
        } else {
            isPlaying = false
            mediaPlayer.reset()
            fabPlay.setImageDrawable(resources.getDrawable(R.drawable.ic_play_arrow, null))
        }
    }

    private fun timerFormat(time: Long): String {

        var result = String.format("%02d:%02d",
            TimeUnit.SECONDS.toMinutes(time),
            TimeUnit.SECONDS.toSeconds(time) - TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(time)))

        var convert = ""
        for (element in result) {
            convert += element
        }
        return convert
    }

    private fun checkPermissions() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            getSongs()
        } else {
            //false-> user asked not to ask me anymore || permission disabled
            //true-> reject before want to use the future again
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                )
            ) {
                Toast.makeText(this, "Music player needs Access to your files", Toast.LENGTH_SHORT)
                    .show()
            }
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_CODE_EXTERNAL_STORAGE
            )
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_CODE_EXTERNAL_STORAGE -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //read songs
                getSongs()
            } else {
                Toast.makeText(this, "Permission is not granted", Toast.LENGTH_SHORT).show()
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun getSongs() {
        val selection = MediaStore.Audio.Media.IS_MUSIC
        val projection = arrayOf(
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DATA
        )
        val cursor = contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            null
        )

        if (cursor != null) {
            while (cursor.moveToNext()) {
                songList.add(Music(cursor.getString(0), cursor.getString(1), cursor.getString(2)))
            }
        }
        cursor?.close()
        adapter = MusicAdapter(songList, this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    override fun itemClicked(position: Int) {
        mediaPlayer.reset()
        isPlaying = false
        this.currentPosition = position
        play(currentPosition)
    }
}
