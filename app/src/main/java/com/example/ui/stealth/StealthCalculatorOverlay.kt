package com.example.ui.stealth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RecordingStatus
import com.example.ui.theme.StealthCyan
import com.example.ui.theme.StealthRed

@Composable
fun StealthCalculatorOverlay(
    status: RecordingStatus,
    onExitStealth: () -> Unit
) {
    var display by remember { mutableStateOf("0") }
    var operand1 by remember { mutableDoubleStateOf(0.0) }
    var operator by remember { mutableStateOf<String?>(null) }
    var isNewNumber by remember { mutableStateOf(true) }

    fun onDigit(d: String) {
        if (isNewNumber || display == "0") {
            display = d
            isNewNumber = false
        } else {
            if (display.length < 12) {
                display += d
            }
        }
    }

    fun onOp(op: String) {
        operand1 = display.toDoubleOrNull() ?: 0.0
        operator = op
        isNewNumber = true
    }

    fun onEqual() {
        // Secret code to exit: if formula is 5555= or 0000=
        if (display == "5555" || display == "0000") {
            onExitStealth()
            return
        }

        val op = operator ?: return
        val operand2 = display.toDoubleOrNull() ?: 0.0
        val result = when (op) {
            "+" -> operand1 + operand2
            "-" -> operand1 - operand2
            "×" -> operand1 * operand2
            "÷" -> if (operand2 != 0.0) operand1 / operand2 else 0.0
            else -> operand2
        }

        val resultStr = if (result % 1.0 == 0.0) {
            result.toLong().toString()
        } else {
            String.format("%.4f", result).trimEnd('0').trimEnd('.')
        }
        display = resultStr
        operator = null
        isNewNumber = true
    }

    fun onClear() {
        display = "0"
        operand1 = 0.0
        operator = null
        isNewNumber = true
    }

    fun onToggleSign() {
        val num = display.toDoubleOrNull() ?: 0.0
        val inverted = -num
        display = if (inverted % 1.0 == 0.0) inverted.toLong().toString() else inverted.toString()
    }

    fun onPercent() {
        val num = display.toDoubleOrNull() ?: 0.0
        display = (num / 100.0).toString()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF101216))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Disguised App Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onExitStealth() }
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Calculate,
                    contentDescription = "Calculator",
                    tint = Color(0xFF8E95A5),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Calculator",
                    color = Color(0xFF8E95A5),
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
            }

            // Secret exit icon disguise
            IconButton(
                onClick = onExitStealth,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LockOpen,
                    contentDescription = "Exit Stealth",
                    tint = Color(0xFF4A5060),
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Calculator Display
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.End
        ) {
            if (operator != null) {
                Text(
                    text = "$operand1 $operator",
                    fontSize = 20.sp,
                    color = Color(0xFF8E95A5)
                )
            }
            Text(
                text = display,
                fontSize = if (display.length > 8) 38.sp else 54.sp,
                fontWeight = FontWeight.Light,
                color = Color.White,
                maxLines = 1,
                textAlign = TextAlign.End
            )
        }

        // Keypad Grid
        val buttonSpacing = 12.dp
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(buttonSpacing)
        ) {
            // Row 1: C, +/-, %, ÷
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(buttonSpacing)) {
                CalcButton(label = "C", bg = Color(0xFF2A2E39), textColor = StealthRed, modifier = Modifier.weight(1f)) { onClear() }
                CalcButton(label = "±", bg = Color(0xFF2A2E39), textColor = Color.White, modifier = Modifier.weight(1f)) { onToggleSign() }
                CalcButton(label = "%", bg = Color(0xFF2A2E39), textColor = Color.White, modifier = Modifier.weight(1f)) { onPercent() }
                CalcButton(label = "÷", bg = Color(0xFFFF9500), textColor = Color.White, modifier = Modifier.weight(1f)) { onOp("÷") }
            }

            // Row 2: 7, 8, 9, ×
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(buttonSpacing)) {
                CalcButton(label = "7", modifier = Modifier.weight(1f)) { onDigit("7") }
                CalcButton(label = "8", modifier = Modifier.weight(1f)) { onDigit("8") }
                CalcButton(label = "9", modifier = Modifier.weight(1f)) { onDigit("9") }
                CalcButton(label = "×", bg = Color(0xFFFF9500), textColor = Color.White, modifier = Modifier.weight(1f)) { onOp("×") }
            }

            // Row 3: 4, 5, 6, -
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(buttonSpacing)) {
                CalcButton(label = "4", modifier = Modifier.weight(1f)) { onDigit("4") }
                CalcButton(label = "5", modifier = Modifier.weight(1f)) { onDigit("5") }
                CalcButton(label = "6", modifier = Modifier.weight(1f)) { onDigit("6") }
                CalcButton(label = "-", bg = Color(0xFFFF9500), textColor = Color.White, modifier = Modifier.weight(1f)) { onOp("-") }
            }

            // Row 4: 1, 2, 3, +
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(buttonSpacing)) {
                CalcButton(label = "1", modifier = Modifier.weight(1f)) { onDigit("1") }
                CalcButton(label = "2", modifier = Modifier.weight(1f)) { onDigit("2") }
                CalcButton(label = "3", modifier = Modifier.weight(1f)) { onDigit("3") }
                CalcButton(label = "+", bg = Color(0xFFFF9500), textColor = Color.White, modifier = Modifier.weight(1f)) { onOp("+") }
            }

            // Row 5: 0, ., =
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(buttonSpacing)) {
                CalcButton(label = "0", modifier = Modifier.weight(2f)) { onDigit("0") }
                CalcButton(label = ".", modifier = Modifier.weight(1f)) { if (!display.contains(".")) onDigit(".") }
                CalcButton(label = "=", bg = Color(0xFFFF9500), textColor = Color.White, modifier = Modifier.weight(1f)) { onEqual() }
            }
        }

        // Discreet exit bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Tip: Enter '5555=' or tap unlock icon to return",
                color = Color(0xFF4A5060),
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun CalcButton(
    label: String,
    modifier: Modifier = Modifier,
    bg: Color = Color(0xFF1E222B),
    textColor: Color = Color.White,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .aspectRatio(if (label == "0") 2.1f else 1f)
            .clip(RoundedCornerShape(32.dp))
            .background(bg)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 24.sp,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}
