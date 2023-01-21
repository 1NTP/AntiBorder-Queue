package me.uwuaden.kotlinplugin

import com.google.common.io.ByteStreams
import me.clip.placeholderapi.PlaceholderAPI
import me.uwuaden.kotlinplugin.Main.Companion.PlayerCount
import me.uwuaden.kotlinplugin.Main.Companion.Scheduler
import me.uwuaden.kotlinplugin.Main.Companion.plugin
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerChatEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.server.PluginEnableEvent
import java.util.*


class Events: Listener {
    companion object {
        var PlayerQueue = ArrayList<UUID>()
    }

    @EventHandler
    fun onJoin(e: PlayerJoinEvent) {
        getPlayerCount(e.player, "afk")
        e.player.gameMode = GameMode.SURVIVAL
        e.joinMessage = ""
        e.player.teleport(Location(plugin.server.getWorld("world")!!, 0.5, 0.0, 0.5, 0.0F, 0.0F))
        if(e.player.isOp) {
            e.player.sendMessage("§a대기열 서버입니다")
            e.player.sendMessage("§a본서버로 이동하기 위해서는")
            e.player.sendMessage("§a/force_send ${e.player.name} 명령어를 쳐주세요.")
        } else {
            PlayerQueue.removeIf { it == e.player.uniqueId }
            sortQueue()
            Scheduler.scheduleSyncDelayedTask(plugin, {
                PlayerQueue.add(e.player.uniqueId)
            }, 20*3)
        }
    }
    @EventHandler
    fun onDMG(e: EntityDamageEvent) {
        if(e.entity.type == EntityType.PLAYER) {
            e.isCancelled = true
        }
    }

    @EventHandler
    fun onChat(e: PlayerChatEvent) {
        e.isCancelled = true
    }

    @EventHandler
    fun onQuit(e: PlayerQuitEvent) {
        getPlayerCount(e.player, "ALL")
        e.quitMessage = ""
        PlayerQueue.removeIf { it == e.player.uniqueId }
        sortQueue()

    }

    @EventHandler
    fun onEnable(e: PluginEnableEvent) {
        Scheduler.scheduleSyncRepeatingTask(plugin, {
            sortQueue()
            if(PlayerQueue.isNotEmpty()) {
                val player = plugin.server.getPlayer(PlayerQueue[0])
                if (player != null) {
                    if (enoughServerPlayers()) {
                        val out = ByteStreams.newDataOutput()

                        val currentPlayers = plugin.server.onlinePlayers.size
                        val allPlayers = PlayerCount
                        val orgServerPlayers = allPlayers - currentPlayers

                        println("§a대기열 플레이어 수: $currentPlayers")
                        println("§a본 서버 플레이어 수: $orgServerPlayers")
                        println("§a전체 플레이어 수: $allPlayers")

                        player.sendMessage("§a서버 접속에 성공했습니다.")
                        PlayerQueue.remove(PlayerQueue[0])
                        out.writeUTF("Connect")
                        out.writeUTF("lobby")
                        player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray())
                    }
                } else {
                    PlayerQueue.remove(PlayerQueue[0])
                    sortQueue()
                }
            }
            PlayerQueue.forEach { uid ->
                val player = plugin.server.getPlayer(uid)
                player?.sendTitle("§e서버에 사람이 많아 대기 중입니다", "§a남은 플레이어 수: ${PlayerQueue.size}", 1, 20*10, 1)
            }
        },0 ,20*3)

        Scheduler.scheduleSyncRepeatingTask(plugin, {
            plugin.server.onlinePlayers.forEach { player1 ->
                plugin.server.onlinePlayers.forEach { player2 ->
                    if(player1 != player2) {
                        player1.hidePlayer(player2)
                    }
                }
            }
        }, 0, 1)
    }
}