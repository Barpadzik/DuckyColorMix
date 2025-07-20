package pl.barpad.duckycolormix.game;

import pl.barpad.duckycolormix.config.ConfigManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * PlatformManager handles all platform-related operations for the DuckyColorMix game.
 * This includes platform setup, wool generation, unsafe block removal, and player detection.
 * The platform is defined by two corner locations and can be filled with random colored wool.
 *
 * @author Barpad
 * @version 1.0.0
 */
public class PlatformManager implements Listener {

    private final ConfigManager configManager;

    // Available wool colors for the platform
    private static final DyeColor[] WOOL_COLORS = {
            DyeColor.WHITE, DyeColor.ORANGE, DyeColor.MAGENTA, DyeColor.LIGHT_BLUE,
            DyeColor.YELLOW, DyeColor.LIME, DyeColor.PINK, DyeColor.GRAY,
            DyeColor.LIGHT_GRAY, DyeColor.CYAN, DyeColor.PURPLE, DyeColor.BLUE,
            DyeColor.BROWN, DyeColor.GREEN, DyeColor.RED, DyeColor.BLACK
    };

    private boolean platformMode = false;
    private Player platformSetter = null;
    private Location corner1 = null;
    private Location corner2 = null;
    private final Random random = new Random();

    /**
     * Constructor for PlatformManager.
     *
     * @param configManager The configuration manager for messages
     */
    public PlatformManager(ConfigManager configManager) {
        this.configManager = configManager;
    }

    /**
     * Checks if a platform has been properly set up.
     * A platform is considered set up when both corner locations are defined.
     *
     * @return true if a platform is set up, false otherwise
     */
    public boolean hasPlatform() {
        return corner1 != null && corner2 != null;
    }

    /**
     * Enables platform setup mode for a specific player.
     * When in platform mode, the player can click two blocks to define the platform area.
     *
     * @param player The player who will set up the platform
     */
    public void setPlatformMode(Player player) {
        this.platformMode = true;
        this.platformSetter = player;
        this.corner1 = null;
        this.corner2 = null;
    }

