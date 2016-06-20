package org.jointheleague.ecolban.cleverrobot;

import java.io.IOException;

import org.jointheleague.ecolban.rpirobot.IRobotAdapter;
import org.jointheleague.ecolban.rpirobot.IRobotInterface;
import org.jointheleague.ecolban.rpirobot.SimpleIRobot;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

public class CleverRobot extends IRobotAdapter implements Runnable {

    // 11 left, 12 front, 13 right (Pi pins) are trigger. 15 left, 16 front, 18
    // right are echo.
    private static Pin LEFT_TRIGGER = RaspiPin.GPIO_00;
    private static Pin FRONT_TRIGGER = RaspiPin.GPIO_01;
    private static Pin RIGHT_TRIGGER = RaspiPin.GPIO_02;
    private static Pin LEFT_ECHO = RaspiPin.GPIO_03;
    private static Pin FRONT_ECHO = RaspiPin.GPIO_04;
    private static Pin RIGHT_ECHO = RaspiPin.GPIO_05;

    private boolean running;
    private final boolean debug = true; // Set to true to get debug messages.
    private int leftSpeed;
    private int rightSpeed;
    private GpioPinDigitalOutput[] triggers = new GpioPinDigitalOutput[3];
    private GpioPinDigitalInput[] echos = new GpioPinDigitalInput[3];

    public CleverRobot(IRobotInterface iRobot) {
        super(iRobot);
        if (debug) {
            System.out.println("Hello. I'm CleverRobot");
        }
    }

    public static void main(String[] args) throws IOException {
        try {
            IRobotInterface base = new SimpleIRobot();
            CleverRobot rob = new CleverRobot(base);
            rob.initialize();
            rob.run();
        } catch (InterruptedException | IOException e) {
            System.out.println(e.getMessage());
        }
    }

    /* This method is executed when the robot first starts up. */
    private void initialize() throws IOException {
        // what would you like me to do, Clever Human?
        if (debug) {
            System.out.println("Initializing...");
        }

        final GpioController gpio = GpioFactory.getInstance();
        triggers = new GpioPinDigitalOutput[3];
        echos = new GpioPinDigitalInput[3];
        triggers[0] = gpio.provisionDigitalOutputPin(LEFT_TRIGGER, PinState.LOW);
        triggers[1] = gpio.provisionDigitalOutputPin(FRONT_TRIGGER, PinState.LOW);
        triggers[2] = gpio.provisionDigitalOutputPin(RIGHT_TRIGGER, PinState.LOW);
        echos[0] = gpio.provisionDigitalInputPin(LEFT_ECHO, PinPullResistance.PULL_DOWN);
        echos[1] = gpio.provisionDigitalInputPin(FRONT_ECHO, PinPullResistance.PULL_DOWN);
        echos[2] = gpio.provisionDigitalInputPin(RIGHT_ECHO, PinPullResistance.PULL_DOWN);

        Runnable pinger = new Runnable() {

            @Override
            public void run() {
                long leftDistance;
                long frontDistance;
                long rightDistance;
                while (true) {
                    try {
                        leftDistance = pingLeft();
                        Thread.sleep(10);
                        frontDistance = pingFront();
                        Thread.sleep(10);
                        rightDistance = pingRight();
                        Thread.sleep(10);
                        System.out.format("L = %d, F = %d, R = %d\n", leftDistance, frontDistance, rightDistance);
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }
                }

            }

        };
        //new Thread(pinger).start();
    }

    public void run() {
        setLeftSpeed(-50);
        setRightSpeed(50);
        setRunning(true);
        int dotCount = 0;
        System.out.print("Running");
        while (isRunning()) {
            System.out.println(pingLeft());
            if (dotCount > 70) {
                System.out.println();
                dotCount = 0;
            }
            System.out.print(".");
            dotCount++;
            try {
                driveDirect(leftSpeed, rightSpeed);
                Thread.sleep(20);
                readSensors(SENSORS_BUMPS_AND_WHEEL_DROPS);
                if (isBumpLeft() && isBumpRight()) {
                    // adjust the wheel speeds
                }
                if (isBumpLeft()) {
                    // adjust the wheel speeds
                }
                if (isBumpRight()) {
                    // adjust the wheel speeds
                }
                if (debug) {
                    // System.out.format("Left bumper = %s, Right bumper =
                    // %s\n", isBumpLeft(), isBumpRight());
                    // System.out.format("Left wheel speed = %d, Right wheel
                    // speed = %d\n", leftSpeed, rightSpeed);
                }
                // ...
                // if (/* goal reached */){
                // running = false;
                // }

            } catch (IOException | InterruptedException e) {
                System.out.println(String.format("%s:%s", e.toString(), e.getMessage()));
                setRunning(false);
            }
        }
        System.out.println("Outside loop.");
        try {
            System.out.println("Trying shutdown...");
            shutDown();
        } catch (IOException e) {
        }

    }

    public synchronized int getLeftSpeed() {
        return leftSpeed;
    }

    public synchronized void setLeftSpeed(int leftSpeed) {
        this.leftSpeed = leftSpeed;
    }

    public synchronized int getRightSpeed() {
        return rightSpeed;
    }

    public synchronized void setRightSpeed(int rightSpeed) {
        this.rightSpeed = rightSpeed;
    }

    public synchronized boolean isRunning() {
        return running;
    }

    public synchronized void setRunning(boolean running) {
        this.running = running;
    }

    private long ping(int sensorId) {
        
        long start = 0L;
        long end = 0L;
        long diff = 0;

        try {
            triggers[sensorId].high();
            Thread.sleep(10);
            triggers[sensorId].low();
            System.out.println("Trigger pulse sent.");
            while (echos[sensorId].isLow()) {
            }
            start = System.nanoTime();
            System.out.println("Echo is high.");

            while (echos[sensorId].isHigh()) {
            }
            end = System.nanoTime();
            System.out.println("Echo is low.");

            diff = (end - start) / 58000;

            return diff;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public long pingLeft() {
        System.out.println("Left Sensor Triggered");
        return ping(0);
    }

    public long pingFront() {
        System.out.println("Front Sensor Triggered");
        return ping(1);
    }

    public long pingRight() {
        System.out.println("Right Sensor Triggered");
        return ping(2);
    }

    private void shutDown() throws IOException {
        if (debug) {
            System.out.println("Shutting down...");
        }
        stop();
        closeConnection();
    }
}
