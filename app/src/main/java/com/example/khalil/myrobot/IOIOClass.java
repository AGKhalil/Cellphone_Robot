package com.example.khalil.myrobot;

/**
 * Created by Khalil on 4/10/17.
 * This class is responsible for the IOIO connection between the phone and the robot. Here, the
 * output pins are declared and all the command to the IOIO are sent. There are two main methods
 * that take care of that,  setup() sets up all the output pins, and loop() keeps a loop going that
 * sends commands to the pins. setMotion() is used to change the directions of the four motors.
 */

import ioio.lib.api.DigitalOutput;
import ioio.lib.api.PwmOutput;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.IOIOLooperProvider;
import ioio.lib.util.android.IOIOAndroidApplicationHelper;

public class IOIOClass extends BaseIOIOLooper implements IOIOLooperProvider {

    // Motor DC : Right Forward.
    private DigitalOutput INA1F; // L298n In 1
    private DigitalOutput INA2F; // L298n In 2
    private PwmOutput ENAF; // L298n Enable 1

    // Motor DC : Right Rear.
    private DigitalOutput INA1R; // L298n In 1
    private DigitalOutput INA2R; // L298n In 2
    private PwmOutput ENAR; // L298n Enable 1

    // Motor DC : Left Forward.
    private DigitalOutput INA3F; // L298n In 3
    private DigitalOutput INA4F; // L298n In 4
    private PwmOutput ENBF; // L298n Enable 2

    // Motor DC : Left Rear.
    private DigitalOutput INA3R; // L298n In 3
    private DigitalOutput INA4R; // L298n In 4
    private PwmOutput ENBR; // L298n Enable 2

    // All motors are initialized to stop.
    private boolean FMotorLeft = false;
    private boolean FMotorRight = false;
    private float FRightSpeed = 0;
    private float FLeftSpeed = 0;
    private boolean RMotorLeft = false;
    private boolean RMotorRight = false;
    private float RRightSpeed = 0;
    private float RLeftSpeed = 0;

    // These strings are used as commands.
    private static final String FORWARDS = "forwards";
    private static final String RIGHT = "right";
    private static final String LEFT = "left";
    private static final String STOP = "stop";

    private IOIOAndroidApplicationHelper helper;  // This helper is necessary to start the IOIOClass
            // loop from another class, TaskActivity.

    /**
     * This constructor is used to create the helper.
    */
    public IOIOClass(TaskActivity mTheGui) {
        helper = new IOIOAndroidApplicationHelper(mTheGui, this);
    }

    /**
     * This method is used to set up all the IOIO pins.
     */
    @Override
    protected void setup() throws ConnectionLostException, InterruptedException {
        try {
            // Motor DC : Right Forward.
            INA1F = ioio_.openDigitalOutput(1);
            INA2F = ioio_.openDigitalOutput(2);
            ENAF = ioio_.openPwmOutput(3, 100);

            // Motor DC : Right Rear.
            INA1R = ioio_.openDigitalOutput(11);
            INA2R = ioio_.openDigitalOutput(12);
            ENAR = ioio_.openPwmOutput(7, 100);

            // Motor DC : Left Forward.
            INA3F = ioio_.openDigitalOutput(4);
            INA4F = ioio_.openDigitalOutput(5);
            ENBF = ioio_.openPwmOutput(6, 100);

            // Motor DC : Left Rear.
            INA3R = ioio_.openDigitalOutput(13);
            INA4R = ioio_.openDigitalOutput(14);
            ENBR = ioio_.openPwmOutput(10, 100);
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
            // Right motor forward.
            ENBF.setDutyCycle(FRightSpeed);
            INA4F.write(FMotorRight);
            INA3F.write(!FMotorRight);

            // Left motor rear.
            ENBR.setDutyCycle(RLeftSpeed);
            INA4R.write(RMotorLeft);
            INA3R.write(!RMotorLeft);

            // Left motor forward.
            ENAF.setDutyCycle(FLeftSpeed);
            INA2F.write(FMotorLeft);
            INA1F.write(!FMotorLeft);

            // Right motor rear.
            ENAR.setDutyCycle(RRightSpeed);
            INA2R.write(RMotorRight);
            INA1R.write(!RMotorRight);

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
     * This method is publicly accessible by other classes, such as TaskActivity.
     */
    public void setMotion(String direction) {
        if (direction.equals(FORWARDS)) {
            FLeftSpeed = (float) 0.6;
            FRightSpeed = (float) 0.6;
            FMotorLeft = true;
            FMotorRight = false;

            RLeftSpeed = (float) 0.6;
            RRightSpeed = (float) 0.6;
            RMotorLeft = true;
            RMotorRight = false;
        } else if (direction.equals(RIGHT)) {
            FLeftSpeed = (float) 0.4;
            FRightSpeed = (float) 0.4;
            FMotorLeft = true;
            FMotorRight = true;

            RLeftSpeed = (float) 0.4;
            RRightSpeed = (float) 0.4;
            RMotorLeft = true;
            RMotorRight = true;
        } else if (direction.equals(LEFT)) {
            FLeftSpeed = (float) 0.4;
            FRightSpeed = (float) 0.4;
            FMotorLeft = false;
            FMotorRight = false;

            RLeftSpeed = (float) 0.4;
            RRightSpeed = (float) 0.4;
            RMotorRight = false;
            RMotorLeft = false;
        } else if (direction.equals(STOP)) {
            FLeftSpeed = 0;
            FRightSpeed = 0;

            RLeftSpeed = 0;
            RRightSpeed = 0;
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
    public IOIOAndroidApplicationHelper getIOIOAndroidApplicationHelper() {
        return helper;
    }
}

