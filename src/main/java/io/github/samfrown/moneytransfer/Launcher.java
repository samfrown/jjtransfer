package io.github.samfrown.moneytransfer;

import io.github.samfrown.moneytransfer.dao.AccountsDao;
import io.github.samfrown.moneytransfer.dao.TransfersDao;
import io.github.samfrown.moneytransfer.rest.AccountResource;
import io.github.samfrown.moneytransfer.service.AccountService;
import io.github.samfrown.moneytransfer.service.TransferService;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Launcher {
    private static final Logger LOG = LoggerFactory.getLogger(Launcher.class);
    private static final int DEFAULT_HTTP_PORT = 8080;

    public static void main(String[] args) throws Exception {
        Server server = new Server(getPort());
        server.setHandler(createApplication());
        try {
            server.start();
            server.join();
        } catch (Exception e) {
            LOG.error("Error starting jetty server", e);
            server.stop();
        }
    }

    private static Handler createApplication() {
        // Services
        TransferService transferService = new TransferService(new TransfersDao());
        AccountService accountService = new AccountService(new AccountsDao());
        // REST API
        ResourceConfig resourceConfig = new ResourceConfig();
        resourceConfig.register(new AccountResource(accountService, transferService));
        resourceConfig.register(JacksonFeature.class);
        ServletHolder jerseyServlet = new ServletHolder(new ServletContainer(resourceConfig));
        ServletContextHandler contextHandler = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        contextHandler.addServlet(jerseyServlet, "/*");
        return contextHandler;
    }

    private static int getPort() {
        String portValue = System.getProperty("http.port", "8080");
        try {
            int port = Integer.parseInt(portValue);
            if (port > 0 && port < 65536) {
                return port;
            }
        } catch(NumberFormatException e) {
        }
        LOG.error("Invalid 'http.port' specified: {}. Use default: {}", portValue, DEFAULT_HTTP_PORT);
        return 8080;
    }
}
