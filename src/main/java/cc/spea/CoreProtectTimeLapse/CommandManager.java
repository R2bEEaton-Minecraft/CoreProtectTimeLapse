package cc.spea.CoreProtectTimeLapse;

import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static cc.spea.CoreProtectTimeLapse.Helpers.sendFancy;
import static org.bukkit.Bukkit.getServer;

public class CommandManager implements CommandExecutor, TabCompleter {
    private static final String USE_PERMISSION = "coreprotecttimelapse.use";

    private final JavaPlugin plugin;
    private final FileConfiguration config;
    private final CoreProtectAPI api;
    private final StartWizardManager startWizardManager;
    private InterruptableThread rollbackThread;
    private Thread undoThread;
    private final ArrayList<Integer> rollbackDepths = new ArrayList<>();
    private int lastRadius = -1;
    private Location lastLocation = null;

    public CommandManager(JavaPlugin plugin, FileConfiguration config) {
        this.plugin = plugin;
        this.api = getCoreProtect();
        this.config = config;
        this.startWizardManager = new StartWizardManager(plugin, this);
    }

    public void registerAll() {
        PluginCommand command = plugin.getCommand("cptl");
        if (command == null) {
            throw new IllegalStateException("Command 'cptl' is not defined in plugin.yml");
        }

        command.setExecutor(this);
        command.setTabCompleter(this);
        plugin.getServer().getPluginManager().registerEvents(startWizardManager, plugin);
        startWizardManager.startCleanupTask();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(USE_PERMISSION)) {
            sendFancy(sender, "You must be an operator to use this command.");
            return true;
        }

        if (!config.getBoolean("acknowledgesDestruction")) {
            sendFancy(sender, "Please update the config file in your plugins/CoreProtectTimeLapse folder to acknowledge the destructive ability of this plugin. ONLY run this plugin on a backup! Restart or reload when the config has been updated.");
            return true;
        }

        if (args.length == 0) {
            sendFancy(sender, "Usage: /" + label + " <setup|start|stop|undo>");
            return true;
        }

