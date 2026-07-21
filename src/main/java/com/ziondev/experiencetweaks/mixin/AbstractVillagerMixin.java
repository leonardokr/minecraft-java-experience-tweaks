package com.ziondev.experiencetweaks.mixin;

import com.ziondev.experiencetweaks.ModConfig;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Applies configurable unlimited trading to Wandering Traders and Villagers.
 */
@Mixin(AbstractVillager.class)
public abstract class AbstractVillagerMixin {

    /**
     * Resets offer uses after a trade completes if unlimited trades is enabled for the entity type.
     *
     * @param offer the completed trade offer
     * @param ci    the callback info
     */
    @Inject(method = "notifyTrade", at = @At("TAIL"))
    private void experienceTweaks$onNotifyTrade(MerchantOffer offer, CallbackInfo ci) {
        AbstractVillager villager = (AbstractVillager) (Object) this;
        if (villager instanceof WanderingTrader && ModConfig.isWanderingTraderUnlimitedTrades()) {
            offer.resetUses();
        } else if (villager instanceof Villager && ModConfig.isVillagerUnlimitedTrades()) {
            offer.resetUses();
        }
    }

    /**
     * Resets offer uses when trade offers are queried if unlimited trades is enabled for the entity type.
     *
     * @param cir the callback info returnable
     */
    @Inject(method = "getOffers", at = @At("RETURN"))
    private void experienceTweaks$onGetOffers(CallbackInfoReturnable<MerchantOffers> cir) {
        AbstractVillager villager = (AbstractVillager) (Object) this;
        boolean isWandering = villager instanceof WanderingTrader && ModConfig.isWanderingTraderUnlimitedTrades();
        boolean isVillager = villager instanceof Villager && ModConfig.isVillagerUnlimitedTrades();

        if (isWandering || isVillager) {
            MerchantOffers offers = cir.getReturnValue();
            if (offers != null) {
                for (MerchantOffer offer : offers) {
                    offer.resetUses();
                }
            }
        }
    }
}
