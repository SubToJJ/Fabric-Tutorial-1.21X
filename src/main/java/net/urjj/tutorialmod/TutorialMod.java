package net.urjj.tutorialmod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// very important comment
public class TutorialMod implements ModInitializer {
    public static final String MOD_ID = "tutorialmod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing TutorialMod...");

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            GlowCommand.register(dispatcher, registryAccess);
            LOGGER.info("Registered /glowall command");
        });
    }
}