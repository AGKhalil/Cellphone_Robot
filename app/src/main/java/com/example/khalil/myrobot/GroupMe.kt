package com.example.khalil.myrobot

import android.util.Log
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
/**
 * Created by Michael on 2017/10/21.
 */

class GroupMe {
    val TAG = "Groupme"
    private val REQUEST_URL = "https://api.groupme.com/v3/bots/post"

    fun sendTextMessage(message: String, botID: String) {
        var urlParameters:String = "" //""bot_id=$botID&text=$message&param3=c"
        val postData = JSONObject()
        postData.put("bot_id",botID)
        postData.put("text","hello")
        urlParameters = postData.toString()
        Log.d(TAG,urlParameters)
        try {
            val url = URL(REQUEST_URL)
            val connection = url.openConnection() as HttpURLConnection
            connection.doOutput = true
            connection.doInput = true
            connection.instanceFollowRedirects = false
            connection.requestMethod = "POST"
            connection.useCaches = false

            val out = BufferedOutputStream(connection.outputStream)
            val wr = BufferedWriter(OutputStreamWriter(out, "UTF-8"))
            wr.write(urlParameters)
            wr.flush()
            wr.close()
            connection.disconnect()

            val responseCode = connection.responseCode
            if (responseCode != 202)
                println(responseCode.toString() + " error has occured while sending the message: " + message)
        } catch (e: MalformedURLException) {
            println("Error occured while establishing a connection")
            e.printStackTrace()
        } catch (e: IOException) {
            println("Error occured while sending data")
            e.printStackTrace()
        }

    }

    fun sendImage(text: String, imageURL: String, botID: String) {
        try {
            val urlParameters = "{\"bot_id\":\"$botID\",\"text\":\"$text"+"\",\"attachments\":[{\"type\":\"image\",\"url\":\"" + imageURL + "\"}]}"
            val request = "https://api.groupme.com/v3/bots/post"
            val url = URL(request)
            val connection = url.openConnection() as HttpURLConnection
            connection.setDoOutput(true)
            connection.setDoInput(true)
            connection.setInstanceFollowRedirects(false)
            connection.setRequestMethod("POST")
            connection.setUseCaches(false)

            val wr = DataOutputStream(connection.getOutputStream())
            wr.writeBytes(urlParameters)
            wr.flush()
            wr.close()
            connection.disconnect()

            val responseCode = connection.getResponseCode()
            if (responseCode != 202)
                println(responseCode.toString() + " error has occured while sending the image: " + imageURL)
        } catch (e: MalformedURLException) {
            println("Error occured while establishing a connection")
            e.printStackTrace()
        } catch (e: IOException) {
            println("Error occured while sending data")
            e.printStackTrace()
        }

    }

    fun sendLocation(text: String, locationName: String, longitude: Double, latitude: Double, botID: String) {
        try {
            val urlParameters = "{\"bot_id\":\"$botID\",\"text\":\"$text\",\"attachments\":[{\"type\":\"location\",\"lng\":\""
            (+longitude).toString() + "\",\"lat\":\"" + latitude + "\",\"name\":\"" + locationName + "\"}]}"
            val request = "https://api.groupme.com/v3/bots/post"
            val url = URL(request)
            val connection = url.openConnection() as HttpURLConnection
            connection.setDoOutput(true)
            connection.setDoInput(true)
            connection.setInstanceFollowRedirects(false)
            connection.setRequestMethod("POST")
            connection.setUseCaches(false)

            val wr = DataOutputStream(connection.getOutputStream())
            wr.writeBytes(urlParameters)
            wr.flush()
            wr.close()
            connection.disconnect()

            val responseCode = connection.getResponseCode()
            if (responseCode != 202)
                println(responseCode.toString() + " error has occured while sending the location: " + latitude + " " + longitude)
        } catch (e: MalformedURLException) {
            println("Error occured while establishing a connection")
            e.printStackTrace()
        } catch (e: IOException) {
            println("Error occured while sending data")
            e.printStackTrace()
        }

    }
}