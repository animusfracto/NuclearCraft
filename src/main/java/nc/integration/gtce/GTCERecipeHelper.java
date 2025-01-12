package nc.integration.gtce;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import gregtech.api.items.metaitem.MetaItem.MetaValueItem;
import gregtech.api.recipes.CountableIngredient;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.util.GTUtility;
import gregtech.common.items.MetaItems;
import nc.config.NCConfig;
import nc.recipe.ProcessorRecipe;
import nc.recipe.RecipeHelper;
import nc.recipe.ingredient.IFluidIngredient;
import nc.recipe.ingredient.IItemIngredient;
import nc.recipe.ingredient.OreIngredient;
import nc.util.NCUtil;
import nc.util.OreDictHelper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Optional;

public class GTCERecipeHelper {
	
	// Thanks so much to Firew0lf for the original method!
	@Optional.Method(modid = "gregtech")
	public static void addGTCERecipe(String recipeName, ProcessorRecipe recipe) {
		RecipeMap<?> recipeMap = null;
		RecipeBuilder<?> builder = null;
		
		switch (recipeName) {
		case "manufactory":
			recipeMap = RecipeMaps.MACERATOR_RECIPES;
			builder = addStats(recipeMap.recipeBuilder(), recipe, 12, 8);
			break;
		case "isotope_separator":
			recipeMap = RecipeMaps.THERMAL_CENTRIFUGE_RECIPES;
			builder = addStats(recipeMap.recipeBuilder(), recipe, 48, 160);
			break;
		case "decay_hastener":
			return;
		case "fuel_reprocessor":
			recipeMap = RecipeMaps.CENTRIFUGE_RECIPES;
			builder = addStats(recipeMap.recipeBuilder(), recipe, 24, 60);
			break;
		case "alloy_furnace":
			recipeMap = RecipeMaps.ALLOY_SMELTER_RECIPES;
			builder = addStats(recipeMap.recipeBuilder(), recipe, 16, 10);
			break;
		case "infuser":
			recipeMap = RecipeMaps.CHEMICAL_BATH_RECIPES;
			builder = addStats(recipeMap.recipeBuilder(), recipe, 16, 12);
			break;
		case "melter":
			recipeMap = RecipeMaps.FLUID_EXTRACTION_RECIPES;
			builder = addStats(recipeMap.recipeBuilder(), recipe, 32, 16);
			break;
		case "supercooler":
			recipeMap = RecipeMaps.VACUUM_RECIPES;
			builder = addStats(recipeMap.recipeBuilder(), recipe, 240, 20);
			break;
		case "electrolyser":
			recipeMap = RecipeMaps.ELECTROLYZER_RECIPES;
			builder = addStats(recipeMap.recipeBuilder(), recipe, 30, 16);
			break;
		case "irradiator":
			return;
		case "ingot_former":
			recipeMap = RecipeMaps.FLUID_SOLIDFICATION_RECIPES;
			builder = addStats(recipeMap.recipeBuilder(), recipe, 8, 1);
			break;
		case "pressurizer":
			if (isPlateRecipe(recipe)) {
				recipeMap = RecipeMaps.BENDER_RECIPES;
				builder = addStats(recipeMap.recipeBuilder(), recipe, 24, 10);
			}
			else {
				recipeMap = RecipeMaps.COMPRESSOR_RECIPES;
				builder = addStats(recipeMap.recipeBuilder(), recipe, 2, 20);
			}
			break;
		case "chemical_reactor":
			recipeMap = RecipeMaps.CHEMICAL_RECIPES;
			builder = addStats(recipeMap.recipeBuilder(), recipe, 30, 30);
			break;
		case "salt_mixer":
			recipeMap = RecipeMaps.MIXER_RECIPES;
			builder = addStats(recipeMap.recipeBuilder(), recipe, 8, 12);
			break;
		case "crystallizer":
			recipeMap = RecipeMaps.CHEMICAL_RECIPES;
			builder = addStats(recipeMap.recipeBuilder(), recipe, 30, 10);
			break;
		case "dissolver":
			recipeMap = RecipeMaps.CHEMICAL_RECIPES;
			builder = addStats(recipeMap.recipeBuilder(), recipe, 20, 20);
			break;
		case "extractor":
			recipeMap = RecipeMaps.FLUID_EXTRACTION_RECIPES;
			builder = addStats(recipeMap.recipeBuilder(), recipe, 16, 12);
			break;
		case "centrifuge":
			recipeMap = RecipeMaps.CENTRIFUGE_RECIPES;
			builder = addStats(recipeMap.recipeBuilder(), recipe, 16, 80);
			break;
		case "rock_crusher":
			recipeMap = RecipeMaps.MACERATOR_RECIPES;
			builder = addStats(recipeMap.recipeBuilder(), recipe, 20, 12);
			return;
		}
		
		if (recipeMap == null || builder == null) {
			return;
		}
		
		List<List<ItemStack>> itemInputLists = new ArrayList<>();
		List<List<FluidStack>> fluidInputLists = new ArrayList<>();
		
		for (IItemIngredient item : recipe.itemIngredients()) itemInputLists.add(item.getInputStackList());
		for (IFluidIngredient fluid : recipe.fluidIngredients()) fluidInputLists.add(fluid.getInputStackList());
		
		int arrSize = recipe.itemIngredients().size() + recipe.fluidIngredients().size();
		int[] inputNumbers = new int[arrSize];
		Arrays.fill(inputNumbers, 0);
		
		int[] maxNumbers  = new int[arrSize];
		for (int i = 0; i < itemInputLists.size(); i++) {
			int maxNumber = itemInputLists.get(i).size() - 1;
			if (maxNumber < 0) return;
			maxNumbers[i] = maxNumber;
		}
		for (int i = 0; i < fluidInputLists.size(); i++) {
			int maxNumber = fluidInputLists.get(i).size() - 1;
			if (maxNumber < 0) return;
			maxNumbers[i + itemInputLists.size()] = maxNumber;
		}
		
		List<Pair<List<ItemStack>, List<FluidStack>>> materialListTuples = new ArrayList<>();
		
		RecipeHelper.generateMaterialListTuples(materialListTuples, maxNumbers, inputNumbers, itemInputLists, fluidInputLists);
		
		for (Pair<List<ItemStack>, List<FluidStack>> materials : materialListTuples) {
			if (findRecipe(recipeMap, materials.getLeft(), materials.getRight()) != null) {
				return;
			}
		}
		
		List<RecipeBuilder<?>> builders = new ArrayList<RecipeBuilder<?>>(); // Holds all the recipe variants
		builders.add(builder);
		
		for (IItemIngredient input : recipe.itemIngredients()) {
			if (input instanceof OreIngredient) {
				for (RecipeBuilder<?> builderVariant : builders) {
					builderVariant.input(((OreIngredient)input).oreName, ((OreIngredient)input).stackSize);
				}
			}
			else {
				List<String> ingredientOreList = new ArrayList<String>(); // Hold the different oreDict names
				List<RecipeBuilder<?>> newBuilders = new ArrayList<RecipeBuilder<?>>();
				for (ItemStack inputVariant : input.getInputStackList()) {
					if(inputVariant.isEmpty()) continue;
					Set<String> variantOreList = OreDictHelper.getOreNames(inputVariant);
					
					if (!variantOreList.isEmpty()) { // This variant has oreDict entries
						if (ingredientOreList.containsAll(variantOreList)) {
							continue;
						}
						ingredientOreList.addAll(variantOreList);
						
						for (RecipeBuilder<?> recipeBuilder : builders) {
							newBuilders.add(recipeBuilder.copy().input(variantOreList.iterator().next(), inputVariant.getCount()));
						}
					}
					else {
						for (RecipeBuilder<?> recipeBuilder : builders) {
							newBuilders.add(recipeBuilder.copy().inputs(inputVariant));
						}
					}
				}
				builders = newBuilders;
			}
		}
		
		if (recipeMap == RecipeMaps.FLUID_SOLIDFICATION_RECIPES) {
			MetaValueItem mold = getIngotFormerMold(recipe);
			for (RecipeBuilder<?> builderVariant : builders) {
				builderVariant.notConsumable(mold);
			}
		}
		else if (recipeMap == RecipeMaps.BENDER_RECIPES) {
			for (RecipeBuilder<?> builderVariant : builders) {
				builderVariant.notConsumable(MetaItems.INTEGRATED_CIRCUIT);
			}
		}
		
		for (IFluidIngredient input : recipe.fluidIngredients()) {
			if (input.getInputStackList().isEmpty()) continue;
			for (RecipeBuilder<?> builderVariant : builders) {
				builderVariant.fluidInputs(input.getInputStackList().get(0));
			}
		}
		
		for (IItemIngredient output : recipe.itemProducts()) {
			List<ItemStack> outputStackList = output.getOutputStackList();
			if (outputStackList.isEmpty()) continue;
			for (RecipeBuilder<?> builderVariant : builders) {
				builderVariant = builderVariant.outputs(outputStackList.get(0));
			}
		}
		
		for (IFluidIngredient output : recipe.fluidProducts()) {
			if (output.getOutputStackList().isEmpty()) continue;
			for (RecipeBuilder<?> builderVariant : builders) {
				builderVariant.fluidOutputs(output.getOutputStackList().get(0));
			}
		}
		
		boolean built = false;
		for (RecipeBuilder<?> builderVariant : builders) {
			if (!builderVariant.getInputs().isEmpty() || !builderVariant.getFluidInputs().isEmpty()) {
				builderVariant.buildAndRegister();
				built = true;
			}
		}
		
		if (built && NCConfig.gtce_recipe_logging) {
			NCUtil.getLogger().info("Injected GTCE " + recipeMap.unlocalizedName + " recipe: " + RecipeHelper.getRecipeString(recipe));
		}
	}
	
