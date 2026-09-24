package com.medinexz.notenoughresources.core;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

public class OreGenerationManager {

    private OreProfiler profiler;

    private boolean profiled = false;

    private Map<OreKey, OreData> ores;

    private static final OreGenerationManager INSTANCE =
        new OreGenerationManager();

    private OreGenerationManager() {
        ores = new HashMap<OreKey, OreData>();
        profiler = new OreProfiler();
    }

    public static OreGenerationManager getInstance() {
        return INSTANCE;
    }

    public void registerOre(OreData data) {
        OreKey key = new OreKey(
            data.getBlock(),
            data.getMetadata()
        );

        ores.put(key, data);
    }

    public OreData getOreData(Block block, int metadata) {
        OreKey key = new OreKey(block, metadata);

        return ores.get(key);
    }

    public OreGenerationProfile profileOre(
        World world,
        OreData oreData,
        int minX,
        int maxX,
        int minZ,
        int maxZ
    ) {
        OreGenerationProfile profile = profiler.profile(
            world,
            oreData,
            minX,
            maxX,
            minZ,
            maxZ
        );
        oreData.setGenerationProfile(profile);

        registerOre(oreData);

        return profile;
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {

        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        if (event.player.worldObj.isRemote) {
            return;
        }

        if (profiled) {
            return;
        }

        profiled = true;

        EntityPlayer player = event.player;

        int playerX = (int) player.posX;
        int playerZ = (int) player.posZ;

        for (OreData oreData : OreRegistry.getOres()) {

            profileOre(
                player.worldObj,
                oreData,
                playerX - 16,
                playerX + 16,
                playerZ - 16,
                playerZ + 16
            );
        }
    }
}
