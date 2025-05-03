package su.sergiusonesimus.metaworlds.zmixin.mixins.fixes;

import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.google.common.collect.Multiset;
import com.google.common.collect.Multiset.Entry;
import com.google.common.collect.Multisets;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import su.sergiusonesimus.metaworlds.MetaworldsMod;
import su.sergiusonesimus.metaworlds.zmixin.Mixins;

@Mixin(value = FMLProxyPacket.class, priority = Mixins.fmlPatchPriority)
public class MixinFMLProxyPacket {

    // MetaWorlds are not actually causing memory leaks, but forge is unfortunately complaining due to the way some
    // packets are handled. To prevent players from worrying too much, we are suppressing memory leak messages about our
    // mod.

    @Shadow(remap = false)
    private static Multiset<String> badPackets;

    @WrapOperation(
        method = "processPacket",
        at = @At(
            value = "INVOKE",
            target = "Lcpw/mods/fml/common/FMLLog;severe(Ljava/lang/String;[Ljava/lang/Object;)V",
            ordinal = 0,
            opcode = Opcodes.INVOKESTATIC))
    public void wrapSevereHeading(String format, Object[] data, Operation<Void> original) {
        boolean suppressMessage = true;
        for (Entry<String> s : Multisets.copyHighestCountFirst(badPackets)
            .entrySet()) {
            if (s.getElement() != MetaworldsMod.CHANNEL2) {
                suppressMessage = false;
                break;
            }
        }
        if (!suppressMessage) original.call(format, data);
    }

    @WrapOperation(
        method = "processPacket",
        at = @At(
            value = "INVOKE",
            target = "Lcpw/mods/fml/common/FMLLog;severe(Ljava/lang/String;[Ljava/lang/Object;)V",
            ordinal = 1,
            opcode = Opcodes.INVOKESTATIC))
    public void wrapSevereOffenders(String format, Object[] data, Operation<Void> original) {
        if (data[0] != MetaworldsMod.CHANNEL2) original.call(format, data);
    }

}
