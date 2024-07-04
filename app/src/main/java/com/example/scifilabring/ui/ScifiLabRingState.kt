package com.example.scifilabring.ui

import com.example.scifilabring.SpeechSynthesis.Emotion

data class ScifiLabRingState(
    val corpus : List<String> = listOf(),
    val stringForSerialization : String = "",
    val currentText : String = "",
    val input : String = "",
    val threeWordSuggestions : List<String> = listOf(),
    val threeEmotionSuggestions: List<Emotion> = listOf(Emotion.Default ,Emotion.Cheerful, Emotion.Sad),
    val previousInputWord: String = "",
    val previousSuggestion: String = "",
    val wordToSpeak: String = "",
    val previousAddedEmotion: Emotion = Emotion.Default,
    )
