package com.medinexz.notenoughresources.nei;

import codechicken.nei.api.API;
import codechicken.nei.api.IConfigureNEI;

import com.medinexz.notenoughresources.gui.OreGenerationNEIHandler;

public class NEINotEnoughResourcesConfig
    implements IConfigureNEI {

    @Override
    public void loadConfig() {

        System.out.println(
            "=== NER NEI CONFIG LOADED ==="
        );

        API.registerUsageHandler(
            new OreGenerationNEIHandler()
        );
    }


    @Override
    public String getName() {

        return "NotEnoughResources Continuation";
    }


    @Override
    public String getVersion() {

        return "1.0";
    }
}
