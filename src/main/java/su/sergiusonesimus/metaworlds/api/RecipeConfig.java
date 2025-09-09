package su.sergiusonesimus.metaworlds.api;

import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Configuration;

import cpw.mods.fml.common.registry.GameRegistry;

public class RecipeConfig {

    public final boolean isValid;
    public final ItemStack itemToCraft;
    public final boolean isShaped;
    public final String[] stringsToParse;
    public final RecipeConfig.RecipePlaceHolderDef[] parsedPlaceHolders;

    public RecipeConfig(Configuration sourceConfig, String recipeName, ItemStack craftedItem, boolean defaultShaped,
        String[] defaultShape, RecipeConfig.RecipePlaceHolderDef[] placeholders) {
        this.itemToCraft = craftedItem;
        String[] defaultString = new String[4 + 2 * placeholders.length];
        defaultString[0] = defaultShaped ? "shaped" : "shapeless";
        defaultString[1] = defaultShape[0];
        defaultString[2] = defaultShape[1];
        defaultString[3] = defaultShape[2];
        int i = 4;
        RecipeConfig.RecipePlaceHolderDef[] newPlaceHolderChar = placeholders;
        int newPlaceHolderItemName = placeholders.length;

        for (int curPlaceHolder = 0; curPlaceHolder < newPlaceHolderItemName; ++curPlaceHolder) {
            RecipeConfig.RecipePlaceHolderDef curDef = newPlaceHolderChar[curPlaceHolder];
            defaultString[i++] = curDef.placeHolder.toString();
            defaultString[i++] = curDef.placeHolderItem;
        }

        this.stringsToParse = sourceConfig.get("recipe", recipeName, defaultString)
            .getStringList();
        if (this.stringsToParse.length >= 6 && (this.stringsToParse.length - 4) % 2 == 0
            && (this.stringsToParse[0].equalsIgnoreCase("shaped")
                || this.stringsToParse[0].equalsIgnoreCase("shapeless"))) {
            this.isShaped = this.stringsToParse[0].equalsIgnoreCase("shaped");
            this.parsedPlaceHolders = new RecipeConfig.RecipePlaceHolderDef[(this.stringsToParse.length - 4) / 2];

            for (i = 0; i < this.parsedPlaceHolders.length; ++i) {
                char var13 = this.stringsToParse[4 + i * 2].charAt(0);
                String var14 = this.stringsToParse[4 + i * 2 + 1];
                RecipeConfig.RecipePlaceHolderDef var15 = new RecipeConfig.RecipePlaceHolderDef(
                    Character.valueOf(var13),
                    var14);
                this.parsedPlaceHolders[i] = var15;
            }

            this.isValid = true;
        } else {
            this.isValid = false;
            this.isShaped = false;
            this.parsedPlaceHolders = null;
        }
    }

    public boolean addRecipeToGameRegistry() {
        if (!this.isValid) {
            return false;
        } else {
            RecipeConfig.RecipePlaceHolderDef[] arr$;
            int len$;
            int i$;
            RecipeConfig.RecipePlaceHolderDef curPlaceHolderDef;
            if (this.isShaped) {
                Object[] recipeContents = new Object[this.stringsToParse.length - 1];
                recipeContents[0] = this.stringsToParse[1];
                recipeContents[1] = this.stringsToParse[2];
                recipeContents[2] = this.stringsToParse[3];
                int argsList = 3;
                arr$ = this.parsedPlaceHolders;
                len$ = arr$.length;

                for (i$ = 0; i$ < len$; ++i$) {
                    curPlaceHolderDef = arr$[i$];
                    recipeContents[argsList++] = curPlaceHolderDef.placeHolder;
                    ItemStack pattern = getItemByName(curPlaceHolderDef.placeHolderItem);
                    if (pattern == null) {
                        return false;
                    }

                    recipeContents[argsList++] = pattern;
                }

                GameRegistry.addShapedRecipe(this.itemToCraft, recipeContents);
            } else {
                String var10 = this.stringsToParse[1] + this.stringsToParse[2] + this.stringsToParse[3];
                LinkedList<ItemStack> var11 = new LinkedList<ItemStack>();
                arr$ = this.parsedPlaceHolders;
                len$ = arr$.length;

                for (i$ = 0; i$ < len$; ++i$) {
                    curPlaceHolderDef = arr$[i$];
                    Pattern var12 = Pattern.compile(curPlaceHolderDef.placeHolder.toString());
                    Matcher matcher = var12.matcher(var10);
                    ItemStack curItem = getItemByName(curPlaceHolderDef.placeHolderItem);
                    if (curItem == null) {
                        return false;
                    }

                    while (matcher.find()) {
                        var11.add(curItem);
                    }
                }

                GameRegistry.addShapelessRecipe(this.itemToCraft, var11.toArray());
            }

            return true;
        }
    }

    public static ItemStack getItemByName(String itemName) {
        Block block = Block.getBlockFromName(itemName);
        if (block != null) {
            return new ItemStack(block, 1, 0);
        } else {
            Item item = (Item) Item.itemRegistry.getObject(itemName);
            return item != null ? new ItemStack(item, 1, 0) : null;
        }
    }

    public static class RecipePlaceHolderDef {

        public Character placeHolder;
        public String placeHolderItem;

        public RecipePlaceHolderDef(Character placeHolderChar, String itemName) {
            this.placeHolder = placeHolderChar;
            this.placeHolderItem = itemName;
        }
    }
}
