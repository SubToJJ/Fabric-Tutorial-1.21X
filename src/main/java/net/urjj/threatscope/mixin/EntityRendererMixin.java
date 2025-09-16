package net.urjj.threatscope.mixin;

import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {
    @Inject(method = "getLight", at = @At("HEAD"), cancellable = true)
    private void forceMaxLight(Entity entity, float tickDelta, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(15 << 4); // max brightness
    }
}