package com.example.flappybirdclone

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.media.AudioAttributes
import android.media.SoundPool
import android.view.MotionEvent
import android.view.View
import java.util.Random

class GameView(context: Context) : View(context), Runnable {

    private var thread: Thread? = Thread(this)
    private var isPlaying = false
    private val paint = Paint()

    // SharedPreferences high score
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("FlappyBirdPrefs", Context.MODE_PRIVATE)
    private var highScore = 0

    private var isWaitingToStart = true
    private var isWaitingAfterRevive = false
    private var isGameOver = false

    // SoundPool variables
    private lateinit var soundPool: SoundPool
    private var jumpSound = 0
    private var gameOverSound = 0

    // --- बैलेंस्ड Classic Physics (ना बहुत तेज गिरेगी, ना उड़ेगी) ---
    private var birdX = 200f
    private var birdY = 500f
    private var birdRadius = 45f
    private var velocity = 0f
    // स्क्रीन हाइट के हिसाब से डायनेमिक वैल्यू, जो बिल्कुल तेरे पसंदीदा 1.50f और -21f जैसी ही फीलिंग देगी


    // Screen Dimensions for Pipe & Bird Scaling
    val displayMetrics = resources.displayMetrics
    val screenWidth = displayMetrics.widthPixels.toFloat()
    val screenHeight = displayMetrics.heightPixels.toFloat()
    // स्क्रीन हाइट के हिसाब से डायनेमिक वैल्यू, जो बिल्कुल तेरे पसंदीदा 1.50f और -21f जैसी ही फीलिंग देगी
    private val gravity = screenHeight * 0.00065f
    private val jumpStrength = -(screenHeight * 0.009f)

    // Pipes variables
    val pipeWidth = screenWidth * 0.18f
    var pipeGap = screenHeight * 0.22f
    private val minPipeGap = screenHeight * 0.16f // न्यूनतम गैप लिमिट (नया जोड़ ले)
    private var pipeX = 1800f
    private var topPipeHeight = 500f
    private var pipeSpeed = 13f        // पाइप की स्पीड को भी थोड़ा बैलेंस्ड किया
    private val random = Random()

    private var score = 0

