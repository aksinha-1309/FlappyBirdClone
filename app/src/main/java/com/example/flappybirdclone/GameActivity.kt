package com.example.flappybirdclone

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
// import com.google.android.gms.ads.AdRequest
// import com.google.android.gms.ads.LoadAdError
// import com.google.android.gms.ads.MobileAds
// import com.google.android.gms.ads.interstitial.InterstitialAd
// import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
// import com.google.android.gms.ads.rewarded.RewardedAd
// import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

class GameActivity : AppCompatActivity() {

    // private var interstitialAd: InterstitialAd? = null
    // private var rewardedAd: RewardedAd? = null
    // private val TAG = "AdMobGameActivity"

    private lateinit var gameView: GameView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1. Mobile Ads SDK को Initialize करें (डिसेबल)
        // MobileAds.initialize(this) {}

        // 2. बैकग्राउंड में दोनों एड्स लोड करना शुरू करें (डिसेबल)
        // loadInterstitialAd()
        // loadRewardedAd()

        gameView = GameView(this)
        setContentView(gameView)
    }

    // --- Interstitial Ad लोड करने का फंक्शन (डिसेबल) ---
    fun loadInterstitialAd() {}

    // --- Interstitial Ad दिखाने का फंक्शन (डिसेबल) ---
    fun showInterstitialAd() {}

    // --- Rewarded Ad लोड करने का फंक्शन (डिसेबल) ---
    fun loadRewardedAd() {}

    // --- Rewarded Ad दिखाने का फंक्शन ---
    fun showRewardedAd(onRewardEarned: () -> Unit) {
        // बिना रुकावट के सीधे रिवॉर्ड/रिवाइव एक्शन दें
        Toast.makeText(this, "Game Continued.", Toast.LENGTH_SHORT).show()
        onRewardEarned()
    }
}