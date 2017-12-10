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
    /**
     * This class provides tools to translate sentences, translate language, break sentence,
     * and convert text to speech via Azure service.
     * @param key: Azure service key
     */
    val TAG = "TextProcess"
    init {
        Translate.setSubscriptionKey(key)
        Detect.setSubscriptionKey(key)
        Speak.setSubscriptionKey(key)
        BreakSentences.setSubscriptionKey(key)
    }

    fun translate(message: String, from: Language, to: Language):String {
        /**
         * This function translate message from a language to another language.
         * Set your Azure Portal Subscription Key - See
         * https://www.microsoft.com/cognitive-services/en-us/translator-api/documentation/
         * TranslatorInfo/overview
         * @param message(String): String to translate.
         * @param from(Language): input language type (could be found in public enum Language{...})
         * @param to(Language): output language type
         * return value(String): Translated message.
         */
        val translatedText = Translate.execute(message, from, to)
        Log.d(TAG,translatedText)
        return message
    }

    fun detect_language(message: String):Language{
        /**
         * This function detects the language of the input message
         * @param message(String): String to detect
         * return value(Language): language of the message
         */
        val lang = Detect.execute(message)
        Log.d(TAG, lang.name)
        return lang
    }

    fun break_sentence(message: String, from: Language):Array<Int>{
        /**
         * This function parse the sentence by providing an array of breaking positions.
         * @param message(String): String to parse
         * @param from(Language): input language type (could be found in public enum Language{...})
         * return value: an array of breaking positions
         */
        val res = BreakSentences.execute(message,from)
        return res
    }

    fun speak(message: String, language: SpokenDialect){
        /**
         * This function uploads the message to server and plays the audio downloaded from the
         * provided URL.
         * @param message(String): String to parse
         * @param language(SpokenDialect): spoken dialect type (could be found in public enum
         * SpokenDialect {...})
         */
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
