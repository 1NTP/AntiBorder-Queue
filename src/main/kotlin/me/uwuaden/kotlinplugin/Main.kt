package me.uwuaden.kotlinplugin

import com.google.common.io.ByteStreams
import io.github.monun.kommand.getValue
import io.github.monun.kommand.kommand
import me.uwuaden.kotlinplugin.Events.Companion.PlayerQueue
import me.uwuaden.kotlinplugin.Main.Companion.PlayerCount
import me.uwuaden.kotlinplugin.Main.Companion.QueueSize
import me.uwuaden.kotlinplugin.Main.Companion.plugin
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.util.*


fun getPlayerCount(player: Player, server: String) {
    val out = ByteStreams.newDataOutput()
    out.writeUTF("PlayerCount")
    out.writeUTF(server)
    player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray())
}
fun enoughServerPlayers(): Boolean {
    val currentPlayers = plugin.server.onlinePlayers.size
    val allPlayers = PlayerCount
    val orgServerPlayers = allPlayers - currentPlayers
    return QueueSize > orgServerPlayers
}

fun sortQueue() {
    val NewQueue = ArrayList<UUID>()
    PlayerQueue.removeIf { plugin.server.getPlayer(it) == null }
    PlayerQueue.forEach {
        NewQueue.add(it)
    }
    PlayerQueue = NewQueue
}

class Main: JavaPlugin() {
    companion object {
        lateinit var plugin: JavaPlugin
            private set
        val Scheduler = Bukkit.getScheduler()
        var QueueSize = 60
        var PlayerCount = 0

    }
    override fun onEnable() {
        logger.info("Plugin Enabled")
        plugin = this

        Bukkit.getPluginManager().registerEvents(Events(), this)
        plugin.server.messenger.registerOutgoingPluginChannel(this, "BungeeCord")
        plugin.server.messenger.registerIncomingPluginChannel(this, "BungeeCord", BungeeC())

        kommand {
            register("force_send") {
                requires { isOp }
                executes {
                    player.sendMessage("§a/force_send <플레이어>")
                    player.sendMessage("§a플레이어를 큐를 무시하고 본 서버로 이동시킵니다.")
                }
                then("player" to player()) {
                    executes {
                        val player: Player by it
                        val out = ByteStreams.newDataOutput()
                        out.writeUTF("Connect")
                        out.writeUTF("lobby")
                        player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray())
                    }
                }
            }
            register("list_all") {
                requires { isOp || isConsole }
                executes {
                    val currentPlayers = plugin.server.onlinePlayers.size
                    val allPlayers = PlayerCount
                    val orgServerPlayers = allPlayers - currentPlayers
                    if(isPlayer) {
                        player.sendMessage("§a대기열 플레이어 수: $currentPlayers")
                        player.sendMessage("§a본 서버 플레이어 수: $orgServerPlayers")
                        player.sendMessage("§a전체 플레이어 수: $allPlayers")
                    } else {
                        println("§a대기열 플레이어 수: $currentPlayers")
                        println("§a본 서버 플레이어 수: $orgServerPlayers")
                        println("§a전체 플레이어 수: $allPlayers")
                    }
                }
            }
            register("qs_get") {
                requires { isOp || isConsole }
                executes {
                    if(isPlayer) {
                        player.sendMessage("§a본 서버 최대 플레이어 수: $QueueSize")
                    } else {
                        println("본 서버 최대 플레이어 수: $QueueSize")
                    }
                }
            }
            register("qs_set") {
                requires { isOp || isConsole }
                executes {
                    player.sendMessage("§a/qs_set <숫자>")
                }
                then("size" to int(0)) {
                    executes {
                        val size: Int by it
                        val queue_before = size
                        QueueSize = size
                        if(isPlayer) {
                            player.sendMessage("§a원래 본 서버 최대 플레이어 수: $queue_before")
                            player.sendMessage("§a변경된 본 서버 최대 플레이어 수: $QueueSize")
                        } else {
                            println("§a원래 본 서버 최대 플레이어 수: $queue_before")
                            println("§a변경된 본 서버 최대 플레이어 수: $QueueSize")
                        }
                    }
                }
            }
        }
    }
    override fun onDisable() {
        plugin.server.messenger.unregisterOutgoingPluginChannel(this, "BungeeCord")
        plugin.server.messenger.unregisterIncomingPluginChannel(this, "BungeeCord", BungeeC())
        logger.info("Plugin Disabled")
    }
}
