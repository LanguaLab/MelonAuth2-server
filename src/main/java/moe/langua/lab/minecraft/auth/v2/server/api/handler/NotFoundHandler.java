package moe.langua.lab.minecraft.auth.v2.server.api.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import moe.langua.lab.minecraft.auth.v2.server.util.AbstractHandler;


public class NotFoundHandler extends AbstractHandler {
    public NotFoundHandler(int limit, long periodInMilliseconds, HttpServer httpServer, String handlePath) {
        super(limit, periodInMilliseconds, httpServer, handlePath);
    }

    @Override
    public void process(HttpExchange httpExchange) {
        if(httpExchange.getRequestURI().getPath().equalsIgnoreCase("/")){

        }
    }
}
