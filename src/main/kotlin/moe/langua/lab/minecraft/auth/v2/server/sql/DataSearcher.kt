package moe.langua.lab.minecraft.auth.v2.server.sql

import moe.langua.lab.minecraft.auth.v2.server.json.server.PlayerStatus
import java.net.InetAddress
import java.sql.SQLException
import java.util.*

interface DataSearcher {
    @Throws(SQLException::class)
    fun getPlayerStatus(uniqueID: UUID): PlayerStatus?

    @Throws(SQLException::class)
    fun setPlayerStatus(uniqueID: UUID, status: Boolean, commitAddress: InetAddress?)
}