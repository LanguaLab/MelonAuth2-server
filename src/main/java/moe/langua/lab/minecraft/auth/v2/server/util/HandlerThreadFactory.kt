package moe.langua.lab.minecraft.auth.v2.server.util

import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

class HandlerThreadFactory : ThreadFactory {
    companion object {
        private val atomInt = AtomicInteger(0)
    }

    override fun newThread(runnable: Runnable): Thread {
        val thread = Thread(runnable)
        thread.name = "Request Handler #${atomInt.getAndAdd(1)}"
        return thread
    }
}