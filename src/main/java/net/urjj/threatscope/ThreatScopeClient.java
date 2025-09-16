package net.urjj.threatscope;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class ThreatScopeClient implements ClientModInitializer {
    private static boolean scopeEnabled = false;

    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(literal("scope")
                    .executes(context -> {
                        scopeEnabled = !scopeEnabled;
                        MinecraftClient.getInstance().player.sendMessage(Text.literal("ThreatScope " + (scopeEnabled ? "enabled" : "disabled")), false);
                        return 1;
                    }));
        });

        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            if (!scopeEnabled) return;

            MinecraftClient client = MinecraftClient.getInstance();
            TextRenderer textRenderer = client.textRenderer;

            int x = 10;
            int y = 10;

            for (Entity entity : client.world.getEntities()) {
                if (!(entity instanceof PlayerEntity) || entity == client.player) continue;

                PlayerEntity target = (PlayerEntity) entity;
                boolean sprinting = target.isSprinting();
                boolean sneaking = target.isSneaking();
                ItemStack weapon = target.getMainHandStack();
                boolean armed = !weapon.isEmpty();
                float distance = (float) target.getPos().distanceTo(client.player.getPos());

                String threatLevel = sprinting && armed ? "AGGRESSIVE" : sneaking ? "CAUTIOUS" : "PASSIVE";
                int color = threatLevel.equals("AGGRESSIVE") ? 0xFF0000 : threatLevel.equals("CAUTIOUS") ? 0xFFFF00 : 0x00FF00;

                String name = target.getName().getString();
                String weaponName = armed ? weapon.getName().getString() : "Unarmed";
                int health = (int) ((LivingEntity) target).getHealth();
                String overlay = String.format("%s [%d❤] %s (%.1fm)", name, health, weaponName, distance);

                drawContext.drawText(textRenderer, overlay, x, y, color, false);
                y += 12;
            }
        });
    }
}