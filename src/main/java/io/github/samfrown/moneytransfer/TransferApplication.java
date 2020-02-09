package io.github.samfrown.moneytransfer;

import io.github.samfrown.moneytransfer.dao.AccountsDao;
import io.github.samfrown.moneytransfer.dao.TransfersDao;
import io.github.samfrown.moneytransfer.processing.TransferProcessing;
import io.github.samfrown.moneytransfer.rest.AccountsResource;
import io.github.samfrown.moneytransfer.rest.TransfersResource;
import io.github.samfrown.moneytransfer.service.AccountService;
import io.github.samfrown.moneytransfer.service.TransferService;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TransferApplication extends ServletContextHandler {

    private final Thread transferProcessingEngine;

    public TransferApplication(int transferChannelSize) {
        super(ServletContextHandler.NO_SESSIONS);
        // Channel
        BlockingQueue<UUID> transferChannel = new LinkedBlockingQueue<>(transferChannelSize);
        TransferService transferService = new TransferService(new TransfersDao(), transferChannel);
        AccountService accountService = new AccountService(new AccountsDao());
        // REST API
        ResourceConfig resourceConfig = new ResourceConfig();
        resourceConfig.register(new AccountsResource(accountService, transferService));
        resourceConfig.register(new TransfersResource(transferService));
        resourceConfig.register(JacksonFeature.class);
        // Servlet
        ServletHolder jerseyServlet = new ServletHolder(new ServletContainer(resourceConfig));
        addServlet(jerseyServlet, "/*");

        // Engine
        transferProcessingEngine = new Thread(new TransferProcessing(accountService, transferService, transferChannel));
        transferProcessingEngine.start();
    }

    @Override
    public void destroy() {
        transferProcessingEngine.interrupt();
        super.destroy();
    }
}
