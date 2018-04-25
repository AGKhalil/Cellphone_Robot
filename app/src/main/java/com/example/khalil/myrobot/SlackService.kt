package com.example.khalil.myrobot

import allbegray.slack.SlackClientFactory
import allbegray.slack.exception.SlackResponseErrorException
import allbegray.slack.rtm.Event
import allbegray.slack.rtm.EventListener
import allbegray.slack.rtm.SlackRealTimeMessagingClient
import allbegray.slack.type.Channel
import allbegray.slack.type.File
import allbegray.slack.webapi.SlackWebApiClient
import android.app.Service
import android.content.Intent
import android.os.AsyncTask
import android.os.IBinder
import android.util.Log
import com.fasterxml.jackson.databind.JsonNode

/**
 * Created by Michael on 2017/10/25.
 */

class SlackService : Service() {
    /**
     * This script provides a service class which could receive messages and send messages on Slack
     * in the background.
     * @param slackToken:String is where you store your slack bot token. Check here to get your token.
     * @param mWebApiClient:SlackWebApiClient is Slack api client.
     * @param webSocketUrl is socket URL.
     * @param mRtmClient:SlackRealTimeMessagingClient is Slack messageing client.
     */
    var slackToken:String? = null
    var mWebApiClient:SlackWebApiClient? = null
    var webSocketUrl:String? = null
    var mRtmClient:SlackRealTimeMessagingClient? = null
    val TAG = "SlackService"
    var myIdentifier = Commands.MICKEY
    var usr = ""
    override fun onBind(intent: Intent?): IBinder {
        TODO("not implemented")
    }

    override fun onCreate() {
        /**
         * This function is where you initiate your slack messaging client and register your
         * listener in yout SlackRealTimeMessagingClient to all kinds of message types. You are
         * able to extract information received.
         */
        super.onCreate()
        Log.d(TAG, "oncreate")
        AsyncTask.execute {
            slackToken = resources.getString(R.string.slack_key) //=  resources.getString(R.string.slack_key)
            mWebApiClient = SlackClientFactory.createWebApiClient(slackToken)
            webSocketUrl = mWebApiClient!!.startRealTimeMessagingApi().findPath("url").asText()
            mRtmClient = SlackRealTimeMessagingClient(webSocketUrl)
            val TAG = "SlackService"
            mRtmClient!!.addListener(Event.HELLO, object : EventListener {

                override fun onMessage(message: JsonNode) {
                    val authentication = mWebApiClient!!.auth()
                    val mBotId = authentication.user_id
                    usr = authentication.user

                    System.out.println("User id: " + mBotId)
                    System.out.println("Team name: " + authentication.team)
                    System.out.println("User name: " + authentication.user)
                }
            })

            mRtmClient!!.addListener(Event.MESSAGE, object : EventListener {
                override fun onMessage(message: JsonNode) {
                    val channelId = message.findPath("channel").asText()
                    val userId = message.findPath("user").asText()
                    val text = message.findPath("text").asText()
                    val authentication = mWebApiClient!!.auth()
                    val mBotId = authentication.getUser_id()
                    if (userId != null && userId != mBotId) {
                        var channel: Channel?
                        try {
                            channel = mWebApiClient!!.getChannelInfo(channelId)
                        } catch (e: SlackResponseErrorException) {
                            channel = null
                        }

                        val user = mWebApiClient!!.getUserInfo(userId)
                        val userName = user.getName()

                        println("Channel id: " + channelId)
                        Log.d(TAG, "Channel ID: " + channelId)
                        Log.d(TAG, "User ID: " + userId)
                        println("Channel name: " + if (channel != null) "#" + channel!!.getName() else "DM")
                        println("User id: " + userId)
                        println("User name: " + userName)
                        println("Text: " + text)
                        val i = Intent(baseContext, NaturalLanguageProcessService::class.java)
                        i.putExtra("msg", text)
                        i.putExtra("myIdentifier", myIdentifier)
                        i.putExtra("contact",channelId)
                        Log.d(TAG,"myIdentifier："+ myIdentifier)
                        startService(i)
                    }
                }
            })
            mRtmClient!!.connect()
        }

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        /**
         * This function would be called to send messages to a certain channel when you do the
         * following in another service or activity.
         * @param msg is the String message you would like to send
         * @param channel is the channel ID where the message will be delivered
         */
        val message = intent!!.getStringExtra("msg")
        val channel = intent.getStringExtra("channelID")
        val filepath  = "/storage/self/primary"
        val filename  = "thePicture001.jpg"//"/storage/emulated/0/thePicture001.jpg"//= intent.getStringExtra("filepath")
        Log.d(TAG,filepath)
        if (message =="init") {return Service.START_STICKY}
        if (message != ""){
            Log.d(TAG,"Message Sending")
            AsyncTask.execute {mWebApiClient!!.meMessage(channel, message)}
            return Service.START_STICKY
        } else {
            Log.d(TAG,filepath+"!!")
            var testFile = java.io.File(filepath,filename)
            if (testFile != null && testFile.exists()) {
                Log.d(TAG,"Sending Image")
                val person = intent.getStringExtra("person")
                Log.d(TAG, "Person is " + person)
                AsyncTask.execute{val slackFile:allbegray.slack.type.File = mWebApiClient!!.uploadFile(testFile, "Here is " + person + "!!!", "I did it!", channel)}
            }
        }
        //AsyncTask.execute {mWebApiClient!!.meMessage(channel, usr+": "+message)}
        return Service.START_STICKY
    }


}