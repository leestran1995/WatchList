package com.example.archer.watchlist

import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.reward.RewardItem
import com.google.android.gms.ads.reward.RewardedVideoAd
import com.google.android.gms.ads.reward.RewardedVideoAdListener
import kotlinx.android.synthetic.main.activity_play.*
import java.util.*

class PlayActivity : AppCompatActivity(), RewardedVideoAdListener {
    override fun onRewarded(reward: RewardItem) {
        advanceProgramming()
    }

    override fun onRewardedVideoAdLeftApplication() {
        Toast.makeText(this, "onRewardedVideoAdLeftApplication", Toast.LENGTH_SHORT).show()
    }

    override fun onRewardedVideoAdClosed() {
        loadRewardedVideoAd()
    }

    override fun onRewardedVideoAdFailedToLoad(errorCode: Int) {
        Toast.makeText(this, "Ad failed to load", Toast.LENGTH_SHORT).show()
    }

    override fun onRewardedVideoAdLoaded() {

    }

    override fun onRewardedVideoAdOpened() {
    }

    override fun onRewardedVideoStarted() {
    }

    override fun onRewardedVideoCompleted() {
    }


    lateinit var mChannel: Channel
    val mHandler: Handler = Handler()
    lateinit var mRewardedVideoAd: RewardedVideoAd

    fun loadRewardedVideoAd() {
        mRewardedVideoAd.loadAd("ca-app-pub-3940256099942544/5224354917",
                AdRequest.Builder().build())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play)
        setSupportActionBar(toolbar)

        MobileAds.initialize(this, "ca-app-pub-3940256099942544/5224354917")
        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this)
        mRewardedVideoAd.rewardedVideoAdListener = this
        loadRewardedVideoAd()

        fab.hide()  // Can't figure out how to get rid of this icon otherwise

        mChannel = intent.getSerializableExtra("channel") as Channel

        displayCurrentMedia()
        displayClockView()

        //  Update the programming on the 00, 20, and 40 minute marks
        val runnableCode = object: Runnable {
            override fun run() {
                advanceProgramming()
                val currentTime = Calendar.getInstance().timeInMillis
                val nextTime = getNextTwentyMinuteMark().time
                mHandler.postDelayed(this, nextTime - currentTime)
            }
        }
        mHandler.post(runnableCode)
    }

    fun displayCurrentMedia() {
        val titleView: TextView = findViewById(R.id.play_title)
        val imageView: ImageView = findViewById(R.id.play_image)
        val summaryView: TextView = findViewById(R.id.play_summary)

        val randomNum = Random(mChannel.currentSeed.toLong())
        val nextInt = randomNum.nextInt(mChannel.media.size)
        val currentMedia: Media = mChannel.media[nextInt]

        titleView.text = currentMedia.title
        summaryView.text = currentMedia.summary

        supportActionBar?.title = mChannel.title

        Glide.with(this)
                .load(currentMedia.imageLink)
                .into(imageView)
    }

    private fun displayClockView() {
        val clockView: TextView = findViewById(R.id.textClock)
        clockView.setOnClickListener {
            Toast.makeText(this, "Programming changes every 20 minutes", Toast.LENGTH_SHORT).show()
            if(mRewardedVideoAd.isLoaded) {
                mRewardedVideoAd.show()
            }
        }
    }

    fun advanceProgramming() {
        /*
         * We increment by 10,000 because of oddities of
         * pseudo-random number generation when
         * seeds change by only a small amount.
         */
        mChannel.currentSeed += 10000
        displayCurrentMedia()
    }

    fun getNextTwentyMinuteMark(): Date {
        val date = Calendar.getInstance(Locale.getDefault())
        val t: Long = date.timeInMillis
        val minsToAdd: Int = 20 - (date.get(Calendar.MINUTE) % 20)
        val secondsToSubtract: Int = date.get(Calendar.SECOND)
        return Date(t + (minsToAdd * 60000) - (secondsToSubtract * 1000)) // 60000 is one minute in milliseconds
    }
}
