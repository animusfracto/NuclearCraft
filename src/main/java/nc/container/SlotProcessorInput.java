package nc.container;

import nc.recipe.ProcessorRecipeHandler;
import nc.tile.inventory.ITileInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotProcessorInput extends Slot {
	
	public final ProcessorRecipeHandler recipeHandler;
	
	public SlotProcessorInput(ITileInventory tile, ProcessorRecipeHandler recipeHandler, int slotIndex, int xPosition, int yPosition) {
		super(tile.getInventory(), slotIndex, xPosition, yPosition);
		this.recipeHandler = recipeHandler;
	}
	
	@Override
	public boolean isItemValid(ItemStack stack) {
		return recipeHandler.isValidItemInput(stack);
	}
}
