package com.example.ui.camera

import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import com.example.recorder.RecorderViewModel

@Composable
fun CameraPreviewView(
    viewModel: RecorderViewModel,
    modifier: Modifier = Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    AndroidView(
        factory = { ctx ->
            PreviewView(ctx).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                viewModel.attachCameraPreview(lifecycleOwner, this)
            }
        },
        update = { previewView ->
            viewModel.attachCameraPreview(lifecycleOwner, previewView)
        },
        modifier = modifier.fillMaxSize()
    )
}
