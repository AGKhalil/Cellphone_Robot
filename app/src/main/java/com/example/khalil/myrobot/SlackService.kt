package com.example.khalil.myrobot

import allbegray.slack.SlackClientFactory
import allbegray.slack.exception.SlackResponseErrorException
import allbegray.slack.rtm.Event
import allbegray.slack.rtm.EventListener
import allbegray.slack.rtm.SlackRealTimeMessagingClient
import allbegray.slack.type.Channel
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
    var slackToken:String? = null//=  resources.getString(R.string.slack_key)
    var mWebApiClient:SlackWebApiClient? = null
    var webSocketUrl:String? = null
    var mRtmClient:SlackRealTimeMessagingClient? = null
    val TAG = "SlackService"
    var myIdentifier = Commands.MICKEY

    override fun onBind(intent: Intent?): IBinder {
        TODO("not implemented")
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "oncreate")
        AsyncTask.execute {
            slackToken = "xoxb-261603682465-QyB6BLreiew6tGQESKIK4rux" //=  resources.getString(R.string.slack_key)
            mWebApiClient = SlackClientFactory.createWebApiClient(slackToken)
            webSocketUrl = mWebApiClient!!.startRealTimeMessagingApi().findPath("url").asText()
            mRtmClient = SlackRealTimeMessagingClient(webSocketUrl)
            val TAG = "SlackService"
            mRtmClient!!.addListener(Event.HELLO, object : EventListener {

                override fun onMessage(message: JsonNode) {
                    val authentication = mWebApiClient!!.auth()
                    val mBotId = authentication.user_id

                    System.out.println("User id: " + mBotId)
                    System.out.println("Team name: " + authentication.team)
                    System.out.println("User name: " + authentication.user)
                    mWebApiClient!!.meMessage("D7Q7NDWVB", authentication.user +"is back!")

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
                        println("Channel name: " + if (channel != null) "#" + channel!!.getName() else "DM")
                        println("User id: " + userId)
                        println("User name: " + userName)
                        println("Text: " + text)
                        val i = Intent(baseContext, NaturalLanguageProcessService::class.java)
                        i.putExtra("msg", text)
                        i.putExtra("myIdentifier", myIdentifier)
                        i.putExtra("phonenumber",channelId)
                        Log.d(TAG,"myIdentifier："+ myIdentifier)
                        startService(i)
                        // Copy cat
                        mWebApiClient!!.meMessage(channelId, userName + ": " + text)
                    }
                }
            })
            mRtmClient!!.connect()
        }

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val message = intent!!.getStringExtra("msg")
        val channel = intent!!.getStringExtra("channelID")
        if (message =="init") {return Service.START_STICKY}
        AsyncTask.execute {mWebApiClient!!.meMessage(channel, message)}
        return Service.START_STICKY
    }


}