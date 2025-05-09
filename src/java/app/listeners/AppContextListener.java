package app.listeners;

import db.DBUtil;
import db.DatabaseInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener; // Import this annotation

@WebListener // This annotation registers the listener with the container
public class AppContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("**********************************************");
        System.out.println("TechTroveWebApp: Application Context Initializing...");
        System.out.println("**********************************************");

        ServletContext context = sce.getServletContext();

        // In a real application, you might load these from a config file or environment variables
        // For now, we'll rely on a setup page to configure DBUtil
        // However, for initial schema creation to work if already configured (e.g. on redeploy),
        // we should check DBUtil.isConfigured()

        // For this phase, we assume DBUtil will be configured by an initial setup page.
        // The DatabaseInitializer will check if DBUtil.isConfigured() before proceeding.

        // We will create a servlet/JSP to handle the initial DB configuration.
        // For now, we just print a message. If DBUtil is already configured (e.g. from a previous run
        // and you haven't restarted the server or if you manually set params in DBUtil for testing),
        // then initializeDatabaseSchema() might work.

        if (!DBUtil.isConfigured()) {
            System.out.println("DBUtil is not yet configured. Database schema initialization will be skipped until configuration is provided.");
            // Store a flag in servlet context so JSP/Servlets know to show setup page
            context.setAttribute("dbConfigNeeded", true);
        } else {
            System.out.println("DBUtil is configured. Attempting to initialize database schema...");
            try {
                DatabaseInitializer.initializeDatabaseSchema();
                System.out.println("Database schema initialization process completed.");
                context.setAttribute("dbConfigNeeded", false);
                 // Upon successful initialization, you might want to set an attribute
                context.setAttribute("dbInitialized", true);
            } catch (Exception e) {
                System.err.println("FATAL ERROR: Database schema initialization FAILED during context startup.");
                e.printStackTrace();
                // Mark that initialization failed but configuration was attempted
                context.setAttribute("dbConfigNeeded", false); // It was configured, but init failed
                context.setAttribute("dbInitializationFailed", true);
                context.setAttribute("dbInitializationError", e.getMessage());
            }
        }
        System.out.println("**********************************************");
        System.out.println("TechTroveWebApp: Application Context Initialized.");
        System.out.println("**********************************************");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("**********************************************");
        System.out.println("TechTroveWebApp: Application Context Destroying...");
        System.out.println("**********************************************");
        // Add cleanup tasks here if needed, e.g., closing a connection pool
        // For simple DriverManager connections, there's not much to do here globally.
    }
}