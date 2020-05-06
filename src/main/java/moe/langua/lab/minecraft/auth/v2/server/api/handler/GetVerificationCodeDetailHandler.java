package moe.langua.lab.minecraft.auth.v2.server.api.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import moe.langua.lab.minecraft.auth.v2.server.json.server.VerificationCodeDetail;
import moe.langua.lab.minecraft.auth.v2.server.util.Utils;
import moe.langua.lab.minecraft.auth.v2.server.util.Verification;
import moe.langua.lab.minecraft.auth.v2.server.util.VerificationCodeManager;

public class GetVerificationCodeDetailHandler extends AbstractHandler {
    private final VerificationCodeManager verificationCodeManager;

    public GetVerificationCodeDetailHandler(int limit, long periodInMilliseconds, HttpServer httpServer, String handlePath, VerificationCodeManager verificationCodeManager) {
        super(limit, periodInMilliseconds, httpServer, handlePath);
        this.verificationCodeManager = verificationCodeManager;
    }

    @Override
    public void process(HttpExchange httpExchange) {
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
