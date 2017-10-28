package com.example.khalil.myrobot

import allbegray.slack.SlackClientFactory
import allbegray.slack.exception.SlackResponseErrorException
import allbegray.slack.rtm.Event
import allbegray.slack.rtm.EventListener
import allbegray.slack.rtm.SlackRealTimeMessagingClient
import allbegray.slack.type.Channel
import com.fasterxml.jackson.databind.JsonNode

/**
 * Created by Michael on 2017/10/25.
 */
class Slack(var slackToken:String){
    val mWebApiClient = SlackClientFactory.createWebApiClient(slackToken)
    val webSocketUrl = mWebApiClient.startRealTimeMessagingApi().findPath("url").asText()
    val mRtmClient = SlackRealTimeMessagingClient(webSocketUrl)

    init {

        //val slackToken = "xoxb-261603682465-QyB6BLreiew6tGQESKIK4rux"
        mRtmClient.addListener(Event.HELLO, object : EventListener {

            override fun onMessage(message: JsonNode) {
                val authentication = mWebApiClient.auth()
                val mBotId = authentication.user_id

                System.out.println("User id: " + mBotId)
                System.out.println("Team name: " + authentication.team)
                System.out.println("User name: " + authentication.user)
                mWebApiClient.meMessage("D7Q7NDWVB", authentication.user +"is back!")
            }
        })

        mRtmClient.addListener(Event.MESSAGE, object : EventListener {
            override fun onMessage(message: JsonNode) {
                val channelId = message.findPath("channel").asText()
                val userId = message.findPath("user").asText()
                val text = message.findPath("text").asText()
                val authentication = mWebApiClient.auth()
                val mBotId = authentication.getUser_id()

                if (userId != null && userId != mBotId) {
                    var channel: Channel?
                    try {
                        channel = mWebApiClient.getChannelInfo(channelId)
                    } catch (e: SlackResponseErrorException) {
                        channel = null
                    }

                    val user = mWebApiClient.getUserInfo(userId)
                    val userName = user.getName()

                    println("Channel id: " + channelId)
                    println("Channel name: " + if (channel != null) "#" + channel!!.getName() else "DM")
                    println("User id: " + userId)
                    println("User name: " + userName)
                    println("Text: " + text)

                    // Copy cat
                    mWebApiClient.meMessage(channelId, userName + ": " + text)
                }
            }
        })
        mRtmClient.connect()

    }

    fun sendMessage(channel: String, messge: String){
        mWebApiClient.meMessage(channel, messge)
    }

    fun connect(){
        mRtmClient.connect()
    }

}