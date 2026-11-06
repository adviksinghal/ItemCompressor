package com.ic.client.mixin;

import com.ic.client.ItemClusterManager;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntityRenderer.class)
public abstract class ItemEntityRendererMixin {

    @Inject(method = "extractRenderState", at = @At("HEAD"))
    private void onExtractRenderStateHead(ItemEntity itemEntity, ItemEntityRenderState state, float f, CallbackInfo ci) {
        if (itemEntity.level() instanceof ClientLevel clientLevel) {
            ItemClusterManager.updateClustersIfNeeded(clientLevel);
        }
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void onExtractRenderStateTail(ItemEntity itemEntity, ItemEntityRenderState state, float f, CallbackInfo ci) {
        if (ItemClusterManager.shouldSuppressRender(itemEntity)) {
            state.count = 0;
            return;
        }

        int clusterCount = ItemClusterManager.getClusterCount(itemEntity);
        if (clusterCount > 1) {
            state.count = clusterCount;
            Component clusterName = ItemClusterManager.getClusterName(itemEntity);
            if (clusterName != null) {
                state.nameTag = clusterName;
                // Force name tag attachment so vanilla's submitNameDisplay renders it floating above the item
                state.nameTagAttachment = itemEntity.getAttachments().getNullable(EntityAttachment.NAME_TAG, 0, itemEntity.getYRot(f));
            }
        }
    }
}