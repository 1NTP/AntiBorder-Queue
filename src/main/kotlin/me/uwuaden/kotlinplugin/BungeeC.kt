package me.uwuaden.kotlinplugin

import com.google.common.io.ByteArrayDataInput
import com.google.common.io.ByteStreams
import me.uwuaden.kotlinplugin.Main.Companion.PlayerCount
import org.bukkit.entity.Player
import org.bukkit.plugin.messaging.PluginMessageListener

class BungeeC: PluginMessageListener {
    override fun onPluginMessageReceived(channel: String, player: Player, message: ByteArray) {
        if (channel != "BungeeCord") {
            return
        }
        val `in`: ByteArrayDataInput = ByteStreams.newDataInput(message)
        val subchannel: String = `in`.readUTF()
        if (subchannel.equals("PlayerCount")) {
            var server: String? = `in`.readUTF()
            val playerCount: Int = `in`.readInt()
            println(playerCount)
            PlayerCount = playerCount
        }
    }
}