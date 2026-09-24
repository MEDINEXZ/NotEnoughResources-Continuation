package com.medinexz.notenoughresources.core;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;

public class OreProfilerTest {

    private boolean tested = false;

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {

        // Выполняем только в конце тика
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        // Выполняем только на серверной стороне
        if (event.player.worldObj.isRemote) {
            return;
        }

        // Выполняем профилирование только один раз
        if (tested) {
            return;
        }

        // Сразу ставим true, чтобы профилирование
        // не запустилось повторно
        tested = true;

        System.out.println("OreProfilerTest: PlayerTick");

        EntityPlayer player = event.player;

        int playerX = (int) player.posX;
        int playerZ = (int) player.posZ;

        OreData ironOre = new OreData(
            Blocks.iron_ore,
            0,
            0,
            64
        );

        OreProfiler profiler = new OreProfiler();

        OreGenerationProfile profile = profiler.profile(
            player.worldObj,
            ironOre,
            playerX - 16,
            playerX + 16,
            playerZ - 16,
            playerZ + 16
        );

        System.out.println("=== ORE PROFILER FINISHED ===");

        for (int y = profile.getMinY(); y <= profile.getMaxY(); y++) {
            System.out.println(
                "Iron Ore Y=" + y +
                    " probability=" + profile.getProbability(y)
            );
        }
    }
}
