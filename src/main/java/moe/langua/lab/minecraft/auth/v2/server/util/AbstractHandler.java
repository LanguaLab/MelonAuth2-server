package moe.langua.lab.minecraft.auth.v2.server.util;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import moe.langua.lab.minecraft.auth.v2.server.api.Limiter;
import moe.langua.lab.utils.logger.utils.LogRecord;

import java.net.InetAddress;
import java.net.UnknownHostException;

public abstract class AbstractHandler implements HttpHandler {
    private final Limiter limiter;
    private final String workerName = this.getClass().getName() + "-Receiver";

    public AbstractHandler(int limit, long periodInMilliseconds, HttpServer httpServer, String handlePath) {
        limiter = new Limiter(limit, periodInMilliseconds, handlePath);
        httpServer.createContext(handlePath, this);
    }

    public Limiter getLimiter() {
        return limiter;
    }

    public void process(HttpExchange httpExchange) {

    }

    int counter = 0;
    @Override
    public void handle(HttpExchange httpExchange) {
        new Thread(() -> {
            long startTime = System.nanoTime();
            if(!httpExchange.getRequestHeaders().containsKey("X-Real-IP")){
                Utils.server.errorReturn(httpExchange,400, Utils.server.BAD_REQUEST);
                Utils.logger.log(LogRecord.Level.WARN,httpExchange.getRemoteAddress().toString()+" tried to GET "+httpExchange.getRequestURI().getPath()+" with a bad request (No X-Real-IP Header).");
                return;
            }
            InetAddress requestAddress;
            try {
                requestAddress = InetAddress.getByName(httpExchange.getRequestHeaders().getFirst("X-Real-IP"));
            } catch (UnknownHostException e) {
                Utils.logger.log(LogRecord.Level.ERROR, e.toString());
                Utils.logger.log(LogRecord.Level.ERROR, "It may caused by inappropriate reverse proxy configurations, please see 'url of reverse proxy configuration manual here' and reconfiguration your reverse proxy server.");
                return;
            }
            if (!getLimiter().getUsabilityAndAdd1(requestAddress)) {
                Utils.server.errorReturn(httpExchange, 429, Utils.server.TOO_MANY_REQUEST_ERROR.clone().setExtra("" + (getLimiter().getNextReset() - System.currentTimeMillis())));
            }else{
                process(httpExchange);
                if(httpExchange.getResponseCode()==-1) Utils.server.errorReturn(httpExchange,404, Utils.server.NOT_FOUND_ERROR);
            }
            Utils.logger.log(LogRecord.Level.FINE,requestAddress.toString()+" GET "+httpExchange.getRequestURI().getPath()+" "+httpExchange.getResponseCode()+" "+((System.nanoTime()-startTime)/1000000D)+"ms");
        }, workerName).start();
    }

}
