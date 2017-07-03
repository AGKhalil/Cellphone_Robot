package com.example.khalil.myrobot

import ai.api.AIServiceException
import ai.api.RequestExtras
import ai.api.android.AIConfiguration
import ai.api.android.AIDataService
import ai.api.model.*
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_aitext_sample.*


/**
 * Created by Michael Wang on 05/28/17.
 * This is for text communication
 */

class TextActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener, View.OnClickListener {

    //private val gson = GsonFactory.getGson()
    var destination = ""
    var action = ""
    private var aiDataService: AIDataService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_aitext_sample)

        val eventAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, Config.events)
        eventSpinner!!.adapter = eventAdapter

        //eventCheckBox = findViewById(R.id.eventsCheckBox) as CheckBox?
        checkBoxClicked()
        eventCheckBox!!.setOnClickListener(this)

        val languagesAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, Config.languages)
        spinner.adapter = languagesAdapter
        spinner.onItemSelectedListener = this

        val msg:String = intent.getStringExtra("message")
        queryEditText.setText(msg)

        //set onclick event
        buttonClear.setOnClickListener {clearEditText()}
        buttonSend.setOnClickListener { sendRequest()}
        eventCheckBox.setOnClickListener { checkBoxClicked()}
        autocompletebutton.setOnClickListener { autocompleteactivity() }


    }

    private fun autocompleteactivity(){

       /* if (action == "maps.search"){
            val intent1 = Intent(this, autocomplete::class.java)
            intent1.putExtra("placename",destination)
            startActivity(intent1)
        }
        else {
            Toast.makeText(this,"You did not mention a place",Toast.LENGTH_SHORT).show();
        }*/
    }

    private fun initService(selectedLanguage: LanguageConfig) {
        val lang = ai.api.AIConfiguration.SupportedLanguages.fromLanguageTag(selectedLanguage.languageCode)
        val config = AIConfiguration(selectedLanguage.accessToken,
                lang,
                AIConfiguration.RecognitionEngine.System)
        Log.d(TAG,"initservice")
        aiDataService = AIDataService(this, config)
    }


    private fun clearEditText() {
        queryEditText!!.setText("")
    }

    private fun sendRequest() {

        val queryString = if (!eventSpinner!!.isEnabled) queryEditText!!.text.toString() else null
        val eventString = if (eventSpinner!!.isEnabled) eventSpinner!!.selectedItem.toString() else null
        val contextString = contextEditText.text.toString()

        if (TextUtils.isEmpty(queryString) && TextUtils.isEmpty(eventString)) {
            onError(AIError(getString(R.string.non_empty_query)))
            Toast.makeText(this,"Write something please!",0).show()
            return
        }

        Log.d(TAG,"send request")
        val task = object : AsyncTask<String, Void, AIResponse>() {

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
                    onError(aiError!!)
                }
            }
        }

        task.execute(queryString, eventString, contextString)
    }

    fun checkBoxClicked() {
        eventSpinner!!.isEnabled = eventCheckBox!!.isChecked
        queryEditText!!.visibility = if (!eventCheckBox!!.isChecked) View.VISIBLE else View.GONE
    }

    // process the messages from the server
    private fun onResult(response: AIResponse) {
        runOnUiThread {
            Log.d(TAG, "onResult")

            //resultTextView!!.text = gson.toJson(response)

            Log.i(TAG, "Received success response")

            // this is example how to get different parts of result object
            val status = response.status
            Log.i(TAG, "Status code: " + status.code!!)
            Log.i(TAG, "Status type: " + status.errorType)
            errorTextView.setText("Status type:"+status.errorType)

            val result = response.result
            Log.i(TAG, "Resolved query: " + result.resolvedQuery)

            Log.i(TAG, "Action: " + result.action)
            actionTextView.setText("Action: "+ result.action)
            action =result.action

            val speech = result.fulfillment.speech
            Log.i(TAG, "Speech: " + speech)
            resultTextView.setText("Speech: "+speech)

            //Speak out
            //TTS.speak(speech)

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
                    if (key == "destination"){destination = value.toString()}
                }
            }
            parameterTextView.setText(param_String)

        }
    }

    private fun onError(error: AIError) {
        runOnUiThread { resultTextView!!.text = error.toString() }
    }


    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        val selectedLanguage = parent.getItemAtPosition(position) as LanguageConfig
        initService(selectedLanguage)
    }

    override fun onNothingSelected(parent: AdapterView<*>) {}

    override fun onClick(p0: View?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    companion object {

        val TAG = TextActivity::class.java!!.getName()
    }
}
