package pl.barpad.duckycolormix;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import pl.barpad.duckycolormix.commands.ColorMixCommand;
import pl.barpad.duckycolormix.config.ConfigManager;
import pl.barpad.duckycolormix.game.ColorMixGame;
import pl.barpad.duckycolormix.utils.MetricsLite;
import pl.barpad.duckycolormix.utils.UpdateChecker;

import java.util.ArrayList;
import java.util.List;

public final class Main extends JavaPlugin {

    private ConfigManager configManager;
    private ColorMixGame colorMixGame;
    private final List<Listener> registeredListeners = new ArrayList<>();

    @Override
    public void onLoad() {
        getLogger().info("DuckyColorMix Loading...");
    }

    @Override
    public void onEnable() {
        try {
            saveDefaultConfig();
            initializeComponents();
            registerListeners();
            registerCommands();
            initializeExtras();

            getLogger().info("DuckyColorMix Enabled - Let the colors mix! | Author: Barpad");
        } catch (Exception e) {
            getLogger().severe("Failed to enable DuckyColorMix: " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        if (colorMixGame != null && colorMixGame.isGameActive()) {
            colorMixGame.stopGame();
        }

        getLogger().info("DuckyColorMix Disabled - Thank you for playing! | Author: Barpad");
    }

    private void initializeComponents() {
        this.configManager = new ConfigManager(this);
        this.colorMixGame = new ColorMixGame(this);
    }

    private void registerListeners() {
        registerListener(colorMixGame);

        for (Listener listener : registeredListeners) {
            getServer().getPluginManager().registerEvents(listener, this);
        }

        getLogger().info("Registered " + registeredListeners.size() + " event listeners");
    }

    private void registerListener(Listener listener) {
        registeredListeners.add(listener);
    }

    private void registerCommands() {
        ColorMixCommand colorMixCommand = new ColorMixCommand(this, configManager, colorMixGame);
        colorMixCommand.register();
    }

    private void initializeExtras() {
        new MetricsLite(this, 26585);

        getServer().getScheduler().runTaskAsynchronously(this, () -> {
            try {
                new UpdateChecker(this, configManager).checkForUpdates();
            } catch (Exception e) {
                getLogger().warning("Failed to check for updates: " + e.getMessage());
            }
        });
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public ColorMixGame getColorMixGame() {
        return colorMixGame;
    }
}