    /**
     * Handles player interaction events for platform setup.
     * When a player is in platform mode, clicking blocks will set the platform corners.
     *
     * @param event The PlayerInteractEvent to handle
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Only handle events when platform mode is active and the correct player is clicking
        if (!platformMode || !event.getPlayer().equals(platformSetter)) {
            return;
        }

        // Only handle block click events
        if (event.getAction() != Action.LEFT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        event.setCancelled(true);
        Block clickedBlock = event.getClickedBlock();

        if (clickedBlock == null) {
            return;
        }

        // Set the first corner
        if (corner1 == null) {
            corner1 = clickedBlock.getLocation();
            event.getPlayer().sendMessage(configManager.getMessage("platform-first-corner",
                    "&aFirst corner set! Click second corner."));
        }
        // Set the second corner and finish platform setup
        else if (corner2 == null) {
            corner2 = clickedBlock.getLocation();
            event.getPlayer().sendMessage(configManager.getMessage("platform-second-corner",
                    "&aThe second corner is set! The platform is ready."));
            platformMode = false;
            platformSetter = null;
        }
    }

    /**
     * Fills the entire platform with randomly colored wool blocks.
     * Each block on the platform gets a random color from the available wool colors.
     * This method is used to reset the platform between rounds.
     */
    public void fillPlatformWithWool() {
        if (!hasPlatform()) return;

        // Calculate platform boundaries
        int minX = Math.min(corner1.getBlockX(), corner2.getBlockX());
        int maxX = Math.max(corner1.getBlockX(), corner2.getBlockX());
        int minZ = Math.min(corner1.getBlockZ(), corner2.getBlockZ());
        int maxZ = Math.max(corner1.getBlockZ(), corner2.getBlockZ());
        int y = Math.max(corner1.getBlockY(), corner2.getBlockY()); // Use the higher Y coordinate

        World world = corner1.getWorld();

        // Fill each block with random colored wool
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                assert world != null;
                Block block = world.getBlockAt(x, y, z);
                DyeColor randomColor = WOOL_COLORS[random.nextInt(WOOL_COLORS.length)];
                block.setType(Material.valueOf(randomColor.name() + "_WOOL"));
            }
        }
    }

    /**
     * Removes all wool blocks except those of the specified safe color.
     * This method is called after the countdown ends to eliminate unsafe areas.
     * Players standing on removed blocks will fall.
     *
     * @param safeColor The DyeColor that should remain on the platform
     */
    public void removeUnsafeWool(DyeColor safeColor) {
        if (!hasPlatform()) return;

        // Calculate platform boundaries
        int minX = Math.min(corner1.getBlockX(), corner2.getBlockX());
        int maxX = Math.max(corner1.getBlockX(), corner2.getBlockX());
        int minZ = Math.min(corner1.getBlockZ(), corner2.getBlockZ());
        int maxZ = Math.max(corner1.getBlockZ(), corner2.getBlockZ());
        int y = Math.max(corner1.getBlockY(), corner2.getBlockY());

        World world = corner1.getWorld();
        Material safeWool = Material.valueOf(safeColor.name() + "_WOOL");

        // Remove all blocks that are not the safe color
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                assert world != null;
                Block block = world.getBlockAt(x, y, z);
                if (block.getType() != safeWool) {
                    block.setType(Material.AIR);
                }
            }
        }

        // Play explosion sound effect for all players
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
        }
    }

    /**
     * Gets the number of players currently in the game area.
     * Players are considered "in game" if they are within the platform boundaries
     * and up to 5 blocks above the platform level.
     *
     * @return The number of players in the game area
     */
    public int getPlayersInGame() {
        if (!hasPlatform()) return 0;

        return getPlayersInGameList().size();
    }

    /**
     * Gets a list of all players currently in the game area.
     * This method defines the game area as the platform plus 5 blocks of height above it.
     *
     * @return A list of Player objects who are currently in the game area
     */
    public List<Player> getPlayersInGameList() {
        List<Player> players = new ArrayList<>();
        if (!hasPlatform()) return players;

        // Calculate game area boundaries
        int minX = Math.min(corner1.getBlockX(), corner2.getBlockX());
        int maxX = Math.max(corner1.getBlockX(), corner2.getBlockX());
        int minZ = Math.min(corner1.getBlockZ(), corner2.getBlockZ());
        int maxZ = Math.max(corner1.getBlockZ(), corner2.getBlockZ());
        int minY = Math.max(corner1.getBlockY(), corner2.getBlockY()) + 1; // Start 1 block above a platform
        int maxY = minY + 5; // 5 blocks above platform

        World world = corner1.getWorld();

        // Check each online player's location
        for (Player player : Bukkit.getOnlinePlayers()) {
            // Skip players in different worlds
            if (!player.getWorld().equals(world)) continue;

            Location loc = player.getLocation();

            // Check if player is within the game area boundaries
            if (loc.getBlockX() >= minX && loc.getBlockX() <= maxX &&
                    loc.getBlockZ() >= minZ && loc.getBlockZ() <= maxZ &&
                    loc.getBlockY() >= minY && loc.getBlockY() <= maxY) {
                players.add(player);
            }
        }

        return players;
    }

    /**
     * Gets a random wool color from the available colors.
     * This method is used to select the safe color for each round.
     *
     * @return A random DyeColor from the available wool colors
     */
    public DyeColor getRandomWoolColor() {
        return WOOL_COLORS[random.nextInt(WOOL_COLORS.length)];
    }

    /**
     * Clears the entire platform by setting all blocks to air.
     * This method can be used for cleanup or testing purposes.
     */
    public void clearPlatform() {
        if (!hasPlatform()) return;

        // Calculate platform boundaries
        int minX = Math.min(corner1.getBlockX(), corner2.getBlockX());
        int maxX = Math.max(corner1.getBlockX(), corner2.getBlockX());
        int minZ = Math.min(corner1.getBlockZ(), corner2.getBlockZ());
        int maxZ = Math.max(corner1.getBlockZ(), corner2.getBlockZ());
        int y = Math.max(corner1.getBlockY(), corner2.getBlockY());

        World world = corner1.getWorld();

        // Set all platform blocks to air
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                assert world != null;
                Block block = world.getBlockAt(x, y, z);
                block.setType(Material.AIR);
            }
        }
    }
}