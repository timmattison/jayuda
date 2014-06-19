package com.timmattison.jayuda.jarloader.logging;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.*;

/**
 * Created by timmattison on 6/19/14.
 */
public class Sledgehammer {
    public static void forceConsoleLogging() {
        // Get the root logger instance
        LogManager logManager = LogManager.getLogManager();
        Logger rootLogger = logManager.getLogger("");

        // Set the default logging level to all
        rootLogger.setLevel(Level.ALL);

        // Loop and see if a console handler is already installed
        boolean consoleHandlerInstalled = false;

        for (Handler handler : rootLogger.getHandlers()) {
            if (handler instanceof ConsoleHandler) {
                consoleHandlerInstalled = true;
                break;
            }
        }

        // Is a console handler already installed?
        if (consoleHandlerInstalled) {
            // Yes, do nothing
            return;
        }

        // No console handler installed, install one
        rootLogger.addHandler(new ConsoleHandler());
    }

    public static void logEverything() {
        // Get the logger instance
        LogManager logManager = LogManager.getLogManager();
        Logger rootLogger = logManager.getLogger("");

        // Set the default logging level to all
        rootLogger.setLevel(Level.ALL);

        // Loop and see if any console handlers are already installed
        List<ConsoleHandler> consoleHandlers = new ArrayList<ConsoleHandler>();

        for (Handler handler : rootLogger.getHandlers()) {
            if (handler instanceof ConsoleHandler) {
                consoleHandlers.add((ConsoleHandler) handler);
            }
        }

        // Is a console handler already installed?
        if (consoleHandlers.size() == 0) {
            // No, create one.  Add it to the list and to the root logger.
            Handler consoleHandler = new ConsoleHandler();
            consoleHandlers.add((ConsoleHandler) consoleHandler);
            rootLogger.addHandler(consoleHandler);
        }

        // Loop through all console handlers and make them log everything
        for (ConsoleHandler consoleHandler : consoleHandlers) {
            consoleHandler.setLevel(Level.ALL);
        }
    }
}
