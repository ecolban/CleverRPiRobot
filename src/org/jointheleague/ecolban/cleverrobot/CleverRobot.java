package org.jointheleague.ecolban.cleverrobot;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

import org.jointheleague.ecolban.rpirobot.IRobotAdapter;
import org.jointheleague.ecolban.rpirobot.IRobotInterface;
import org.jointheleague.ecolban.rpirobot.SimpleIRobot;

public class CleverRobot extends IRobotAdapter implements Runnable {

    private boolean running;
    private final boolean debug = true; // Set to true to get debug messages.
    private int leftSpeed;
    private int rightSpeed;

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
//        new Thread(new RemoteControl(this)).start();
    }

    public void run() {
        setLeftSpeed(-50);
        setRightSpeed(50);
        setRunning(true);
        int dotCount = 0;
        System.out.print("Running");
        while (isRunning()) {
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
//                    System.out.format("Left bumper = %s, Right bumper = %s\n", isBumpLeft(), isBumpRight());
//                    System.out.format("Left wheel speed = %d, Right wheel speed = %d\n", leftSpeed, rightSpeed);
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

    private void shutDown() throws IOException {
        if (debug) {
            System.out.println("Shutting down...");
        }
        stop();
        closeConnection();
    }
}
