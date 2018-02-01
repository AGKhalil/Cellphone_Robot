package com.example.khalil.myrobot


import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_hub.*


class CentralHub : AppCompatActivity() {
    /**
     * CentralHub is the app's MainActivity. When an SMS or Slack message is received by the SMSCom
     * IN or SlackService classes, they are sent to the NLP for decoding. The Natural Language
     * Processor (NLP) then files back the response, which is reported to CentralHub that then
     * allocates the appropriate activity or service that must be started while passing the necessary
     * parameters as an intent. Moreover, the user can manually enter their command on this
     * activity's main screen rather than messaging it. Once the user hits the SEND button, the
     * command is sent to the NLP. This can also be repeated through speech.
     * This class is written entirely in Kotlin.
     */
    private var robotControllerIntent: Intent? = null
    var action = ""
    var speech = ""
    var socialmedia = ""
    var message = ""
    var contact = ""
    var translate_key = ""
    var textprocess: TextProcess? = null

    /**
     * Start of Testing Methods *****************************************
     */

    @SuppressLint("WrongConstant")
    fun mockSend(view: View){
        /**
         *  Send the message on the screen to natural language process service
         */
        val msg = TEXT_Send.text.toString()
        Toast.makeText(this,msg,0).show()
        val i = Intent(this, NaturalLanguageProcessService::class.java)
        i.putExtra("msg", msg)
        i.putExtra("contact","G7Q5G4XS8")
        startService(i)
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
        Log.d(TAG,"Send to slack"+ msg)
        startService(i)
    }


    /**
     * End of Testing Methods *******************************************
     */

    /**
     * This method sets up the entire app, from the different intents that will be issued throughout
     * its lifecycle to the main XML file that is issued to the user.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hub) // Sets the XML view file that appears to the user.

        translate_key = resources.getString(R.string.microsoft_translate_key)
        textprocess = TextProcess(translate_key)
        //init service
        sendtoSlack("init","")

        // RobotManipulator.
        robotControllerIntent = Intent(this, RobotController::class.java)

        // This IF block insures all permissions are granted.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(this, "Please grant all the permissions needed for this app to " + "function fully.", Toast.LENGTH_LONG).show()
            return
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(NLPReceiver,
                IntentFilter("NLP-event"))
    }

    private val NLPReceiver = object : BroadcastReceiver() {
        /**
         * This is a local broadcast receiver receiving results from Natural Language Process service.
         */
        override fun onReceive(context: Context, intent: Intent) {
            // Get extra data included in the Intent
            action = intent.getStringExtra(Commands.NLP_ACTION)
            speech = intent.getStringExtra(Commands.NLP_SPEECH)
            contact = intent.getStringExtra(Commands.NLP_ACTION_CONTACT)
            message = intent.getStringExtra(Commands.ORIGINAL_MESSAGE)

            Log.d("receiver", "Got intent: " + action)
            TEXT_Receive.setText(speech)
            when(action){
                Commands.NLP_ACTION_PICTURE_TAKING -> {
                    socialmedia = intent.getStringExtra(Commands.NLP_PICTURE_TAKING_SOCIAL_MEDIA)
                }
                Commands.NLP_ACTION_WALK -> {
                    startRobotController(action,contact)
                }
            }
        }
    }

    /**
     * This is the method that handles RobotController.
     */
    private fun startRobotController(action: String, contact: String) {
        /**
         * This function is for starting a robot controlling activity corresponding to intent "walk".
         * @param action: the name of the action return from NLP server
         * @param contact: the contact info where the messages will be delivered
         */
        robotControllerIntent!!.putExtra("action", action)
        Log.d(TAG, "uri: "+TEXT_URL.text)
        robotControllerIntent!!.putExtra("uri", TEXT_URL.text.toString())
        robotControllerIntent!!.putExtra("contact", contact)
        this.startActivity(robotControllerIntent)
    }

    companion object {
        val TAG = CentralHub::class.java.getName()
    }
}

