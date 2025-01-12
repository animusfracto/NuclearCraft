package nc.container;

import nc.tile.inventory.ITileInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public class SlotSpecificInput extends Slot {
	
	public final Object[] inputs;
	
	public SlotSpecificInput(ITileInventory tile, int slotIndex, int xPosition, int yPosition, Object... inputs) {
		this(tile.getInventory(), slotIndex, xPosition, yPosition, inputs);
	}
	
	public SlotSpecificInput(IInventory inv, int slotIndex, int xPosition, int yPosition, Object... inputs) {
		super(inv, slotIndex, xPosition, yPosition);
		this.inputs = inputs;
	}
	
	@Override
	public boolean isItemValid(ItemStack stack) {
		for (int i = 0; i < inputs.length; i++) {
			if (inputs[i] instanceof String) {
				ItemStack[] ores = (ItemStack[])OreDictionary.getOres((String)inputs[i]).toArray();
				for (int j = 0; j < inputs.length; j++) {
					if (ItemStack.areItemsEqual(ores[j], stack)) return true;
				}
			} else if (inputs[i] instanceof ItemStack) {
				if (ItemStack.areItemsEqual((ItemStack)inputs[i], stack)) return true;
			} else if (inputs[i] instanceof Item) {
				if ((Item)inputs[i] == stack.getItem()) return true;
			}
		}
		return false;
	}
}
