package com.melikyldrm.hesap.ui.screens.basic

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.melikyldrm.hesap.speech.SpeechState
import com.melikyldrm.hesap.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BasicCalculatorScreen(
    onHistoryClick: () -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: BasicCalculatorViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val speechState by viewModel.speechState.collectAsStateWithLifecycle()

    // Debug: State değişikliklerini logla
    LaunchedEffect(state.result) {
        android.util.Log.d("BasicCalcScreen", "State result changed to: ${state.result}, expression: ${state.expression}")
    }

    val context = LocalContext.current

    // Permission check'i lazy olarak yap - sadece kullanıldığında kontrol et
    var hasAudioPermission by remember { mutableStateOf(false) }

    // İlk composition'dan sonra permission kontrolü yap
    LaunchedEffect(Unit) {
        hasAudioPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasAudioPermission = isGranted
        if (isGranted) {
            viewModel.startListening()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hesap Makinesi") },
                actions = {
                    // Mikrofon butonu TopAppBar'da
                    SmallMicrophoneButton(
                        speechState = speechState,
                        onClick = {
                            if (hasAudioPermission) {
                                if (speechState is SpeechState.Listening) {
                                    viewModel.stopListening()
                                } else {
                                    viewModel.startListening()
                                }
                            } else {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        }
                    )
                    IconButton(onClick = onHistoryClick) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Geçmiş"
                        )
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Ayarlar"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                windowInsets = WindowInsets(0.dp)
            )
        }
    ) { paddingValues ->
        // Speech success durumunda otomatik olarak 2 saniye sonra feedback'i kapat
        LaunchedEffect(speechState) {
            if (speechState is SpeechState.Success) {
                kotlinx.coroutines.delay(2000)
                viewModel.resetSpeechState()
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Speech feedback - basit if ile göster (animasyon yok)
            if (speechState !is SpeechState.Idle) {
                SpeechFeedbackCard(
                    speechState = speechState,
                    modifier = Modifier.padding(vertical = 8.dp),
                    onDismiss = { viewModel.resetSpeechState() }
                )
            }

            // Display - key ile zorla güncelleme
            key(state.result, state.expression) {
                CalculatorDisplay(
                    expression = state.expression,
                    result = state.result,
                    previousExpression = state.previousExpression,
                    isError = state.isError,
                    modifier = Modifier.weight(0.35f)
                )
            }

            // Button pad
            BasicButtonPad(
                onNumberClick = viewModel::onNumberClick,
                onOperatorClick = viewModel::onOperatorClick,
                onDecimalClick = viewModel::onDecimalClick,
                onEqualsClick = viewModel::onEqualsClick,
                onClearClick = viewModel::onClearClick,
                onDeleteClick = viewModel::onDeleteClick,
                onPercentClick = viewModel::onPercentClick,
                modifier = Modifier
                    .weight(0.65f)
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
fun BasicButtonPad(
    onNumberClick: (String) -> Unit,
    onOperatorClick: (String) -> Unit,
    onDecimalClick: () -> Unit,
    onEqualsClick: () -> Unit,
    onClearClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onPercentClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Row 1: C, ⌫, %, ÷
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            CalculatorButton(
                text = "C",
                onClick = onClearClick,
                buttonType = ButtonType.CLEAR,
                modifier = Modifier.weight(1f)
            )
            IconCalculatorButton(
                icon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Backspace,
                        contentDescription = "Sil",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                onClick = onDeleteClick,
                buttonType = ButtonType.FUNCTION,
                modifier = Modifier.weight(1f)
            )
            CalculatorButton(
                text = "%",
                onClick = onPercentClick,
                buttonType = ButtonType.FUNCTION,
                modifier = Modifier.weight(1f)
            )
            CalculatorButton(
                text = "÷",
                onClick = { onOperatorClick("÷") },
                buttonType = ButtonType.OPERATOR,
                modifier = Modifier.weight(1f)
            )
        }

        // Row 2: 7, 8, 9, ×
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            CalculatorButton(
                text = "7",
                onClick = { onNumberClick("7") },
                modifier = Modifier.weight(1f)
            )
            CalculatorButton(
                text = "8",
                onClick = { onNumberClick("8") },
                modifier = Modifier.weight(1f)
            )
            CalculatorButton(
                text = "9",
                onClick = { onNumberClick("9") },
                modifier = Modifier.weight(1f)
            )
            CalculatorButton(
                text = "×",
                onClick = { onOperatorClick("×") },
                buttonType = ButtonType.OPERATOR,
                modifier = Modifier.weight(1f)
            )
        }

        // Row 3: 4, 5, 6, -
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            CalculatorButton(
                text = "4",
                onClick = { onNumberClick("4") },
                modifier = Modifier.weight(1f)
            )
            CalculatorButton(
                text = "5",
                onClick = { onNumberClick("5") },
                modifier = Modifier.weight(1f)
            )
            CalculatorButton(
                text = "6",
                onClick = { onNumberClick("6") },
                modifier = Modifier.weight(1f)
            )
            CalculatorButton(
                text = "−",
                onClick = { onOperatorClick("-") },
                buttonType = ButtonType.OPERATOR,
                modifier = Modifier.weight(1f)
            )
        }

        // Row 4: 1, 2, 3, +
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            CalculatorButton(
                text = "1",
                onClick = { onNumberClick("1") },
                modifier = Modifier.weight(1f)
            )
            CalculatorButton(
                text = "2",
                onClick = { onNumberClick("2") },
                modifier = Modifier.weight(1f)
            )
            CalculatorButton(
                text = "3",
                onClick = { onNumberClick("3") },
                modifier = Modifier.weight(1f)
            )
            CalculatorButton(
                text = "+",
                onClick = { onOperatorClick("+") },
                buttonType = ButtonType.OPERATOR,
                modifier = Modifier.weight(1f)
            )
        }

        // Row 5: 00, 0, ., =
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            CalculatorButton(
                text = "00",
                onClick = { onNumberClick("00") },
                modifier = Modifier.weight(1f)
            )
            CalculatorButton(
                text = "0",
                onClick = { onNumberClick("0") },
                modifier = Modifier.weight(1f)
            )
            CalculatorButton(
                text = ".",
                onClick = onDecimalClick,
                modifier = Modifier.weight(1f)
            )
            CalculatorButton(
                text = "=",
                onClick = onEqualsClick,
                buttonType = ButtonType.EQUALS,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

