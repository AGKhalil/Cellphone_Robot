package com.example.khalil.myrobot

/**
 * Created by Khalil on 4/8/17
 * This is the app's MainActivity. Here the app's view is set up, an instance of IOIOClass is
 * created and controlled, NavigationService is called, the picture is taken, and is uploaded
 * to Twitter. This is the app's main hub.
 */

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import io.github.firemaples.language.Language
import kotlinx.android.synthetic.main.activity_hub.*


class CentralHub : AppCompatActivity() {
    private var robotDriverIntent: Intent? = null
    private var robotManipulatorIntent: Intent? = null
    var action = ""
    var speech = ""
    var rotation_direction = ""
    var socialmedia = ""
    var destination =""
    var shape = ""
    var platform = ""
    var message = ""
    var contact = ""
    var myIdentifier = Commands.MICKEY
    var selectid:Int = 0
    var radioButton:RadioButton ?= null
    var translate_key = ""
    var textprocess: TextProcess? = null
    /**
     * Start of Testing Methods *****************************************
     */

    /**
     * This method is used to bypass the SMS segment of the code. It starts NavigationService with
     * a mock SMS destination.
     * @param view is the button view SEND SMS
     */

    fun mockStartRobotDriver(view: View) {
        selectid = radioRobot.checkedRadioButtonId
        radioButton = findViewById(selectid) as RadioButton
        myIdentifier = radioButton!!.text.toString()
        // Start robot driver
        if (destination != null) {
            Toast.makeText(this, destination, Toast.LENGTH_SHORT).show()
            startRobotDriver(destination, myIdentifier)
        }
    }


    fun mockSend(view: View){
        // Start NLP service
        val msg = TEXT_Send.text.toString()
        Toast.makeText(this,msg,0).show()
        val i = Intent(this, NaturalLanguageProcessService::class.java)
        i.putExtra("msg", msg)
        i.putExtra("myIdentifier", myIdentifier)
        i.putExtra("phonenumber","D7Q7NDWVB")
        Log.d(TAG,"myIdentifier"+ myIdentifier)
        startService(i)
    }

    fun sendtoNLP(msg:String, myIdentifier:String, channel:String){
        val i = Intent(this, NaturalLanguageProcessService::class.java)
        i.putExtra("msg", msg)
        i.putExtra("myIdentifier", myIdentifier)
        i.putExtra("phonenumber",channel)
        Log.d(TAG,"myIdentifier"+ myIdentifier)
        startService(i)
    }

    fun sendtoSlack(msg:String, channel:String){
        val i = Intent(this, SlackService::class.java)
        i.putExtra("msg", msg)
        i.putExtra("channelID", channel)
        Log.d(TAG,"Send to slack"+ msg)
        startService(i)
    }

    fun mocktranslate(view: View){
        AsyncTask.execute {
            val test_string = "Here, parameters fName and personAge inside the parenthesis accepts values Joe and 25 respectively when person1 object is created. However, fName and personAge are used without using var or val, and are not properties of the Person class."
            val response = textprocess!!.translate("Wählen Sie die Sprache, in der Sie bevorzugt stöbern, einkaufen und Mitteilungen von uns erhalten möchten.",Language.GERMAN,Language.ENGLISH)
            Log.d("Translate",response)
//            val language = textprocess!!.detect_language(test_string)
//            val language2 = textprocess!!.break_sentence(test_string, Language.ENGLISH)
//            Log.d("Translate",language.)
//            Log.d("Translate",language2.toString())
        }

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
        robotDriverIntent = Intent(this, RobotDriver::class.java) // Associates mIntent with
        robotManipulatorIntent = Intent(this, RobotManipulator::class.java) // Associates mIntent with

        translate_key = resources.getString(R.string.microsoft_translate_key)
        textprocess = TextProcess(translate_key)
        //init service
        sendtoSlack("init","")

        // RobotManipulator.

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
        selectid = radioRobot.checkedRadioButtonId
        radioButton = findViewById(selectid) as RadioButton
    }

