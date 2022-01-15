package xyz.reportcards.warrior.utils;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class ConfigHandler {

    @Getter private final Plugin plugin;
    @Getter private final FileConfiguration config;

    public ConfigHandler(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
        this.config = plugin.getConfig();
    }

    public void saveConfig() {
        plugin.saveConfig();
    }

    public Location getSpawn(String name) {
        return config.getLocation("game.spawns." + name);
    }

}
