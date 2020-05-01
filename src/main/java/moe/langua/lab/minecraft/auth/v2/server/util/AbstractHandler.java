package moe.langua.lab.minecraft.auth.v2.server.util;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import moe.langua.lab.minecraft.auth.v2.server.api.Limiter;

public abstract class AbstractHandler implements HttpHandler {
    private final Limiter limiter;
    private final String handlePath;

    public AbstractHandler(int limit, long periodInMilliseconds, HttpServer httpServer, String handlePath) {
        this.handlePath = handlePath;
        limiter = new Limiter(limit, periodInMilliseconds);
        httpServer.createContext(handlePath, this);
    }

    public Limiter getLimiter() {
        return limiter;
    }

    public void process(HttpExchange httpExchange) {
    }

    @Override
    public void handle(HttpExchange httpExchange) {
        new Thread(() -> process(httpExchange)).start();
    }

}
