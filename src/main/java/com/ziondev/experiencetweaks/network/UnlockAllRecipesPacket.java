package com.ziondev.experiencetweaks.network;

import com.ziondev.experiencetweaks.ExperienceTweaksMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

/**
 * Packet sent from client to server to unlock all recipes for all players.
 * Only processed if the sending player has OP permissions.
 */
public record UnlockAllRecipesPacket() implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<UnlockAllRecipesPacket> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(ExperienceTweaksMod.MODID, "unlock_all_recipes"));

    public static final StreamCodec<ByteBuf, UnlockAllRecipesPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public @NonNull UnlockAllRecipesPacket decode(@NonNull ByteBuf buf) {
            return new UnlockAllRecipesPacket();
        }

        @Override
        public void encode(@NonNull ByteBuf buf, @NonNull UnlockAllRecipesPacket value) {
        }
    };

    @Override
    public CustomPacketPayload.@NonNull Type<UnlockAllRecipesPacket> type() {
        return TYPE;
    }
}
