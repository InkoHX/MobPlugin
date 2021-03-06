package nukkitcoders.mobplugin.entities.monster.flying;

import cn.nukkit.entity.Entity;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import nukkitcoders.mobplugin.entities.monster.FlyingMonster;

public class EnderDragon extends FlyingMonster {

    public static final int NETWORK_ID = 53;

    public EnderDragon(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    public float getWidth() {
        return 13f;
    }

    @Override
    public float getHeight() {
        return 4f;
    }

    @Override
    public void initEntity() {
        super.initEntity();
        this.setMaxHealth(200);
        this.setHealth(200);
    }

    @Override
    public int getKillExperience() {
        for (int i = 0; i < 167; ) {
            this.level.dropExpOrb(this, 3);
            i++;
        }
        return 0;
    }

    @Override
    public void attackEntity(Entity player) {
        //TODO
    }

    @Override
    public String getName() {
        return "Ender Dragon";
    }
}
