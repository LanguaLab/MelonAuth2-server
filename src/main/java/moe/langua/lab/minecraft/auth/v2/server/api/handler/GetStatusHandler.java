package moe.langua.lab.minecraft.auth.v2.server.api.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import moe.langua.lab.minecraft.auth.v2.server.json.server.PlayerStatus;
import moe.langua.lab.minecraft.auth.v2.server.sql.DataSearcher;
import moe.langua.lab.minecraft.auth.v2.server.util.Utils;

import java.net.InetAddress;
import java.sql.SQLException;
import java.util.UUID;

public class GetStatusHandler extends AbstractHandler {
    private final DataSearcher dataSearcher;

    public GetStatusHandler(long limit, long periodInMilliseconds, HttpServer httpServer, String handlePath, DataSearcher dataSearcher) {
        super(limit, periodInMilliseconds, httpServer, handlePath);
        this.dataSearcher = dataSearcher;
    }

    @Override
    public void process(HttpExchange httpExchange, InetAddress requestAddress) {
        if (!httpExchange.getRequestHeaders().containsKey("Authorization")) {
            getLimiter().add(requestAddress, 1);
            httpExchange.getResponseHeaders().set("WWW-Authenticate", "Basic");
            Utils.server.returnNoContent(httpExchange, 401);
            return;
        } else {
            String[] pass = httpExchange.getRequestHeaders().getFirst("Authorization").split(" ");
            boolean passed = false;
            if (!(pass.length < 2)) {
                if (pass[0].equalsIgnoreCase("Basic")) {
                    if (Utils.passManager.verifySecret(pass[1], requestAddress)) {
                        passed = true;
                    } else if (Utils.passManager.verifyQueue(pass[1], requestAddress)) {
                        getLimiter().add(requestAddress, 1);
                        passed = true;
                    }
                }
            }
            if (!passed) {
                Utils.server.returnNoContent(httpExchange, 403);
                return;
            }
        }

        UUID uniqueID;
        try {
            uniqueID = UUID.fromString(Utils.getLastChild(httpExchange.getRequestURI()));
        } catch (IllegalArgumentException e) {
            Utils.server.errorReturn(httpExchange, 404, Utils.server.NOT_FOUND_ERROR);
            return;
        }

        PlayerStatus status;
        try {
            status = dataSearcher.getPlayerStatus(uniqueID);
        } catch (SQLException e) {
            Utils.logger.error(e.toString());
            Utils.server.errorReturn(httpExchange, 500, Utils.server.INTERNAL_ERROR);
            return;
        }
        Utils.server.writeJSONAndSend(httpExchange, 200, Utils.gson.toJson(status));
    }
}
