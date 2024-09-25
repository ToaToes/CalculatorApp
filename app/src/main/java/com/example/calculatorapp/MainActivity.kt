package com.example.calculatorapp

import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.pow
import kotlin.math.sqrt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CalculatorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = backgroundColor
                ) {
                    Calculator()
                }
            }
        }
    }
}

private val backgroundColor = Color(0xFFE0E5EC)
private val shadowColorDark = Color(0xFFA3B1C6)
private val shadowColorLight = Color(0xFFFFFFFF)

@Composable
fun Calculator() {
    var input by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Display(input, result, Modifier.weight(1f))
        ButtonGrid(
            onInputChange = { input = it },
            onResultChange = { result = it }
        )
    }
}

@Composable
fun Display(input: String, result: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .neumorphicSurface(RoundedCornerShape(28.dp))
            .padding(24.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = input,
                fontSize = 32.sp,
                fontWeight = FontWeight.Normal,
                color = Color.DarkGray,
                textAlign = TextAlign.End,
                maxLines = 1
            )
            if (result.isNotEmpty()) {
                Text(
                    text = result,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.End,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun ButtonGrid(
    onInputChange: (String) -> Unit,
    onResultChange: (String) -> Unit
) {
    var currentInput by remember { mutableStateOf("") }

    val buttons = listOf(
        "%", "^", "√", "C",
        "7", "8", "9", "÷",
        "4", "5", "6", "×",
        "1", "2", "3", "-",
        "0", ".", "=", "+"
    )

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        buttons.chunked(4).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                row.forEach { symbol ->
                    CalculatorButton(
                        symbol = symbol,
                        onClick = {
                            when (symbol) {
                                "C" -> {
                                    currentInput = ""
                                    onInputChange("")
                                    onResultChange("")
                                }
                                "=" -> {
                                    try {
                                        val result = evaluateExpression(currentInput)
                                        onResultChange(result)
                                        currentInput = result
                                        onInputChange(currentInput)
                                    } catch (e: Exception) {
                                        onResultChange("Error")
                                    }
                                }
                                "%" -> {
                                    if (currentInput.isNotEmpty()) {
                                        val value = currentInput.toDoubleOrNull()
                                        if (value != null) {
                                            currentInput = (value / 100).toString()
                                            onInputChange(currentInput)
                                        }
                                    }
                                }
                                "√" -> {
                                    if (currentInput.isNotEmpty()) {
                                        val value = currentInput.toDoubleOrNull()
                                        if (value != null) {
                                            currentInput = sqrt(value).toString()
                                            onInputChange(currentInput)
                                        }
                                    }
                                }
                                else -> {
                                    currentInput += symbol
                                    onInputChange(currentInput)
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CalculatorButton(symbol: String, onClick: () -> Unit) {
    //val au: MediaPlayer = MediaPlayer.create(context, R.raw.clicksound)
    val textColor = when (symbol) {
        "C" -> Color.Red
        in listOf("%", "^", "√", "÷", "×", "-", "+") -> Color.Blue
        "=" -> Color(0xFF0080FF)
        else -> Color.DarkGray
    }

    Box(
        modifier = Modifier
            .size(if (symbol == "=") 80.dp else 70.dp)
            .neumorphicSurface(CircleShape)
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        TextButton(
            onClick = onClick,
            modifier = Modifier.fillMaxSize(),
            colors = ButtonDefaults.textButtonColors(
                contentColor = textColor
            )
        ) {
            Text(
                text = symbol,
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

fun Modifier.neumorphicSurface(shape: RoundedCornerShape): Modifier = this
    .shadow(
        elevation = 6.dp,
        shape = shape,
        clip = false,
        ambientColor = shadowColorDark,
        spotColor = shadowColorDark
    )
    .shadow(
        elevation = 6.dp,
        shape = shape,
        clip = false,
        ambientColor = shadowColorLight,
        spotColor = shadowColorLight
    )
    .clip(shape)
    .background(backgroundColor)

@Composable
fun CalculatorTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            background = backgroundColor
        ),
        content = content
    )
}

fun evaluateExpression(expression: String): String {
    val tokens = tokenize(expression)
    val postfix = infixToPostfix(tokens)
    return evaluatePostfix(postfix).toString()
}

fun tokenize(expression: String): List<String> {
    return expression.replace("(?<=[-+×÷^√()])|(?=[-+×÷^√()])".toRegex(), " ")
        .trim()
        .split("\\s+".toRegex())
}

fun infixToPostfix(tokens: List<String>): List<String> {
    val output = mutableListOf<String>()
    val operators = ArrayDeque<String>()

    for (token in tokens) {
        when {
            token.toDoubleOrNull() != null -> output.add(token)
            token == "(" -> operators.addLast(token)
            token == ")" -> {
                while (operators.isNotEmpty() && operators.last() != "(") {
                    output.add(operators.removeLast())
                }
                if (operators.isNotEmpty() && operators.last() == "(") {
                    operators.removeLast()
                }
            }
            token in setOf("+", "-", "×", "÷", "^", "√") -> {
                while (operators.isNotEmpty() && precedence(operators.last()) >= precedence(token)) {
                    output.add(operators.removeLast())
                }
                operators.addLast(token)
            }
        }
    }

    while (operators.isNotEmpty()) {
        output.add(operators.removeLast())
    }

    return output
}

fun precedence(operator: String): Int = when (operator) {
    "+", "-" -> 1
    "×", "÷" -> 2
    "^", "√" -> 3
    else -> 0
}

fun evaluatePostfix(tokens: List<String>): Double {
    val stack = ArrayDeque<Double>()

    for (token in tokens) {
        when {
            token.toDoubleOrNull() != null -> stack.addLast(token.toDouble())
            token in setOf("+", "-", "×", "÷", "^", "√") -> {
                val b = stack.removeLast()
                val a = if (token != "√") stack.removeLast() else 0.0
                val result = when (token) {
                    "+" -> a + b
                    "-" -> a - b
                    "×" -> a * b
                    "÷" -> a / b
                    "^" -> a.pow(b)
                    "√" -> sqrt(b)
                    else -> throw IllegalArgumentException("Unknown operator: $token")
                }
                stack.addLast(result)
            }
        }
    }

    return stack.last()
}

@Preview(showBackground = true)
@Composable
fun CalculatorPreview() {
    CalculatorTheme {
        Calculator()
    }
}
