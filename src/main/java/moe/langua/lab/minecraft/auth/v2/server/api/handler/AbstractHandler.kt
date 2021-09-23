package moe.langua.lab.minecraft.auth.v2.server.api.handler

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import moe.langua.lab.minecraft.auth.v2.server.api.Limiter
import moe.langua.lab.minecraft.auth.v2.server.json.server.settngs.MainSettings
import moe.langua.lab.minecraft.auth.v2.server.util.HandlerThreadFactory
import moe.langua.lab.minecraft.auth.v2.server.util.Utils
import org.apache.logging.log4j.Level
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.concurrent.Executors

abstract class AbstractHandler(limit: Long, periodInMilliseconds: Long, httpServer: HttpServer, handlePath: String?) :
    HttpHandler {
    protected val limiter: Limiter<InetAddress>
    abstract fun process(httpExchange: HttpExchange, requestAddress: InetAddress)
    override fun handle(httpExchange: HttpExchange) {
        threadPool.submit {
            val startTime = System.nanoTime()
            if (httpExchange.requestHeaders.containsKey("Origin")) {
                //filling the CORS header
                val origin = Utils.removeSlashAtTheEnd(httpExchange.requestHeaders.getFirst("Origin"))
                if (cORSSet.contains(origin)) httpExchange.responseHeaders["Access-Control-Allow-Origin"] = origin
            }
            if (!httpExchange.requestHeaders.containsKey("Proxy-Authorization")) {
                Utils.server.returnNoContent(httpExchange, 407)
            } else if (httpExchange.requestHeaders.getFirst("Proxy-Authorization") != MainSettings.instance.proxyKey) {
                Utils.server.returnNoContent(httpExchange, 403)
                Utils.logger.warn(
                    httpExchange.remoteAddress.toString() + " tried to " + httpExchange.requestMethod + " " + httpExchange.requestURI.path + " with a wrong proxy password(" + httpExchange.requestHeaders.getFirst(
                        "Proxy-Authorization"
                    ) + ")."
                )
            } else {
                //proxy key authorized
                if (httpExchange.requestHeaders.containsKey("X-Forwarded-For") && httpExchange.requestHeaders.containsKey(
                        "X-Forwarded-Host"
                    ) && httpExchange.requestHeaders.containsKey("X-Forwarded-Proto")
                ) {
                    // X-Forwarded headers are correct
                    val requestAddress: InetAddress = try {
                        InetAddress.getByName(httpExchange.requestHeaders.getFirst("X-Forwarded-For"))
                    } catch (e: UnknownHostException) {
                        Utils.logger.error(e.toString())
                        Utils.logger.error("It may caused by inappropriate reverse proxy configurations, please see 'url of reverse proxy configuration manual here' and reconfiguration your reverse proxy server.")
                        Utils.server.returnNoContent(httpExchange, 403)
                        return@submit
                    }
                    if (limiter.isUseable(requestAddress)) {
                        if (httpExchange.requestMethod.equals("GET", ignoreCase = true)) {
                            process(httpExchange, requestAddress)
                            if (httpExchange.responseCode == -1) /* default response 404 if request is not processed */ Utils.server.errorReturn(
                                httpExchange,
                                404,
                                Utils.server.NOT_FOUND_ERROR
                            )
                        } else {
                            Utils.server.returnNoContent(httpExchange, 405)
                        }
                    } else {
                        limiter.add(requestAddress, 1)
                        val retryAfterInMilliSeconds = limiter.nextReset - System.currentTimeMillis()
                        httpExchange.responseHeaders.add(
                            "Retry-After",
                            (retryAfterInMilliSeconds / 1000 + 1).toString()
                        )
                        Utils.server.errorReturn(
                            httpExchange,
                            429,
                            Utils.server.TOO_MANY_REQUEST_ERROR.setExtra("" + retryAfterInMilliSeconds)
                        )
                    }
                    val workTime = (System.nanoTime() - startTime) / 1000000f
                    Utils.logger.log(
                        if ((httpExchange.responseCode / 100 == 2 || httpExchange.responseCode == 429) && workTime < 1000) Level.INFO else Level.WARN /* FINE if response code is (2xx OR 429) AND workTime is less than 1000ms, WARN if others.*/,
                        requestAddress.hostAddress + " " + httpExchange.requestMethod + " " + httpExchange.requestURI.path + " " + httpExchange.responseCode + " " + workTime + "ms"
                    )
                } else {
                    Utils.server.returnNoContent(httpExchange, 403)
                    Utils.logger.warn(httpExchange.remoteAddress.toString() + " tried to " + httpExchange.requestMethod + " " + httpExchange.requestURI.path + " with a bad request (Insufficient X-Forwarded Headers).")
                }
            }
        }
    }

    companion object {
        private val cORSSet = HashSet(MainSettings.instance.corsList)
        private val threadPool =
            Executors.newFixedThreadPool(MainSettings.instance.workerThreads, HandlerThreadFactory())
    }

    init {
        limiter = Limiter(limit, periodInMilliseconds, handlePath)
        registerToHTTPServer(httpServer, handlePath)
    }

    private fun registerToHTTPServer(httpServer: HttpServer, handlePath: String?) {
        httpServer.createContext(handlePath, this)
    }
}