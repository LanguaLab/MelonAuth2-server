package moe.langua.lab.minecraft.auth.v2.server.api.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import moe.langua.lab.minecraft.auth.v2.server.json.server.Config;
import moe.langua.lab.minecraft.auth.v2.server.json.server.Overview;
import moe.langua.lab.minecraft.auth.v2.server.util.Utils;

import java.net.InetAddress;


public class NotFoundHandler extends AbstractHandler {
    private final String status;

    public NotFoundHandler(int limit, long periodInMilliseconds, HttpServer httpServer, String handlePath) {
        super(limit, periodInMilliseconds, httpServer, handlePath);
        Overview overview = Overview.getDefault();
        overview.description = Config.instance.getApplicationDescription();
        overview.applicationOwner = Config.instance.getApplicationOwner();
        status = Utils.gson.toJson(overview);
    }

    @Override
    public void process(HttpExchange httpExchange, InetAddress requestAddress) {
        if (httpExchange.getRequestURI().getPath().equalsIgnoreCase("/")) {
            Utils.server.writeJSONAndSend(httpExchange, 200, status);
        }
    }
}
