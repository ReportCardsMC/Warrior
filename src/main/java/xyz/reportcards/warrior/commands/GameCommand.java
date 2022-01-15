package xyz.reportcards.warrior.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.util.Vector;
import xyz.reportcards.warrior.game.Game;
import xyz.reportcards.warrior.utils.Common;

import java.util.*;

@CommandAlias("game")
@CommandPermission("warrior.game")
public class GameCommand extends BaseCommand {

    @Description("Starts the game")
    @Subcommand("start") // Start the countdown for the game
    public void startCommand(Player player) {
        Game game = Common.getInstance().getGame();
        if (game.getGameState().isStartedState()) {
            player.sendMessage(Common.parse("&cGame &4&l> &fThe game has already started."));
        } else {
            player.sendMessage(Common.parse("&cGame &4&l> &aStarting the game..."));
            game.startCountdown();
        }
    }

    @Description("Ends the game")
    @Subcommand("end")
    public void endCommand(Player player) {
        Game game = Common.getInstance().getGame();
        if (!game.getGameState().isStartedState()) {
            player.sendMessage(Common.parse("&cGame &4&l> &fThe game has already ended."));
        } else {
            player.sendMessage(Common.parse("&cGame &4&l> &aEnding the game..."));
            game.forceEnd();
        }
    }

    @CommandCompletion("@players")
    @Description("Removes a player from the game")
    @Syntax("<player>")
    @Subcommand("remove") // Remove a player from the game
    public void removeCommand(CommandSender player, String rp) {
        Player removePlayer = parsePlayer(rp);
        Game game = Common.getInstance().getGame();
        if (game.getPlayers().contains(removePlayer.getUniqueId())) {
            player.sendMessage(Common.parse("&cGame &4&l> &fRemoving " + removePlayer.getName() + " from the game."));
            game.removePlayer(removePlayer);
        } else {
            player.sendMessage(Common.parse("&cGame &4&l> &f" + removePlayer.getName() + " is not in the game."));
        }
    }

    @CommandCompletion("@players")
    @Description("Adds a player to the game")
    @Syntax("<player>")
    @Subcommand("add") // Add a player to the game
    public void addCommand(CommandSender player, String ap) {
        Player addPlayer = parsePlayer(ap);
        Game game = Common.getInstance().getGame();
        if (game.getPlayers().contains(addPlayer.getUniqueId())) {
            player.sendMessage(Common.parse("&cGame &4&l> &f" + addPlayer.getName() + " is already in the game."));
        } else {
            player.sendMessage(Common.parse("&cGame &4&l> &fAdding " + addPlayer.getName() + " to the game."));
            game.addPlayer(addPlayer);
        }
    }

    @CommandCompletion("spectator|1|2")
    @Description("Teleports you to the specified spawn")
    @Syntax("<spawn>")
    @Subcommand("spawn")
    public void spawnCommand(Player player, String spawn) {
        List<String> spawns = Arrays.asList("spectator", "1", "2");
        if (!spawns.contains(spawn)) {
            player.sendMessage(Common.parse("&cGame &4&l> &fInvalid spawn type, use one of the following: &c" + String.join(", ", spawns)));
        } else {
            Location spawnLocation = Common.getInstance().getConfigHandler().getSpawn(spawn);
            if (spawnLocation == null) {
                player.sendMessage(Common.parse("&cGame &4&l> &fSpawn location not found."));
            } else {
                player.sendMessage(Common.parse("&cGame &4&l> &fTeleporting to spawn " + spawn + "."));
                player.teleport(spawnLocation);
            }
        }
    }

    @CommandCompletion("spectator|1|2")
    @Description("Sets the spawn to the your location")
    @Syntax("<spawn>")
    @Subcommand("setspawn")
    public void setSpawnCommand(Player player, String spawn) {
        List<String> spawns = Arrays.asList("spectator", "1", "2");
        if (!spawns.contains(spawn)) {
            player.sendMessage(Common.parse("&cGame &4&l> &fInvalid spawn type, use one of the following: &c" + String.join(", ", spawns)));
        } else {
            Common.getInstance().getConfigHandler().getConfig().set("game.spawns." + spawn, player.getLocation());
            Common.getInstance().getConfigHandler().saveConfig();
            player.sendMessage(Common.parse("&cGame &4&l> &fSet spawn for " + spawn));
        }
    }

    @CommandCompletion("@players")
    @Description("Force a player to fight kup")
    @Syntax("<player>")
    @Subcommand("fightkup")
    public void fightKupCommand(CommandSender player, String fp) {
        Player fightPlayer = parsePlayer(fp);
        if (player instanceof Player p) {
            Game game = Common.getInstance().getGame();
            if (game.getGameState().isStartedState()) {
                p.sendMessage(Common.parse("&cGame &4&l> &fThere's a current game going on."));
            } else {
                Player kup = Bukkit.getPlayer("Kup1995");
                if (kup == null) {
                    p.sendMessage(Common.parse("&cGame &4&l> &fKup is not online."));
                } else {
                    p.sendMessage(Common.parse("&cGame &4&l> &fForcing " + fightPlayer.getName() + " to fight kup."));
                    game.getPlayers().clear();
                    game.getPlayers().addAll(Bukkit.getOnlinePlayers().stream().map(Player::getUniqueId).toList());
                    List<UUID> playing = List.of(fightPlayer.getUniqueId(), kup.getUniqueId());
                    List<UUID> notPlaying = new ArrayList<>(game.getPlayers());
                    notPlaying.removeAll(playing);
                    game.getDead().clear();
                    game.getDead().addAll(notPlaying);
                    game.startGame();
                    int i = 0;
                    Random r = new Random();
                    for (Vector vector : Common.offset(List.of(Common.generateCircle(50, 20f)), new Vector(1297.5, 80, -376.5))) {
                        i += 1;
                        Bukkit.getScheduler().runTaskLater(Common.getInstance(), () -> {
                            Firework firework = (Firework) kup.getWorld().spawnEntity(vector.toLocation(game.getGameWorld()), EntityType.FIREWORK);
                            FireworkMeta fm = firework.getFireworkMeta();
                            fm.addEffect(FireworkEffect.builder().withColor(Color.fromRGB(r.nextInt(255) + 1, r.nextInt(255) + 1, r.nextInt(255) + 1)).with(FireworkEffect.Type.BURST).build());
                            firework.setFireworkMeta(fm);
                        }, i);
                    }
                    i += 5;
                    Bukkit.getScheduler().runTaskLater(Common.getInstance(), () -> {
                        for (Vector vector : Common.offset(List.of(Common.generateCircle(5, 5f)), new Vector(1297.5, 70, -376.5))) {
                            Firework firework = (Firework) kup.getWorld().spawnEntity(vector.toLocation(game.getGameWorld()), EntityType.FIREWORK);
                            FireworkMeta fm = firework.getFireworkMeta();
                            fm.addEffect(FireworkEffect.builder().withColor(Color.fromRGB(r.nextInt(255) + 1, r.nextInt(255) + 1, r.nextInt(255) + 1)).with(FireworkEffect.Type.BURST).build());
                            firework.setFireworkMeta(fm);
                        }
                    }, i);
                }
            }
        }
    }

    @HelpCommand
    @Syntax("[search]")
    public void gameCommand(Player player, CommandHelp help) {
        help.setPerPage(6);
        help.showHelp();
    }

    private Player parsePlayer(String p) {
        Player player = Bukkit.getPlayer(p);
        if (player == null) {
            player = Bukkit.getPlayer(UUID.fromString(p));
        }
        return player;
    }

}
