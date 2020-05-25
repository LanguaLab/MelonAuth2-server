package moe.langua.lab.minecraft.auth.v2.server.api.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import moe.langua.lab.minecraft.auth.v2.server.json.server.VerificationCodeDetail;
import moe.langua.lab.minecraft.auth.v2.server.util.Utils;
import moe.langua.lab.minecraft.auth.v2.server.util.Verification;
import moe.langua.lab.minecraft.auth.v2.server.util.VerificationCodeManager;

import java.net.InetAddress;

public class GetCodeHandler extends AbstractHandler {
    private final VerificationCodeManager verificationCodeManager;

    public GetCodeHandler(long limit, long periodInMilliseconds, HttpServer httpServer, String handlePath, VerificationCodeManager verificationCodeManager) {
        super(limit, periodInMilliseconds, httpServer, handlePath);
        this.verificationCodeManager = verificationCodeManager;
    }

    @Override
    public void process(HttpExchange httpExchange, InetAddress requestAddress) {
        getLimiter().add(requestAddress, 1);
        int verificationCode;
        try {
            verificationCode = Integer.parseInt(Utils.getLastChild(httpExchange.getRequestURI()));
        } catch (NumberFormatException e) {
            return;
        }
        if (!verificationCodeManager.hasVerification(verificationCode)) {
            return;
        } else if (verificationCodeManager.getVerification(verificationCode).isExpired()) {
            verificationCodeManager.removeVerification(verificationCode);
            return;
        }
        Verification verification = verificationCodeManager.getVerification(verificationCode);
        VerificationCodeDetail verificationCodeDetail = new VerificationCodeDetail(verification);
        Utils.server.writeJSONAndSend(httpExchange, 200, Utils.gson.toJson(verificationCodeDetail));
    }
}