    // Bitmaps
    private val backgroundBitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.back_image)
    private val birdBitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.bird)
    private val restartBitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.restar)

    private lateinit var scaledBgBitmap: Bitmap
    private lateinit var scaledBirdBitmap: Bitmap
    private lateinit var scaledRestartBitmap: Bitmap

    init {
        highScore = sharedPreferences.getInt("HIGH_SCORE", 0)

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(2)
            .setAudioAttributes(audioAttributes)
            .build()

        try {
            jumpSound = soundPool.load(context, R.raw.jump, 1)
            gameOverSound = soundPool.load(context, R.raw.gameover, 1)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        isPlaying = true
        thread?.start()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0 && h > 0) {
            scaledBgBitmap = Bitmap.createScaledBitmap(backgroundBitmap, w, h, true)

            // सिर्फ Bird का साइज़ हर स्क्रीन के हिसाब से ऑटो-एडजस्ट
            val birdWidth = (w * 0.11f).toInt()
            val birdHeight = (birdWidth * 0.8f).toInt()
            scaledBirdBitmap = Bitmap.createScaledBitmap(birdBitmap, birdWidth, birdHeight, true)

            birdRadius = birdWidth / 2f
            scaledRestartBitmap = Bitmap.createScaledBitmap(restartBitmap, 160, 160, true)
        }
    }

    override fun run() {
        while (isPlaying) {
            if (!isGameOver && !isWaitingAfterRevive && !isWaitingToStart) {
                update()
            }
            postInvalidate()
            try {
                Thread.sleep(17) // ~60 FPS
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }

    private fun handleGameOverLogic() {
        if (!isGameOver) {
            playGameOverSound()
            checkHighScore()
            isGameOver = true

            // यहाँ हर 3 गेम ओवर पर इंटरस्टिशियल ऐड दिखाने वाला लॉजिक कॉल किया गया है
            if (context is GameActivity) {
                (context as GameActivity).checkAndShowInterstitialOnGameOver()
            }
        }
    }
    private fun update() {
        velocity += gravity
        birdY += velocity

        if (height > 0 && birdY >= height - birdRadius) {
            birdY = (height - birdRadius).toFloat()
            handleGameOverLogic()
        }
        if (birdY <= birdRadius) {
            birdY = birdRadius
            velocity = 0f
        }

        // 1. स्कोर बढ़ने के साथ स्पीड धीरे-धीरे बढ़ेगी
        pipeSpeed = (screenHeight * 0.006f) + (score * 0.25f).coerceAtMost(6f)

        // 2. स्कोर बढ़ने के साथ गैप धीरे-धीरे सिमटेगा
        pipeGap = (screenHeight * 0.22f - (score * 3f)).coerceAtLeast(minPipeGap)

        pipeX -= pipeSpeed
        if (pipeX < -pipeWidth) {
            pipeX = screenWidth + 300f

            // 3. पाइप्स की हाइट अब ज़्यादा ऊपर-नीचे नहीं भटकेगी,
            // यह स्क्रीन के बीच में रहेगी और केवल हल्का-फुल्का वेरिएशन देगी।
            val centerHeight = screenHeight * 0.35f
            val variation = screenHeight * 0.12f
            val minH = (centerHeight - variation).toInt()
            val maxH = (centerHeight + variation).toInt()
            topPipeHeight = (random.nextInt(maxH - minH) + minH).toFloat()

            score++
        }

        val bottomPipeY = topPipeHeight + pipeGap
        if (birdX + birdRadius > pipeX && birdX - birdRadius < pipeX + pipeWidth) {
            if (birdY - birdRadius < topPipeHeight || birdY + birdRadius > bottomPipeY) {
                handleGameOverLogic()
            }
        }
    }

    private fun playJumpSound() {
        if (jumpSound != 0) {
            soundPool.play(jumpSound, 1f, 1f, 0, 0, 1f)
        }
    }

    private fun playGameOverSound() {
        if (gameOverSound != 0) {
            soundPool.play(gameOverSound, 1f, 1f, 0, 0, 1f)
        }
    }

    private fun checkHighScore() {
        if (score > highScore) {
            highScore = score
            val editor = sharedPreferences.edit()
            editor.putInt("HIGH_SCORE", highScore)
            editor.apply()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Background
        if (::scaledBgBitmap.isInitialized) {
            canvas.drawBitmap(scaledBgBitmap, 0f, 0f, null)
        } else {
            canvas.drawColor(Color.CYAN)
        }

        // Pipes
        paint.color = Color.parseColor("#8B5A2B")
        canvas.drawRoundRect(pipeX, 0f, pipeX + pipeWidth, topPipeHeight, 0f, 25f, paint)
        paint.color = Color.parseColor("#5C4033")
        canvas.drawRoundRect(pipeX - 6f, topPipeHeight - 40f, pipeX + pipeWidth + 6f, topPipeHeight, 15f, 15f, paint)

        val bottomPipeY = topPipeHeight + pipeGap
        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor("#8B5A2B")
        canvas.drawRoundRect(pipeX, bottomPipeY, pipeX + pipeWidth, height.toFloat() + 30f, 25f, 25f, paint)
        paint.color = Color.parseColor("#5C4033")
        canvas.drawRoundRect(pipeX - 6f, bottomPipeY, pipeX + pipeWidth + 6f, bottomPipeY + 40f, 15f, 15f, paint)
        paint.style = Paint.Style.FILL

        // Bird
        if (::scaledBirdBitmap.isInitialized) {
            canvas.drawBitmap(scaledBirdBitmap, birdX - scaledBirdBitmap.width / 2, birdY - scaledBirdBitmap.height / 2, null)
        }

        // Score
        paint.color = Color.WHITE
        paint.textSize = 70f
        canvas.drawText("Score: $score", 50f, 130f, paint)

        // Tap to Start Message
        if (isWaitingToStart && !isGameOver) {
            paint.style = Paint.Style.FILL
            paint.color = Color.parseColor("#77000000")
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

            paint.textSize = 65f
            paint.color = Color.YELLOW
            canvas.drawText("Tap to Start!", 110f, (height / 2).toFloat(), paint)
        }

        // Continue / Revive Message
        if (isWaitingAfterRevive) {
            paint.style = Paint.Style.FILL
            paint.color = Color.parseColor("#77000000")
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

            paint.textSize = 65f
            paint.color = Color.YELLOW
            canvas.drawText("Continue", 120f, (height / 2).toFloat(), paint)
        }

        // Game Over Screen
        if (isGameOver) {
            paint.style = Paint.Style.FILL
            paint.color = Color.parseColor("#99000000")
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

            paint.textSize = 90f
            paint.color = Color.RED
            canvas.drawText("Game Over", 180f, (height / 2 - 220).toFloat(), paint)

            paint.textSize = 60f
            paint.color = Color.WHITE
            canvas.drawText("Score: $score", 220f, (height / 2 - 120).toFloat(), paint)
            canvas.drawText("High Score: $highScore", 220f, (height / 2 - 50).toFloat(), paint)

            // Watch Ad to Revive Button
            paint.color = Color.parseColor("#4CAF50")
            paint.style = Paint.Style.FILL
            val reviveBtnLeft = (width / 2 - 300).toFloat()
            val reviveBtnTop = (height / 2 + 20).toFloat()
            val reviveBtnRight = (width / 2 + 300).toFloat()
            val reviveBtnBottom = (height / 2 + 130).toFloat()
            canvas.drawRoundRect(reviveBtnLeft, reviveBtnTop, reviveBtnRight, reviveBtnBottom, 30f, 30f, paint)

            paint.color = Color.WHITE
            paint.textSize = 45f
            canvas.drawText("▶️ Watch Ad to Revive", reviveBtnLeft + 60f, reviveBtnTop + 75f, paint)

            // Restart Image Button
            if (::scaledRestartBitmap.isInitialized) {
                val restartLeft = (width / 2 - scaledRestartBitmap.width / 2).toFloat()
                val restartTop = (height / 2 + 160).toFloat()
                canvas.drawBitmap(scaledRestartBitmap, restartLeft, restartTop, null)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            if (isWaitingToStart) {
                isWaitingToStart = false
                velocity = jumpStrength
                playJumpSound()
                return true
            }

            if (isWaitingAfterRevive) {
                isWaitingAfterRevive = false
                velocity = jumpStrength
                playJumpSound()
                return true
            }

            if (isGameOver) {
                val touchX = event.x
                val touchY = event.y

                val reviveBtnLeft = (width / 2 - 300).toFloat()
                val reviveBtnTop = (height / 2 + 20).toFloat()
                val reviveBtnRight = (width / 2 + 300).toFloat()
                val reviveBtnBottom = (height / 2 + 130).toFloat()

                if (touchX in reviveBtnLeft..reviveBtnRight && touchY in reviveBtnTop..reviveBtnBottom) {
                    if (context is GameActivity) {
                        (context as GameActivity).showRewardedAd {
                            isGameOver = false
                            isWaitingAfterRevive = true
                            birdY = (height / 2).toFloat()
                            velocity = 0f
                            pipeX = width.toFloat() + 400f
                        }
                    }
                }

                if (::scaledRestartBitmap.isInitialized) {
                    val restartLeft = (width / 2 - scaledRestartBitmap.width / 2).toFloat()
                    val restartRight = restartLeft + scaledRestartBitmap.width
                    val restartTop = (height / 2 + 160).toFloat()
                    val restartBottom = restartTop + scaledRestartBitmap.height

                    if (touchX in restartLeft..restartRight && touchY in restartTop..restartBottom) {
                        isGameOver = false
                        isWaitingToStart = true
                        isWaitingAfterRevive = false
                        birdY = 500f
                        velocity = 0f
                        pipeX = 1800f
                        score = 0
                    }
                }
            } else {
                velocity = jumpStrength
                playJumpSound()
            }
        }
        return true
    }
}