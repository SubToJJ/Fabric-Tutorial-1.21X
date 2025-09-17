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
import net.minecraft.util.math.Vec3d;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class ThreatScopeClient implements ClientModInitializer {
    private static boolean scopeEnabled = false;
    private static boolean radarEnabled = false;

    private static final int RADAR_RADIUS = 60;
    private static final int DETECTION_RANGE = 32;

    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(literal("scope")
                    .executes(context -> {
                        scopeEnabled = !scopeEnabled;
                        MinecraftClient.getInstance().player.sendMessage(Text.literal("ThreatScope " + (scopeEnabled ? "enabled" : "disabled")), false);
                        return 1;
                    }));

            dispatcher.register(literal("scopemap")
                    .executes(context -> {
                        radarEnabled = !radarEnabled;
                        MinecraftClient.getInstance().player.sendMessage(Text.literal("Radar " + (radarEnabled ? "enabled" : "disabled")), false);
                        return 1;
                    }));
        });

        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null || client.world == null) return;

            if (scopeEnabled) {
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
            }

            if (radarEnabled) {
                int cx = drawContext.getScaledWindowWidth() - RADAR_RADIUS - 10;
                int cy = RADAR_RADIUS + 10;

                // Square background
                drawContext.fill(cx - RADAR_RADIUS, cy - RADAR_RADIUS, cx + RADAR_RADIUS, cy + RADAR_RADIUS, 0xCC000000);

                // Neon square border
                drawContext.fill(cx - RADAR_RADIUS, cy - RADAR_RADIUS, cx + RADAR_RADIUS, cy - RADAR_RADIUS + 2, 0xFF111111); // top
                drawContext.fill(cx - RADAR_RADIUS, cy + RADAR_RADIUS - 2, cx + RADAR_RADIUS, cy + RADAR_RADIUS, 0xFF111111); // bottom
                drawContext.fill(cx - RADAR_RADIUS, cy - RADAR_RADIUS, cx - RADAR_RADIUS + 2, cy + RADAR_RADIUS, 0xFF111111); // left
                drawContext.fill(cx + RADAR_RADIUS - 2, cy - RADAR_RADIUS, cx + RADAR_RADIUS, cy + RADAR_RADIUS, 0xFF111111); // right

                // Center dot (you)
                drawContext.fill(cx - 4, cy - 4, cx + 4, cy + 4, 0x33AA00FF); // glow
                drawContext.fill(cx - 2, cy - 2, cx + 2, cy + 2, 0xFFAA00FF); // core

                for (PlayerEntity other : client.world.getPlayers()) {
                    if (other == client.player || other.isInvisible()) continue;

                    double distSq = client.player.squaredDistanceTo(other);
                    if (distSq > DETECTION_RANGE * DETECTION_RANGE) continue;

                    Vec3d rel = other.getPos().subtract(client.player.getPos());
                    double yaw = Math.toRadians(client.player.getYaw());

                    // Flipped 180°: front = top
                    double dx = -(rel.x * Math.cos(yaw) + rel.z * Math.sin(yaw));
                    double dy = -(-rel.x * Math.sin(yaw) + rel.z * Math.cos(yaw));

                    int px = cx + (int)(dx / DETECTION_RANGE * RADAR_RADIUS);
                    int py = cy + (int)(dy / DETECTION_RANGE * RADAR_RADIUS);

                    drawContext.fill(px - 3, py - 3, px + 3, py + 3, 0x33FF9900); // glow
                    drawContext.fill(px - 2, py - 2, px + 2, py + 2, 0xFFFF9900); // core
                }
            }
        });
    }
}