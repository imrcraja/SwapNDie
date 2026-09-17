package com.rcraja.swapndie.score;

import com.rcraja.swapndie.SwapNDie;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class ScoreManager {

    private final SwapNDie plugin;
    private final File file;
    private YamlConfiguration data;

    public ScoreManager(SwapNDie plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "scores.yml");
        load();
    }

    private void load() {
        if (!file.exists()) {
            plugin.getDataFolder().mkdirs();
            try {
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().warning("Could not create scores.yml: " + e.getMessage());
            }
        }
        data = YamlConfiguration.loadConfiguration(file);
    }

    public void save() {
        try {
            data.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save scores.yml: " + e.getMessage());
        }
    }

    private String path(UUID id, String key) {
        return id.toString() + "." + key;
    }

    public void addWin(UUID id, String name) {
        data.set(path(id, "name"), name);
        data.set(path(id, "wins"), getWins(id) + 1);
        data.set(path(id, "points"), getPoints(id) + 10);
        save();
    }

    public void addLoss(UUID id, String name) {
        data.set(path(id, "name"), name);
        data.set(path(id, "losses"), getLosses(id) + 1);
        data.set(path(id, "points"), getPoints(id) + 2);
        save();
    }

    public int getWins(UUID id) { return data.getInt(path(id, "wins"), 0); }
    public int getLosses(UUID id) { return data.getInt(path(id, "losses"), 0); }
    public int getPoints(UUID id) { return data.getInt(path(id, "points"), 0); }

    /** Returns top N players by points, name -> points, sorted descending. */
    public Map<String, Integer> getTop(int n) {
        Map<String, Integer> all = new LinkedHashMap<>();
        if (data.getKeys(false) != null) {
            for (String key : data.getKeys(false)) {
                String name = data.getString(key + ".name", key);
                int points = data.getInt(key + ".points", 0);
                all.put(name, points);
            }
        }
        return all.entrySet().stream()
                .sorted((a, b) -> b.getValue() - a.getValue())
                .limit(n)
                .collect(LinkedHashMap::new, (m, e) -> m.put(e.getKey(), e.getValue()), Map::putAll);
    }
}

