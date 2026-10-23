package com.example.flappybirdclone

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.startapp.sdk.adsbase.StartAppAd
import com.startapp.sdk.adsbase.StartAppSDK

class MainActivity : AppCompatActivity() {

    companion object {
        var preloadedRewardedAd: StartAppAd? = null
        var preloadedInterstitialAd: StartAppAd? = null
        var gameOverCounter = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Start.io SDK Initialize
        StartAppSDK.init(this, "209351830", false)
        StartAppSDK.enableReturnAds(false)

        preloadAds()

        val btnPlay = findViewById<Button>(R.id.btnPlay)
        val tvHighScore = findViewById<TextView>(R.id.tvHighScore)

        val sharedPreferences: SharedPreferences = getSharedPreferences("FlappyBirdPrefs", MODE_PRIVATE)
        val highScore = sharedPreferences.getInt("HIGH_SCORE", 0)
        tvHighScore.text = "High Score: $highScore"

        btnPlay.setOnClickListener {
            val intent = Intent(this, GameActivity::class.java)
            startActivity(intent)
        }
    }

    private fun preloadAds() {
        // Rewarded Video Preload
        preloadedRewardedAd = StartAppAd(this)
        preloadedRewardedAd?.loadAd(StartAppAd.AdMode.REWARDED_VIDEO)

        // Interstitial Preload
        preloadedInterstitialAd = StartAppAd(this)
        preloadedInterstitialAd?.loadAd(StartAppAd.AdMode.AUTOMATIC)
    }

    override fun onResume() {
        super.onResume()
        val sharedPreferences = getSharedPreferences("FlappyBirdPrefs", MODE_PRIVATE)
        val highScore = sharedPreferences.getInt("HIGH_SCORE", 0)
        val tvHighScore = findViewById<TextView>(R.id.tvHighScore)
        tvHighScore.text = "High Score: $highScore"

        if (preloadedRewardedAd == null || !preloadedRewardedAd!!.isReady) {
            preloadedRewardedAd = StartAppAd(this)
            preloadedRewardedAd?.loadAd(StartAppAd.AdMode.REWARDED_VIDEO)
        }
        if (preloadedInterstitialAd == null || !preloadedInterstitialAd!!.isReady) {
            preloadedInterstitialAd = StartAppAd(this)
            preloadedInterstitialAd?.loadAd(StartAppAd.AdMode.AUTOMATIC)
        }
    }
}