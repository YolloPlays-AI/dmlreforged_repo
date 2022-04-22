package com.yollo.dmlreforged.common.util.container;

import com.yollo.dmlreforged.common.blocks.entity.BlockEntitySimulationChamber;
import com.yollo.dmlreforged.common.energy.DeepEnergyStorage;
import com.yollo.dmlreforged.common.items.init.BlockInit;
import com.yollo.dmlreforged.common.items.init.ContainerInit;
import com.yollo.dmlreforged.common.util.DataModelHelper;
import com.yollo.dmlreforged.common.util.container.slots.SlotSimulationChamber;
import com.yollo.dmlreforged.common.util.container.sync.SimulationChamberContainerData;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuConstructor;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class SimulationChamberContainer extends AbstractContainerMenu {

	public static final int DATA_MODEL_SLOT = 0;
	public static final int POLYMER_SLOT = 1;
	public static final int LIVING_SLOT = 2;
	public static final int PRISTINE_SLOT = 3;
	private final ContainerLevelAccess containerAccess;
	public final ContainerData data;
	public IItemHandler handler;

	// Client
	public SimulationChamberContainer(int id, Inventory playerInv) {
		this(id, playerInv, new ItemStackHandler(4), BlockPos.ZERO, new SimpleContainerData(3));
	}

	// Server
	public SimulationChamberContainer(int pContainerId, Inventory pInventory, IItemHandler handler, BlockPos pPos,
			ContainerData data) {
		super(ContainerInit.SIMULATION_CHAMBER.get(), pContainerId);
		this.handler = handler;
		this.containerAccess = ContainerLevelAccess.create(pInventory.player.level, pPos);
		this.data = data;
		addInventorySlots(pInventory);
		addSlotsToHandler(handler);
		addDataSlots(data);
	}

	private void addInventorySlots(Inventory playerInv) {
		for (int row = 0; row < 3; row++) {
			for (int column = 0; column < 9; column++) {
				int x = 8 + column * 18;
				int y = 119 + row * 18;
				int index = column + row * 9 + 9;
				Slot slot = new Slot(playerInv, index, x, y);
				addSlot(slot);
			}
		}
		// Bind actionbar
		for (int row = 0; row < 9; row++) {
			Slot slot = new Slot(playerInv, row, 8 + row * 18, 177);
			addSlot(slot);
		}
	}

	private void addSlotsToHandler(IItemHandler handler) {
		addSlot(new SlotSimulationChamber(handler, DATA_MODEL_SLOT, -40, -35));
		addSlot(new SlotSimulationChamber(handler, POLYMER_SLOT, 148, -29));
		addSlot(new SlotSimulationChamber(handler, LIVING_SLOT, 168, -29));
		addSlot(new SlotSimulationChamber(handler, PRISTINE_SLOT, 158, -9));
	}

	@Override
	public boolean stillValid(Player pPlayer) {
		return stillValid(this.containerAccess, pPlayer, BlockInit.SIMULATION_CHAMBER.get());
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		var retStack = ItemStack.EMPTY;
		final Slot slot = getSlot(index);
		if (slot.hasItem()) {
			final ItemStack item = slot.getItem();
			retStack = item.copy();
			if (index < 36) {
				if (!moveItemStackTo(item, 36, this.slots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else if (!moveItemStackTo(item, 0, 36, false)) {
				return ItemStack.EMPTY;
			}

			if (item.isEmpty()) {
				slot.set(ItemStack.EMPTY);
			} else
				slot.setChanged();
		}
		return retStack;
	}

	public static MenuConstructor getServerContainer(BlockEntitySimulationChamber be, BlockPos pos) {
		return (id, playerInv, player) -> new SimulationChamberContainer(id, playerInv, be.inventory, pos,
				new SimulationChamberContainerData(be, 3));
	}
	
	public ItemStack getDataModel() {
		return handler.getStackInSlot(DATA_MODEL_SLOT);
	}
	
	public ItemStack getPolymerClay() {
		return handler.getStackInSlot(POLYMER_SLOT);
	}
	
    private static boolean dataModelMatchesOutput(ItemStack stack, ItemStack output) {
        Item livingMatter = DataModelHelper.getMobMetaData(stack).getLivingMatter();
        return livingMatter.getClass().equals(output.getItem().getClass());
    }
    
    private static boolean dataModelMatchesPristine(ItemStack stack, ItemStack pristine) {
        Item pristineMatter = DataModelHelper.getMobMetaData(stack).getPristineMatter();
        return pristineMatter.getClass().equals(pristine.getItem().getClass());
    }

    public boolean outputIsFull() {
        ItemStack stack = handler.getStackInSlot(LIVING_SLOT);
        if(stack.isEmpty()) {
            return false;
        }

        boolean stackLimitReached = stack.getCount() == handler.getSlotLimit(LIVING_SLOT);
        boolean outputMatches = dataModelMatchesOutput(getDataModel(), handler.getStackInSlot(LIVING_SLOT));

        return stackLimitReached || !outputMatches;
    }
    
    public boolean pristineIsFull() {
        ItemStack stack = handler.getStackInSlot(PRISTINE_SLOT);
        if(stack.isEmpty()) {
            return false;
        }

        boolean stackLimitReached = stack.getCount() == handler.getSlotLimit(PRISTINE_SLOT);
        boolean outputMatches = dataModelMatchesPristine(getDataModel(), handler.getStackInSlot(PRISTINE_SLOT));

        return stackLimitReached || !outputMatches;
    }
    
}
