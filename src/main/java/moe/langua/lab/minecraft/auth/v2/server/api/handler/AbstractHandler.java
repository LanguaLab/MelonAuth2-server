package moe.langua.lab.minecraft.auth.v2.server.api.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import moe.langua.lab.minecraft.auth.v2.server.api.Limiter;
import moe.langua.lab.minecraft.auth.v2.server.json.server.settngs.MainSettings;
import moe.langua.lab.minecraft.auth.v2.server.util.Utils;
import moe.langua.lab.utils.logger.utils.LogRecord;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;

public abstract class AbstractHandler implements HttpHandler {
    private static HashSet<String> cORSSet = new HashSet<>(MainSettings.instance.getCORSList());
    private final Limiter<InetAddress> limiter;
    private final String workerName = this.getClass().getName() + "-Receiver";

    public AbstractHandler(long limit, long periodInMilliseconds, HttpServer httpServer, String handlePath) {
        limiter = new Limiter<InetAddress>(limit, periodInMilliseconds, handlePath);
        httpServer.createContext(handlePath, this);
    }

    public Limiter<InetAddress> getLimiter() {
        return limiter;
    }

    public void process(HttpExchange httpExchange, InetAddress requestAddress) {

    }

    @Override
    public void handle(HttpExchange httpExchange) {
        new Thread(() -> {
            long startTime = System.nanoTime();
            if (!httpExchange.getRequestHeaders().containsKey("Proxy-Authorization")) {
                Utils.server.returnNoContent(httpExchange, 407);
                return;
            } else if (!httpExchange.getRequestHeaders().getFirst("Proxy-Authorization").equals(MainSettings.instance.getProxyKey())) {
                Utils.server.returnNoContent(httpExchange, 403);
                Utils.logger.log(LogRecord.Level.WARN, httpExchange.getRemoteAddress().toString() + " tried to " + httpExchange.getRequestMethod() + " " + httpExchange.getRequestURI().getPath() + " with a wrong proxy password(" + httpExchange.getRequestHeaders().getFirst("Proxy-Authorization") + ").");
                return;
            }
            if (httpExchange.getRequestHeaders().containsKey("Origin")) {
                String origin = Utils.removeSlashAtTheEnd(httpExchange.getRequestHeaders().getFirst("Origin"));
                if (cORSSet.contains(origin))
                    httpExchange.getResponseHeaders().set("Access-Control-Allow-Origin", origin);
            }
            InetAddress requestAddress;
            if (!httpExchange.getRequestHeaders().containsKey("X-Real-IP")) {
                Utils.server.returnNoContent(httpExchange, 403);
                Utils.logger.log(LogRecord.Level.WARN, httpExchange.getRemoteAddress().toString() + " tried to " + httpExchange.getRequestMethod() + " " + httpExchange.getRequestURI().getPath() + " with a bad request (No X-Real-IP Header).");
                return;
            }
            try {
                requestAddress = InetAddress.getByName(httpExchange.getRequestHeaders().getFirst("X-Real-IP"));
            } catch (UnknownHostException e) {
                Utils.logger.log(LogRecord.Level.ERROR, e.toString());
                Utils.logger.log(LogRecord.Level.ERROR, "It may caused by inappropriate reverse proxy configurations, please see 'url of reverse proxy configuration manual here' and reconfiguration your reverse proxy server.");
                Utils.server.returnNoContent(httpExchange, 403);
                return;
            }
            if (!getLimiter().getUsability(requestAddress)) {
                Utils.server.errorReturn(httpExchange, 429, Utils.server.TOO_MANY_REQUEST_ERROR.setExtra("" + (getLimiter().getNextReset() - System.currentTimeMillis())));
                getLimiter().add(requestAddress, 1);
                return;
            }
            process:
            {
                if (!httpExchange.getRequestMethod().equalsIgnoreCase("GET")) {
                    Utils.server.returnNoContent(httpExchange, 405);
                    break process;
                }
                process(httpExchange, requestAddress);
                if (httpExchange.getResponseCode() == -1)
                    Utils.server.errorReturn(httpExchange, 404, Utils.server.NOT_FOUND_ERROR);
            }
            float workTime = (System.nanoTime() - startTime) / 1000000F;
            Utils.logger.log(((
                            httpExchange.getResponseCode() / 100 == 2) || httpExchange.getResponseCode() == 429) && workTime < 1000
                            ? LogRecord.Level.FINE : LogRecord.Level.WARN/* FINE if response code is (2xx OR 429) AND workTime is less than 1000ms, WARN if others.*/,
                    requestAddress.toString() + " " + httpExchange.getRequestMethod() + " " + httpExchange.getRequestURI().getPath() + " " + httpExchange.getResponseCode() + " " + workTime + "ms");
        }, workerName).start();
    }

}
