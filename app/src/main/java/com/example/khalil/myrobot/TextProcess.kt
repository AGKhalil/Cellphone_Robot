package com.example.khalil.myrobot

import android.media.AudioManager
import android.media.MediaPlayer
import android.util.Log
import io.github.firemaples.detect.Detect
import io.github.firemaples.language.Language
import io.github.firemaples.language.SpokenDialect
import io.github.firemaples.sentence.BreakSentences
import io.github.firemaples.speak.Speak
import io.github.firemaples.translate.Translate


/**
 * Created by Michael on 2017/10/20.
 */


class TextProcess(var key:String) {
    val TAG = "TextProcess"
    init {
        Translate.setSubscriptionKey(key)
        Detect.setSubscriptionKey(key)
        Speak.setSubscriptionKey(key)
        BreakSentences.setSubscriptionKey(key)
//        Log.d(TAG,key)
    }

    fun translate(message: String, from: Language, to: Language):String {
        // Set your Azure Portal Subscription Key - See https://www.microsoft.com/cognitive-services/en-us/translator-api/documentation/TranslatorInfo/overview
        val translatedText = Translate.execute(message, from, to)
        Log.d(TAG,translatedText)
        return message
    }

    fun detect_language(message: String):Language{
        val lang = Detect.execute(message)
        Log.d(TAG, lang.name)
        return lang
    }

    // Return an array consisting of character lengths of each sentence
    fun break_sentence(message: String, from: Language):Array<Int>{
        val res = BreakSentences.execute(message,from)
        return res
    }

    fun speak(message: String, language: SpokenDialect){
        val url = Speak.execute(message,language)
        try {
            val player = MediaPlayer()
            player.setAudioStreamType(AudioManager.STREAM_MUSIC)
            player.setDataSource(url)
            player.prepare()
            player.start()

        } catch (e: Exception) {
            Log.d(TAG, "Fail to play wav file")
        }

    }

}
