package com.example.ui.stealth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RecordingStatus
import com.example.ui.theme.StealthCyan
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StealthClockOverlay(
    status: RecordingStatus,
    batteryLevel: Int,
    onExitStealth: () -> Unit
) {
    var currentTime by remember { mutableStateOf("") }
    var currentDate by remember { mutableStateOf("") }
    var showExitButton by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            val now = Date()
            currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(now)
            currentDate = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(now)
            delay(500)
        }
    }

    LaunchedEffect(showExitButton) {
        if (showExitButton) {
            delay(4000)
            showExitButton = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF07080B))
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { onExitStealth() },
                    onTap = { showExitButton = true }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = currentTime,
                fontSize = 58.sp,
                fontWeight = FontWeight.Light,
                color = Color(0xFF64B5F6),
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = currentDate,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF90A4AE),
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Color(0xFF4CAF50), CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Battery $batteryLevel% • Connected",
                    fontSize = 13.sp,
                    color = Color(0xFF78909C)
                )
            }
        }

        AnimatedVisibility(
            visible = showExitButton,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
        ) {
            Button(
                onClick = onExitStealth,
                colors = ButtonDefaults.buttonColors(containerColor = StealthCyan),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LockOpen,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Exit Desk Clock", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}
