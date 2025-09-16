package net.urjj.tutorialmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class TutorialModClient implements ClientModInitializer {
    private static boolean glowEnabled = false;

    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(literal("glowall")
                    .executes(context -> {
                        glowEnabled = !glowEnabled;
                        MinecraftClient client = MinecraftClient.getInstance();

                        if (client.world != null) {
                            for (Entity entity : client.world.getEntities()) {
                                entity.setGlowing(glowEnabled);
                            }
                            client.player.sendMessage(Text.literal("Client glow toggled " + (glowEnabled ? "ON" : "OFF")), false);
                        }

                        return 1;
                    }));
        });
    }
}