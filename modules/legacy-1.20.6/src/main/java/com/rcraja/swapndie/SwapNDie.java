package com.rcraja.swapndie;

import com.rcraja.swapndie.commands.SwapNDieCommand;
import com.rcraja.swapndie.game.GameManager;
import com.rcraja.swapndie.integration.IntegrationManager;
import com.rcraja.swapndie.items.CatalogGUI;
import com.rcraja.swapndie.items.ItemManager;
import com.rcraja.swapndie.listeners.GameListener;
import com.rcraja.swapndie.score.ScoreManager;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.plugin.java.JavaPlugin;

public class SwapNDie extends JavaPlugin {

    private GameManager gameManager;
    private ScoreManager scoreManager;
    private ItemManager itemManager;
    private IntegrationManager integrationManager;
    private CatalogGUI catalogGUI;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.gameManager = new GameManager(this);
        this.scoreManager = new ScoreManager(this);
        this.itemManager = new ItemManager(this);
        this.integrationManager = new IntegrationManager(this);
        this.catalogGUI = new CatalogGUI(this);

        SwapNDieCommand cmd = new SwapNDieCommand(this);
        getCommand("swapndie").setExecutor(cmd);
        getCommand("swapndie").setTabCompleter(cmd);

        getServer().getPluginManager().registerEvents(new GameListener(this), this);
        getServer().getPluginManager().registerEvents(catalogGUI, this);

        getLogger().info("SwapNDie enabled — by RC RAJA GAMER 2.0");
    }

    @Override
    public void onDisable() {
        if (scoreManager != null) scoreManager.save();
        getLogger().info("SwapNDie disabled.");
    }

    /** Legacy-color-coded string for a config message key under 'messages.' */
    public String msg(String key) {
        String raw = getConfig().getString("messages." + key, "");
        return LegacyComponentSerializer.legacySection().serialize(MiniMessage.miniMessage().deserialize(raw));
    }

    public GameManager getGameManager() { return gameManager; }
    public ScoreManager getScoreManager() { return scoreManager; }
    public ItemManager getItemManager() { return itemManager; }
    public IntegrationManager getIntegrationManager() { return integrationManager; }
    public CatalogGUI getCatalogGUI() { return catalogGUI; }
}

