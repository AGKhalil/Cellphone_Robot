package com.example.khalil.myrobot

import ai.api.AIServiceException
import ai.api.RequestExtras
import ai.api.android.AIConfiguration
import ai.api.android.AIDataService
import ai.api.model.*
import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.os.AsyncTask
import android.os.IBinder
import android.support.v4.content.LocalBroadcastManager
import android.text.TextUtils
import android.util.Log
import com.google.gson.JsonElement


@Suppress("UNREACHABLE_CODE")
/**
 * Created by Michael Wang on 05/28/17.
 * The following only deals with the self-defined Dialogflow service using Kotlin.
 * It reads the message and contact info from social media and broadcast intents, action parameters,
 * and contact info to Centralhub.
 */

class NaturalLanguageProcessService : Service() {

    var action = ""
    var speech = ""
    var socialmedia = ""
    var contact = ""
    var message = ""
    var target = ""
    var room = ""
    private var parameters: HashMap<String, JsonElement>? = null
    private var aiDataService: AIDataService? = null
    var translate_key = ""
    var textprocess: TextProcess? = null

    override fun onBind(intent: Intent?): IBinder {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCreate() {
        /**
         * This function call initService() to initiate the service.
         */
        super.onCreate()
        initService()
        Log.d(TAG,"onCreate")

    }

    private fun initService() {
//        This function initiates service and configures the language as well as the connection between android application and the server.
        val NLP_token = resources.getString(R.string.NLP_key)
        val selectedLanguage = LanguageConfig("en", NLP_token)
        val lang = ai.api.AIConfiguration.SupportedLanguages.fromLanguageTag(selectedLanguage.languageCode)
        val config = AIConfiguration(selectedLanguage.accessToken,
                lang,
                AIConfiguration.RecognitionEngine.System)
        Log.d(TAG,"initservice")
        aiDataService = AIDataService(this,config)
        translate_key = resources.getString(R.string.microsoft_translate_key)
        textprocess = TextProcess(translate_key)
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        /**
         * @param intent(Intent): This intent consists of message and the receiver of the message.
         *  For clarification, contact could be a cellphone number or a slack channel ID.
         */
        val msg = intent!!.getStringExtra("msg")  // Obtains the SMS.
        message = msg
        contact = intent!!.getStringExtra("contact")
        Log.d(TAG, "onStartCommand: " + contact + msg)
        // Creates a new thread upon which NavigationService runs.
        val t = Thread(Runnable // Checks if the message is null, if not the service starts.
        {
            if (msg != null) {
                sendRequest(msg)
            }
        })
        t.start()  // The thread starts.
        return Service.START_STICKY
    }

     fun sendRequest(queryString: String,eventString: String = "",contextString: String = "") {
         /**
          * This function sends the message to Dialogflow server.
          * @param queryString(String): The message sent to Dialogflow.
          * @param eventString(String): The event configured in Dialogflow.
          * @param contextString(String): The context con figured in Dialogflow.
          */
        Log.d(TAG,"send request")
        val task = @SuppressLint("StaticFieldLeak")
        object : AsyncTask<String, Void, AIResponse>() {

            private var aiError: AIError? = null

            override fun doInBackground(vararg params: String): AIResponse? {
                val request = AIRequest()
                val query = params[0]
                val event = params[1]

                if (!TextUtils.isEmpty(query))
                    request.setQuery(query)
                if (!TextUtils.isEmpty(event))
                    request.setEvent(AIEvent(event))
                val contextString = params[2]
                var requestExtras: RequestExtras? = null
                if (!TextUtils.isEmpty(contextString)) {
                    val contexts = listOf(AIContext(contextString))
                    requestExtras = RequestExtras(contexts, null)
                }

                try {
                    return aiDataService!!.request(request, requestExtras)
                } catch (e: AIServiceException) {
                    aiError = AIError(e)
                    return null
                }
            }

            override fun onPostExecute(response: AIResponse?) {
                if (response != null) {
                    onResult(response)
                } else {
                    Log.d(TAG, "Onresult error.")
                    //onError(aiError!!)
                }
            }
        }

        task.execute(queryString, eventString, contextString)
    }


    private fun onResult(response: AIResponse) {
        /**
         * This listener retrieves the message from the server and convert the response to JSON
         * before extracting and broadcasting needed information.
         * @param response(AIResponse): The response received from the server.
         */
        Log.d(TAG, "onResult")
        Log.i(TAG, "Received success response")

        // this is example how to get different parts of result object
        val status = response.status
        Log.i(TAG, "Status code: " + status.code!!)
        Log.i(TAG, "Status type: " + status.errorType)
        //errorTextView.setText("Status type:"+status.errorType)

        val result = response.result
        Log.i(TAG, "Resolved query: " + result.resolvedQuery)

        Log.i(TAG, "Action: " + result.action)
        action =result.action

        speech = result.fulfillment.speech
        Log.i(TAG, "Speech: " + speech)

        parameters = result.parameters

        if (parameters != null) {
            target = parameters!!["name"].toString()
            room = parameters!!["room"].toString()
        }

        val metadata = result.metadata
        if (metadata != null) {
            Log.i(TAG, "Intent id: " + metadata.intentId)
            Log.i(TAG, "Intent name: " + metadata.intentName)
        }

        val params = result.parameters
        var param_String = ""

        if (params != null && !params.isEmpty()) {
            Log.i(TAG, "Parameters: ")
            for ((key, value) in params) {
                param_String= param_String + String.format("%s: %s\n", key, value.toString())
                Log.i(TAG, String.format("%s: %s", key, value.toString()))
                if (key == Commands.NLP_PICTURE_TAKING_SOCIAL_MEDIA) {
                    socialmedia = value.toString()
                }
            }
        }
        sendMessage()
    }

    private fun clear(){
        /**
         * This function clears all the recorded response parameter for fear of interference
         * of next message request.
         */
        action = ""
        speech = ""
        socialmedia = ""
        contact = ""
        target = ""
        room = ""
    }

    fun sendtoSlack(msg:String, channel:String){
        /**
         * This function sends message to a certain Slack channel by starting Slack service.
         * @param msg(String): The message to be sent to Slack
         * @param channel(Sring): Slack channel ID
         */
        val i = Intent(this, SlackService::class.java)
        i.putExtra("msg", msg)
        i.putExtra("channelID", channel)
        Log.d(CentralHub.TAG,"Send to slack"+ msg)
        startService(i)
    }

    private fun sendMessage() {
        /**
         * This function broadcasts the processing result locally and Central Hub will get notified
         * of the result. This function is called in private fun onResult(response: AIResponse).
         */
        val intent = Intent("NLP-event")
        Log.d("sender", "Broadcasting message")
        // include action parameters.
        if (socialmedia !="") {
            socialmedia = socialmedia.substring(1, socialmedia.length - 1)
            Log.d(TAG, socialmedia + "!!!")
        }

        Log.d(TAG, "Speech:" + speech)

        intent.putExtra(Commands.NLP_ACTION, action)
        intent.putExtra(Commands.NLP_SPEECH,speech)
        intent.putExtra(Commands.ORIGINAL_MESSAGE, message)
        intent.putExtra(Commands.NLP_ACTION_CONTACT, contact)
        intent.putExtra("room", room)
        intent.putExtra("name", target)

        // contact could be a slack id number
        if (contact.length ==9 ){
            // Uncomment this part to play audio
//            AsyncTask.execute { textprocess!!.speak(speech, SpokenDialect.ENGLISH_UNITED_STATES) }
            sendtoSlack(speech, contact)
        }
        else{
            // This is for sending messages.
//            val sms = CommunicationOut(speech)
//            sms.sendSMS(contact)
        }

        when (action){
            Commands.NLP_ACTION_PICTURE_TAKING -> if (socialmedia == "") {
                Log.d(TAG, "picture taking requires social media")
                return
            }
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
        clear()
    }

    companion object {
        val TAG = NaturalLanguageProcessService::class.java.getName()
    }
}
