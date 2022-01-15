package xyz.reportcards.warrior.listeners;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import xyz.reportcards.warrior.game.Game;
import xyz.reportcards.warrior.utils.Common;

public class MainEvents implements Listener {

    @EventHandler
    public void hungerChange(org.bukkit.event.entity.FoodLevelChangeEvent e) {
        e.setFoodLevel(20);
    }

    @EventHandler
    public void damage(org.bukkit.event.entity.EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player && (e.getDamager() instanceof Player || e.getDamager() instanceof Projectile))) {
            e.setCancelled(true);
            return;
        }

        Game game = Common.getInstance().getGame();
        if (game.getGameState().isStartedState()) {
            Player p1 = game.getPlayer1();
            Player p2 = game.getPlayer2();
            Player attacker = e.getDamager() instanceof Player ? (Player) e.getDamager() : (Player) ((Projectile) e.getDamager()).getShooter();
            Player victim = (Player) e.getEntity();

            boolean isPlaying = false;
            if (p1 == attacker || p2 == attacker) {
                if (p1 == victim || p2 == victim) {
                    isPlaying = true;
                }
            }
            if (!isPlaying) {
                e.setCancelled(true);
                return;
            }

            int health1 = (int) Math.ceil(attacker.getHealth());
            int health2 = (int) Math.ceil(victim.getHealth() - e.getFinalDamage());
            victim.sendActionBar(Common.parse("&c♥ " + health1 + "/" + (int) attacker.getMaxHealth()));
            attacker.sendActionBar(Common.parse("&c♥ " + health2 + "/" + (int) victim.getMaxHealth()));

            return;
        }

        e.setCancelled(true);
    }

    @EventHandler
    public void deadEvent(org.bukkit.event.entity.PlayerDeathEvent e) {
        Player victim = e.getEntity();
        Player attacker = victim.getKiller();

        Game game = Common.getInstance().getGame();
        if (game.getGameState().isStartedState()) {
            Player p1 = game.getPlayer1();
            Player p2 = game.getPlayer2();

            boolean isPlaying = false;
            if (p1 == attacker || p2 == attacker) {
                if (p1 == victim || p2 == victim) {
                    isPlaying = true;
                }
            } else if (attacker == null) {
                game.addDeadLava(victim);
                return;
            }
            if (!isPlaying) {
                return;
            }

            game.addDead(victim, attacker);
        } else {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void breakEvent(BlockBreakEvent e) {
        if (e.getPlayer().getGameMode().equals(GameMode.CREATIVE)) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void placeEvent(BlockPlaceEvent e) {
        if (e.getPlayer().getGameMode().equals(GameMode.CREATIVE)) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void dropEvent(org.bukkit.event.player.PlayerDropItemEvent e) {
        if (e.getPlayer().getGameMode().equals(GameMode.CREATIVE)) return;
        e.setCancelled(true);
    }

}
