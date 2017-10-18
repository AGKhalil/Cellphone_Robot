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
import android.util.Log
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_hub.*
import org.ros.address.InetAddressFactory
import org.ros.android.RosActivity
import org.ros.exception.RosRuntimeException
import org.ros.node.NodeConfiguration
import org.ros.node.NodeMainExecutor
import java.net.URI
import java.net.URISyntaxException

class CentralHub : RosActivity("Phone", "Phone") {
    internal var talkernode = Talker(this)
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
    var myIdentifier = Commands.LILY
    var selectid:Int = 0
    var radioButton:RadioButton ?= null

    override fun startMasterChooser() {
        val uri: URI
        try {
            uri = URI("http://192.168.1.101:11311/")
        } catch (e: URISyntaxException) {
            throw RosRuntimeException(e)
        }

        nodeMainExecutorService.masterUri = uri
        object : AsyncTask<Void, Void, Void>() {
            override fun doInBackground(vararg params: Void): Void? {
                this@CentralHub.init(nodeMainExecutorService)
                return null
            }
        }.execute()
    }
    override fun init(p0: NodeMainExecutor) {
        val lisnode = Listener(this, this)

        val nodeConfiguration = NodeConfiguration.newPublic(
                InetAddressFactory.newNonLoopback().hostAddress)
        nodeConfiguration.masterUri = masterUri

        p0.execute(talkernode, nodeConfiguration)
        p0.execute(lisnode, nodeConfiguration)
    }

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
//        val msg = TEXT_Send.text.toString()
//        Toast.makeText(this,msg,0).show()
//        val i = Intent(this, NaturalLanguageProcessService::class.java)
//        i.putExtra("msg", msg)
//        i.putExtra("myIdentifier", myIdentifier)
//        i.putExtra("phonenumber","317914")
//        Log.d(TAG,"myIdentifier"+ myIdentifier)
//        startService(i)
        talkernode.publish("w")
        Log.d("ALAASASAK", "I WOOORKKKKKKK")
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

