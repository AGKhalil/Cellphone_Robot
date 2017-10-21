package com.example.khalil.myrobot;

/**
 * Created by Khalil on 4/10/17.
 * This class is responsible for the IOIO connection between the phone and the robot. Here, the
 * output pins are declared and all the command to the IOIO are sent. There are two main methods
 * that take care of that,  setup() sets up all the output pins, and loop() keeps a loop going that
 * sends commands to the pins. setMotion() is used to change the directions of the four motors.
 */

import android.content.ContextWrapper;
import android.os.Handler;
import android.util.Log;

import ioio.lib.api.DigitalOutput;
import ioio.lib.api.PwmOutput;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.IOIOLooperProvider;
import ioio.lib.util.android.IOIOAndroidApplicationHelper;

import static com.example.khalil.myrobot.Commands.GO_FORWARDS;
import static com.example.khalil.myrobot.Commands.NLP_WALK_SQUARE;
import static com.example.khalil.myrobot.Commands.NLP_WALK_TRI;
import static com.example.khalil.myrobot.Commands.STOP;
import static com.example.khalil.myrobot.Commands.TURN_CLOCKWISE;
import static com.example.khalil.myrobot.Commands.TURN_COUNTERCLOCKWISE;

class IOIOClass extends BaseIOIOLooper implements IOIOLooperProvider {

    private String TAG = "IOIOClass";

    /**
     * Lily
     */
    // Motor DC : Right Forward.
    private DigitalOutput L_INA1; // L298n In 1
    private DigitalOutput L_INA2; // L298n In 2
    private PwmOutput L_ENA; // L298n Enable 1

    // Motor DC : Left Forward.
    private DigitalOutput L_INA3; // L298n In 3
    private DigitalOutput L_INA4; // L298n In 4
    private PwmOutput L_ENB; // L298n Enable 2

    /**
     * Mickey
     */
    // Motor DC : Right Rear.
    private DigitalOutput M_INA1; // L298n In 1
    private DigitalOutput M_INA2; // L298n In 2
    private PwmOutput M_ENA; // L298n Enable 1

    // Motor DC : Left Forward.
    private DigitalOutput M_INA3; // L298n In 3
    private DigitalOutput M_INA4; // L298n In 4
    private PwmOutput M_ENB; // L298n Enable 2

    /**
     * Both robots
     */
    // All motors are initialized to stop.
    private boolean L_MotorLeft = false;
    private boolean L_MotorRight = false;
    private float L_RightSpeed = 0;
    private float L_LeftSpeed = 0;
    private boolean M_MotorLeft = false;
    private boolean M_MotorRight = false;
    private float M_LeftSpeed = 0;
    private float M_RightSpeed = 0;

    private String robotType;

    private IOIOAndroidApplicationHelper helper;  // This helper is necessary to start the IOIOClass
            // loop from another class, CentralHub.

    /**
     * This constructor is used to create the helper.
    */
    IOIOClass(ContextWrapper mTheGui, String mRobotType) {
        helper = new IOIOAndroidApplicationHelper(mTheGui, this);
        this.robotType = mRobotType;
    }

    /**
     * This method is used to set up all the IOIO pins.
     */
    @Override
    protected void setup() throws ConnectionLostException, InterruptedException {
        try {

            if (robotType.equals(Commands.LILY)) {
                // Lily Motor DC : Right Forward.
                L_INA1 = ioio_.openDigitalOutput(1);
                L_INA2 = ioio_.openDigitalOutput(2);
                L_ENA = ioio_.openPwmOutput(3, 100);

                // Lily Motor DC : Left Forward.
                L_INA3 = ioio_.openDigitalOutput(4);
                L_INA4 = ioio_.openDigitalOutput(5);
                L_ENB = ioio_.openPwmOutput(6, 100);


            } else if (robotType.equals(Commands.MICKEY)){

                // Mickey Motor DC : Right Rear.
                M_INA1 = ioio_.openDigitalOutput(1);
                M_INA2 = ioio_.openDigitalOutput(2);
                M_ENA = ioio_.openPwmOutput(3, 100);

                // Mickey Motor DC : Left Forward.
                M_INA3 = ioio_.openDigitalOutput(4);
                M_INA4 = ioio_.openDigitalOutput(5);
                M_ENB = ioio_.openPwmOutput(6, 100);
            }
        } catch (ConnectionLostException e) {
            throw e;
        }
    }

    /**
     * This method is the loop that keeps sending out commands to the IOIO pins.
     */
    @Override
    public void loop() throws ConnectionLostException, InterruptedException {
        ioio_.beginBatch();  // Used when multiple pins are commanded at once.
        try {
            if (robotType.equals(Commands.LILY)) {
                // Lily Right motor forward.
                L_ENB.setDutyCycle(L_RightSpeed);
                L_INA4.write(L_MotorRight);
                L_INA3.write(!L_MotorRight);

                // Lily Left motor forward.
                L_ENA.setDutyCycle(L_LeftSpeed);
                L_INA2.write(L_MotorLeft);
                L_INA1.write(!L_MotorLeft);
            } else if (robotType.equals(Commands.MICKEY)) {
                // Mickey Left motor forward.
                M_ENB.setDutyCycle(M_RightSpeed);
                M_INA4.write(M_MotorLeft);
                M_INA3.write(!M_MotorLeft);

                // Mickey Right motor rear.
                M_ENA.setDutyCycle(M_LeftSpeed);
                M_INA2.write(M_MotorRight);
                M_INA1.write(!M_MotorRight);
            }
            Thread.sleep(10);
        } catch (InterruptedException e) {
            ioio_.disconnect();
        } catch (ConnectionLostException e) {
            throw e;
        } finally {
            ioio_.endBatch();
        }

    }

