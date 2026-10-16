package com.example.flappybirdclone

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
// import com.google.android.gms.ads.AdRequest
// import com.google.android.gms.ads.LoadAdError
// import com.google.android.gms.ads.MobileAds
// import com.google.android.gms.ads.rewarded.RewardedAd
// import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

class MainActivity : AppCompatActivity() {

    // private var rewardedAd: RewardedAd? = null
    // private val TAG = "AdMobTest"

    // गूगल की ऑफिशियल Test Rewarded Ad Unit ID (टेस्टिंग के लिए)
    // private val testRewardedAdId = "ca-app-pub-3940256099942544/5224354917"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Mobile Ads SDK को Initialize करें (अभी डिसेबल है)
        // MobileAds.initialize(this) {}

        // 2. बैकग्राउंड में टेस्ट एड लोड करना शुरू करें
        // loadRewardedAd()

        val btnPlay = findViewById<Button>(R.id.btnPlay)
        val tvHighScore = findViewById<TextView>(R.id.tvHighScore)

        // SharedPreferences से High Score fetch करके दिखाना
        val sharedPreferences: SharedPreferences = getSharedPreferences("FlappyBirdPrefs", MODE_PRIVATE)
        val highScore = sharedPreferences.getInt("HIGH_SCORE", 0)
        tvHighScore.text = "High Score: $highScore"

        // Play button क्लिक करने पर GameActivity पर जाना
        btnPlay.setOnClickListener {
            val intent = Intent(this, GameActivity::class.java)
            startActivity(intent)
        }
    }

    // एड लोड करने का फंक्शन (डिसेबल)
    /*
    private fun loadRewardedAd() {
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(this, testRewardedAdId, adRequest, object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d(TAG, "Ad Loading Failed: ${adError.message}")
                rewardedAd = null
            }
            override fun onAdLoaded(ad: RewardedAd) {
                Log.d(TAG, "Rewarded Ad Loaded Successfully!")
                rewardedAd = ad
            }
        })
    }
    */

    // जब गेम में प्लेयर आउट हो और रिवाइव करना हो
    fun showRewardedAdToRevive(onSuccess: () -> Unit) {
        // चूंकि अभी ऐड्स हटाए गए हैं, डायरेक्ट रिवाइव एक्शन ट्रिगर होगा
        Toast.makeText(this, "Revived!", Toast.LENGTH_SHORT).show()
        onSuccess()
    }

    // जब user गेम से वापस menu पर आये, तब high score update दिखे इसलिए onResume use कर रहे हैं
    override fun onResume() {
        super.onResume()
        val sharedPreferences = getSharedPreferences("FlappyBirdPrefs", MODE_PRIVATE)
        val highScore = sharedPreferences.getInt("HIGH_SCORE", 0)
        val tvHighScore = findViewById<TextView>(R.id.tvHighScore)
        tvHighScore.text = "High Score: $highScore"
    }
}