package com.melikyldrm.hesap.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "SpeechRecognition"

@Singleton
class SpeechRecognitionManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val commandParser: TurkishCommandParser
) {
    private var speechRecognizer: SpeechRecognizer? = null

    private val _speechState = MutableStateFlow<SpeechState>(SpeechState.Idle)
    val speechState: StateFlow<SpeechState> = _speechState.asStateFlow()

    private val _lastCommand = MutableStateFlow<SpeechCommand?>(null)
    val lastCommand: StateFlow<SpeechCommand?> = _lastCommand.asStateFlow()

    private val recognizerIntent: Intent by lazy {
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "tr-TR")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "tr-TR")
            putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, "tr-TR")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1500L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 1500L)
        }
    }

    /**
     * Speech recognition'ın kullanılabilir olup olmadığını kontrol eder
     */
    fun isAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }

    /**
     * Speech recognizer'ı başlatır
     */
    fun initialize() {
        if (!isAvailable()) {
            _speechState.value = SpeechState.NotAvailable
            return
        }

        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(createRecognitionListener())
            }
        }
    }

    /**
     * Dinlemeyi başlatır
     */
    fun startListening() {
        if (!isAvailable()) {
            _speechState.value = SpeechState.NotAvailable
            return
        }

        initialize()

        _speechState.value = SpeechState.Listening
        speechRecognizer?.startListening(recognizerIntent)
    }

    /**
     * Dinlemeyi durdurur
     */
    fun stopListening() {
        speechRecognizer?.stopListening()
        _speechState.value = SpeechState.Idle
    }

    /**
     * Speech recognizer'ı temizler
     */
    fun destroy() {
        speechRecognizer?.destroy()
        speechRecognizer = null
        _speechState.value = SpeechState.Idle
    }

    /**
     * Durumu sıfırlar
     */
    fun resetState() {
        _speechState.value = SpeechState.Idle
        _lastCommand.value = null
    }

    private fun createRecognitionListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                _speechState.value = SpeechState.Listening
            }

            override fun onBeginningOfSpeech() {
                _speechState.value = SpeechState.Listening
            }

            override fun onRmsChanged(rmsdB: Float) {
                // Ses seviyesi değişikliği - UI'da animasyon için kullanılabilir
            }

            override fun onBufferReceived(buffer: ByteArray?) {
                // Buffer alındı
            }

            override fun onEndOfSpeech() {
                _speechState.value = SpeechState.Processing
            }

            override fun onError(error: Int) {
                val errorMessage = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Ses kaydı hatası"
                    SpeechRecognizer.ERROR_CLIENT -> "İstemci hatası"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Mikrofon izni gerekli"
                    SpeechRecognizer.ERROR_NETWORK -> "Ağ hatası"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Ağ zaman aşımı"
                    SpeechRecognizer.ERROR_NO_MATCH -> "Ses tanınamadı"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Tanıyıcı meşgul"
                    SpeechRecognizer.ERROR_SERVER -> "Sunucu hatası"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Ses algılanamadı"
                    else -> "Bilinmeyen hata"
                }

                if (error == SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS) {
                    _speechState.value = SpeechState.PermissionRequired
                } else {
                    _speechState.value = SpeechState.Error(errorMessage)
                }
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val spokenText = matches?.firstOrNull() ?: ""

                // Debug logging
                Log.d(TAG, "=== SPEECH RESULTS ===")
                Log.d(TAG, "Raw results: $matches")
                Log.d(TAG, "Selected text: '$spokenText'")

                if (spokenText.isNotEmpty()) {
                    processSpokenText(spokenText)
                } else {
                    _speechState.value = SpeechState.Error("Ses tanınamadı")
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val partialText = matches?.firstOrNull() ?: ""

                if (partialText.isNotEmpty()) {
                    // Kısmi sonuçları gösterebiliriz
                    val parsedExpression = commandParser.parseExpression(partialText)
                    _speechState.value = SpeechState.Success(partialText, parsedExpression)
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {
                // Özel eventler
            }
        }
    }

    private fun processSpokenText(text: String) {
        Log.d(TAG, "=== PROCESSING ===")
        Log.d(TAG, "Input text: '$text'")

        // parseExpression sonucunu logla
        val expressionResult = commandParser.parseExpression(text)
        Log.d(TAG, "parseExpression result: '$expressionResult'")

        val command = commandParser.parseCommand(text)
        Log.d(TAG, "parseCommand result: $command")

        _lastCommand.value = command

        val parsedExpression = when (command) {
            is SpeechCommand.Calculate -> {
                Log.d(TAG, "Calculate command with expression: '${command.expression}'")
                command.expression
            }
            is SpeechCommand.Clear -> null
            is SpeechCommand.Delete -> null
            is SpeechCommand.Equals -> null
            is SpeechCommand.KdvCalculate -> "KDV: ${command.amount} @ %${command.rate}"
            is SpeechCommand.TevkifatCalculate -> "Tevkifat: ${command.amount}"
            is SpeechCommand.Convert -> "${command.value} ${command.fromUnit} → ${command.toUnit}"
            is SpeechCommand.Unknown -> {
                Log.w(TAG, "Unknown command for text: '$text'")
                null
            }
        }

        _speechState.value = SpeechState.Success(text, parsedExpression)
        Log.d(TAG, "Final state: Success(text='$text', parsed='$parsedExpression')")
    }
}

