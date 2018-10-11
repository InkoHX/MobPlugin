package nukkitcoders.mobplugin;

import cn.nukkit.IPlayer;
import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import nukkitcoders.mobplugin.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:kniffman@googlemail.com">Michael Gertz (mige)</a>
 */
public class SpawnTask implements Runnable {

    private static final int MAX_SPAWN_RADIUS = 10;

    private static final int MIN_SPAWN_RADIUS = 3;

    private MobPlugin plugin;

    public SpawnTask(MobPlugin plugin) {
        this.plugin = plugin;
    }

    /*
     * (@Override)
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        List<IPlayer> allRegisteredUsers = this.plugin.getAllRegisteredPlayers();

        // we only perform for offline players ...
        List<Player> onlinePlayers = new ArrayList<>();
        for (IPlayer iPlayer : allRegisteredUsers) {
            if (iPlayer instanceof Player) {
                onlinePlayers.add((Player) iPlayer);
            }
        }

        // now that we have all online players, do it for each player online ...
        for (Player player : onlinePlayers) {
            Position pos = player.getPosition();

            // x - longitude, z - latitude, y - high/low (64 is sea level)
            Position spawnPosition = new Position(pos.x, pos.y, pos.z);
            getSpawnPosition(spawnPosition, new int[0], player.getLevel());
        }
    }

    private void getSpawnPosition(Position startSpawnPosition, int[] notAllowedBlockIds, Level level) {
        int spawnX = (int) startSpawnPosition.x; // east/west (increase = west, decrease = east)
        int spawnZ = (int) startSpawnPosition.z; // north/south (increase = south, decrease = north)

        int minSpawnX1 = spawnX - MIN_SPAWN_RADIUS;
        int minSpawnX2 = spawnX + MIN_SPAWN_RADIUS;
        int maxSpawnX1 = spawnX - MAX_SPAWN_RADIUS;
        int maxSpawnX2 = spawnX + MAX_SPAWN_RADIUS;

        int minSpawnZ1 = spawnZ - MIN_SPAWN_RADIUS;
        int minSpawnZ2 = spawnZ + MIN_SPAWN_RADIUS;
        int maxSpawnZ1 = spawnZ - MAX_SPAWN_RADIUS;
        int maxSpawnZ2 = spawnZ + MAX_SPAWN_RADIUS;

        // now we've our x/z boundaries ... let's start to check the blocks ...
        boolean found = false;
        int findTries = 0;
        // find a randomly choosing starting point ...
        boolean startEast = Utils.rand();
        boolean startNorth = Utils.rand();

        int x = startEast ? Utils.rand(minSpawnX1, maxSpawnX1) : Utils.rand(minSpawnX2, maxSpawnX2);
        int z = startNorth ? Utils.rand(minSpawnZ1, maxSpawnZ1) : Utils.rand(minSpawnZ2, maxSpawnZ2);

        while (!found && findTries < 5) {
            int blockId = level.getBlockIdAt(x, spawnZ, z);
            if (isBlockAllowed(blockId, notAllowedBlockIds) && isEnoughAirAboveBlock(x, spawnZ, z, level)) {
                found = true;
            }
            findTries++;
        }

        if (found) {
            new Position(x, spawnZ, z);
        }

    }

    private boolean isBlockAllowed(int blockId, int[] notAllowedBlockIds) {
        if (notAllowedBlockIds.length > 0) {
            for (int notAllowed : notAllowedBlockIds) {
                if (notAllowed == blockId) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isEnoughAirAboveBlock(int x, int y, int z, Level level) {
        int maxTestY = y + 2;
        int addY = 1;
        while ((y + addY) <= maxTestY) {
            int blockId = level.getBlockIdAt(x, y + addY, z);
            if (blockId != Block.AIR) {
                return false;
            }
            addY++;
        }
        return true;
    }
}
