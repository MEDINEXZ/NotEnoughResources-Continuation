package com.medinexz.notenoughresources.integration.thaumcraft;

import com.medinexz.notenoughresources.NotEnoughResources;
import com.medinexz.notenoughresources.core.OreRegistry;
import com.medinexz.notenoughresources.integration.INERIntegration;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.versioning.ArtifactVersion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Thaumcraft 4 ecosystem: Thaumcraft itself and the addons installed next to it.
 *
 * <p>NERIntegrationManager loads this class only when Thaumcraft is present.  It
 * then finds the installed mods that require Thaumcraft (from their FML
 * metadata) and loads the integration module of each one it knows, by class
 * name, so a module is never loaded for an addon that is absent.  A module is
 * skipped if a mod its addon requires is missing.</p>
 *
 * <p>To support another addon: add an {@link INERIntegration} class to the
 * {@code addons} package and a row to {@link #MODULES}.</p>
 */
public class ThaumcraftEcosystemManager implements INERIntegration {

    public static final String THAUMCRAFT = "Thaumcraft";

    private static final String ADDONS = "com.medinexz.notenoughresources.integration.thaumcraft.addons.";

    // { addon mod id, integration module class }
    private static final String[][] MODULES = {
        { "ThaumicTinkerer",    ADDONS + "ThaumicTinkererIntegration" },
        { "ThaumicHorizons",    ADDONS + "ThaumicHorizonsIntegration" },
        { "ThaumicExploration", ADDONS + "ThaumicExplorationIntegration" },
        { "thaumicenergistics", ADDONS + "ThaumicEnergisticsIntegration" },
        { "ThaumicEquivalence", ADDONS + "ThaumicEquivalenceIntegration" },
        { "ForbiddenMagic",     ADDONS + "ForbiddenMagicIntegration" },
        { "gadomancy",          ADDONS + "GadomancyIntegration" },
        { "TaintedMagic",       ADDONS + "TaintedMagicIntegration" },
        { "Automagy",           ADDONS + "AutomagyIntegration" },
        { "WitchingGadgets",    ADDONS + "WitchingGadgetsIntegration" },
        { "NodalMechanics",     ADDONS + "NodalMechanicsIntegration" },
        { "thaumicbases",       ADDONS + "ThaumicBasesIntegration" },
        { "TWarden",            ADDONS + "ThaumicWardenIntegration" },
        { "thaumrev",           ADDONS + "ThaumicRevelationsIntegration" },
    };

    // Mods that require Thaumcraft but were checked to have nothing a module could
    // register: the NEI / scanning companions, and addons without world generation
    // (Thaumic Concilium and Thaumic Additions only build a structure)
    private static final List<String> NOTHING_TO_INTEGRATE = Arrays.asList(
        "thaumcraftneiplugin", "tcinventoryscan", "thaumicdyes", "ForgottenRelics",
        "EMT", "magianaturalis", "technom", "ThaumcraftMobAspects", "ThaumicConcilium",
        "thaumicexpansion", "thaumicinfusion", "WarpTheory", "thaumicadditions",
        "salisarcana", "thaumicboots", "thaumicinsurgence");

    // Requirements every mod has; they say nothing about an addon's own dependencies
    private static final List<String> PLATFORM = Arrays.asList("Forge", "FML", "mcp");

    @Override
    public String getModId() {
        return THAUMCRAFT;
    }

    @Override
    public void registerResources() {
        ModContainer thaumcraft = Loader.instance().getIndexedModList().get(THAUMCRAFT);
        NotEnoughResources.LOG.info("=== NER THAUMCRAFT ECOSYSTEM ===");
        NotEnoughResources.LOG.info("Thaumcraft " + thaumcraft.getVersion() + ": "
            + load(new ThaumcraftIntegration()) + " world generation resources");

        Map<String, String> modules = new HashMap<String, String>();
        for (String[] module : MODULES) modules.put(module[0], module[1]);

        for (ModContainer addon : findAddons(modules)) {
            String label = addon.getName() + " (" + addon.getModId() + " " + addon.getVersion() + ")";
            String moduleClass = modules.get(addon.getModId());

            if (moduleClass == null) {
                if (NOTHING_TO_INTEGRATE.contains(addon.getModId())) {
                    NotEnoughResources.LOG.info("NOTHING TO INTEGRATE  " + label);
                } else {
                    NotEnoughResources.LOG.warn("Unsupported Thaumcraft addon: " + label
                        + " - no integration module; see ThaumcraftEcosystemManager.MODULES");
                }
                continue;
            }

            List<String> required = requiredMods(addon);
            List<String> missing = new ArrayList<String>();
            for (String modId : required) {
                if (!Loader.isModLoaded(modId)) missing.add(modId);
            }
            if (!missing.isEmpty()) {
                NotEnoughResources.LOG.info("SKIPPED  " + label + ", requires " + required + ", missing " + missing);
                continue;
            }

            try {
                int registered = load((INERIntegration) Class.forName(moduleClass).newInstance());
                NotEnoughResources.LOG.info("LOADED   " + label + ", requires " + required + ": "
                    + (registered == 0 ? "no world generation" : registered + " world generation resources"));
            } catch (Throwable t) {
                NotEnoughResources.LOG.warn("FAILED   " + label, t);
            }
        }
    }

    /** @return the number of resources the integration registered */
    private static int load(INERIntegration integration) {
        int registeredBefore = OreRegistry.getOres().size();
        integration.registerResources();
        return OreRegistry.getOres().size() - registeredBefore;
    }

    /** The installed mods that have a module here or require Thaumcraft. */
    private static List<ModContainer> findAddons(Map<String, String> modules) {
        List<ModContainer> addons = new ArrayList<ModContainer>();
        for (ModContainer mod : Loader.instance().getActiveModList()) {
            if (THAUMCRAFT.equals(mod.getModId())) continue;

            // Only a required dependency: many unrelated mods merely load after Thaumcraft
            if (modules.containsKey(mod.getModId()) || requiresThaumcraft(mod)) addons.add(mod);
        }
        return addons;
    }

    private static boolean requiresThaumcraft(ModContainer mod) {
        if (mod.getRequirements() == null) return false;
        for (ArtifactVersion requirement : mod.getRequirements()) {
            if (THAUMCRAFT.equalsIgnoreCase(requirement.getLabel())) return true;
        }
        return false;
    }

    /** The mods the addon itself declares as required. */
    private static List<String> requiredMods(ModContainer addon) {
        List<String> required = new ArrayList<String>();
        required.add(THAUMCRAFT);
        if (addon.getRequirements() == null) return required;

        for (ArtifactVersion requirement : addon.getRequirements()) {
            String modId = requirement.getLabel();
            if (modId != null && !PLATFORM.contains(modId) && !required.contains(modId)) required.add(modId);
        }
        return required;
    }
}
