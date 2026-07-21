package com.ziondev.experiencetweaks.network;

import com.ziondev.experiencetweaks.ExperienceTweaksMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

/**
 * Packet sent from server to client containing server-side configuration settings
 * to sync client-side GUI and menu logic.
 */
public record SyncServerConfigPacket(
        boolean anvilUseItemCost,
        String anvilCostItem,
        double anvilItemCostMultiplier,
        boolean anvilBypassTooExpensive
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SyncServerConfigPacket> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(ExperienceTweaksMod.MODID, "sync_server_config"));

    public static final StreamCodec<ByteBuf, SyncServerConfigPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public @NonNull SyncServerConfigPacket decode(@NonNull ByteBuf buf) {
            return new SyncServerConfigPacket(
                    ByteBufCodecs.BOOL.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.DOUBLE.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf)
            );
        }

        @Override
        public void encode(@NonNull ByteBuf buf, @NonNull SyncServerConfigPacket value) {
            ByteBufCodecs.BOOL.encode(buf, value.anvilUseItemCost());
            ByteBufCodecs.STRING_UTF8.encode(buf, value.anvilCostItem());
            ByteBufCodecs.DOUBLE.encode(buf, value.anvilItemCostMultiplier());
            ByteBufCodecs.BOOL.encode(buf, value.anvilBypassTooExpensive());
        }
    };

    @Override
    public CustomPacketPayload.@NonNull Type<SyncServerConfigPacket> type() {
        return TYPE;
    }
}
