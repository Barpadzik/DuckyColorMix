package pl.barpad.duckycolormix.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractCommand implements CommandExecutor, TabExecutor {
    protected final String command;
    protected final String description;
    protected final List<String> alias;
    protected final String usage;
    protected final String permMessage;
    protected static CommandMap cmap;

    public AbstractCommand(String command, String usage, String description, String permissionMessage, List<String> aliases) {
        this.command = command.toLowerCase();
        this.usage = usage;
        this.description = description;
        this.permMessage = permissionMessage;
        this.alias = aliases;
    }

    public void register() {
        ReflectCommand cmd = new ReflectCommand(this.command);
        if (this.alias != null) {
            cmd.setAliases(this.alias);
        }
        if (this.description != null) {
            cmd.setDescription(this.description);
        }
        if (this.usage != null) {
            cmd.setUsage(this.usage);
        }
        if (this.permMessage != null) {
            cmd.setPermissionMessage(this.permMessage);
        }
        cmd.setPermission("duckycolormix." + this.command);
        this.getCommandMap().register("", cmd);
        cmd.setExecutor(this);

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.updateCommands();
        }
    }

    final CommandMap getCommandMap() {
        if (cmap == null) {
            try {
                Field f = Bukkit.getServer().getClass().getDeclaredField("commandMap");
                f.setAccessible(true);
                cmap = (CommandMap) f.get(Bukkit.getServer());
                return this.getCommandMap();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            return cmap;
        }
        return this.getCommandMap();
    }

    public abstract boolean onCommand(@NotNull CommandSender var1, @NotNull Command var2, @NotNull String var3, String[] var4);

    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        return null;
    }

    public static List<String> onlinePlayers(CommandSender sender, String[] args) {
        String lastWord = args[args.length - 1];
        Player senderPlayer = sender instanceof Player ? (Player) sender : null;
        ArrayList<String> matchedPlayers = new ArrayList<>();
        for (Player player : sender.getServer().getOnlinePlayers()) {
            String name = player.getName();
            if (senderPlayer != null && !senderPlayer.canSee(player) || !StringUtil.startsWithIgnoreCase(name, lastWord))
                continue;
            matchedPlayers.add(name);
        }
        matchedPlayers.sort(String.CASE_INSENSITIVE_ORDER);
        return matchedPlayers;
    }

    private static final class ReflectCommand
            extends Command {
        private AbstractCommand exe;

        private ReflectCommand(String command) {
            super(command);
            this.exe = null;
        }

        public void setExecutor(AbstractCommand exe) {
            this.exe = exe;
        }

        public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, String[] args) {
            if (this.exe != null) {
                return this.exe.onCommand(sender, this, commandLabel, args);
            }
            return false;
        }

        public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, String[] args) {
            if (this.exe != null) {
                return this.exe.onTabComplete(sender, this, alias, args);
            }
            return null;
        }
    }
}