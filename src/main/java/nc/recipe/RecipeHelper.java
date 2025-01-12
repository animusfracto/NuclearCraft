package nc.recipe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Lists;

import nc.ModCheck;
import nc.recipe.ingredient.ChanceFluidIngredient;
import nc.recipe.ingredient.ChanceItemIngredient;
import nc.recipe.ingredient.EmptyFluidIngredient;
import nc.recipe.ingredient.EmptyItemIngredient;
import nc.recipe.ingredient.FluidArrayIngredient;
import nc.recipe.ingredient.FluidIngredient;
import nc.recipe.ingredient.IFluidIngredient;
import nc.recipe.ingredient.IItemIngredient;
import nc.recipe.ingredient.ItemArrayIngredient;
import nc.recipe.ingredient.ItemIngredient;
import nc.recipe.ingredient.OreIngredient;
import nc.tile.internal.fluid.Tank;
import nc.util.CollectionHelper;
import nc.util.FluidRegHelper;
import nc.util.GasHelper;
import nc.util.OreDictHelper;
import nc.util.StringHelper;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

public class RecipeHelper {
	
	public static boolean containsItemIngredient(List<IItemIngredient> list, IItemIngredient ingredient) {
		for (IItemIngredient i : list) {
			if (i == null) continue;
			if (i.match(ingredient, IngredientSorption.NEUTRAL).matches()) return true;
		}
		return false;
	}
	
	public static boolean containsFluidIngredient(List<IFluidIngredient> list, IFluidIngredient ingredient) {
		for (IFluidIngredient i : list) {
			if (i == null) continue;
			if (i.match(ingredient, IngredientSorption.NEUTRAL).matches()) return true;
		}
		return false;
	}
	
	public static ItemStack fixItemStack(Object object) {
		if (object == null) return null;
		
		else if (object instanceof ItemStack) {
			ItemStack stack = ((ItemStack) object).copy();
			if (stack.getCount() <= 0) {
				stack.setCount(1);
			}
			return stack;
		} else if (object instanceof Item) {
			return new ItemStack((Item) object, 1);
		} else {
			if (!(object instanceof Block)) {
				throw new RuntimeException(String.format("Invalid ItemStack: %s", object));
			}
			return new ItemStack((Block) object, 1);
		}
	}
	
	public static FluidStack fixFluidStack(Object object) {
		if (object == null) return null;
		
		else if (object instanceof FluidStack) {
			FluidStack fluidstack = ((FluidStack) object).copy();
			if (fluidstack.amount <= 0) {
				fluidstack.amount = 1000;
			}
			return fluidstack;
		} else {
			if (!(object instanceof Fluid)) {
				throw new RuntimeException(String.format("Invalid FluidStack: %s", object));
			}
			return new FluidStack((Fluid) object, 1000);
		}
	}
	
	public static OreIngredient oreStackFromString(String name) {
		if (OreDictHelper.oreExists(name)) return new OreIngredient(name, 1);
		return null;
	}
	
	public static FluidIngredient fluidStackFromString(String name) {
		if (FluidRegHelper.fluidExists(name)) return new FluidIngredient(name, 1000);
		return null;
	}

	public static List<List<ItemStack>> getItemInputLists(List<IItemIngredient> ingredientList) {
		List<List<ItemStack>> values = new ArrayList<List<ItemStack>>();
		ingredientList.forEach(ingredient -> values.add(ingredient.getInputStackList()));
		return values;
	}
	
	public static List<List<FluidStack>> getFluidInputLists(List<IFluidIngredient> ingredientList) {
		List<List<FluidStack>> values = new ArrayList<List<FluidStack>>();
		ingredientList.forEach(ingredient -> values.add(ingredient.getInputStackList()));
		return values;
	}
	
	public static List<List<ItemStack>> getItemOutputLists(List<IItemIngredient> ingredientList) {
		List<List<ItemStack>> values = new ArrayList<List<ItemStack>>();
		ingredientList.forEach(ingredient -> values.add(getItemOutputStackList(ingredient)));
		return values;
	}
	
	public static List<List<FluidStack>> getFluidOutputLists(List<IFluidIngredient> ingredientList) {
		List<List<FluidStack>> values = new ArrayList<List<FluidStack>>();
		ingredientList.forEach(ingredient -> values.add(getFluidOutputStackList(ingredient)));
		return values;
	}
	
