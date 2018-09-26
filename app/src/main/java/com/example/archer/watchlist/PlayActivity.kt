package com.example.archer.watchlist

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide

import kotlinx.android.synthetic.main.activity_play.*
import kotlinx.android.synthetic.main.layout_listitem.view.*
import org.w3c.dom.Text
import java.util.*

class PlayActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

        val mChannel = intent.getSerializableExtra("channel") as Channel

        val titleView: TextView = findViewById(R.id.play_title)
        val imageView: ImageView = findViewById(R.id.play_image)
        val summaryView: TextView = findViewById(R.id.play_summary)

        val randomNum = Random()
        val currentMedia: Media = mChannel.media[randomNum.nextInt(mChannel.media.size)]

        titleView.text = currentMedia.title
        summaryView.text = currentMedia.summary


        Glide.with(this)
                .load(currentMedia.imageLink)
                .into(imageView)

        val clockView: TextView = findViewById(R.id.textClock)
        clockView.setOnClickListener {
            Toast.makeText(this, "Programming changes every 20 minutes", Toast.LENGTH_SHORT).show()
        }
    }

}
