package com.ziondev.experiencetweaks.network;

import com.ziondev.experiencetweaks.ExperienceTweaksMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

/**
 * Packet sent from client to server to sync player-specific settings.
 */
public record SyncClientSettingsPacket(
        boolean keepExperience,
        boolean directExperience,
        boolean giveExperienceEveryDay,
        boolean riptideAnywhere,
        boolean mobArrowsCollectible,
        boolean neverRepeatTreasureMaps
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SyncClientSettingsPacket> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(ExperienceTweaksMod.MODID, "sync_client_settings"));

    public static final StreamCodec<ByteBuf, SyncClientSettingsPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public @NonNull SyncClientSettingsPacket decode(@NonNull ByteBuf buf) {
            return new SyncClientSettingsPacket(
                    ByteBufCodecs.BOOL.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf)
            );
        }

        @Override
        public void encode(@NonNull ByteBuf buf, @NonNull SyncClientSettingsPacket value) {
            ByteBufCodecs.BOOL.encode(buf, value.keepExperience());
            ByteBufCodecs.BOOL.encode(buf, value.directExperience());
            ByteBufCodecs.BOOL.encode(buf, value.giveExperienceEveryDay());
            ByteBufCodecs.BOOL.encode(buf, value.riptideAnywhere());
            ByteBufCodecs.BOOL.encode(buf, value.mobArrowsCollectible());
            ByteBufCodecs.BOOL.encode(buf, value.neverRepeatTreasureMaps());
        }
    };

    @Override
    public CustomPacketPayload.@NonNull Type<SyncClientSettingsPacket> type() {
        return TYPE;
    }
}