    private val NLPReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // Get extra data included in the Intent
            action = intent.getStringExtra(Commands.NLP_ACTION)
            speech = intent.getStringExtra(Commands.NLP_SPEECH)
            platform = intent.getStringExtra(Commands.NLP_ACTION_ROBOT)
            contact = intent.getStringExtra(Commands.NLP_ACTION_CONTACT)
            selectid = radioRobot.checkedRadioButtonId
            radioButton = findViewById(selectid) as RadioButton
            myIdentifier = radioButton!!.text.toString()
            message = intent.getStringExtra(Commands.ORIGINAL_MESSAGE)


            val sendsms = CommunicationOut(message)

            Log.d("receiver", "Got intent: " + action)
            TEXT_Receive.setText(speech)
            when(action){
                Commands.NLP_ACTION_NAVIGATION -> {
                    destination = intent.getStringExtra(Commands.NLP_NAVIGATION_DESTINATION)
                }

                Commands.NLP_ACTION_PICTURE_TAKING -> {
                    socialmedia = intent.getStringExtra(Commands.NLP_PICTURE_TAKING_SOCIAL_MEDIA)
                }

                Commands.NLP_ACTION_TURNAROUND -> {
                    rotation_direction = intent.getStringExtra(Commands.NLP_TURNAROUND_ROTATION_DIRECTION)
                }

                Commands.NLP_ACTION_WALK -> {
                    shape = intent.getStringExtra(Commands.NLP_WALK_SHAPE)
                }
            }

            Log.d(CentralHub.TAG, "Platform is: " + platform)
            Log.d(CentralHub.TAG, "Identifier is: " + myIdentifier)

            if (platform == "" || platform == myIdentifier) {
                when(action){
                    Commands.NLP_ACTION_NAVIGATION -> {
                        startRobotDriver(destination, myIdentifier)
                    }

                    Commands.NLP_ACTION_PICTURE_TAKING -> {
                        startRobotManipulator(action, socialmedia, myIdentifier)
                    }

                    Commands.NLP_ACTION_TURNAROUND -> {
                        startRobotManipulator(action, rotation_direction, myIdentifier)
                    }

                    Commands.NLP_ACTION_WALK -> {
                        startRobotManipulator(action, shape, myIdentifier)
                    }
                }
            } else if (platform == Commands.MICKEY && myIdentifier == Commands.LILY){
                //send back the speech to wherever the message comes from
                sendsms.sendSMS("7654095215")
            } else if (platform == Commands.LILY && myIdentifier == Commands.MICKEY){
                //send back the speech to wherever the message comes from
                sendsms.sendSMS("7654096743")
            }
        }
    }

    /**
     * This is the method that starts the RobotDriver by filing the mIntent when called.
     */
    private fun startRobotDriver(message: String, platform: String) {
        robotDriverIntent!!.putExtra(Commands.HUB_TO_DRIVER_DESTINATION_MESSAGE, message) // The SMS, which is the destination name, is passed
        // to RobotDriver, because it needs it to file the API request.
        robotDriverIntent!!.putExtra(Commands.HUB_TO_DRIVER_ROBOT_TYPE, platform) // Lily or Mickey
        Log.d(TAG, "startRobotDriver: "+platform)
        this.startActivity(robotDriverIntent)  // This line is what actually starts the RobotDriver.
    }

    /**
     * This is the method that handles RobotManipulator.
     */
    private fun startRobotManipulator(action: String, actionParameter: String,platform: String) {
        robotManipulatorIntent!!.putExtra(Commands.HUB_TO_Manipulator_ACTION, action)
        robotManipulatorIntent!!.putExtra(Commands.HUB_TO_Manipulator_ACTION_PAREMETER, actionParameter)
        robotManipulatorIntent!!.putExtra(Commands.HUB_TO_Manipulator_ROBOT_TYPE, platform) // Lily or Mickey
        Log.d(TAG, "startRobotManipulator: "+platform)
        this.startActivity(robotManipulatorIntent)
    }




    companion object {
        val TAG = CentralHub::class.java.getName()
    }

}

