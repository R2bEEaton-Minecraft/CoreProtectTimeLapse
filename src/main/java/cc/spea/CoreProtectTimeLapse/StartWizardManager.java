package cc.spea.CoreProtectTimeLapse;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static cc.spea.CoreProtectTimeLapse.Helpers.sendFancy;

public class StartWizardManager implements Listener {
    private static final long SESSION_TIMEOUT_MILLIS = 10L * 60L * 1000L;

    private final JavaPlugin plugin;
    private final CommandManager commandManager;
    private final Map<UUID, WizardSession> sessions = new ConcurrentHashMap<>();
    private final SecureRandom secureRandom = new SecureRandom();
    private final ZoneId zoneId = ZoneId.systemDefault();
    private final Clock clock = Clock.system(zoneId);
    private final DateTimeFormatter summaryFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z")
        .withZone(zoneId);

    public StartWizardManager(JavaPlugin plugin, CommandManager commandManager) {
        this.plugin = plugin;
        this.commandManager = commandManager;
    }

    public void startCleanupTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, this::expireInactiveSessions, 20L * 60L, 20L * 60L);
    }

    public void start(Player player) {
        WizardSession session = new WizardSession(newToken());
        sessions.put(player.getUniqueId(), session);
        sendFancy(player, "Starting interactive timelapse setup. Type `cancel` at any time to exit.");
        prompt(player, session);
    }

    public void cancel(Player player) {
        WizardSession removed = sessions.remove(player.getUniqueId());
        if (removed == null) {
            sendFancy(player, "There is no active start wizard.");
            return;
        }

        sendFancy(player, "Start wizard cancelled.");
    }

    public void handleWizardCommand(Player player, String[] args) {
        if (args.length < 4) {
            sendFancy(player, "That wizard action is invalid. Run `/cptl start` to begin again.");
            return;
        }

        WizardSession session = sessions.get(player.getUniqueId());
        if (session == null) {
            sendFancy(player, "That wizard session has expired. Run `/cptl start` to begin again.");
            return;
        }

        if (!session.token.equals(args[1])) {
            sendFancy(player, "That wizard action is no longer valid. Run `/cptl start` to begin again.");
            return;
        }

        String stepName = session.step.name().toLowerCase(Locale.ROOT);
        if (!stepName.equalsIgnoreCase(args[2])) {
            sendFancy(player, "That wizard prompt is no longer current.");
            prompt(player, session);
            return;
        }

        String input = decodeChoice(String.join(" ", Arrays.copyOfRange(args, 3, args.length)));
        processInput(player, session, input);
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        WizardSession session = sessions.get(event.getPlayer().getUniqueId());
        if (session == null) {
            return;
        }

        event.setCancelled(true);
        Player player = event.getPlayer();
        String message = event.getMessage();
        Bukkit.getScheduler().runTask(plugin, () -> {
            WizardSession currentSession = sessions.get(player.getUniqueId());
            if (currentSession == null) {
                return;
            }

            processInput(player, currentSession, message);
        });
    }

    private void processInput(Player player, WizardSession session, String rawInput) {
        session.touch();
        String input = rawInput == null ? "" : rawInput.trim();
        String lowered = input.toLowerCase(Locale.ROOT);
        boolean negativeConfirmation = session.step == Step.CONFIRM && ("no".equals(lowered) || "n".equals(lowered));

        if ("cancel".equals(lowered) || negativeConfirmation) {
            sessions.remove(player.getUniqueId());
            sendFancy(player, "Start wizard cancelled.");
            return;
        }

        if ("back".equals(lowered)) {
            if (session.step == Step.RADIUS) {
                sendFancy(player, "Already at the first step.");
            } else {
                session.step = session.step.previous();
            }
            prompt(player, session);
            return;
        }

        try {
            switch (session.step) {
                case RADIUS -> handleRadius(player, session, input);
                case START_TIME -> handleStartTime(player, session, input);
                case END_TIME -> handleEndTime(player, session, input);
                case INTERVAL -> handleInterval(player, session, input);
                case CENTER -> handleCenter(player, session, input);
                case CONFIRM -> handleConfirm(player, session, lowered);
            }
        } catch (IllegalArgumentException ex) {
            sendFancy(player, ex.getMessage());
            prompt(player, session);
        }
    }

    private void handleRadius(Player player, WizardSession session, String input) {
        int radius;
        try {
            radius = Integer.parseInt(input);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Radius must be a whole number between 100 and 512.");
        }
        if (radius < 100 || radius > 512) {
            throw new IllegalArgumentException("Radius must be between 100 and 512.");
        }

        session.radius = radius;
        session.step = Step.START_TIME;
        prompt(player, session);
    }

    private void handleStartTime(Player player, WizardSession session, String input) {
        if ("default".equalsIgnoreCase(input) || "current".equalsIgnoreCase(input)) {
            throw new IllegalArgumentException("Start time has no default. Enter an exact time or relative time like 7d ago.");
        }

        session.startTime = TimeInputParser.parsePastOrNow(input, clock, zoneId);
        session.step = Step.END_TIME;
        prompt(player, session);
    }

    private void handleEndTime(Player player, WizardSession session, String input) {
        String value = input;
        if ("default".equalsIgnoreCase(value) || "current".equalsIgnoreCase(value)) {
            value = "now";
        }

        session.endTime = TimeInputParser.parsePastOrNow(value, clock, zoneId);
        session.step = Step.INTERVAL;
        prompt(player, session);
    }

    private void handleInterval(Player player, WizardSession session, String input) {
        String value = input;
        if ("default".equalsIgnoreCase(value)) {
            value = "1d";
        }

        session.interval = DurationInputParser.parseSeconds(value);
        session.step = Step.CENTER;
        prompt(player, session);
    }

    private void handleCenter(Player player, WizardSession session, String input) {
        Location center;
        if ("default".equalsIgnoreCase(input) || "current".equalsIgnoreCase(input)) {
            center = blockLocation(player.getLocation());
        } else {
            String[] parts = input.split("\\s+");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Center must be `current`, `default`, `~ ~ ~`, or three coordinates.");
            }
            center = commandManager.parseBlockLocation(player, parts[0], parts[1], parts[2]);
        }

        session.center = center;
        session.step = Step.CONFIRM;
        prompt(player, session);
    }

    private void handleConfirm(Player player, WizardSession session, String loweredInput) {
        if (!"yes".equals(loweredInput) && !"y".equals(loweredInput) && !"confirm".equals(loweredInput)) {
            throw new IllegalArgumentException("Type `yes` to start, `back` to change a value, or `cancel` to exit.");
        }

        boolean started = commandManager.startTimelapse(player, new StartOptions(
            session.radius,
            session.startTime,
            session.endTime,
            session.interval,
            session.center
        ));
        if (started) {
            sessions.remove(player.getUniqueId());
        } else {
            prompt(player, session);
        }
    }

    private void prompt(Player player, WizardSession session) {
        session.touch();
        switch (session.step) {
            case RADIUS -> {
                sendFancy(player, "Step 1/6: Enter radius in blocks (`100`-`512`).");
                sendChoices(player, session, "100", "256", "512");
            }
            case START_TIME -> {
                sendFancy(player, "Step 2/6: Enter start time. Timezone: " + zoneId + ". Examples: `2026-04-01 18:30`, `7d ago`, or Unix seconds.");
                sendChoices(player, session, "1d ago", "7d ago", "30d ago");
            }
            case END_TIME -> {
                sendFancy(player, "Step 3/6: Enter end time. Use `default` for now.");
                sendChoices(player, session, "now", "1d ago", "7d ago");
            }
            case INTERVAL -> {
                sendFancy(player, "Step 4/6: Enter interval between snapshots. Use `default` for 1d.");
                sendChoices(player, session, "1h", "6h", "1d", "1w");
            }
            case CENTER -> {
                sendFancy(player, "Step 5/6: Enter center location. Use `current`, `~ ~ ~`, absolute coordinates, or relative coordinates like `~10 ~ ~-5`.");
                sendChoices(player, session, "current");
            }
            case CONFIRM -> {
                sendFancy(player, "Step 6/6: Confirm destructive timelapse start.");
                sendSummary(player, session);
                sendChoices(player, session, "yes", "back", "cancel");
            }
        }
    }

    private void sendSummary(Player player, WizardSession session) {
        long normalizedStartTime = Math.min(session.startTime, session.endTime);
        long normalizedEndTime = Math.max(session.startTime, session.endTime);
        Location center = session.center;

        sendFancy(player, "Radius: " + session.radius);
        sendFancy(player, "Start: " + normalizedStartTime + " (" + formatEpoch(normalizedStartTime) + ")");
        sendFancy(player, "End: " + normalizedEndTime + " (" + formatEpoch(normalizedEndTime) + ")");
        sendFancy(player, "Interval: " + session.interval + " seconds");
        sendFancy(player, "Center: " + center.getWorld().getName() + " " + center.getBlockX() + " " + center.getBlockY() + " " + center.getBlockZ());
    }

    private void sendChoices(Player player, WizardSession session, String... choices) {
        TextComponent root = prefixComponent("Click: ");
        for (String choice : choices) {
            TextComponent option = new TextComponent("[" + choice + "]");
            option.setColor(ChatColor.AQUA);
            option.setClickEvent(new ClickEvent(
                ClickEvent.Action.RUN_COMMAND,
                "/cptl wizard " + session.token + " " + session.step.name().toLowerCase(Locale.ROOT) + " " + encodeChoice(choice)
            ));
            root.addExtra(option);
            root.addExtra(" ");
        }

        TextComponent hint = new TextComponent("or type a value. Type back/cancel as needed.");
        hint.setColor(ChatColor.GRAY);
        root.addExtra(hint);
        player.spigot().sendMessage(root);
    }

    private TextComponent prefixComponent(String message) {
        TextComponent root = new TextComponent("[");
        root.setColor(ChatColor.WHITE);

        TextComponent tag = new TextComponent("CPTL");
        tag.setColor(ChatColor.RED);
        root.addExtra(tag);

        TextComponent suffix = new TextComponent("] " + message);
        suffix.setColor(ChatColor.WHITE);
        root.addExtra(suffix);
        return root;
    }

    private void expireInactiveSessions() {
        long now = System.currentTimeMillis();
        sessions.entrySet().removeIf(entry -> {
            if (now - entry.getValue().lastActivityMillis <= SESSION_TIMEOUT_MILLIS) {
                return false;
            }

            Player player = Bukkit.getPlayer(entry.getKey());
            if (player != null && player.isOnline()) {
                sendFancy(player, "Start wizard expired after 10 minutes of inactivity.");
            }
            return true;
        });
    }

    private String newToken() {
        byte[] bytes = new byte[12];
        secureRandom.nextBytes(bytes);
        StringBuilder token = new StringBuilder(bytes.length * 2);
        for (byte value : bytes) {
            token.append(String.format("%02x", value));
        }
        return token.toString();
    }

    private Location blockLocation(Location location) {
        return new Location(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    private String formatEpoch(long epochSecond) {
        return summaryFormatter.format(Instant.ofEpochSecond(epochSecond));
    }

    private String encodeChoice(String choice) {
        return choice.replace(" ", "_");
    }

    private String decodeChoice(String choice) {
        return choice.replace("_", " ");
    }

    private enum Step {
        RADIUS,
        START_TIME,
        END_TIME,
        INTERVAL,
        CENTER,
        CONFIRM;

        private Step previous() {
            return switch (this) {
                case RADIUS -> RADIUS;
                case START_TIME -> RADIUS;
                case END_TIME -> START_TIME;
                case INTERVAL -> END_TIME;
                case CENTER -> INTERVAL;
                case CONFIRM -> CENTER;
            };
        }
    }

    private static final class WizardSession {
        private final String token;
        private Step step = Step.RADIUS;
        private int radius;
        private long startTime;
        private long endTime;
        private int interval;
        private Location center;
        private long lastActivityMillis = System.currentTimeMillis();

        private WizardSession(String token) {
            this.token = token;
        }

        private void touch() {
            lastActivityMillis = System.currentTimeMillis();
        }
    }
}