	@Optional.Method(modid = "gregtech")
	private static RecipeBuilder<?> addStats(RecipeBuilder<?> builder, ProcessorRecipe recipe, int processPower, int processTime) {
		return builder.EUt(Math.max((int) recipe.getBaseProcessPower(processPower), 1)).duration((int) recipe.getBaseProcessTime(20D*processTime));
	}
	
	// GTCE recipe matching - modified from GTCE source
	
	@Optional.Method(modid = "gregtech")
	private static Recipe findRecipe(RecipeMap<?> recipeMap, List<ItemStack> inputs, List<FluidStack> fluidInputs) {
		if (recipeMap.getRecipeList().isEmpty())
			return null;
		if (recipeMap.getMinFluidInputs() > 0 && GTUtility.amountOfNonNullElements(fluidInputs) < recipeMap.getMinFluidInputs()) {
			return null;
		}
		if (recipeMap.getMinInputs() > 0 && GTUtility.amountOfNonEmptyStacks(inputs) < recipeMap.getMinInputs()) {
			return null;
		}
		if (recipeMap.getMaxInputs() > 0) {
			return findByInputs(recipeMap, inputs, fluidInputs);
		}
		else {
			return findByFluidInputs(recipeMap, inputs, fluidInputs);
		}
	}
	
