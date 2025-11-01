package com.example.myapplication

import android.animation.ValueAnimator
import android.graphics.Color
import android.media.SoundPool
import android.os.Bundle
import android.os.Vibrator
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.databinding.ActivityMainBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        setupUI()
        observeViewModel()
    }


    private fun setupUI() {
        binding.btnGuess.setOnClickListener {
            viewModel.onGuess(binding.etGuess.text.toString())
            binding.etGuess.text?.clear()
        }
        binding.btnHint.setOnClickListener {
            viewModel.onHint(binding.etGuess.text.toString())
        }
        binding.btnNext.setOnClickListener {
            viewModel.goNextLevel()
        }
        binding.btnRestart.setOnClickListener { viewModel.restartGame() }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.gameState.collectLatest { state ->
                updateUiWithState(state)
            }
        }
    }

    private fun updateUiWithState(state: GameState) {
        binding.tvStage.text = getString(R.string.stage_fmt, state.level)
        binding.tvRange.text = getString(R.string.range_fmt, state.rangeMax)
        binding.tvChances.text = getString(R.string.chances_fmt, state.remainingChances)
        binding.tvMessage.text = state.message

        when (state.status) {
            GameStatus.RUNNING -> {
                setGameInProgress(true)
            }
            GameStatus.WON -> {
                vibrate(longArrayOf(0, 100, 100, 100))
                binding.btnNext.isVisible = true
                setGameInProgress(false)
            }
            GameStatus.LOST -> {
                vibrate(longArrayOf(0, 400))
                binding.btnRestart.isVisible = true
                setGameInProgress(false)
            }
            else -> {}
        }

        if (state.error != null) {
            binding.tvMessage.text = when (state.error) {
                ErrorType.INVALID_INPUT -> getString(R.string.msg_invalid_input)
                ErrorType.OUT_OF_RANGE -> getString(R.string.msg_out_of_range, state.rangeMax)
            }
        }
    }

    private fun setGameInProgress(inProgress: Boolean) {
        binding.btnGuess.isEnabled = inProgress
        binding.btnHint.isEnabled = inProgress
        binding.tilGuess.isEnabled = inProgress
        binding.btnNext.isVisible = !inProgress && viewModel.gameState.value.status == GameStatus.WON
        binding.btnRestart.isVisible = !inProgress && viewModel.gameState.value.status == GameStatus.LOST
    }


    private fun vibrate(pattern: LongArray) {
        val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        if (vibrator.hasVibrator()) {
            vibrator.vibrate(pattern, -1)
        }
    }


    override fun onDestroy() {
        super.onDestroy()
    }
}
