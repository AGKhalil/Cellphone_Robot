package com.example.khalil.myrobot

/**
 * Created by Michael Wang on 05/28/17.
 * This class is for converting language code into.
 */


class LanguageConfig(val languageCode: String, val accessToken: String) {
    /**
     * This class stores the configuration of NLP service
     * @param languageCode
     * @param accessToken
     */
    override fun toString(): String {
        return languageCode
    }
}

