package nukkitcoders.mobplugin.entities.block;

import cn.nukkit.Player;
import cn.nukkit.block.BlockID;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.blockentity.BlockEntitySpawnable;
import cn.nukkit.entity.Entity;
import cn.nukkit.level.Position;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.ShortTag;
import nukkitcoders.mobplugin.MobPlugin;
import nukkitcoders.mobplugin.utils.Utils;

import java.util.ArrayList;

public class BlockEntitySpawner extends BlockEntitySpawnable {

    private int entityId;
    private int spawnRange;
    private int maxNearbyEntities;
    private int requiredPlayerRange;

    private int delay = 0;

    private int minSpawnDelay;
    private int maxSpawnDelay;

    public static final String TAG_ID = "id";
    public static final String TAG_X = "x";
    public static final String TAG_Y = "y";
    public static final String TAG_Z = "z";
    public static final String TAG_ENTITY_ID = "EntityId";
    private static final String TAG_SPAWN_RANGE = "SpawnRange";
    private static final String TAG_MIN_SPAWN_DELAY = "MinSpawnDelay";
    private static final String TAG_MAX_SPAWN_DELAY = "MaxSpawnDelay";
    private static final String TAG_MAX_NEARBY_ENTITIES = "MaxNearbyEntities";
    private static final String TAG_REQUIRED_PLAYER_RANGE = "RequiredPlayerRange";

    private static final short SPAWN_RANGE = 8;
    private static final short MIN_SPAWN_DELAY = 200;
    private static final short MAX_SPAWN_DELAY = 5000;
    private static final short MAX_NEARBY_ENTITIES = 20;
    private static final short REQUIRED_PLAYER_RANGE = 16;

    public BlockEntitySpawner(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
        this.entityId = this.namedTag.getInt(TAG_ENTITY_ID);
    }

    @Override
    protected void initBlockEntity() {
        /* for some reason entity id changes to -1
        if (this.namedTag.contains(TAG_ENTITY_ID)) {
            this.entityId = this.namedTag.getInt(TAG_ENTITY_ID);
        }
        */

        if (!this.namedTag.contains(TAG_SPAWN_RANGE) || !(this.namedTag.get(TAG_SPAWN_RANGE) instanceof ShortTag)) {
            this.namedTag.putShort(TAG_SPAWN_RANGE, SPAWN_RANGE);
        }

        if (!this.namedTag.contains(TAG_MIN_SPAWN_DELAY) || !(this.namedTag.get(TAG_MIN_SPAWN_DELAY) instanceof ShortTag)) {
            this.namedTag.putShort(TAG_MIN_SPAWN_DELAY, MIN_SPAWN_DELAY);
        }

        if (!this.namedTag.contains(TAG_MAX_SPAWN_DELAY) || !(this.namedTag.get(TAG_MAX_SPAWN_DELAY) instanceof ShortTag)) {
            this.namedTag.putShort(TAG_MAX_SPAWN_DELAY, MAX_SPAWN_DELAY);
        }

        if (!this.namedTag.contains(TAG_MAX_NEARBY_ENTITIES) || !(this.namedTag.get(TAG_MAX_NEARBY_ENTITIES) instanceof ShortTag)) {
            this.namedTag.putShort(TAG_MAX_NEARBY_ENTITIES, MAX_NEARBY_ENTITIES);
        }

        if (!this.namedTag.contains(TAG_REQUIRED_PLAYER_RANGE) || !(this.namedTag.get(TAG_REQUIRED_PLAYER_RANGE) instanceof ShortTag)) {
            this.namedTag.putShort(TAG_REQUIRED_PLAYER_RANGE, REQUIRED_PLAYER_RANGE);
        }

        this.spawnRange = this.namedTag.getShort(TAG_SPAWN_RANGE);
        this.minSpawnDelay = this.namedTag.getInt(TAG_MIN_SPAWN_DELAY);
        this.maxSpawnDelay = this.namedTag.getInt(TAG_MAX_SPAWN_DELAY);
        this.maxNearbyEntities = this.namedTag.getShort(TAG_MAX_NEARBY_ENTITIES);
        this.requiredPlayerRange = this.namedTag.getShort(TAG_REQUIRED_PLAYER_RANGE);

        this.scheduleUpdate();
        super.initBlockEntity();
    }

    @Override
    public boolean onUpdate() {
        if (this.closed) {
            return false;
        }

        if (this.delay++ >= Utils.rand(this.minSpawnDelay, this.maxSpawnDelay)) {
            this.delay = 0;

            ArrayList<Entity> list = new ArrayList<>();
            boolean isValid = false;
            for (Entity entity : this.level.getEntities()) {
                if (entity.distance(this) <= this.requiredPlayerRange) {
                    if (entity instanceof Player) {
                        isValid = true;
                    }
                    list.add(entity);
                }
            }

            if (isValid && list.size() <= this.maxNearbyEntities) {
                Position pos = new Position
                        (
                                this.x + Utils.rand(-this.spawnRange, this.spawnRange),
                                this.y,
                                this.z + Utils.rand(-this.spawnRange, this.spawnRange),
                                this.level
                        );
                Entity entity = MobPlugin.create(this.entityId, pos);
                if (entity == null) return true;
                entity.spawnToAll();
            }
        }
        return true;
    }

    @Override
    public void saveNBT() {
        super.saveNBT();

        this.namedTag.putInt(TAG_ENTITY_ID, this.entityId);
        this.namedTag.putString(TAG_ID, "MobSpawner"); // fix get save id
        this.namedTag.putShort(TAG_SPAWN_RANGE, this.spawnRange);
        this.namedTag.putShort(TAG_MIN_SPAWN_DELAY, this.minSpawnDelay);
        this.namedTag.putShort(TAG_MAX_SPAWN_DELAY, this.maxSpawnDelay);
        this.namedTag.putShort(TAG_MAX_NEARBY_ENTITIES, this.maxNearbyEntities);
        this.namedTag.putShort(TAG_REQUIRED_PLAYER_RANGE, this.requiredPlayerRange);
    }

    @Override
    public CompoundTag getSpawnCompound() {
        return new CompoundTag()
                .putString(TAG_ID, BlockEntity.MOB_SPAWNER)
                .putInt(TAG_ENTITY_ID, this.entityId)
                .putInt(TAG_X, (int) this.x)
                .putInt(TAG_Y, (int) this.y)
                .putInt(TAG_Z, (int) this.z);
    }

    @Override
    public boolean isBlockEntityValid() {
        return this.getBlock().getId() == BlockID.MONSTER_SPAWNER;
    }

    public int getSpawnEntityType() {
        return this.entityId;
    }

    public void setSpawnEntityType(int entityId) {
        this.entityId = entityId;
        this.spawnToAll();
    }

    public void setMinSpawnDelay(int minDelay) {
        if (minDelay > this.maxSpawnDelay) {
            return;
        }

        this.minSpawnDelay = minDelay;
    }

    public void setMaxSpawnDelay(int maxDelay) {
        if (this.minSpawnDelay > maxDelay) {
            return;
        }

        this.maxSpawnDelay = maxDelay;
    }

    public void setSpawnDelay(int minDelay, int maxDelay) {
        if (minDelay > maxDelay) {
            return;
        }

        this.minSpawnDelay = minDelay;
        this.maxSpawnDelay = maxDelay;
    }

    public void setRequiredPlayerRange(int range) {
        this.requiredPlayerRange = range;
    }

    public void setMaxNearbyEntities(int count) {
        this.maxNearbyEntities = count;
    }
}