	public static List<ItemStack> getItemOutputStackList(IItemIngredient ingredient) {
		if (ingredient instanceof ChanceItemIngredient) return ingredient.getOutputStackList();
		else return Lists.newArrayList(ingredient.getStack());
	}
	
	public static List<FluidStack> getFluidOutputStackList(IFluidIngredient ingredient) {
		if (ingredient instanceof ChanceFluidIngredient) return ingredient.getOutputStackList();
		else return Lists.newArrayList(ingredient.getStack());
	}
	
	@Nullable
	public static List<ItemStack> getItemOutputList(List<IItemIngredient> list) {
		if (list.contains(null)) return new ArrayList<ItemStack>();
		List stacks = new ArrayList<ItemStack>();
		list.forEach(ingredient -> stacks.add(ingredient.getStack()));
		if (stacks.contains(null)) return new ArrayList<ItemStack>();
		return stacks;
	}
	
	@Nullable
	public static List<FluidStack> getFluidOutputList(List<IFluidIngredient> list) {
		if (list.contains(null)) return new ArrayList<FluidStack>();
		List stacks = new ArrayList<FluidStack>();
		list.forEach(ingredient -> stacks.add(ingredient.getStack()));
		if (stacks.contains(null)) return new ArrayList<FluidStack>();
		return stacks;
	}
	
	@Nullable
	public static ItemStack getItemStackFromIngredientList(List<IItemIngredient> list, int pos) {
		if (!list.isEmpty() && pos < list.size()) {
			IItemIngredient object = list.get(pos);
			return object.getStack();
		}
		return null;
	}
	
	@Nullable
	public static FluidStack getFluidStackFromIngredientList(List<IFluidIngredient> list, int pos) {
		if (!list.isEmpty() && pos < list.size()) {
			IFluidIngredient object = list.get(pos);
			return object.getStack();
		}
		return null;
	}
	
	@Nullable
	public static IItemIngredient buildItemIngredient(Object object) {
		if (AbstractRecipeHandler.requiresItemFixing(object)) {
			object = RecipeHelper.fixItemStack(object);
		}
		if (object instanceof IItemIngredient) {
			return checkedItemIngredient((IItemIngredient) object);
		} else if (object instanceof ArrayList) {
			ArrayList list = (ArrayList) object;
			List<IItemIngredient> buildList = new ArrayList<IItemIngredient>();
			if (!list.isEmpty()) {
				for (Object listObject : list) {
					if (listObject instanceof IItemIngredient) {
						buildList.add((IItemIngredient)listObject);
					}
					else if (listObject != null) {
						IItemIngredient recipeObject = checkedItemIngredient(buildItemIngredient(listObject));
						if (recipeObject != null) {
							buildList.add(recipeObject);
						}
					}
				}
				if (buildList.isEmpty()) return null;
				return checkedItemIngredient(new ItemArrayIngredient(buildList));
			} else {
				return null;
			}
		} else if (object instanceof String) {
			return checkedItemIngredient(RecipeHelper.oreStackFromString((String) object));
		}
		if (object instanceof ItemStack) {
			return checkedItemIngredient(new ItemIngredient((ItemStack) object));
		}
		return null;
	}
	
	@Nullable
	public static IItemIngredient checkedItemIngredient(IItemIngredient ingredient) {
		return ingredient == null || !ingredient.isValid() ? null : ingredient;
	}
	
	@Nullable
	public static IFluidIngredient buildFluidIngredient(Object object) {
		if (AbstractRecipeHandler.requiresFluidFixing(object)) {
			object = RecipeHelper.fixFluidStack(object);
		}
		if (needsExpanding() && object instanceof FluidIngredient) {
			return checkedFluidIngredient(buildFluidIngredient(expandedFluidStackList((FluidIngredient)object)));
		}
		if (object instanceof IFluidIngredient) {
			return checkedFluidIngredient((IFluidIngredient) object);
		} else if (object instanceof ArrayList) {
			ArrayList list = (ArrayList) object;
			List<IFluidIngredient> buildList = new ArrayList<IFluidIngredient>();
			if (!list.isEmpty()) {
				for (Object listObject : list) {
					if (listObject instanceof IFluidIngredient) {
						buildList.add((IFluidIngredient)listObject);
					}
					else if (listObject != null) {
						IFluidIngredient recipeObject = checkedFluidIngredient(buildFluidIngredient(listObject));
						if (recipeObject != null) buildList.add(recipeObject);
					}
				}
				if (buildList.isEmpty()) return null;
				return checkedFluidIngredient(new FluidArrayIngredient(buildList));
			} else {
				return null;
			}
		} else if (object instanceof String) {
			return checkedFluidIngredient(RecipeHelper.fluidStackFromString((String) object));
		}
		if (object instanceof FluidStack) {
			return checkedFluidIngredient(new FluidIngredient((FluidStack) object));
		}
		return null;
	}
	