	@Optional.Method(modid = "gregtech")
	private static Recipe findByFluidInputs(RecipeMap<?> recipeMap, List<ItemStack> inputs, List<FluidStack> fluidInputs) {
		for (FluidStack fluid : fluidInputs) {
			if (fluid == null) continue;
			Collection<Recipe> recipes = recipeMap.getRecipesForFluid(fluid);
			if (recipes == null) continue;
			for (Recipe recipe : recipes) {
				if (recipeMatches(recipe, inputs, fluidInputs)) {
					return recipe;
				}
			}
		}
		return null;
	}
	
	@Optional.Method(modid = "gregtech")
	private static Recipe findByInputs(RecipeMap<?> recipeMap, List<ItemStack> inputs, List<FluidStack> fluidInputs) {
		for (Recipe recipe : recipeMap.getRecipeList()) {
			if (recipeMatches(recipe, inputs, fluidInputs)) {
				return recipe;
			}
		}
		return null;
	}
	
	@Optional.Method(modid = "gregtech")
	private static boolean recipeMatches(Recipe recipe, List<ItemStack> inputs, List<FluidStack> fluidInputs) {
		itemLoop : for (CountableIngredient ingredient : recipe.getInputs()) {
			for (ItemStack input : inputs) {
				if (!input.isEmpty() && ingredient.getIngredient().apply(input)) {
					continue itemLoop;
				}
			}
			return false;
		}
		fluidLoop : for (FluidStack fluid : recipe.getFluidInputs()) {
			for (FluidStack input : fluidInputs) {
				if (input != null && input.isFluidEqual(fluid)) {
					continue fluidLoop;
				}
			}
			return false;
		}
		return true;
	}
	
	private static boolean isPlateRecipe(ProcessorRecipe recipe) {
		ItemStack output = recipe.itemProducts().get(0).getStack();
		return output != null && OreDictHelper.hasOrePrefix(output, "plate", "plateDense");
	}
	
	private static MetaValueItem getIngotFormerMold(ProcessorRecipe recipe) {
		ItemStack output = recipe.itemProducts().get(0).getStack();
		if (output != null) {
			if (OreDictHelper.hasOrePrefix(output, "ingot")) return MetaItems.SHAPE_MOLD_INGOT;
			else if (OreDictHelper.hasOrePrefix(output, "block")) return MetaItems.SHAPE_MOLD_BLOCK;
		}
		return MetaItems.SHAPE_MOLD_BALL;
	}
}
