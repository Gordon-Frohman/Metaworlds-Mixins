package su.sergiusonesimus.metaworlds.zmixin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import cpw.mods.fml.relauncher.FMLLaunchHandler;

public enum Mixins {

    VANILLA_COMMON(new Builder("").addTargetedMod(TargetedMod.VANILLA)
        .setSide(Side.BOTH)
        .setPhase(Phase.EARLY)
        .addMixinClasses(
            "minecraft.entity.MixinEntity",
            "minecraft.entity.MixinEntityLivingBase",
            "minecraft.entity.MixinEntityTrackerEntry",
            "minecraft.entity.item.MixinEntityItem",
            "minecraft.entity.player.MixinEntityPlayer",
            "minecraft.item.MixinItemBucket",
            "minecraft.network.MixinNetHandlerPlayServer",
            "minecraft.network.play.client.MixinC03PacketPlayer",
            "minecraft.network.play.server.MixinS05PacketSpawnPosition",
            "minecraft.network.play.server.MixinS08PacketPlayerPosLook",
            "minecraft.network.play.server.MixinS14PacketEntity",
            "minecraft.network.play.server.MixinS15PacketEntityRelMove",
            "minecraft.network.play.server.MixinS17PacketEntityLookMove",
            "minecraft.network.play.server.MixinS18PacketEntityTeleport",
            "minecraft.server.MixinMinecraftServer",
            "minecraft.server.management.MixinPlayerManager",
            "minecraft.server.management.MixinServerConfigurationManager",
            "minecraft.tileentity.MixinTileEntity",
            "minecraft.util.MixinAxisAlignedBB",
            "minecraft.util.MixinMovingObjectPosition",
            "minecraft.world.MixinWorld",
            "minecraft.world.MixinWorldIntermediate",
            "minecraft.world.MixinWorldServer",
            "minecraft.world.storage.MixinWorldInfo",
            "forge.MixinFMLNetworkHandler",
            "forge.MixinFMLProxyPacket",
            "forge.MixinOpenGui",
            "forge.MixinOpenGuiHandler")),

    VANILLA_CLIENT(new Builder("").addTargetedMod(TargetedMod.VANILLA)
        .setSide(Side.CLIENT)
        .setPhase(Phase.EARLY)
        .addMixinClasses(
            "minecraft.client.MixinMinecraft",
            "minecraft.client.entity.MixinEntityClientPlayerMP",
            "minecraft.client.multiplayer.MixinPlayerControllerMP",
            "minecraft.client.multiplayer.MixinWorldClient",
            "minecraft.client.network.MixinNetHandlerPlayClient",
            "minecraft.client.particle.MixinEffectRenderer",
            "minecraft.client.renderer.MixinDestroyBlockProgress",
            "minecraft.client.renderer.MixinEntityRenderer",
            "minecraft.client.renderer.MixinEntitySorter",
            "minecraft.client.renderer.MixinRenderGlobal",
            "minecraft.client.renderer.MixinRenderList",
            "minecraft.client.renderer.MixinWorldRenderer",
            "minecraft.client.renderer.entity.MixinRender",
            "minecraft.client.renderer.entity.MixinRenderManager",
            "minecraft.client.renderer.entity.MixinRenderMinecart",
            "minecraft.client.renderer.entity.MixinRenderPainting",
            "minecraft.client.renderer.tileentity.MixinRenderItemFrame",
            "minecraft.client.renderer.tileentity.MixinTileEntityRendererDispatcher",
            "minecraft.entity.MixinEntityC",
            "minecraft.entity.MixinEntityLivingBaseC",
            "minecraft.entity.player.MixinEntityPlayerC",
            "minecraft.util.MixinMovingObjectPositionC",
            "minecraft.world.MixinWorldC",
            "forge.MixinFMLNetworkHandlerC")),

    VANILLA_SERVER(new Builder("").addTargetedMod(TargetedMod.VANILLA)
        .setSide(Side.SERVER)
        .setPhase(Phase.EARLY)
        .addMixinClasses("minecraft.util.MixinMovingObjectPositionS")),

    BEDDIUM_COMPAT(new Builder(
        "Reenable vanilla rendering for subworlds disabled by Beddium. Not the best fix, but a fix nevertheless")
            .addTargetedMod(TargetedMod.BEDDIUM)
            .setSide(Side.CLIENT)
            .setPhase(Phase.LATE)
            .addMixinClasses("beddium.MixinRenderGlobal")),