	@Nullable
	public static IFluidIngredient checkedFluidIngredient(IFluidIngredient ingredient) {
		return ingredient == null || !ingredient.isValid() ? null : ingredient;
	}
	
	public static boolean needsExpanding() {
		return ModCheck.mekanismLoaded() || ModCheck.techRebornLoaded();
	}
	
	/** For Mekanism and Tech Reborn fluids */
	public static List<FluidIngredient> expandedFluidStackList(FluidIngredient stack) {
		List<FluidIngredient> fluidStackList = Lists.newArrayList(stack);
		
		if (ModCheck.mekanismLoaded() && !stack.fluidName.equals("helium")) {
			if (GasHelper.TRANSLATION_MAP.containsKey(stack.fluidName)) {
				fluidStackList.add(AbstractRecipeHandler.fluidStack(GasHelper.TRANSLATION_MAP.get(stack.fluidName), stack.amount));
			}
			else {
				fluidStackList.add(AbstractRecipeHandler.fluidStack("liquid" + stack.fluidName, stack.amount));
			}
		}
		
		if (ModCheck.techRebornLoaded()) {
			fluidStackList.add(AbstractRecipeHandler.fluidStack("fluid" + stack.fluidName, stack.amount));
		}
		
		return fluidStackList;
	}
	
	public static RecipeMatchResult matchIngredients(IngredientSorption sorption, List<IItemIngredient> itemIngredients, List<IFluidIngredient> fluidIngredients, List items, List fluids, boolean shapeless) {
		if (itemIngredients.size() != items.size() || fluidIngredients.size() != fluids.size()) return RecipeMatchResult.FAIL;
		
		List<Integer> itemIngredientNumbers = new ArrayList<Integer>(Collections.nCopies(itemIngredients.size(), 0));
		List<Integer> fluidIngredientNumbers = new ArrayList<Integer>(Collections.nCopies(fluidIngredients.size(), 0));
		List<Integer> itemInputOrder = CollectionHelper.increasingList(itemIngredients.size());
		List<Integer> fluidInputOrder = CollectionHelper.increasingList(fluidIngredients.size());
		
		if (!shapeless) {
			for (int i = 0; i < items.size(); i++) {
				IngredientMatchResult matchResult = itemIngredients.get(i).match(items.get(i), sorption);
				if (matchResult.matches()) {
					itemIngredientNumbers.set(i, matchResult.getIngredientNumber());
					continue;
				}
				return RecipeMatchResult.FAIL;
			}
			for (int i = 0; i < fluids.size(); i++) {
				Object fluid = fluids.get(i) instanceof Tank ? ((Tank)fluids.get(i)).getFluid() : fluids.get(i);
				IngredientMatchResult matchResult = fluidIngredients.get(i).match(fluid, sorption);
				if (matchResult.matches()) {
					fluidIngredientNumbers.set(i, matchResult.getIngredientNumber());
					continue;
				}
				return RecipeMatchResult.FAIL;
			}
		}
		else {
			List<IItemIngredient> itemIngredientsRemaining = new ArrayList<IItemIngredient>(itemIngredients);
			itemInputs: for (int i = 0; i < items.size(); i++) {
				for (int j = 0; j < itemIngredients.size(); j++) {
					IItemIngredient itemIngredient = itemIngredientsRemaining.get(j);
					if (itemIngredient == null) continue;
					IngredientMatchResult matchResult = itemIngredient.match(items.get(i), sorption);
					if (matchResult.matches()) {
						itemIngredientsRemaining.set(j, null);
						itemIngredientNumbers.set(i, matchResult.getIngredientNumber());
						itemInputOrder.set(i, j);
						continue itemInputs;
					}
				}
				return RecipeMatchResult.FAIL;
			}
			List<IFluidIngredient> fluidIngredientsRemaining = new ArrayList<IFluidIngredient>(fluidIngredients);
			fluidInputs: for (int i = 0; i < fluids.size(); i++) {
				Object fluid = fluids.get(i) instanceof Tank ? ((Tank)fluids.get(i)).getFluid() : fluids.get(i);
				for (int j = 0; j < fluidIngredients.size(); j++) {
					IFluidIngredient fluidIngredient = fluidIngredientsRemaining.get(j);
					if (fluidIngredient == null) continue;
					IngredientMatchResult matchResult = fluidIngredient.match(fluid, sorption);
					if (matchResult.matches()) {
						fluidIngredientsRemaining.set(j, null);
						fluidIngredientNumbers.set(i, matchResult.getIngredientNumber());
						fluidInputOrder.set(i, j);
						continue fluidInputs;
					}
				}
				return RecipeMatchResult.FAIL;
			}
		}
		return new RecipeMatchResult(true, itemIngredientNumbers, fluidIngredientNumbers, itemInputOrder, fluidInputOrder);
	}
	