    /**
     * This method is used to change the motors' directions and speeds based on a string command.
     * This method is publicly accessible by other classes, such as CentralHub.
     */
    void setMotion(String direction) {
        String[] motionSequence;
        int[] durationSequence;
        if (robotType.equals(Commands.LILY)) {
            Log.d(TAG, "setMotion: "+Commands.LILY+" "+direction);
            switch (direction) {
                case GO_FORWARDS:
                    L_LeftSpeed = (float) 0.3;
                    L_RightSpeed = (float) 0.3;
                    L_MotorLeft = true;
                    L_MotorRight = false;
                    break;
                case TURN_CLOCKWISE:
                    L_LeftSpeed = (float) 0.3;
                    L_RightSpeed = (float) 0.3;
                    L_MotorLeft = true;
                    L_MotorRight = true;
                    break;
                case TURN_COUNTERCLOCKWISE:
                    L_LeftSpeed = (float) 0.3;
                    L_RightSpeed = (float) 0.3;
                    L_MotorLeft = false;
                    L_MotorRight = false;
                    break;
                case Commands.NLP_WALK_CIRCLE:
                    L_LeftSpeed = (float) 0.1;
                    L_RightSpeed = (float) 0.6;
                    L_MotorLeft = true;
                    L_MotorRight = false;
                    break;
                case NLP_WALK_SQUARE:
                    motionSequence = new String[]{GO_FORWARDS, TURN_COUNTERCLOCKWISE,
                        GO_FORWARDS, TURN_COUNTERCLOCKWISE, GO_FORWARDS, TURN_COUNTERCLOCKWISE,
                        GO_FORWARDS, STOP};
                    durationSequence = new int[] {200, 1000, 200, 1000, 200, 1000, 200, 1000};
                    sequenceMotion(motionSequence, durationSequence);
                    break;
                case NLP_WALK_TRI:
                    motionSequence = new String[]{GO_FORWARDS, TURN_COUNTERCLOCKWISE,
                            GO_FORWARDS, TURN_COUNTERCLOCKWISE, GO_FORWARDS, STOP};
                    durationSequence = new int[] {250, 1000, 250, 1000, 250, 1000, 1000};
                    sequenceMotion(motionSequence, durationSequence);
                    break;
                case STOP:
                    L_LeftSpeed = 0;
                    L_RightSpeed = 0;
                    break;
            }
        } else if (robotType.equals(Commands.MICKEY)) {
            Log.d(TAG, "setMotion: "+Commands.MICKEY+" "+direction);
            switch (direction) {
                case Commands.GO_FORWARDS:
                    M_RightSpeed = (float) 0.6;
                    M_LeftSpeed = (float) 0.6;
                    M_MotorLeft = true;
                    M_MotorRight = true;
                    break;
                case Commands.TURN_CLOCKWISE:
                    M_RightSpeed = (float) 0.6;
                    M_LeftSpeed = (float) 0.6;
                    M_MotorLeft = true;
                    M_MotorRight = false;
                    break;
                case Commands.TURN_COUNTERCLOCKWISE:
                    M_RightSpeed = (float) 0.6;
                    M_LeftSpeed = (float) 0.6;
                    M_MotorLeft = false;
                    M_MotorRight = true;
                    break;
                case Commands.NLP_WALK_CIRCLE:
                    M_RightSpeed = (float) 0.6;
                    M_LeftSpeed = (float) 0.1;
                    M_MotorLeft = true;
                    M_MotorRight = true;
                    break;
                case NLP_WALK_SQUARE:
                    motionSequence = new String[]{GO_FORWARDS, TURN_COUNTERCLOCKWISE,
                            GO_FORWARDS, TURN_COUNTERCLOCKWISE, GO_FORWARDS, TURN_COUNTERCLOCKWISE,
                            GO_FORWARDS, STOP};
                    durationSequence = new int[] {625, 1000, 625, 1000, 625, 1000, 625, 1000};
                    sequenceMotion(motionSequence, durationSequence);
                    break;
                case NLP_WALK_TRI:
                    motionSequence = new String[]{GO_FORWARDS, TURN_COUNTERCLOCKWISE,
                            GO_FORWARDS, TURN_COUNTERCLOCKWISE, GO_FORWARDS, STOP};
                    durationSequence = new int[] {850, 1000, 850, 1000, 850, 1000, 1000};
                    sequenceMotion(motionSequence, durationSequence);
                    break;
                case Commands.STOP:
                    M_RightSpeed = 0;
                    M_LeftSpeed = 0;
                    break;
            }
        }
    }

    void declareMotion(final String motion, final String newMotion, int duration) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                setMotion(newMotion);
            }
        }, duration);
        setMotion(motion);
    }

    void sequenceMotion(final String[] motion, int[] duration) {
        int finalDuration = 0;
        for (int i = 0; i < motion.length; i++) {
            final int finalI = i;
            finalDuration = duration[finalI] + finalDuration;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    setMotion(motion[finalI]);
                }
            }, finalDuration);
        }
    }

    /**
     * This method is necessary for the IOIOLooperProvider implementation.
     */
    @Override
    public IOIOLooper createIOIOLooper(String connectionType, Object extra) {
        return this;
    }

    /**
     * This method is used to allow an external class to access the helper to be able to create an
     * instance of the IOIOClass.
     */
    IOIOAndroidApplicationHelper getIOIOAndroidApplicationHelper() {
        return helper;
    }


}

