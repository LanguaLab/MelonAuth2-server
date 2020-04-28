package moe.langua.lab.minecraft.auth.v2.server.util;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import moe.langua.lab.minecraft.auth.v2.server.api.Limiter;

public abstract class AbstractHandler implements HttpHandler {
    protected Limiter limiter;

    public AbstractHandler(int limit, long periodInMilliseconds) {
        limiter = new Limiter(limit, periodInMilliseconds);
    }

    public AbstractHandler() {

    }

    @Override
    public void handle(HttpExchange httpExchange) {
        new Thread(() -> process(httpExchange)).start();
    }

    public void process(HttpExchange httpExchange) {
    }
}
