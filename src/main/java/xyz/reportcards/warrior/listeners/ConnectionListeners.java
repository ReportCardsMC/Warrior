package xyz.reportcards.warrior.listeners;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import xyz.reportcards.warrior.utils.Common;

import java.util.Objects;

public class ConnectionListeners implements Listener {

    @EventHandler
    public void playerJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        event.joinMessage(Common.parse(String.format(
                "&a+ &f%s has joined the server",
                p.getName()
        )));
        p.teleport(Objects.requireNonNull(Common.getInstance().getConfigHandler().getConfig().getLocation("game.spawns.spectator")));
       if (!Common.getInstance().getGame().getGameState().isStartedState()) Common.getInstance().getGame().addPlayer(p);
        p.setGameMode(GameMode.ADVENTURE);
        p.getInventory().clear();
    }

    @EventHandler
    public void playerLeave(PlayerQuitEvent event) {
        Player p = event.getPlayer();
        event.quitMessage(Common.parse(String.format(
                "&c- &f%s has left the server",
                p.getName()
        )));
        Common.getInstance().getGame().removePlayer(p);
    }

}
