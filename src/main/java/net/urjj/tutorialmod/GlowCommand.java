package net.urjj.tutorialmod;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static net.minecraft.server.command.CommandManager.literal;

public class GlowCommand {
    private static boolean glowEnabled = false;

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register(literal("glowall")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(context -> {
                    glowEnabled = !glowEnabled;
                    ServerWorld world = context.getSource().getWorld();

                    List<Entity> entities = StreamSupport.stream(world.iterateEntities().spliterator(), false)
                            .collect(Collectors.toList());

                    for (Entity entity : entities) {
                        entity.setGlowing(glowEnabled);
                    }

                    context.getSource().sendFeedback(() -> Text.literal("GlowAll toggled " + (glowEnabled ? "ON" : "OFF")), true);
                    return 1;
                }));
    }
}