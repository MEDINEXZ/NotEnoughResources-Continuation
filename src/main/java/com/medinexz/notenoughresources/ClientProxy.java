package com.medinexz.notenoughresources;

import com.medinexz.notenoughresources.nei.NEIHandlerInfoRegistrar;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.common.MinecraftForge;

public class ClientProxy extends CommonProxy {

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);

        // NEI posts NEIRegisterHandlerInfosEvent at load-complete; subscribe before that
        if (Loader.isModLoaded("NotEnoughItems")) {
            MinecraftForge.EVENT_BUS.register(new NEIHandlerInfoRegistrar());
        }
    }

    // Override CommonProxy methods here, if you want a different behaviour on the client (e.g. registering renders).
    // Don't forget to call the super methods as well.

}
