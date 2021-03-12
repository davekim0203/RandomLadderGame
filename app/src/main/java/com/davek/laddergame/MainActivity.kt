package com.davek.laddergame

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.widget.AdapterView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.davek.laddergame.databinding.ActivityMainBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.vm = viewModel
        binding.lifecycleOwner = this
        subscribeViewModel()
        setupListeners()
        loadSpeed()
    }

    private fun subscribeViewModel() {
        viewModel.showStartNewGameDialog.observe(this, {
            showStartNewGameDialog()
        })

        viewModel.showAnimationSpeedDialog.observe(this, {
            showAnimationSpeedDialog()
        })
    }

    private fun setupListeners() {
        binding.btnReset.setOnClickListener {
            if (!binding.ladderBoard.isDrawingPlayerPath) {
                viewModel.onResetStepsButtonClick()
            }
        }
    }

    private fun showStartNewGameDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.start_new_game_dialog_title))
            .setMessage(getString(R.string.start_new_game_dialog_message))
            .setPositiveButton(getString(R.string.start_new_game_dialog_positive_button)) { _, _ ->
                viewModel.resetGame()
            }
            .setNegativeButton(getString(R.string.start_new_game_dialog_negative_button), null)
            .show()
    }

    private fun showAnimationSpeedDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.animation_speed_dialog_title))
            .setPositiveButton(getString(R.string.animation_speed_dialog_positive_button)) { dialog: DialogInterface, _: Int ->
                val checkedItemPosition = (dialog as AlertDialog).listView.checkedItemPosition
                if (checkedItemPosition != AdapterView.INVALID_POSITION) {
                    viewModel.setSpeed(checkedItemPosition)
                    saveSpeed(checkedItemPosition)
                }
            }
            .setNegativeButton(getString(R.string.animation_speed_dialog_negative_button), null)
            .setSingleChoiceItems(R.array.animation_speed_options, viewModel.animationSpeed.value?.value ?: 0, null)
            .show()
    }

    private fun saveSpeed(checkedItemPosition: Int) {
        val sharedPreferences = this.getSharedPreferences(PREF_SPEED, Context.MODE_PRIVATE)
        val editor= sharedPreferences?.edit()
        editor?.putInt(PREF_SPEED, checkedItemPosition)?.apply()
    }

    private fun loadSpeed() {
        val sharedPreferences = this.getSharedPreferences(PREF_SPEED, Context.MODE_PRIVATE)
        val savedSpeed = sharedPreferences?.getInt(PREF_SPEED, AnimationSpeed.MEDIUM.value)
        savedSpeed?.let {
            viewModel.setSpeed(it)
        }
    }

    companion object {
        private const val PREF_SPEED = "pref_speed"
    }
}