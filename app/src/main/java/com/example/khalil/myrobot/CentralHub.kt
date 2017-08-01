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
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
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
    var robot = Commands.LILY
    /**
     * Start of Testing Methods *****************************************
     */

    /**
     * This method is used to bypass the SMS segment of the code. It starts NavigationService with
     * a mock SMS destination.
     * @param view is the button view SEND SMS
     */
    fun mockStartRobotDriver(view: View) {
        // Start robot driver
        if (destination != null) {
            Toast.makeText(this, destination, Toast.LENGTH_SHORT).show()
            startRobotDriver(destination)
        }
    }


    fun mockSend(view: View){
        // Start NLP service
        val msg = TEXT_Send.text.toString()
        Toast.makeText(this,msg,0).show()
        val i = Intent(this, NaturalLanguageProcessService::class.java)
        i.putExtra("msg", msg)
        val selectid = radioRobot.checkedRadioButtonId
        val radioButton = findViewById(selectid) as RadioButton
        val platform = radioButton.text
        i.putExtra("platform", platform)
        i.putExtra("phonenumber","317914")
        Log.d(TAG,"platform"+platform)
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

    }

    private val NLPReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // Get extra data included in the Intent
            action = intent.getStringExtra(Commands.NLP_ACTION)
            speech = intent.getStringExtra(Commands.NLP_SPEECH)

            Log.d("receiver", "Got intent: " + action)
            TEXT_Receive.setText(speech)
            when(action){
                Commands.NLP_ACTION_NAVIGATION -> {
                    destination = intent.getStringExtra(Commands.NLP_NAVIGATION_DESTINATION)
                    startRobotDriver(destination)
                }

                Commands.NLP_ACTION_PICTURE_TAKING -> {
                    socialmedia = intent.getStringExtra(Commands.NLP_PICTURE_TAKING_SOCIAL_MEDIA)
                    startRobotManipulator(action, socialmedia)
                }

                Commands.NLP_ACTION_TURNAROUND -> {
                    rotation_direction = intent.getStringExtra(Commands.NLP_TURNAROUND_ROTATION_DIRECTION)
                    startRobotManipulator(action, rotation_direction)
                }

                Commands.NLP_ACTION_WALK ->{
                    shape = intent.getStringExtra(Commands.NLP_WALK_SHAPE)
                    startRobotManipulator(action, shape)
                }
            }
        }
    }

    /**
     * This is the method that starts the RobotDriver by filing the mIntent when called.
     */
    private fun startRobotDriver(message: String) {
        robotDriverIntent!!.putExtra(Commands.HUB_TO_DRIVER_DESTINATION_MESSAGE, message) // The SMS, which is the destination name, is passed
        // to RobotDriver, because it needs it to file the API request.
        robotDriverIntent!!.putExtra(Commands.HUB_TO_DRIVER_ROBOT_TYPE, robot) // Lily or Mickey
        Log.d(TAG, "startRobotDriver")
        this.startActivity(robotDriverIntent)  // This line is what actually starts the RobotDriver.
    }

    /**
     * This is the method that handles RobotManipulator.
     */
    private fun startRobotManipulator(action: String, actionParameter: String) {
        robotManipulatorIntent!!.putExtra(Commands.HUB_TO_Manipulator_ACTION, action)
        robotManipulatorIntent!!.putExtra(Commands.HUB_TO_Manipulator_ACTION_PAREMETER, actionParameter)
        robotManipulatorIntent!!.putExtra(Commands.HUB_TO_Manipulator_ROBOT_TYPE, robot) // Lily or Mickey
        Log.d(TAG, "startRobotManipulator")
        this.startActivity(robotManipulatorIntent)
    }


    companion object {
        val TAG = CentralHub::class.java.getName()
    }

    fun useMickey(view: View){
        robot = Commands.MICKEY
    }
}