        String subcommand = args[0].toLowerCase(Locale.ROOT);
        return switch (subcommand) {
            case "setup" -> handleSetup(sender);
            case "start" -> handleStart(sender, args);
            case "stop" -> handleStop(sender);
            case "undo" -> handleUndo(sender);
            case "wizard" -> handleWizard(sender, args);
            default -> {
                sendFancy(sender, "Unknown subcommand. Use /" + label + " <setup|start|stop|undo>.");
                yield true;
            }
        };
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission(USE_PERMISSION)) {
            return List.of();
        }

        if (args.length == 1) {
            return filterPrefix(List.of("setup", "start", "stop", "undo"), args[0]);
        }

        if (args.length == 2 && "start".equalsIgnoreCase(args[0])) {
            return filterPrefix(List.of("100", "cancel"), args[1]);
        }

        if (args.length >= 3 && args.length <= 5 && "start".equalsIgnoreCase(args[0])) {
            return List.of("0");
        }

        if (args.length >= 6 && args.length <= 8 && "start".equalsIgnoreCase(args[0]) && sender instanceof Player player) {
            Location location = player.getLocation();
            return switch (args.length) {
                case 6 -> List.of(Integer.toString(location.getBlockX()));
                case 7 -> List.of(Integer.toString(location.getBlockY()));
                case 8 -> List.of(Integer.toString(location.getBlockZ()));
                default -> List.of();
            };
        }

        return List.of();
    }

    private boolean handleSetup(CommandSender sender) {
        Player player = requirePlayer(sender);
        if (player == null) {
            return true;
        }

        Bukkit.dispatchCommand(player, "gamerule randomTickSpeed 1000");
        Bukkit.dispatchCommand(player, "gamerule doTileDrops false");
        Bukkit.dispatchCommand(player, "gamerule doFireTick false");
        Bukkit.dispatchCommand(player, "gamerule doEntityDrops false");
        Bukkit.dispatchCommand(player, "gamerule doMobSpawning false");
        Bukkit.dispatchCommand(player, "gamerule doPatrolSpawning false");
        Bukkit.dispatchCommand(player, "gamerule doTraderSpawning false");
        Bukkit.dispatchCommand(player, "gamerule doWeatherCycle false");
        Bukkit.dispatchCommand(player, "gamerule doMobLoot false");
        Bukkit.dispatchCommand(player, "gamerule tntExplodes false");
        Bukkit.dispatchCommand(player, "kill @e[type=!player]");
        Bukkit.dispatchCommand(player, "kill @e[type=!player]");
        Bukkit.dispatchCommand(player, "kill @e[type=!player]");
        Bukkit.dispatchCommand(player, "kill @e[type=!player]");
        Bukkit.dispatchCommand(player, "say Natural tree growth is always disabled with CPTL.");
        Bukkit.dispatchCommand(player, "time set 0");
        Bukkit.dispatchCommand(player, "weather clear");
        sendFancy(player, "Setup complete.");
        return true;
    }

    private boolean handleStart(CommandSender sender, String[] args) {
        Player player = requirePlayer(sender);
        if (player == null) {
            return true;
        }

        if (args.length == 1) {
            startWizardManager.start(player);
            return true;
        }

        if (args.length == 2 && "cancel".equalsIgnoreCase(args[1])) {
            startWizardManager.cancel(player);
            return true;
        }

        if (args.length != 8) {
            sendFancy(player, "Usage: /cptl start <radius> <startTime> <endTime> <interval> <x> <y> <z>");
            return true;
        }

        try {
            StartOptions options = new StartOptions(
                Integer.parseInt(args[1]),
                Long.parseLong(args[2]),
                Long.parseLong(args[3]),
                Integer.parseInt(args[4]),
                parseBlockLocation(player, args[5], args[6], args[7])
            );
            startTimelapse(player, options);
        } catch (IllegalArgumentException ex) {
            sendFancy(player, ex.getMessage());
        }

        return true;
    }

    boolean startTimelapse(Player player, StartOptions options) {
        if (api == null || !api.isEnabled()) {
            sendFancy(player, "CoreProtect API is unavailable or too old (requires API v11+).");
            return false;
        }

        if (options.radius() < 100 || options.radius() > 512) {
            sendFancy(player, "Radius must be between 100 and 512.");
            return false;
        }

        if (options.startTime() < 0 || options.endTime() < 0) {
            sendFancy(player, "startTime and endTime must be 0 or greater.");
            return false;
        }

        if (options.interval() <= 0) {
            sendFancy(player, "interval must be greater than 0.");
            return false;
        }

        if (undoThread != null && undoThread.isAlive()) {
            sendFancy(player, "Undo in progress. You must wait for this to finish.");
            return false;
        }
        if (rollbackThread != null && rollbackThread.isAlive()) {
            sendFancy(player, "Timelapse in progress. To stop it, use the `/cptl stop` command.");
            return false;
        }

        lastRadius = options.radius();
        lastLocation = options.center();

        long normalizedStartTime = options.startTime();
        long normalizedEndTime = options.endTime();
        if (normalizedEndTime < normalizedStartTime) {
            long temp = normalizedStartTime;
            normalizedStartTime = normalizedEndTime;
            normalizedEndTime = temp;
        }

        final long finalStartTime = normalizedStartTime;
        final long finalEndTime = normalizedEndTime;
        final int finalRadius = options.radius();
        final int finalInterval = options.interval();
        final Location finalCenter = options.center().clone();

        rollbackThread = new InterruptableThread(() -> {
            BossBar bossBar = Bukkit.createBossBar("Timelapse", BarColor.GREEN, BarStyle.SEGMENTED_10);
            bossBar.addPlayer(player);
            bossBar.setProgress(0);

            sendFancy(player, "Running initial rollback...");
            rollbackDepths.clear();

            long currentTime = System.currentTimeMillis() / 1000L;
            int initialRollbackDepth = toRollbackDepth(finalEndTime, currentTime);
            api.performRollback(initialRollbackDepth, null, null, null, null, null, finalRadius, finalCenter);
            rollbackDepths.add(initialRollbackDepth);

            if (rollbackThread.getInterrupt()) {
                sendFancy(player, "Stopped job. Run `/cptl undo` to undo those changes.");
                bossBar.removeAll();
                return;
            }

            sendFancy(player, "Now stepping through your interval...");

            long cursor = finalEndTime;
            while (cursor > finalStartTime) {
                if (rollbackThread.getInterrupt()) {
                    sendFancy(player, "Stopped job. Run `/cptl undo` to undo those changes.");
                    bossBar.removeAll();
                    return;
                }

                long nextCursor = Math.max(finalStartTime, cursor - finalInterval);
                int rollbackDepth = toRollbackDepth(nextCursor, currentTime);
                api.performRollback(rollbackDepth, null, null, null, null, null, finalRadius, finalCenter);
                rollbackDepths.add(rollbackDepth);

                if (finalEndTime == finalStartTime) {
                    bossBar.setProgress(1.0D);
                } else {
                    bossBar.setProgress((double) (finalEndTime - nextCursor) / (double) (finalEndTime - finalStartTime));
                }

                cursor = nextCursor;
            }

            sendFancy(player, "Job finished! Run `/cptl undo` to undo those changes.");
            bossBar.removeAll();
        });

        rollbackThread.start();
        return true;
    }

    private boolean handleWizard(CommandSender sender, String[] args) {
        Player player = requirePlayer(sender);
        if (player == null) {
            return true;
        }

        startWizardManager.handleWizardCommand(player, args);
        return true;
    }

    private boolean handleStop(CommandSender sender) {
        Player player = requirePlayer(sender);
        if (player == null) {
            return true;
        }

        if (rollbackThread == null || !rollbackThread.isAlive()) {
            sendFancy(player, "There is no running timelapse.");
            return true;
        }

        sendFancy(player, "Stopping as soon as possible, please wait...");
        rollbackThread.setInterrupt(true);
        return true;
    }

    private boolean handleUndo(CommandSender sender) {
        Player player = requirePlayer(sender);
        if (player == null) {
            return true;
        }

        if (rollbackThread != null && rollbackThread.isAlive()) {
            sendFancy(player, "Timelapse in progress.");
            return true;
        }
        if (undoThread != null && undoThread.isAlive()) {
            sendFancy(player, "Undo in progress. You must wait for this to finish.");
            return true;
        }
        if (rollbackDepths.isEmpty()) {
            sendFancy(player, "Nothing to undo.");
            return true;
        }
        if (api == null || !api.isEnabled()) {
            sendFancy(player, "CoreProtect API is unavailable or too old (requires API v11+).");
            return true;
        }

        undoThread = new Thread(() -> {
            sendFancy(player, "Starting undo. Please do not reload or stop the server.");
            BossBar bossBar = Bukkit.createBossBar("Undo", BarColor.RED, BarStyle.SEGMENTED_10);
            bossBar.addPlayer(player);

            bossBar.setProgress(0);
            int maxRollbackDepth = Collections.max(rollbackDepths);
            api.performRestore(maxRollbackDepth, null, null, null, null, null, lastRadius, lastLocation);
            bossBar.setProgress(1.0D);
            rollbackDepths.clear();
            sendFancy(player, "Undo complete!");
            bossBar.removeAll();
        });

        undoThread.start();
        return true;
    }

    private Player requirePlayer(CommandSender sender) {
        if (sender instanceof Player player) {
            return player;
        }

        sendFancy(sender, "This command can only be run by a player.");
        return null;
    }

    Location parseBlockLocation(Player player, String rawX, String rawY, String rawZ) {
        Location base = player.getLocation();
        World world = player.getWorld();
        double x = parseCoordinate(rawX, base.getX());
        double y = parseCoordinate(rawY, base.getY());
        double z = parseCoordinate(rawZ, base.getZ());
        return new Location(world, Math.floor(x), Math.floor(y), Math.floor(z));
    }

    private double parseCoordinate(String token, double baseValue) {
        if ("~".equals(token)) {
            return baseValue;
        }
        if (token.startsWith("~")) {
            return baseValue + Double.parseDouble(token.substring(1));
        }
        return Double.parseDouble(token);
    }

    private List<String> filterPrefix(List<String> values, String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return values;
        }

        String loweredPrefix = prefix.toLowerCase(Locale.ROOT);
        return values.stream()
            .filter(value -> value.startsWith(loweredPrefix))
            .toList();
    }

    private CoreProtectAPI getCoreProtect() {
        Plugin plugin = getServer().getPluginManager().getPlugin("CoreProtect");

        if (!(plugin instanceof CoreProtect coreProtectPlugin)) {
            return null;
        }

        CoreProtectAPI coreProtectAPI = coreProtectPlugin.getAPI();
        if (coreProtectAPI == null || !coreProtectAPI.isEnabled()) {
            return null;
        }

        if (coreProtectAPI.APIVersion() < 11) {
            return null;
        }

        return coreProtectAPI;
    }

    private int toRollbackDepth(long targetTimestamp, long nowTimestamp) {
        long rollbackDepth = Math.max(0, nowTimestamp - targetTimestamp);
        if (rollbackDepth > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int) rollbackDepth;
    }
}
