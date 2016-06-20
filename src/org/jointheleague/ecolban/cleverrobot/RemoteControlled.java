package org.jointheleague.ecolban.cleverrobot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jointheleague.ecolban.rpirobot.IRobotAdapter;
import org.jointheleague.ecolban.rpirobot.IRobotInterface;
import org.jointheleague.ecolban.rpirobot.SimpleIRobot;

public class RemoteControlled extends IRobotAdapter {

    private static final String DRIVE_DIRECT_COMMAND = "^driveDirect\\(([+-]?\\d+),\\s*([+-]?\\d+)\\)$";
    private static final Pattern DRIVE_DIRECT_PATTERN = Pattern.compile(DRIVE_DIRECT_COMMAND);
    private static final String STOP_COMMAND = "Stop";

    public RemoteControlled(IRobotInterface delegate) {
        super(delegate);
        // TODO Auto-generated constructor stub

    }

    public static void main(String[] args) throws IOException {
        try {
            IRobotInterface base = new SimpleIRobot(false, true, false);
            RemoteControlled rob = new RemoteControlled(base);
            rob.run();
        } catch (InterruptedException | IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private void run() {
        try (ServerSocket serverSocket = new ServerSocket(3333);
                Socket clientSocket = serverSocket.accept();
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            System.out.println("Connection with remote controller established.");
            String line = null;
            boolean running = true;
            while (running && (line = in.readLine()) != null) {
                Matcher m = DRIVE_DIRECT_PATTERN.matcher(line);
                if (m.matches()) {
                    int leftSpeed = Integer.parseInt(m.group(1));
                    int rightSpeed = Integer.parseInt(m.group(2));
                    driveDirect(leftSpeed, rightSpeed);
                } else if (line.matches(STOP_COMMAND)) {
                    running = false;
                } else {
                    System.out.println("No match");
                }
            }

            stop();
            closeConnection();
        } catch (IOException e) {
        }

    }

}
