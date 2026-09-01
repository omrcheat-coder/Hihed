package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.recorder.RecorderViewModel
import com.example.ui.StealthRecorderScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val recorderViewModel: RecorderViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                StealthRecorderScreen(viewModel = recorderViewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        recorderViewModel.rebindCamera()
        recorderViewModel.loadGalleryVideos()
    }
}
