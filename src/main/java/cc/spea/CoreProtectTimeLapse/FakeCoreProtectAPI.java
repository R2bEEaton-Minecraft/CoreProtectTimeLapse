package cc.spea.CoreProtectTimeLapse;

import net.coreprotect.config.Config;
import net.coreprotect.database.Database;
import net.coreprotect.database.Lookup;
import net.coreprotect.database.Rollback;
import net.coreprotect.utility.Util;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FakeCoreProtectAPI {
    public List<String[]> performRollback(long startTime, long endTime, List<String> restrictUsers, List<String> excludeUsers, List<Object> restrictBlocks, List<Object> excludeBlocks, List<Integer> actionList, int radius, Location radiusLocation) {
        if (Config.getGlobal().API_ENABLED) {
            return processData(startTime, endTime, radius, radiusLocation, parseList(restrictBlocks), parseList(excludeBlocks), restrictUsers, excludeUsers, actionList, 0, 2, -1, -1, false);
        }
        return null;
    }

    public boolean isEnabled() {
        return Config.getGlobal().API_ENABLED;
    }

    public int APIVersion() {
        return 9;
    }

    private static Map<Object, Boolean> parseList(List<Object> list) {
        Map<Object, Boolean> result = new HashMap<>();

        if (list != null) {
            for (Object value : list) {
                if (value instanceof Material || value instanceof EntityType) {
                    result.put(value, false);
                }
                else if (value instanceof Integer) {
                    Material material = Util.getType((Integer) value);
                    result.put(material, false);
                }
            }
        }

        return result;
    }

    private List<String[]> processData(long startTime, long endTime, int radius, Location location, Map<Object, Boolean> restrictBlocksMap, Map<Object, Boolean> excludeBlocks, List<String> restrictUsers, List<String> excludeUsers, List<Integer> actionList, int action, int lookup, int offset, int rowCount, boolean useLimit) {
        // You need to either specify time/radius or time/user
        List<String[]> result = new ArrayList<>();
        List<String> uuids = new ArrayList<>();

        if (restrictUsers == null) {
            restrictUsers = new ArrayList<>();
        }

        if (excludeUsers == null) {
            excludeUsers = new ArrayList<>();
        }

        if (actionList == null) {
            actionList = new ArrayList<>();
        }

        List<Object> restrictBlocks = new ArrayList<>(restrictBlocksMap.keySet());
        if (actionList.size() == 0 && restrictBlocks.size() > 0) {
            boolean addedMaterial = false;
            boolean addedEntity = false;

            for (Object argBlock : restrictBlocks) {
                if (argBlock instanceof Material && !addedMaterial) {
                    actionList.add(0);
                    actionList.add(1);
                    addedMaterial = true;
                }
                else if (argBlock instanceof EntityType && !addedEntity) {
                    actionList.add(3);
                    addedEntity = true;
                }
            }
        }

        if (actionList.size() == 0) {
            actionList.add(0);
            actionList.add(1);
        }

        actionList.removeIf(actionListItem -> actionListItem > 3);

        if (restrictUsers.isEmpty()) {
            restrictUsers.add("#global");
        }

        if (radius < 1) {
            radius = -1;
        }

        if (restrictUsers.contains("#global") && radius == -1) {
            return null;
        }

        if (radius > -1 && location == null) {
            return null;
        }

        try (Connection connection = Database.getConnection(false, 1000)) {
            if (connection != null) {
                Statement statement = connection.createStatement();
                boolean restrictWorld = false;

                if (radius > 0) {
                    restrictWorld = true;
                }

                if (location == null) {
                    restrictWorld = false;
                }

                Integer[] argRadius = null;
                if (location != null && radius > 0) {
                    int xMin = location.getBlockX() - radius;
                    int xMax = location.getBlockX() + radius;
                    int zMin = location.getBlockZ() - radius;
                    int zMax = location.getBlockZ() + radius;
                    argRadius = new Integer[] { radius, xMin, xMax, null, null, zMin, zMax, 0 };
                }

                if (lookup == 1) {
                    if (location != null) {
                        restrictWorld = true;
                    }

                    if (useLimit) {
                        result = Lookup.performPartialLookup(statement, null, uuids, restrictUsers, restrictBlocks, excludeBlocks, excludeUsers, actionList, location, argRadius, null, startTime, endTime, offset, rowCount, restrictWorld, true);
                    }
                    else {
                        result = Lookup.performLookup(statement, null, uuids, restrictUsers, restrictBlocks, excludeBlocks, excludeUsers, actionList, location, argRadius, startTime, endTime, restrictWorld, true);
                    }
                }
                else {
                    if (!Bukkit.isPrimaryThread()) {
                        boolean verbose = false;
                        result = Rollback.performRollbackRestore(statement, null, uuids, restrictUsers, null, restrictBlocks, excludeBlocks, excludeUsers, actionList, location, argRadius, startTime, endTime, restrictWorld, false, verbose, action, 0);
                    }
                }

                statement.close();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
}
