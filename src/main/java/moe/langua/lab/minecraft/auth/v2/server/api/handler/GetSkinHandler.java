package moe.langua.lab.minecraft.auth.v2.server.api.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import moe.langua.lab.minecraft.auth.v2.server.util.Utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;

public class GetSkinHandler extends AbstractHandler {
    private final File dataRoot;

    public GetSkinHandler(long limit, long periodInMilliseconds, HttpServer httpServer, String handlePath, File dataRoot) {
        super(limit, periodInMilliseconds, httpServer, handlePath);
        this.dataRoot = dataRoot;
    }

    @Override
    public void process(HttpExchange httpExchange, InetAddress requestAddress) {
        getLimiter().add(requestAddress, 1);
        File fileToGet = new File(dataRoot, Utils.getLastChild(httpExchange.getRequestURI()));
        if (!fileToGet.exists()) return;

        byte[] bytes = new byte[(int) fileToGet.length()]; //Suitable for small file smaller than about 2GB
        try {
            FileInputStream fileInputStream = new FileInputStream(fileToGet);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
            bufferedInputStream.read(bytes, 0, bytes.length);
        } catch (IOException e) {
            Utils.server.errorReturn(httpExchange, 500, Utils.server.INTERNAL_ERROR);
            Utils.logger.warn(e.toString());
            return;
        }
        Utils.server.writeAndSend(httpExchange, 200, "image/png", bytes, bytes.length);
    }
}
