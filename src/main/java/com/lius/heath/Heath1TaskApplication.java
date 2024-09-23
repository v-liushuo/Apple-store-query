package com.lius.heath;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.awt.*;

@SpringBootApplication
public class Heath1TaskApplication {

    public static void main(String[] args) {
        SpringApplication.run(Heath1TaskApplication.class, args);
    }

    private static final SystemTray tray = SystemTray.getSystemTray();

    public static final Image image = Toolkit.getDefaultToolkit().createImage("icon.png");
    public static final TrayIcon trayIcon = new TrayIcon(image, "Apple Tray");

    public static void displayTray(String location) throws AWTException {
        //If the icon is a file
        //Alternative (if the icon is on the classpath):
        //Image image = Toolkit.getDefaultToolkit().createImage(getClass().getResource("icon.png"));

        //Let the system resize the image if needed
        trayIcon.setImageAutoSize(true);
        //Set tooltip text for the tray icon
        trayIcon.setToolTip("System tray icon demo");
        TrayIcon[] trayIcons = tray.getTrayIcons();
        if (trayIcons.length == 0) {
            tray.add(trayIcon);
        }

        trayIcon.displayMessage("Apple is available", location, TrayIcon.MessageType.INFO);
    }
}
