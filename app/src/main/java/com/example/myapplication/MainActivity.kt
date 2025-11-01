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

    private lateinit var soundPool: SoundPool
    private var successSoundId: Int = 0
    private var failureSoundId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        setupSound()
        setupUI()
        observeViewModel()
    }

    private fun setupSound() {
        soundPool = SoundPool.Builder().setMaxStreams(2).build()
        successSoundId = soundPool.load(this, R.raw.success, 1)
        failureSoundId = soundPool.load(this, R.raw.failure, 1)
    }

    private fun setupUI() {
        startNameAnimation()
        binding.btnGuess.setOnClickListener {
            viewModel.onGuess(binding.etGuess.text.toString())
            binding.etGuess.text?.clear()
        }
        binding.btnHint.setOnClickListener {
            viewModel.onHint(binding.etGuess.text.toString())
        }
        binding.btnNext.setOnClickListener {
            binding.lottieAnimationView.visibility = View.GONE
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
                playSound(successSoundId)
                vibrate(longArrayOf(0, 100, 100, 100))
                binding.lottieAnimationView.visibility = View.VISIBLE
                binding.lottieAnimationView.playAnimation()
                binding.btnNext.isVisible = true
                setGameInProgress(false)
            }
            GameStatus.LOST -> {
                playSound(failureSoundId)
                vibrate(longArrayOf(0, 400))
                binding.btnRestart.isVisible = true
                setGameInProgress(false)
            }
            else -> {}
        }

        if (state.error != null) {
            val shake = AnimationUtils.loadAnimation(this, R.anim.shake)
            binding.tilGuess.startAnimation(shake)
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

    private fun playSound(soundId: Int) {
        soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
    }

    private fun vibrate(pattern: LongArray) {
        val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        if (vibrator.hasVibrator()) {
            vibrator.vibrate(pattern, -1)
        }
    }

    private fun startNameAnimation() {
        val animator = ValueAnimator.ofArgb(
            getRandomColor(), getRandomColor(), getRandomColor(), getRandomColor(), getRandomColor()
        )
        animator.duration = 4000
        animator.repeatCount = ValueAnimator.INFINITE
        animator.repeatMode = ValueAnimator.REVERSE
        animator.addUpdateListener { animation ->
            binding.tvName.setTextColor(animation.animatedValue as Int)
        }
        animator.start()
    }

    private fun getRandomColor(): Int {
        val rnd = Random()
        return Color.rgb(rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))
    }

    override fun onDestroy() {
        super.onDestroy()
        soundPool.release()
    }
}