	public static List<String> getItemIngredientNames(List<IItemIngredient> ingredientList) {
		List<String> ingredientNames = new ArrayList<String>();
		for (IItemIngredient ingredient : ingredientList) {
			if (ingredient == null || ingredient instanceof EmptyItemIngredient) ingredientNames.add("null");
			else if (ingredient instanceof ItemArrayIngredient) ingredientNames.add(((ItemArrayIngredient)ingredient).getIngredientRecipeString());
			else ingredientNames.add(ingredient.getMaxStackSize(0) + " x " + ingredient.getIngredientName());
		}
		return ingredientNames;
	}
	
	public static List<String> getFluidIngredientNames(List<IFluidIngredient> ingredientList) {
		List<String> ingredientNames = new ArrayList<String>();
		for (IFluidIngredient ingredient : ingredientList) {
			if (ingredient == null || ingredient instanceof EmptyFluidIngredient) ingredientNames.add("null");
			else if (ingredient instanceof FluidArrayIngredient) ingredientNames.add(((FluidArrayIngredient)ingredient).getIngredientRecipeString());
			else ingredientNames.add(ingredient.getMaxStackSize(0) + " x " + ingredient.getIngredientName());
		}
		return ingredientNames;
	}
	
	public static String getAllIngredientNamesConcat(List<IItemIngredient> itemIngredientList, List<IFluidIngredient> fluidIngredientList) {
		return StringHelper.stringListConcat(getItemIngredientNames(itemIngredientList), getFluidIngredientNames(fluidIngredientList));
	}
	
	public static String getRecipeString(List<IItemIngredient> itemIngredientList, List<IFluidIngredient> fluidIngredientList, List<IItemIngredient> itemProductList, List<IFluidIngredient> fluidProductList) {
		return getAllIngredientNamesConcat(itemIngredientList, fluidIngredientList) + " -> " + getAllIngredientNamesConcat(itemProductList, fluidProductList);
	}
	
	public static String getRecipeString(IRecipe recipe) {
		if (recipe == null) return "nullRecipe";
		return getRecipeString(recipe.itemIngredients(), recipe.fluidIngredients(), recipe.itemProducts(), recipe.fluidProducts());
	}
	
	public static List<String> buildItemIngredientNames(List ingredientList) {
		List<String> ingredientNames = new ArrayList<String>();
		for (Object obj : ingredientList) {
			if (obj == null) ingredientNames.add("null");
			else {
				if (!(obj instanceof IItemIngredient)) obj = buildItemIngredient(obj);
				IItemIngredient ingredient = (IItemIngredient) obj;
				if (ingredient instanceof ItemArrayIngredient) ingredientNames.add(((ItemArrayIngredient)ingredient).getIngredientRecipeString());
				else ingredientNames.add(ingredient.getMaxStackSize(0) + " x " + ingredient.getIngredientName());
			}
		}
		return ingredientNames;
	}
	
	public static List<String> buildFluidIngredientNames(List ingredientList) {
		List<String> ingredientNames = new ArrayList<String>();
		for (Object obj : ingredientList) {
			if (obj == null) ingredientNames.add("null");
			else {
				if (!(obj instanceof IFluidIngredient)) obj = buildFluidIngredient(obj);
				IFluidIngredient ingredient = (IFluidIngredient) obj;
				if (ingredient instanceof FluidArrayIngredient) ingredientNames.add(((FluidArrayIngredient)ingredient).getIngredientRecipeString());
				else ingredientNames.add(ingredient.getMaxStackSize(0) + " x " + ingredient.getIngredientName());
			}
		}
		return ingredientNames;
	}
	
