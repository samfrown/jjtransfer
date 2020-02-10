package io.github.samfrown.moneytransfer;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Launcher {
    private static final Logger LOG = LoggerFactory.getLogger(Launcher.class);
    private static final int DEFAULT_HTTP_PORT = 8080;
    private static final int DEFAULT_THREAD_POOL_SIZE = 10;
    private static final int DEFAULT_CHANNEL_MAX_SIZE = 1000;
    public static final String HTTP_PORT_PROPERTY = "http.port";
    public static final String THREAD_POOL_SIZE_PROPERTY = "thread.pool.size";

    public static void main(String[] args) throws Exception {
        QueuedThreadPool threadPool = new QueuedThreadPool(getThreadPoolSize());
        Server server = new Server(threadPool);
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(getPort());
        server.setConnectors(new Connector[] {connector});
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
        String portValue = System.getProperty(HTTP_PORT_PROPERTY, String.valueOf(DEFAULT_HTTP_PORT));
        try {
            int port = Integer.parseInt(portValue);
            if (port > 0 && port < 65536) {
                return port;
            }
        } catch (NumberFormatException e) {
        }
        LOG.error("Invalid '{}' specified: {}. Use default: {}", HTTP_PORT_PROPERTY, portValue, DEFAULT_HTTP_PORT);
        return DEFAULT_HTTP_PORT;
    }

    private static int getThreadPoolSize() {
        String poolSizeValue = System.getProperty(THREAD_POOL_SIZE_PROPERTY, String.valueOf(DEFAULT_THREAD_POOL_SIZE));
        try {
            int poolSize = Integer.parseInt(poolSizeValue);
            if (poolSize > 1 && poolSize < 1000) {
                return poolSize;
            }
        } catch (NumberFormatException e) {
        }
        LOG.error("Invalid '{}' specified: {}. Use default: {}", THREAD_POOL_SIZE_PROPERTY, poolSizeValue, DEFAULT_THREAD_POOL_SIZE);
        return DEFAULT_THREAD_POOL_SIZE;
    }
}
