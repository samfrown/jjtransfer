package io.github.samfrown.moneytransfer;

import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Launcher {
    private static final Logger LOG = LoggerFactory.getLogger(Launcher.class);
    private static final int DEFAULT_HTTP_PORT = 8080;
    private static final int DEFAULT_CHANNEL_MAX_SIZE = 100;

    public static void main(String[] args) throws Exception {
        Server server = new Server(getPort());
        TransferApplication app = new TransferApplication(DEFAULT_CHANNEL_MAX_SIZE);
        server.setHandler(app);
        try {
            server.start();
            server.join();
        } catch (Exception e) {
            LOG.error("Error starting jetty server", e);
            server.stop();
        }
    }

    private static int getPort() {
        String portValue = System.getProperty("http.port", "8080");
        try {
            int port = Integer.parseInt(portValue);
            if (port > 0 && port < 65536) {
                return port;
            }
        } catch (NumberFormatException e) {
        }
        LOG.error("Invalid 'http.port' specified: {}. Use default: {}", portValue, DEFAULT_HTTP_PORT);
        return 8080;
    }
}