    ANGELICA_COMPAT(new Builder(
        "Reenable vanilla rendering for subworlds disabled by Angelica. Not the best fix, but a fix nevertheless")
            .addTargetedMod(TargetedMod.ANGELICA)
            .setSide(Side.CLIENT)
            .setPhase(Phase.LATE)
            .addMixinClasses(addPrefix("angelica.", "MixinRenderGlobal", "MixinEffectRenderer"))),

    HARDCORE_ENDER_EXPANSION_COMPAT(new Builder("Disable generation of additional data for player proxies")
        .addTargetedMod(TargetedMod.HARDCORE_ENDER_EXPANSION)
        .setSide(Side.BOTH)
        .setPhase(Phase.LATE)
        .addMixinClasses("hee.MixinPlayerDataHandler")),

    CODECHICKENCORE_COMPAT(
        new Builder("Adding subworld data to CodeChickenCore classes (required for ForgeMultipart integration)")
            .addTargetedMod(TargetedMod.CODECHICKENCORE)
            .setSide(Side.BOTH)
            .setPhase(Phase.LATE)
            .addMixinClasses(addPrefix("codechickenlib.", "MixinPacketCustom", "MixinRayTracer", "MixinVector3"))),

    FORGEMULTIPART_COMPAT(
        new Builder("Allow multiparts to be placed correctly on subworlds").addTargetedMod(TargetedMod.FORGEMULTIPART)
            .setSide(Side.BOTH)
            .setPhase(Phase.LATE)
            .addMixinClasses(
                addPrefix(
                    "forgemultipart.",
                    "MixinPlacementGrid",
                    "MixinMicroblockRender",
                    "MixinMicroblockPlacement",
                    "MixinTileMultipart",
                    "MixinMultipartSPH",
                    "MixinMultipartCPH",
                    "MixinMultipartSPH$$anonfun$onTickEnd",
                    "MixinMultipartSPH$$anonfun$onTickEnd$2",
                    "MixinMultipartSPH$$anonfun$onTickEnd$5",
                    "MixinIconHitEffects"))),

    LITTLETILES_COMPAT(
        new Builder("Allow little tiles to be placed correctly on subworlds").addTargetedMod(TargetedMod.LITTLETILES)
            .setSide(Side.BOTH)
            .setPhase(Phase.LATE)
            .addMixinClasses(
                addPrefix(
                    "littletiles.",
                    "MixinLittleTileBlockPos",
                    "MixinPreviewRenderer",
                    "MixinItemBlockTiles",
                    "MixinLittlePlacePacket",
                    "MixinBlockTile",
                    "MixinLittleBlockPacket",
                    "MixinPlacementHelper"))),

    TERRAFIRMACRAFT_COMPAT(new Builder("Adding subworld data to TerraFirmaCraft tile entity packets")
        .addTargetedMod(TargetedMod.TERRAFIRMACRAFT)
        .setSide(Side.BOTH)
        .setPhase(Phase.LATE)
        .addMixinClasses(addPrefix("tfc.", "MixinDataBlockPacket", "MixinNetworkTileEntity"))),

    GREGTECH6_COMPAT(
        new Builder("Adding subworld data to GregTech tile entity packets").addTargetedMod(TargetedMod.GREGTECH6)
            .setSide(Side.BOTH)
            .setPhase(Phase.LATE)
            .addMixinClasses(
                addPrefix(
                    "gregtech6.",
                    "MixinNetworkHandler",
                    "MixinPacketCoordinates",
                    "MixinPacketCoordinatesChild",
                    "MixinPrefixBlockTileEntity",
                    "MixinTileEntityBase",
                    "MixinTileEntityBase01Root"))),

    ;

    private final List<String> mixinClasses;
    private final Supplier<Boolean> applyIf;
    private final Phase phase;
    private final Side side;
    private final List<TargetedMod> targetedMods;
    private final List<TargetedMod> excludedMods;

    Mixins(Builder builder) {
        this.mixinClasses = builder.mixinClasses;
        this.applyIf = builder.applyIf;
        this.side = builder.side;
        this.targetedMods = builder.targetedMods;
        this.excludedMods = builder.excludedMods;
        this.phase = builder.phase;
        if (this.targetedMods.isEmpty()) {
            throw new RuntimeException("No targeted mods specified for " + this.name());
        }
        if (this.applyIf == null) {
            throw new RuntimeException("No ApplyIf function specified for " + this.name());
        }
    }

