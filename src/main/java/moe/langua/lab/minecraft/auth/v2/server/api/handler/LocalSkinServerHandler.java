package moe.langua.lab.minecraft.auth.v2.server.api.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import moe.langua.lab.minecraft.auth.v2.server.util.AbstractHandler;
import moe.langua.lab.minecraft.auth.v2.server.util.Utils;
import moe.langua.lab.utils.logger.utils.LogRecord;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class LocalSkinServerHandler extends AbstractHandler {
    private final File dataRoot;

    public LocalSkinServerHandler(int limit, long periodInMilliseconds, HttpServer httpServer, String handlePath, File dataRoot) {
        super(limit, periodInMilliseconds, httpServer, handlePath);
        this.dataRoot = dataRoot;
    }

    @Override
    public void process(HttpExchange httpExchange) {
        super.process(httpExchange);
        if (httpExchange.getResponseCode() != -1) return;

        File fileToGet = new File(dataRoot, Utils.getLastChild(httpExchange.getRequestURI()));
        if (!fileToGet.exists()) {
            Utils.server.errorReturn(httpExchange, 404, Utils.server.NOT_FOUND_ERROR);
            return;
        }
        byte[] bytes = new byte[(int) fileToGet.length()];

        try {
            FileInputStream fileInputStream = new FileInputStream(fileToGet);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
            bufferedInputStream.read(bytes, 0, bytes.length);
        } catch (IOException e) {
            Utils.server.errorReturn(httpExchange, 500, Utils.server.INTERNAL_ERROR);
            Utils.logger.log(LogRecord.Level.WARN, e.toString());
            return;
        }
        Utils.server.writeAndSend(httpExchange, 200, "image/png", bytes, fileToGet.length());
    }
}
