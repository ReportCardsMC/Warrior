package xyz.reportcards.warrior.utils;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.util.Vector;
import xyz.reportcards.warrior.Warrior;

import java.util.ArrayList;
import java.util.List;

public class Common {

    public static Component parse(String message) {
        return Component.text(ChatColor.translateAlternateColorCodes('&', message));
    }

    public static Warrior getInstance() {
        return (Warrior) Bukkit.getServer().getPluginManager().getPlugin("Warrior");
    }

    public static String plural(int count, String word) {
        return count == 1 ? word : word + "s";
    }

    // Generate a flat circle of vectors with a specified amount of points
    public static Vector[] generateCircle(int points, float radius) {
        List<Vector> vectors = new ArrayList<>();
        for (int i = 0; i < points; i++) {
            double angle = (i * 360.0f / points) * Math.PI / 180.0f;
            vectors.add(new Vector(Math.cos(angle) * radius, 0, Math.sin(angle) * radius));
        }
        return vectors.toArray(new Vector[0]);
    }

    // Method to offset all vectors by a vector
    public static List<Vector> offset(List<Vector> vectors, Vector offset) {
        List<Vector> vecs = new ArrayList<>();
        for (Vector vector : vectors) vecs.add(vector.clone().add(offset));
        return vecs;
    }
}
