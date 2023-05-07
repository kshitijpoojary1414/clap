package com.kpoojary.clap
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.workDataOf


class MainActivity : AppCompatActivity() {

    private var points = 0
    private var currentQuestion: String? = null
    private var currentAnswer: String? = null

    private var totalQuestion = 0
    private var soundPool: SoundPool? = null
    private var successSound = -1
    private var failureSound = -1

    private var imgClap: ImageView? = null
    private var textView1: TextView? = null
    private var response: EditText? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imgClap = findViewById(R.id.imgClap)
        textView1 = findViewById(R.id.textView1)
        response = findViewById(R.id.response_edit_text)
        initialize()
    }

    fun initialize() {
        assignQuestion()
        resetView()
        initializeSound()
    }

    fun initializeSound() {
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setAudioAttributes(attributes)
            .build()

        successSound =  soundPool!!.load(baseContext, R.raw.clap, 1)
        failureSound =  soundPool!!.load(baseContext, R.raw.wrong, 1)

    }


    fun assignQuestion() {
        // Define a list of questions and answers
        val questions = listOf(
            "What is the term for a number that can be divided evenly only by 1 and itself?",
            "What is the name for the mathematical operation of finding the sum of a series of numbers and dividing by the number of terms",
            "What is the term for a polygon with four sides?",
            "What is the term for a line that intersects two or more lines at distinct points?",
            "What is the name for a set of numbers that includes the original number and all of its multiples?",
            "What is the term for the ratio of the circumference of a circle to its diameter?",
        )
        val answers = listOf(
            "Prime", "Mean", "Quadrilateral", "Transversal", "Multiples", "Pi"
        )

        // Randomly select a question and its corresponding answer
        val index = (0 until questions.size).shuffled().first()
        currentQuestion = questions[index]
        currentAnswer = answers[index]
    }


    fun calculateClick(view: View) {
        val response = response?.text.toString()

        if(TextUtils.isEmpty(response)) {
            return;
        }

        if(response == currentAnswer)
            correctResult()
        else
            wrongAnswer()
    }

    fun resetView() {
        response?.setText("")
        textView1?.setText(currentQuestion)
    }

    fun correctResult() {
        points++
        totalQuestion++

        //animate
        val anim: Animation = AnimationUtils.loadAnimation(this, R.anim.animate)
        imgClap?.setBackgroundResource(R.drawable.clap);
        imgClap?.startAnimation(anim)

        //play sound
        soundPool?.play(successSound, 1F, 1F, 0, 0, 1F)

        assignQuestion()
        resetView()
    }

    fun wrongAnswer() {
        assignQuestion()
        resetView()
        totalQuestion++

        soundPool?.play(failureSound, 1F, 1F, 0, 0, 1F)

        //animate
        val anim: Animation = AnimationUtils.loadAnimation(this, R.anim.animate)
        imgClap?.setBackgroundResource(R.drawable.tryagain);
        imgClap?.startAnimation(anim)

    }

    override fun onStop() {
        super.onStop()

        val message = "your score " + points + " out of " + totalQuestion
        // Start the Worker if the timer is running
        val timerWorkRequest: WorkRequest = OneTimeWorkRequestBuilder<TimerWorker>()
            .setInputData(
                workDataOf(
                    KEY_SCORE to message
                )
            ).build()

        WorkManager.getInstance(applicationContext).enqueue(timerWorkRequest)
    }
}