    public static List<String> getEarlyMixins(Set<String> loadedCoreMods) {
        final List<String> mixins = new ArrayList<>();
        final List<String> notLoading = new ArrayList<>();
        for (Mixins mixin : Mixins.values()) {
            if (mixin.phase == Mixins.Phase.EARLY) {
                if (mixin.shouldLoad(loadedCoreMods, Collections.emptySet())) {
                    mixins.addAll(mixin.mixinClasses);
                } else {
                    notLoading.addAll(mixin.mixinClasses);
                }
            }
        }
        return mixins;
    }

    public static List<String> getLateMixins(Set<String> loadedMods) {
        final List<String> mixins = new ArrayList<>();
        final List<String> notLoading = new ArrayList<>();
        for (Mixins mixin : Mixins.values()) {
            if (mixin.phase == Mixins.Phase.LATE) {
                if (mixin.shouldLoad(Collections.emptySet(), loadedMods)) {
                    mixins.addAll(mixin.mixinClasses);
                } else {
                    notLoading.addAll(mixin.mixinClasses);
                }
            }
        }
        return mixins;
    }

    private boolean shouldLoadSide() {
        return side == Side.BOTH || (side == Side.SERVER && FMLLaunchHandler.side()
            .isServer())
            || (side == Side.CLIENT && FMLLaunchHandler.side()
                .isClient());
    }

    private boolean allModsLoaded(List<TargetedMod> targetedMods, Set<String> loadedCoreMods, Set<String> loadedMods) {
        if (targetedMods.isEmpty()) return false;

        for (TargetedMod target : targetedMods) {
            if (target == TargetedMod.VANILLA) continue;

            // Check coremod first
            if (!loadedCoreMods.isEmpty() && target.coreModClass != null
                && !loadedCoreMods.contains(target.coreModClass)) return false;
            else if (!loadedMods.isEmpty() && target.modId != null && !loadedMods.contains(target.modId)) return false;
        }

        return true;
    }

    private boolean noModsLoaded(List<TargetedMod> targetedMods, Set<String> loadedCoreMods, Set<String> loadedMods) {
        if (targetedMods.isEmpty()) return true;

        for (TargetedMod target : targetedMods) {
            if (target == TargetedMod.VANILLA) continue;

            // Check coremod first
            if (!loadedCoreMods.isEmpty() && target.coreModClass != null
                && loadedCoreMods.contains(target.coreModClass)) return false;
            else if (!loadedMods.isEmpty() && target.modId != null && loadedMods.contains(target.modId)) return false;
        }

        return true;
    }

    private boolean shouldLoad(Set<String> loadedCoreMods, Set<String> loadedMods) {
        return (shouldLoadSide() && applyIf.get()
            && allModsLoaded(targetedMods, loadedCoreMods, loadedMods)
            && noModsLoaded(excludedMods, loadedCoreMods, loadedMods));
    }

    private static class Builder {

        private final List<String> mixinClasses = new ArrayList<>();
        private Supplier<Boolean> applyIf = () -> true;
        private Side side = Side.BOTH;
        private Phase phase = Phase.LATE;
        private final List<TargetedMod> targetedMods = new ArrayList<>();
        private final List<TargetedMod> excludedMods = new ArrayList<>();

        public Builder(@SuppressWarnings("unused") String description) {}

        public Builder addMixinClasses(String... mixinClasses) {
            this.mixinClasses.addAll(Arrays.asList(mixinClasses));
            return this;
        }

        public Builder setPhase(Phase phase) {
            this.phase = phase;
            return this;
        }

        public Builder setSide(Side side) {
            this.side = side;
            return this;
        }

        public Builder setApplyIf(Supplier<Boolean> applyIf) {
            this.applyIf = applyIf;
            return this;
        }

        public Builder addTargetedMod(TargetedMod mod) {
            this.targetedMods.add(mod);
            return this;
        }

        public Builder addExcludedMod(TargetedMod mod) {
            this.excludedMods.add(mod);
            return this;
        }
    }

    @SuppressWarnings("SimplifyStreamApiCallChains")
    private static String[] addPrefix(String prefix, String... values) {
        return Arrays.stream(values)
            .map(s -> prefix + s)
            .collect(Collectors.toList())
            .toArray(new String[values.length]);
    }

    private enum Side {
        BOTH,
        CLIENT,
        SERVER
    }

    private enum Phase {
        EARLY,
        LATE,
    }
}
