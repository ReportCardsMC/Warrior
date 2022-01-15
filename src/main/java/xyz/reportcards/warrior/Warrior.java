package xyz.reportcards.warrior;

import co.aikar.commands.PaperCommandManager;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.reportcards.warrior.commands.GameCommand;
import xyz.reportcards.warrior.game.Game;
import xyz.reportcards.warrior.listeners.ConnectionListeners;
import xyz.reportcards.warrior.listeners.MainEvents;
import xyz.reportcards.warrior.utils.ConfigHandler;

public final class Warrior extends JavaPlugin {

    PaperCommandManager commandManager;
    @Getter Game game;
    @Getter ConfigHandler configHandler;

    @Override
    public void onEnable() {
        // Plugin startup logic
        configHandler = new ConfigHandler(this);
        commandManager = new PaperCommandManager(this);
        commandManager.enableUnstableAPI("help");
        registerCommands();

        game = new Game(Bukkit.getWorlds().get(0));

        getServer().getPluginManager().registerEvents(new MainEvents(), this);
        getServer().getPluginManager().registerEvents(new ConnectionListeners(), this);

        for (Player player : getServer().getOnlinePlayers()) {
            game.addPlayer(player);
        }

        for (String key : getConfigHandler().getConfig().getConfigurationSection("game.spawns").getKeys(false)) {
            getLogger().info("Loading " + key);
            getConfigHandler().getConfig().getLocation("game.spawns." + key).getChunk().load(true);
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private void registerCommands() {
        commandManager.registerCommand(new GameCommand());
    }
}
