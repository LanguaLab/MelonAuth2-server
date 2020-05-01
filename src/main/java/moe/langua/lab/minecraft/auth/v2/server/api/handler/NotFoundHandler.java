package moe.langua.lab.minecraft.auth.v2.server.api.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import moe.langua.lab.minecraft.auth.v2.server.util.AbstractHandler;
import moe.langua.lab.minecraft.auth.v2.server.util.Utils;


public class NotFoundHandler extends AbstractHandler {
    public NotFoundHandler(int limit, long periodInMilliseconds, HttpServer httpServer, String handlePath) {
        super(limit, periodInMilliseconds, httpServer, handlePath);
    }

    @Override
    public void process(HttpExchange httpExchange) {
        Utils.server.errorReturn(httpExchange, 404, Utils.server.NOT_FOUND_ERROR);
    }
}