	public static List<List<String>> validFluids(ProcessorRecipeHandler recipes) {
		return validFluids(recipes, new ArrayList<String>());
	}
	
	public static List<List<String>> validFluids(ProcessorRecipeHandler recipes, List<String> exceptions) {
		int fluidInputSize = recipes.fluidInputSize;
		int fluidOutputSize = recipes.fluidOutputSize;
		
		List<FluidStack> fluidStackList = new ArrayList<FluidStack>();
		for (Fluid fluid : FluidRegistry.getRegisteredFluids().values()) fluidStackList.add(new FluidStack(fluid, 1000));
		
		List<String> fluidNameList = new ArrayList<String>();
		for (FluidStack fluidStack : fluidStackList) {
			String fluidName = fluidStack.getFluid().getName();
			if (recipes.isValidFluidInput(fluidStack) && !exceptions.contains(fluidName)) fluidNameList.add(fluidName);
		}
		
		List<List<String>> allowedFluidLists = new ArrayList<List<String>>();
		for (int i = 0; i < fluidInputSize; i++) allowedFluidLists.add(fluidNameList);
		for (int i = fluidInputSize; i < fluidInputSize + fluidOutputSize; i++) allowedFluidLists.add(null);
		
		return allowedFluidLists;
	}
	
	public static OreIngredient getOreStackFromItems(List<ItemStack> stackList, int stackSize) {
		if (stackList == null || stackList.isEmpty()) return null;
		String oreName = OreDictHelper.getOreNameFromStacks(stackList);
		if (oreName.equals("Unknown")) return null;
		return new OreIngredient(oreName, stackSize);
	}
	
	public static void generateMaterialListTuples(List<Pair<List<ItemStack>, List<FluidStack>>> tuples, int[] maxNumbers, int[] inputNumbers, List<List<ItemStack>> itemInputLists, List<List<FluidStack>> fluidInputLists) {
		int itemInputSize = itemInputLists.size(), fluidInputSize = fluidInputLists.size();
		
		List<ItemStack> itemInputs = new ArrayList<>();
		List<FluidStack> fluidInputs = new ArrayList<>();
		
		for (int i = 0; i < itemInputSize; i++) {
			itemInputs.add(itemInputLists.get(i).get(inputNumbers[i]));
		}
		
		for (int i = 0; i < fluidInputSize; i++) {
			fluidInputs.add(fluidInputLists.get(i).get(inputNumbers[i + itemInputSize]));
		}
		
		tuples.add(Pair.of(itemInputs, fluidInputs));
		
		boolean itemEnd = false;
		if (itemInputSize == 0) {
			itemEnd = true;
		}
		else {
			for (int i = 0; i < itemInputSize; i++) {
				if (inputNumbers[i] < maxNumbers[i]) {
					inputNumbers[i]++;
					break;
				}
				else {
					inputNumbers[i] = 0;
					if (i == itemInputSize - 1) itemEnd = true;
				}
			}
		}
		
		boolean fluidEnd = false;
		if (fluidInputSize == 0) {
			fluidEnd = true;
		}
		else if (itemEnd) {
			for (int i = 0; i < fluidInputSize; i++) {
				if (inputNumbers[i + itemInputSize] < maxNumbers[i + itemInputSize]) {
					inputNumbers[i + itemInputSize]++;
					break;
				}
				else {
					inputNumbers[i + itemInputSize] = 0;
					if (i == fluidInputSize - 1) fluidEnd = true;
				}
			}
		}
		
		if (!itemEnd || !fluidEnd) {
			generateMaterialListTuples(tuples, maxNumbers, inputNumbers, itemInputLists, fluidInputLists);
		}
	}
	
	public static InventoryCrafting fakeCrafter(int width, int height) {
		return new FakeCrafting(width, height);
	}
	
	private static class FakeCrafting extends InventoryCrafting {
		
		private static final FakeCraftingContainer FAKE_CONTAINER = new FakeCraftingContainer();
		
		private static class FakeCraftingContainer extends Container {
			
			@Override
			public void onCraftMatrixChanged(IInventory inventory) {
				
			}
			
			@Override
			public boolean canInteractWith(EntityPlayer player) {
				return false;
			}
		}
		
		private FakeCrafting(int width, int height) {
			super(FAKE_CONTAINER, width, height);
		}
	}
}
