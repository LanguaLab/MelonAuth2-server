package moe.langua.lab.minecraft.auth.v2.server.api.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import moe.langua.lab.minecraft.auth.v2.server.json.server.PlayerStatus;
import moe.langua.lab.minecraft.auth.v2.server.json.server.settngs.MainSettings;
import moe.langua.lab.minecraft.auth.v2.server.sql.DataSearcher;
import moe.langua.lab.minecraft.auth.v2.server.util.Utils;
import moe.langua.lab.utils.logger.utils.LogRecord;

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
        getLimiter().add(requestAddress, 1);
        if (!httpExchange.getRequestHeaders().containsKey("Authorization")) {
            httpExchange.getResponseHeaders().set("WWW-Authenticate", "Password required");
            Utils.server.returnNoContent(httpExchange, 401);
        } else {
            if (!httpExchange.getRequestHeaders().getFirst("Authorization").equals(MainSettings.instance.getQueueKey())) {
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

        if (uniqueID.getLeastSignificantBits() == 0 && uniqueID.getMostSignificantBits() == 0) {
            Utils.server.returnNoContent(httpExchange, 204);
            return;
        }

        boolean passed;
        try {
            passed = dataSearcher.getPlayerStatus(uniqueID);
        } catch (SQLException e) {
            Utils.logger.log(LogRecord.Level.ERROR, e.toString());
            Utils.server.errorReturn(httpExchange, 500, Utils.server.INTERNAL_ERROR);
            return;
        }

        PlayerStatus status = PlayerStatus.get(uniqueID, passed);
        Utils.server.writeJSONAndSend(httpExchange, 200, Utils.gson.toJson(status));
    }
}
