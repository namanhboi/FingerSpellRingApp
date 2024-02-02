package com.example.scifilabring.ui

data class ScifiLabRingState(
    val corpus : List<String> = listOf(),
    val stringForSerialization : String = "",
    val currentText : String = "",
    val input : String = "",
    val threeSuggestions : List<String> = listOf(),
    val previousWord: String = "",
    val previousSuggestion: String = "",
    )
