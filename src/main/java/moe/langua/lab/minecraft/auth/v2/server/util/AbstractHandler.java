package moe.langua.lab.minecraft.auth.v2.server.util;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import moe.langua.lab.minecraft.auth.v2.server.api.Limiter;
import moe.langua.lab.utils.logger.utils.LogRecord;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

public abstract class AbstractHandler implements HttpHandler {
    private final Limiter limiter;
    private final String workerName = this.getClass().getName()+"-Receiver";

    public AbstractHandler(int limit, long periodInMilliseconds, HttpServer httpServer, String handlePath) {
        limiter = new Limiter(limit, periodInMilliseconds,handlePath);
        httpServer.createContext(handlePath, this);
    }

    public Limiter getLimiter() {
        return limiter;
    }

    public void process(HttpExchange httpExchange) {
        String x_real_ip_header = httpExchange.getRequestHeaders().getFirst("X-Real-IP");
        InetAddress address;
        try {
            address = x_real_ip_header == null ? httpExchange.getRemoteAddress().getAddress():InetAddress.getByName(x_real_ip_header);
        } catch (UnknownHostException e) {
            Utils.logger.log(LogRecord.Level.ERROR,e.toString());
            Utils.logger.log(LogRecord.Level.ERROR,"It may caused by inappropriate reverse proxy configurations, please see 'url of reverse proxy configuration manual here' and reconfiguration your reverse proxy server.");
            return;
        }
        if (!getLimiter().getUsabilityAndAdd1(address)) {
            Utils.server.errorReturn(httpExchange, 429, Utils.server.TOO_MANY_REQUEST_ERROR.clone().setExtra("" + (getLimiter().getNextReset() - System.currentTimeMillis())));
        }
    }

    @Override
    public void handle(HttpExchange httpExchange) {
        new Thread(() -> process(httpExchange),workerName).start();
    }

}
