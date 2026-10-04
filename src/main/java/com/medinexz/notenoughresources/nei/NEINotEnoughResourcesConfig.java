package com.medinexz.notenoughresources.nei;

import codechicken.nei.api.API;
import codechicken.nei.api.IConfigureNEI;

import com.medinexz.notenoughresources.NotEnoughResources;
import com.medinexz.notenoughresources.gui.OreGenerationNEIHandler;

public class NEINotEnoughResourcesConfig
    implements IConfigureNEI {

    @Override
    public void loadConfig() {
        OreGenerationNEIHandler handler = new OreGenerationNEIHandler();
        API.registerUsageHandler(handler);
        API.registerRecipeHandler(handler);
    }

    @Override
    public String getName() {
        return NotEnoughResources.MODNAME;
    }

    @Override
    public String getVersion() {
        return "1.0";
    }
}
