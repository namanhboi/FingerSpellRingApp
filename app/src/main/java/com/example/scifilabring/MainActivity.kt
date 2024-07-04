package com.example.scifilabring

import AutoCorrect
import GenerateJSONFiles
import android.content.Context
import android.graphics.Paint
import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.scifilabring.SpeechSynthesis.Emotion
import com.example.scifilabring.ui.ScifiLabRingViewModel
import com.example.scifilabring.ui.theme.ScifiLabRingTheme
import kotlinx.serialization.json.Json
import java.io.File
import java.util.Formatter
import java.util.Locale


class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*var corpuss = applicationContext.assets.open("google-10000-english.txt").bufferedReader().use {
            it.readLines();
        }
        val string = applicationContext.assets.open("data.json").bufferedReader().use {
            it.readText()
        }
        val data = Json.decodeFromString<GenerateJSONFiles>(string)
        val auto = AutoCorrect(data, corpuss)*/
        setContent {
            ScifiLabRingTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppLayout()
                }
            }
        }
    }
}

fun ExtractTopThreeWords(firstWord : String?, secondWord: String, auto: AutoCorrect) : List<Pair<String, Double>> {
    if (firstWord == null) {
        return auto.suggest_this_word(secondWord)
        //return listOf(Pair(secondWord, 0.0))
    } else {
        return auto.suggest_this_word_given_last(firstWord, secondWord)
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KeyboardInput(modifier : Modifier = Modifier, value : String, onValueChange : (String) -> Unit){
    TextField(
        singleLine =true,
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .padding(horizontal = 32.dp)
            .fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii)
    )
}
@Composable
fun PreviousInputAndSuggestion(modifier: Modifier = Modifier, previousInput: String, previousSuggestion : String, wordToSpeak: String) {
    Column (
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 32.dp, end = 32.dp)
            .background(Color.DarkGray),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.SpaceAround
    ) {
        Text (
            text = "Previous Input: ${previousInput}",
            fontSize = 16.sp,
            color = Color.White,
        )
        Text(
            //modifier = Modifier.fillMaxSize(),
            text = "Previous Suggestion : ${previousSuggestion}",
            fontSize = 16.sp,
            color = Color.White,
        )
        Text(
            //modifier = Modifier.fillMaxSize(),
            text = "Word to Speak : ${wordToSpeak}",
            fontSize = 16.sp,
            color = Color.White,
        )
    }

}

@Composable
fun InputFromTCPClient(modifier : Modifier = Modifier, value : String) {
    Row (
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 32.dp, end = 32.dp)
            .background(Color.Black),
        horizontalArrangement = Arrangement.Center
    ){
        Text (
            text = "Input: ",
            fontSize = 24.sp,
            color = Color.White,
        )
        Text(
            //modifier = Modifier.fillMaxSize(),
            text = value,
            fontSize = 24.sp,
            color = Color.White,
        )

    }
}


@Composable
fun AppLayout(modifier: Modifier = Modifier, scifiLabRingViewModel: ScifiLabRingViewModel = viewModel() ) {
    val scifiLabRingState by scifiLabRingViewModel.scifiLabRingState.collectAsState()
    /*
    val allWords : List<String> = scifiLabRingState.currentText.split(" ")
    var prevWord : String?

    if(scifiLabRingState.currentText.isBlank()) {
        prevWord = null
    } else {
        prevWord = allWords[allWords.size - 1]
    }
    val reg = Regex("[a-z]+")
    val normalizedInput = reg.find(input)?.value ?: ""

    var list = ExtractTopThreeWords(prevWord, normalizedInput, auto = auto)

    var firstSuggestion : String = if (list.size > 0) list[0].first else ""
    var secondSuggestion : String = if (list.size > 1) list[1].first else ""
    var thirdSuggestion : String = if (list.size > 2) list[2].first else ""(*/
    scifiLabRingViewModel.extractTopThreeSuggestions(
        scifiLabRingViewModel.lastWordInCurrentText(),
        scifiLabRingViewModel.normalizedInput()
    )
    Column (
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly){
        Text(
            text = "Autocorrect Demo",
            textAlign = TextAlign.Center,
            fontSize = 32.sp,
            modifier = Modifier.padding(16.dp),
        )
        CurrentText(currentText = scifiLabRingState.currentText)

//        KeyboardInput(
//            value = scifiLabRingState.input,
//            onValueChange = {newWord -> scifiLabRingViewModel.onKeyboardValueChange(newWord) }
//        )
        PreviousInputAndSuggestion(previousInput = scifiLabRingState.previousInputWord, previousSuggestion = scifiLabRingState.previousSuggestion, wordToSpeak = scifiLabRingState.wordToSpeak)
        InputFromTCPClient(
            value = scifiLabRingState.input
        )
        Text(
            text = "Autocorrect Suggestions",
            color = Color.White,
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .background(Color.DarkGray)
                .fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontSize = 24.sp

        )
        AutoCorrectWordSuggestion(
            value =
            if (scifiLabRingState.threeWordSuggestions.isNotEmpty())
                scifiLabRingState.threeWordSuggestions[0]
            else
                "",
            onClick = {
                scifiLabRingViewModel.onClickWordSuggestion(0)
            }
        )
        AutoCorrectWordSuggestion(
            value =
            if (scifiLabRingState.threeWordSuggestions.size > 1)
                scifiLabRingState.threeWordSuggestions[1]
            else
                "",
            onClick = {
                scifiLabRingViewModel.onClickWordSuggestion(1)
            }
        )
        AutoCorrectWordSuggestion(
            value =
            if (scifiLabRingState.threeWordSuggestions.size > 2)
                scifiLabRingState.threeWordSuggestions[2]
            else
                "",
            onClick = {
                scifiLabRingViewModel.onClickWordSuggestion(2)
            }
        )
        EmotionSuggestionList(
            emotionSuggestions = scifiLabRingState.threeEmotionSuggestions,
            viewModel = scifiLabRingViewModel
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            DeleteLastCharButton(
                modifier = Modifier,
                onClick = {
                    scifiLabRingViewModel.onClickDelete()
                }
            )
            ConfirmCurrentWord(
                modifier = Modifier,
                onClick = {
                    scifiLabRingViewModel.onClickAdd()
                }
            )
        }
    }

}


