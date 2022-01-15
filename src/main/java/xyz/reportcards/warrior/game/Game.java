package xyz.reportcards.warrior.game;

import lombok.Getter;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.reportcards.warrior.utils.Common;

import java.util.*;

public class Game {

    @Getter private final World gameWorld;
    @Getter private final List<UUID> players = new ArrayList<>();
    @Getter private final List<UUID> dead = new ArrayList<>();
    @Getter private GameStatus gameState;
    @Getter private Player player1;
    @Getter private Player player2;

    public Game(World world) {
        this.gameWorld = world;
        this.gameState = GameStatus.WAITING;
    }

    public void addPlayer(Player player) {
        if (!players.contains(player.getUniqueId())) players.add(player.getUniqueId());
    }

    public void removePlayer(Player player) {
        if (player1 == player && getGameState().isStartedState()) {
            addDead(player, player2);
        } else if (player2 == player && getGameState().isStartedState()) {
            addDead(player, player1);
        }
        dead.remove(player.getUniqueId());
        players.remove(player.getUniqueId());
    }

    private @org.jetbrains.annotations.Nullable Player getPlayer(UUID player) {
        return Bukkit.getPlayer(player);
    }

    public void addDead(Player player, Player killer) {
        if (players.contains(player.getUniqueId())) {
            dead.add(player.getUniqueId());
            broadcastDeath(player, killer);
            for (Entity entity : getGameWorld().getEntities()) {
                if (entity instanceof Trident || entity instanceof Arrow) {
                    entity.remove();
                }
            }
            if (getAlive().size() <= 1) {
                endGame();
                return;
            }
            setupPlayers(killer);
            new BukkitRunnable() {
                @Override
                public void run() {
                    player.getInventory().clear();
                    player.setFoodLevel(20);
                    player.setHealth(20);
                    player.teleport(Common.getInstance().getConfigHandler().getSpawn("spectator"));
                }
            }.runTaskLater(Common.getInstance(), 5);
        }
    }


    private void broadcastDeath(Player player, Player killer) {
        getGameWorld().sendMessage(Common.parse(String.format(
                "&4☠ &c%s was killed by %s &4☠",
                player.getName(),
                killer.getName()
        )));
    }

    public void startCountdown() {
        this.gameState = GameStatus.STARTING;
        new BukkitRunnable() {
            int count = 5;

            @Override
            public void run() {
                if (count == 0) {
                    startGame();
                    this.cancel();
                } else {
                    getGameWorld().sendActionBar(Common.parse(String.format(
                            "&4&l»&c %s %s until the game starts &4&l«",
                            count,
                            Common.plural(count, "second")
                    )));
                    count--;
                }
            }
        }.runTaskTimer(Common.getInstance(), 0, 20);
    }

    public void startGame() {
        Player randomPlayer = getRandomPlayer();
        if (randomPlayer == null) {
            getGameWorld().sendMessage(Common.parse("&4&l»&c Error: no players to start."));
            endGame();
            return;
        }
        gameState = GameStatus.RUNNING;
        setupPlayers(randomPlayer);
    }

    private void setupPlayers(Player alive) {
        player1 = alive;
        player2 = getRandomPlayer(alive.getUniqueId());
        Title title = Title.title(Common.parse("&7"), Common.parse("&cYou are playing!"));
        List<Player> playing = Arrays.asList(player1, player2);
        player1.teleportAsync(Common.getInstance().getConfigHandler().getSpawn("1"));
        player2.teleportAsync(Common.getInstance().getConfigHandler().getSpawn("2"));
        for (Player player : playing) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20, 7));
            player.setHealth(20);
            player.setFireTicks(0);
            setupItems(player);
            player.showTitle(title);
            player.setGameMode(GameMode.ADVENTURE);
        }
    }

    private Player getRandomPlayer(UUID... exclude) {
        List<UUID> alive = getAlive();
        alive.removeAll(List.of(exclude));
        if (alive.isEmpty()) return null;
        return Objects.requireNonNull(getPlayer(alive.get(new Random().nextInt(alive.size()))));
    }

    private List<UUID> getAlive() {
        List<UUID> alive = new ArrayList<>(players);
        alive.removeAll(dead);
        return alive;
    }

    private void setupItems(Player player) {
        player.getInventory().clear();
        ItemStack helm = new ItemStack(Material.DIAMOND_HELMET);
        ItemStack chest = new ItemStack(Material.DIAMOND_CHESTPLATE);
        ItemStack legs = new ItemStack(Material.DIAMOND_LEGGINGS);
        ItemStack boots = new ItemStack(Material.DIAMOND_BOOTS);
        player.getInventory().setItemInOffHand(new ItemStack(Material.SHIELD));
        player.getInventory().setArmorContents(new ItemStack[]{boots, legs, chest, helm});
        player.getInventory().addItem(new ItemStack(Material.IRON_SWORD), new ItemStack(Material.IRON_AXE), new ItemStack(Material.BOW), new ItemStack(Material.TRIDENT), new ItemStack(Material.ARROW, 16));
    }

    private void endGame() {
//        getGameWorld().sendMessage(Common.parse("&4&l»&c The game has ended!"));
        getGameWorld().sendMessage(Common.parse(String.format(
                "&4&l»&c The winner is %s!!!",
                getAlive().size() == 0 ? "nobody?" : getPlayer(getAlive().get(0)).getName()
        )));
        dead.clear();
        gameState = GameStatus.WAITING;
        playerEnd();
    }

    public void forceEnd() {
        getGameWorld().sendMessage(Common.parse("&4&l»&c The game has been forcefully stopped."));
        dead.clear();
        this.gameState = GameStatus.WAITING;
        playerEnd();
    }

    private void playerEnd() {
        Bukkit.getScheduler().runTaskLater(Common.getInstance(), () -> {
            for (UUID player : players) {
                Player p = getPlayer(player);
                if (p == null) continue;
                p.teleport(Common.getInstance().getConfigHandler().getSpawn("spectator"));
                p.getInventory().clear();
                p.setHealth(20);
            }
        }, 10);
    }

    public void addDeadLava(Player victim) {
        Player otherPerson = null;
        if (player1 == victim && getGameState().isStartedState()) {
            otherPerson = player2;
        } else if (player2 == victim && getGameState().isStartedState()) {
            otherPerson = player1;
        }
//        Bukkit.broadcast(Common.parse("Deaded: " + player.getName() + " " + killer.getName()));
        if (players.contains(victim.getUniqueId())) {
            dead.add(victim.getUniqueId());
            getGameWorld().sendMessage(Common.parse(String.format(
                    "&4☠ &c%s was killed by lava &4☠",
                    victim.getName()
            )));
            if (getAlive().size() <= 1) {
                endGame();
                return;
            }

            if (otherPerson != null) setupPlayers(otherPerson);
            new BukkitRunnable() {
                @Override
                public void run() {
                    victim.getInventory().clear();
                    victim.setFoodLevel(20);
                    victim.setHealth(20);
                    victim.teleport(Common.getInstance().getConfigHandler().getSpawn("spectator"));
                }
            }.runTaskLater(Common.getInstance(), 5);
        }
    }
}
