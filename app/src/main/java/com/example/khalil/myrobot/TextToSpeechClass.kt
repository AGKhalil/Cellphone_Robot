package com.example.khalil.myrobot

import android.content.Context
import android.speech.tts.TextToSpeech


/**
 * Created by Michael Wang on 05/28/17.
 * This is for text to speech
 */

object TextToSpeechClass {

    private var textToSpeech: TextToSpeech? = null

    fun init(context: Context) {
        if (textToSpeech == null) {
            textToSpeech = TextToSpeech(context, TextToSpeech.OnInitListener { })
        }
    }

    fun speak(text: String) {
        textToSpeech!!.speak(text, TextToSpeech.QUEUE_FLUSH, null)
    }
}


