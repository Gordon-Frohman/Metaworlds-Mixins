package su.sergiusonesimus.metaworlds.zmixin;

public enum TargetedMod {

    VANILLA("Minecraft", null),
    BEDDIUM("Beddium", "com.ventooth.beddium.Beddium", "beddium"),
    ANGELICA("Angelica", "com.gtnewhorizons.angelica.AngelicaMod", "angelica"),
    HARDCORE_ENDER_EXPANSION("Hardcore Ender Expansion", "chylex.hee.HardcoreEnderExpansion", "HardcoreEnderExpansion"),
    CODECHICKENCORE("CodeChicken Core", "codechicken.core.launch.CodeChickenCorePlugin", "CodeChickenCore"),
    FORGEMULTIPART("Minecraft Multipart Plugin", "codechicken.multipart.minecraft.MinecraftMultipartMod",
        "McMultipart"),
    LITTLETILES("LittleTiles", "com.creativemd.littletiles.LittleTiles", "littletiles"),
    TERRAFIRMACRAFT("TerraFirmaCraft", "com.bioxx.tfc.TerraFirmaCraft", "terrafirmacraft");

    /** The "name" in the @Mod annotation */
    public final String modName;
    /** Class that implements the IFMLLoadingPlugin interface */
    public final String coreModClass;
    /** The "modid" in the @Mod annotation */
    public final String modId;

    TargetedMod(String modName, String coreModClass) {
        this(modName, coreModClass, null);
    }

    TargetedMod(String modName, String coreModClass, String modId) {
        this.modName = modName;
        this.coreModClass = coreModClass;
        this.modId = modId;
    }

    @Override
    public String toString() {
        return "TargetedMod{modName='" + modName + "', coreModClass='" + coreModClass + "', modId='" + modId + "'}";
    }
}