@Composable
fun DeleteLastCharButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    var color by remember { mutableStateOf(Color.DarkGray) }
    Button(
        modifier = modifier.onFocusChanged { if (it.isFocused) color = Color.Cyan else color = Color.DarkGray},
        shape = CircleShape,
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(color)
    ) {
        Icon(imageVector = Icons.Rounded.Delete, contentDescription = null)
    }
}

@Composable
fun CurrentText(modifier : Modifier = Modifier, currentText: String) {
    Box (
        modifier = Modifier
            .padding(horizontal = 32.dp)
            .background(Color.DarkGray)
            .fillMaxWidth()
            .size(120.dp)
            .verticalScroll(rememberScrollState())
    )
    {
        Text(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxSize(),
            text = currentText,
            color = Color.White,
            fontSize = 24.sp,
            textAlign = TextAlign.Justify
        )
    }
}

@Composable
fun AutoCorrectWordSuggestion(modifier : Modifier = Modifier, value: String, onClick: () -> Unit) {
    var color by remember {mutableStateOf(Color.DarkGray)}
    Button(
        modifier = Modifier
            .padding(horizontal = 64.dp)
            .fillMaxWidth()
            .onFocusChanged { if (it.isFocused) color = Color.Cyan else color = Color.DarkGray },
        onClick = onClick,
        shape = RoundedCornerShape(30),
        colors = ButtonDefaults.buttonColors(color)
    ) {
        Text(text = value, color = Color.White )
    }

}

@Composable
fun EmojiBasedOnEmotion(emotion: Emotion) : String {
    return stringResource(id = when (emotion) {
        Emotion.Default -> R.string.emoji_emotion_default
        Emotion.Sad -> R.string.emoji_emotion_sad
        else -> R.string.emoji_emotion_cheerful
    })
}

@Composable
fun EmotionSuggestionList(modifier : Modifier = Modifier, emotionSuggestions : List<Emotion>, viewModel: ScifiLabRingViewModel) {
    Row (modifier = modifier) {
        emotionSuggestions.forEachIndexed { index, emotion ->
            EmotionSuggestion(
                modifier = Modifier.padding(8.dp),
                emotion = emotion,
                text = EmojiBasedOnEmotion(emotion = emotion),
                onClick = { viewModel.onClickEmotionSuggestion(index)}
            )
        }
    }
}
@Composable
fun EmotionSuggestion(modifier: Modifier = Modifier, emotion: Emotion, text : String, onClick: () -> Unit) {
    var color by remember { mutableStateOf(Color.DarkGray) }
    Button (
        modifier = modifier.onFocusChanged { if (it.isFocused) color = Color.Cyan else color = Color.DarkGray },
        onClick =  onClick,
        colors = ButtonDefaults.buttonColors(color)
    ) {
        Text (text = text)
    }
}

@Composable
fun ConfirmCurrentWord(modifier: Modifier = Modifier, onClick: () -> Unit) {
    var color by remember { mutableStateOf( Color.DarkGray) }
    Button(
        modifier = modifier.onFocusChanged { if (it.isFocused) color = Color.Cyan else color = Color.DarkGray},
        shape = CircleShape,
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(color)
    ) {
        Icon(imageVector = Icons.Rounded.AddCircle, contentDescription = null)
    }
}

