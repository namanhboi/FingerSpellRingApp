package com.example.scifilabring.ui

import AutoCorrect
import GenerateJSONFiles
import android.app.Application
import android.os.Build
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scifilabring.R
import com.example.scifilabring.SpeechSynthesis.Emotion
import com.example.scifilabring.SpeechSynthesis.SpeechSynthesis
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.ServerSocket
import java.net.Socket
import java.util.Locale
import kotlin.coroutines.cancellation.CancellationException

class ScifiLabRingViewModel(application: Application) : AndroidViewModel(application) {
    private val _scifiLabRingState: MutableStateFlow<ScifiLabRingState> =
        MutableStateFlow(ScifiLabRingState())
    val scifiLabRingState = _scifiLabRingState.asStateFlow()
//    private val textToSpeechEngine: TextToSpeech by lazy {
//        TextToSpeech(application,
//            TextToSpeech.OnInitListener { status ->
//                // set our locale only if init was success.
//                if (status == TextToSpeech.SUCCESS) {
//                    textToSpeechEngine.language = Locale.UK
//                }
//            })
//    }
    // Now using Azure tts
    init {
        loadData(application)
        getLatestWord()
        //textToSpeechEngine.speak("Have to put 1 input in for tts to work", TextToSpeech.QUEUE_FLUSH, null, "tts1")

    }

    val auto: AutoCorrect =
        AutoCorrect(
            Json.decodeFromString<GenerateJSONFiles>
                (_scifiLabRingState.value.stringForSerialization),
            _scifiLabRingState.value.corpus
        )

    private fun loadData(application: Application) {
        runBlocking {
            _scifiLabRingState.update { currentState ->
                currentState.copy(
                    corpus = loadDataForBKTree(application).await(),
                    stringForSerialization = loadDataForModel(application).await()
                )
            }

        }
    }

    private fun loadDataForBKTree(application: Application): Deferred<List<String>> {
        return viewModelScope.async(Dispatchers.IO) {
            application.assets.open("google-10000-english.txt").bufferedReader().use {
                it.readLines();
            }
        }
    }

    private fun loadDataForModel(application: Application): Deferred<String> {
        return viewModelScope.async(Dispatchers.IO) {
            application.assets.open("data.json").bufferedReader().use {
                it.readText()
            }
        }
    }
    // may need to launch its own co routine
     fun extractTopThreeSuggestions(
        firstWord: String?,
        secondWord: String,
        ) {
        val threeSuggestions : List<String> =
            if (firstWord == null) {
                auto.suggest_this_word(secondWord).map {
                    it.first
                }
            } else {
                auto.suggest_this_word_given_last(firstWord, secondWord).map {
                    it.first
                }

            }

        _scifiLabRingState.update { currentState ->
            currentState.copy(
                threeWordSuggestions = threeSuggestions
            )
        }
    }

    fun onClickWordSuggestion(index: Int) {
        if (index >= _scifiLabRingState.value.threeWordSuggestions.size) return
        val suggestion : String = _scifiLabRingState.value.threeWordSuggestions[index]
        //textToSpeechEngine.speak(suggestion, TextToSpeech.QUEUE_FLUSH, null, "tts1")
        _scifiLabRingState.update { currentState ->
            currentState.copy(
                currentText = "${_scifiLabRingState.value.currentText} $suggestion",
                previousInputWord = _scifiLabRingState.value.input,
                previousSuggestion = suggestion,
                wordToSpeak = suggestion,
                input = ""
            )
        }
        //textToSpeechEngine.speak(_scifiLabRingState.value.previousSuggestion, TextToSpeech.QUEUE_FLUSH, null, "tts1")
    }

    /** How does sending an emotion work?
     * Send the current word along with the selected emotion.
     * This is where the AzureTTS is invoked, sends the previousSuggestion and
     * threeEmotionSuggestion[index] to api*/
    fun onClickEmotionSuggestion(index : Int) {
        if (index >= _scifiLabRingState.value.threeEmotionSuggestions.size || index < 0) return
        if (_scifiLabRingState.value.wordToSpeak == "") {
            return
        }
        val emotionSuggestion : Emotion = _scifiLabRingState.value.threeEmotionSuggestions[index]
        _scifiLabRingState.update { currentState ->
            currentState.copy(
                previousAddedEmotion = emotionSuggestion
            )
        }
        AzureTTS(_scifiLabRingState.value.wordToSpeak, _scifiLabRingState.value.previousAddedEmotion)
        _scifiLabRingState.update { currentState ->
            currentState.copy(
                wordToSpeak = ""
            )
        }
    }


    fun onClickDelete() {
        if (_scifiLabRingState.value.input.length > 0) {
            _scifiLabRingState.update { currentState ->
                currentState.copy(
                    input = _scifiLabRingState.value.input.substring(0, _scifiLabRingState.value.input.length - 1)
                )
            }
        }
    }

    fun onClickAdd() {
        if (_scifiLabRingState.value.input.length > 0) {
            _scifiLabRingState.update { currentState ->
                currentState.copy(
                    currentText = "${_scifiLabRingState.value.currentText} ${_scifiLabRingState.value.input}",
                    previousInputWord = _scifiLabRingState.value.input,
                    previousSuggestion = "",
                    wordToSpeak = _scifiLabRingState.value.input,
                    input = ""
                )
            }
            // Call Lollipop+ function
            //textToSpeechEngine.speak(_scifiLabRingState.value.previousWord, TextToSpeech.QUEUE_FLUSH, null, "tts1")
            //AzureTTS(_scifiLabRingState.value.previousWord, Emotion.Cheerful)
        }
    }
    fun onKeyboardValueChange(newInput : String) {
        _scifiLabRingState.update { currentState ->
            currentState.copy(
                input = newInput
            )
        }
    }

    fun lastWordInCurrentText() : String? {
        if (_scifiLabRingState.value.currentText == "") return null
        return _scifiLabRingState.value.currentText.split(" ").last()
    }

    fun normalizedInput() : String {
        val reg = Regex("[a-z]+")
        return reg.find(_scifiLabRingState.value.input)?.value ?: ""
    }

    private fun getLatestWord(){
        viewModelScope.launch(Dispatchers.IO) {
            var server : ServerSocket = ServerSocket()
            var socket: Socket = Socket()
            try {
                server = ServerSocket(5657)

                //server.reuseAddress = true
                //server.bind(InetSocketAddress(5657))
                while (true) {
                    socket = server.accept()
                    val text = BufferedReader(InputStreamReader(socket.inputStream)).readLine()
                    _scifiLabRingState.update { currentState ->
                        currentState.copy(
                            input = text
                        )
                    }
                }
            } catch (e : CancellationException) {
                Log.e("ViewModel", e.toString())
                server.close()
                socket.close()
                throw e
            }
        }
    }
    fun AzureTTS(text : String, emotion : Emotion) {
        SpeechSynthesis.textToSpeech(text, emotion)
    }
}