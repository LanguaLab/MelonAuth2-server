package moe.langua.lab.minecraft.auth.v2.server.api.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import moe.langua.lab.minecraft.auth.v2.server.json.server.VerificationCodeDetail;
import moe.langua.lab.minecraft.auth.v2.server.util.AbstractHandler;
import moe.langua.lab.minecraft.auth.v2.server.util.Utils;
import moe.langua.lab.minecraft.auth.v2.server.util.Verification;
import moe.langua.lab.minecraft.auth.v2.server.util.VerificationCodeManager;
import moe.langua.lab.utils.logger.utils.LogRecord;

import static moe.langua.lab.minecraft.auth.v2.server.util.Utils.server.NOT_FOUND_ERROR;
import static moe.langua.lab.minecraft.auth.v2.server.util.Utils.server.errorReturn;

public class GetVerificationCodeDetailHandler extends AbstractHandler {
    private final VerificationCodeManager verificationCodeManager;

    public GetVerificationCodeDetailHandler(int limit, long periodInMilliseconds, HttpServer httpServer, String handlePath, VerificationCodeManager verificationCodeManager) {
        super(limit, periodInMilliseconds, httpServer, handlePath);
        this.verificationCodeManager = verificationCodeManager;
    }

    @Override
    public void process(HttpExchange httpExchange) {
        super.process(httpExchange);
        if(httpExchange.getResponseCode()!=-1) return;
        int verificationCode;
        try {
            verificationCode = Integer.parseInt(Utils.getLastChild(httpExchange.getRequestURI()));
        } catch (NumberFormatException e) {
            errorReturn(httpExchange, 404, NOT_FOUND_ERROR);
            return;
        }
        if (!verificationCodeManager.hasVerification(verificationCode) || verificationCodeManager.getVerification(verificationCode).isExpired()) {
            errorReturn(httpExchange, 404, NOT_FOUND_ERROR);
            return;
        }
        Verification verification = verificationCodeManager.getVerification(verificationCode);
        VerificationCodeDetail verificationCodeDetail = new VerificationCodeDetail(verification);
        Utils.server.writeJSONAndSend(httpExchange, 200, Utils.gson.toJson(verificationCodeDetail));
        Utils.logger.log(LogRecord.Level.FINE, "Sending detail of verification code " + verificationCode);
    }
}
