package com.example.myapplication

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val ranges = listOf(10, 20, 50, 100, 200)
    private val chancesByLevel = listOf(3, 4, 5, 6, 7)

    private var level = 1
    private var rangeMax = 0
    private var remainingChances = 0
    private var target = 0

    private lateinit var tvStatus: TextView
    private lateinit var tvMessage: TextView
    private lateinit var etGuess: EditText
    private lateinit var btnGuess: Button
    private lateinit var btnHint: Button
    private lateinit var btnNext: Button
    private lateinit var btnRestart: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvStatus = findViewById(R.id.tvStatus)
        tvMessage = findViewById(R.id.tvMessage)
        etGuess = findViewById(R.id.etGuess)
        btnGuess = findViewById(R.id.btnGuess)
        btnHint = findViewById(R.id.btnHint)
        btnNext = findViewById(R.id.btnNext)
        btnRestart = findViewById(R.id.btnRestart)

        startLevel(level)

        btnGuess.setOnClickListener {
            handleGuess()
        }

        btnHint.setOnClickListener {
            handleHint()
        }

        btnNext.setOnClickListener {
            level++
            startLevel(level)
        }

        btnRestart.setOnClickListener {
            level = 1
            startLevel(level)
        }
    }

    private fun startLevel(lvl: Int) {
        level = lvl
        rangeMax = ranges[level - 1]
        remainingChances = chancesByLevel[level - 1]
        target = (1..rangeMax).random()

        tvStatus.text = "مرحله: $level | بازه: 1-$rangeMax | شانس: $remainingChances"
        tvMessage.text = ""
        etGuess.text.clear()

        btnGuess.isEnabled = true
        btnHint.isEnabled = true
        btnNext.visibility = View.GONE
        btnRestart.visibility = View.GONE
    }

    private fun handleGuess() {
        val guessStr = etGuess.text.toString()
        if (guessStr.isEmpty()) {
            tvMessage.text = getString(R.string.invalid_input)
            return
        }

        val guess = guessStr.toInt()
        if (guess < 1 || guess > rangeMax) {
            tvMessage.text = getString(R.string.out_of_range)
            return
        }

        when {
            guess == target -> {
                if (level == 5) {
                    tvMessage.text = getString(R.string.you_won)
                    btnGuess.isEnabled = false
                    btnHint.isEnabled = false
                    btnNext.visibility = View.GONE
                    btnRestart.visibility = View.VISIBLE
                } else {
                    tvMessage.text = getString(R.string.correct_guess)
                    btnGuess.isEnabled = false
                    btnHint.isEnabled = false
                    btnNext.visibility = View.VISIBLE
                }
            }
            else -> {
                remainingChances--
                if (remainingChances == 0) {
                    tvMessage.text = getString(R.string.you_lost)
                    btnGuess.isEnabled = false
                    btnHint.isEnabled = false
                    btnRestart.visibility = View.VISIBLE
                } else {
                    tvMessage.text = getString(R.string.wrong_guess)
                }
                tvStatus.text = "مرحله: $level | بازه: 1-$rangeMax | شانس: $remainingChances"
            }
        }
    }

    private fun handleHint() {
        if (remainingChances > 0) {
            remainingChances--
            val guessStr = etGuess.text.toString()
            if (guessStr.isNotEmpty()) {
                val guess = guessStr.toInt()
                if (guess < target) {
                    tvMessage.text = getString(R.string.bigger)
                } else {
                    tvMessage.text = getString(R.string.smaller)
                }
            } else {
                if (target > rangeMax / 2) {
                    tvMessage.text = getString(R.string.bigger)
                } else {
                    tvMessage.text = getString(R.string.smaller)
                }
            }
            tvStatus.text = "مرحله: $level | بازه: 1-$rangeMax | شانس: $remainingChances"
        }
    }
}
