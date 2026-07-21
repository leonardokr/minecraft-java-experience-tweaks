package com.ziondev.experiencetweaks.network;

import com.ziondev.experiencetweaks.ExperienceTweaksMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

/**
 * Packet sent from client to server to update server-side configuration settings.
 * Only processed if the sending player has OP permissions.
 */
public record UpdateServerConfigPacket(
        int giveExperienceEveryDayBase,
        double giveExperienceEveryDayGrowth,
        boolean anvilBypassTooExpensive,
        boolean anvilUseItemCost,
        String anvilCostItem,
        double anvilItemCostMultiplier,
        boolean allowMendingWithInfinity,
        boolean anvilEnchantmentExtraction,
        boolean anvilEnchantmentExtractionDestroySource,
        String enchantmentCostItem,
        double enchantmentCostMultiplier,
        String enchantmentCooldownType,
        boolean waterBelowHydratesFarmland,
        int waterHydrationRadius,
        int milkBucketNutrition
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<UpdateServerConfigPacket> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(ExperienceTweaksMod.MODID, "update_server_config"));

    public static final StreamCodec<ByteBuf, UpdateServerConfigPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public @NonNull UpdateServerConfigPacket decode(@NonNull ByteBuf buf) {
            return new UpdateServerConfigPacket(
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.DOUBLE.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.DOUBLE.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.DOUBLE.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf)
            );
        }

        @Override
        public void encode(@NonNull ByteBuf buf, @NonNull UpdateServerConfigPacket value) {
            ByteBufCodecs.VAR_INT.encode(buf, value.giveExperienceEveryDayBase());
            ByteBufCodecs.DOUBLE.encode(buf, value.giveExperienceEveryDayGrowth());
            ByteBufCodecs.BOOL.encode(buf, value.anvilBypassTooExpensive());
            ByteBufCodecs.BOOL.encode(buf, value.anvilUseItemCost());
            ByteBufCodecs.STRING_UTF8.encode(buf, value.anvilCostItem());
            ByteBufCodecs.DOUBLE.encode(buf, value.anvilItemCostMultiplier());
            ByteBufCodecs.BOOL.encode(buf, value.allowMendingWithInfinity());
            ByteBufCodecs.BOOL.encode(buf, value.anvilEnchantmentExtraction());
            ByteBufCodecs.BOOL.encode(buf, value.anvilEnchantmentExtractionDestroySource());
            ByteBufCodecs.STRING_UTF8.encode(buf, value.enchantmentCostItem());
            ByteBufCodecs.DOUBLE.encode(buf, value.enchantmentCostMultiplier());
            ByteBufCodecs.STRING_UTF8.encode(buf, value.enchantmentCooldownType());
            ByteBufCodecs.BOOL.encode(buf, value.waterBelowHydratesFarmland());
            ByteBufCodecs.VAR_INT.encode(buf, value.waterHydrationRadius());
            ByteBufCodecs.VAR_INT.encode(buf, value.milkBucketNutrition());
        }
    };

    @Override
    public CustomPacketPayload.@NonNull Type<UpdateServerConfigPacket> type() {
        return TYPE;
    }
}
