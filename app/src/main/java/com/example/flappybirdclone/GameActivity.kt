package com.example.flappybirdclone

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.startapp.sdk.adsbase.StartAppAd
import com.startapp.sdk.adsbase.adlisteners.VideoListener
import com.startapp.sdk.adsbase.Ad
import com.startapp.sdk.adsbase.adlisteners.AdEventListener

class GameActivity : AppCompatActivity() {

    private lateinit var gameView: GameView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        gameView = GameView(this)
        setContentView(gameView)
    }

    // 3 बार गेम ओवर पर Interstitial Ad दिखाएं
    fun checkAndShowInterstitialOnGameOver() {
        MainActivity.gameOverCounter++

        if (MainActivity.gameOverCounter % 3 == 0) {
            val interstitialAd = MainActivity.preloadedInterstitialAd
            if (interstitialAd != null && interstitialAd.isReady) {
                interstitialAd.showAd()
            } else {
                val fallbackAd = StartAppAd(this)
                fallbackAd.loadAd(StartAppAd.AdMode.AUTOMATIC, object : AdEventListener {
                    override fun onReceiveAd(ad: Ad) {
                        fallbackAd.showAd()
                    }
                    override fun onFailedToReceiveAd(ad: Ad?) {}
                })
            }
            MainActivity.preloadedInterstitialAd = StartAppAd(this)
            MainActivity.preloadedInterstitialAd?.loadAd(StartAppAd.AdMode.AUTOMATIC)
        }
    }

    // Rewarded Ad विद स्मार्ट बैकअप (क्विज़ ऐप वाला लॉजिक)
    fun showRewardedAd(onRewardEarned: () -> Unit) {
        val adToPlay = MainActivity.preloadedRewardedAd

        // 1st Priority: अगर असली Rewarded Ad ready है, तो वही चलाओ (ज़्यादा कमाई)
        if (adToPlay != null && adToPlay.isReady) {
            adToPlay.setVideoListener(object : VideoListener {
                override fun onVideoCompleted() {
                    runOnUiThread {
                        onRewardEarned()
                        MainActivity.preloadedRewardedAd = StartAppAd(this@GameActivity)
                        MainActivity.preloadedRewardedAd?.loadAd(StartAppAd.AdMode.REWARDED_VIDEO)
                    }
                }
            })
            adToPlay.showAd()
        } else {
            // 2nd Priority (Backup): अगर वीडियो फेल हो जाए, तो बैकअप के लिए Normal Ad चलाकर रिवाइव कर दो
            val backupAd = StartAppAd(this)
            backupAd.loadAd(StartAppAd.AdMode.AUTOMATIC, object : AdEventListener {
                override fun onReceiveAd(ad: Ad) {
                    backupAd.showAd()
                    onRewardEarned() // यूज़र को रिवाइव कर दो
                }
                override fun onFailedToReceiveAd(ad: Ad?) {
                    // अगर दोनों ही फेल हो जाएं, तब भी यूज़र को गेम खेलने दो
                    Toast.makeText(this@GameActivity, "Ad not available, reviving...", Toast.LENGTH_SHORT).show()
                    onRewardEarned()
                }
            })

            // साथ ही बैकग्राउंड में अगला Rewarded Ad लोड होने रख दो
            MainActivity.preloadedRewardedAd = StartAppAd(this)
            MainActivity.preloadedRewardedAd?.loadAd(StartAppAd.AdMode.REWARDED_VIDEO)
        }
    }
}