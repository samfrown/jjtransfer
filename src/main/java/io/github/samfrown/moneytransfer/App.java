package io.github.samfrown.moneytransfer;

import io.github.samfrown.moneytransfer.dao.AccountsDao;
import io.github.samfrown.moneytransfer.rest.AccountResource;
import io.github.samfrown.moneytransfer.service.AccountService;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

public class App {
    public static void main(String[] args) throws Exception {

        Server server = new Server(8080);
        ResourceConfig resourceConfig = new ResourceConfig();
        resourceConfig.register(new AccountResource(new AccountService(new AccountsDao())));
        resourceConfig.register(JacksonFeature.class);
        ServletContextHandler ctx = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        ctx.setContextPath("/");
        server.setHandler(ctx);
        ServletHolder jerseyServlet = new ServletHolder(new ServletContainer(resourceConfig));
        ctx.addServlet(jerseyServlet, "/*");

        server.start();
        server.join();
    }
}
