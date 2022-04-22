package com.yollo.dmlreforged.client.screen;

import java.text.DecimalFormat;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import com.yollo.dmlreforged.DeepMobLearning;
import com.yollo.dmlreforged.common.items.ItemDeepLearner;
import com.yollo.dmlreforged.common.mobmetas.DragonMeta;
import com.yollo.dmlreforged.common.mobmetas.EndermanMeta;
import com.yollo.dmlreforged.common.mobmetas.GhastMeta;
import com.yollo.dmlreforged.common.mobmetas.MobMetaData;
import com.yollo.dmlreforged.common.mobmetas.SlimeMeta;
import com.yollo.dmlreforged.common.mobmetas.SpiderMeta;
import com.yollo.dmlreforged.common.mobmetas.WitchMeta;
import com.yollo.dmlreforged.common.mobmetas.WitherMeta;
import com.yollo.dmlreforged.common.util.DataModelHelper;
import com.yollo.dmlreforged.common.util.container.DeepLearnerContainer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class DeepLearnerScreen extends AbstractContainerScreen<DeepLearnerContainer>{
	
    public static final int WIDTH =  338;
    public static final int HEIGHT = 235;
    protected ItemStack deepLearner;
    private InteractionHand hand;
    private MobMetaData meta;
    private Level world;
    private NonNullList<ItemStack> validDataModels;
    private int currentItem = 0;
    private PoseStack pose;
	private ImageButton imgBtnPrev;
	private ImageButton imgBtnNext;
    private static final ResourceLocation base = new ResourceLocation(DeepMobLearning.MOD_ID, "textures/gui/deeplearner_base.png");
    private static final ResourceLocation extras = new ResourceLocation(DeepMobLearning.MOD_ID, "textures/gui/deeplearner_extras.png");
    private static final ResourceLocation defaultGui = new ResourceLocation(DeepMobLearning.MOD_ID, "textures/gui/default_gui.png");

	public DeepLearnerScreen(DeepLearnerContainer container, Inventory playerInv, Component component) {
		super(container, playerInv, component);
        this.world = playerInv.player.level;
        hand = playerInv.player.getMainHandItem().getItem() instanceof ItemDeepLearner ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        this.deepLearner = playerInv.player.getItemInHand(hand);
	}

	@Override
	protected void renderBg(PoseStack pose, float partialTicks, int mouseX, int mouseY) {
		int left = getGuiLeft();
		int top = getGuiTop();
		this.pose = pose;
		
		//Render base
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
		RenderSystem.setShaderTexture(0, base);
		blit(pose, left - 41, top-36 , 0, 0, 256, 140);
		
		//Render playerInv
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
		RenderSystem.setShaderTexture(0, defaultGui);
		blit(pose, left + 0, top + 111, 0, 0, 176, 90);
		
        // Get the meta for the first ItemDataModel in this deeplearner
        NonNullList<ItemStack> list = ItemDeepLearner.getContainedItems(deepLearner);
        this.validDataModels = DataModelHelper.getValidFromList(list);
        // Render cycle buttons if we have multiple models
        if(validDataModels.size() > 1) {
        	if(renderables.size() < 2) {
        		renderCycleButtons(pose, left, top);
        	}    
        }
        else {
        	removeWidget(imgBtnNext);
        	removeWidget(imgBtnPrev);
        	
        }

        // If we have at least 1 valid data model
        if(validDataModels.size() >= 1 && currentItem <= validDataModels.size()) {      	
        	int fixPos = (validDataModels.size()-1 - currentItem) < 0 ? validDataModels.size()-1 : validDataModels.size()-1 - currentItem;
            this.meta = DataModelHelper.getMobMetaData(validDataModels.get(validDataModels.size() >= 1 ? fixPos : currentItem));
            renderMetaDataText(meta, left, top, validDataModels.get(fixPos), pose);
            renderMobDisplayBox(pose, left, top);
            renderEntityInInventory(getGuiLeft() - 85, getGuiTop() + 52, 30, partialTicks, meta, world);
            /*if(meta instanceof ZombieMeta || meta instanceof SpiderMeta) {
                renderEntityInInventory(getGuiLeft() + 51, getGuiTop() + 75, 30, (float)(getGuiLeft() + 51) - this.xMouse, (float)(getGuiTop() + 75 - 50) - this.yMouse,(LivingEntity) meta.getExtraEntity(world));
            }*/
        } else {
            renderDefaultScreen(pose);
        }
	}
	
	@Override
	public void render(PoseStack p_97795_, int p_97796_, int p_97797_, float p_97798_) {
		this.renderBackground(p_97795_);
		super.render(p_97795_, p_97796_, p_97797_, p_97798_);
		this.renderTooltip(p_97795_, p_97796_, p_97797_);
	}
	
	
	
	private void renderDefaultScreen(PoseStack pose) {
		int leftStart = getGuiLeft() - 32;
        int top = getGuiTop() - 32;
        int spacing = 12;
		drawString(pose, font, new TranslatableComponent("dmlreforged.gui.deep_learner.not_found"), leftStart, top + spacing, 0x55FFFF);
		drawString(pose, font, new TranslatableComponent("dmlreforged.gui.deep_learner.insert"), leftStart, top + (spacing*2), 0xFFFFFF);
		drawString(pose, font, new TranslatableComponent("dmlreforged.gui.deep_learner.collect_data"), leftStart, top + (spacing*3), 0xFFFFFF);
		drawString(pose, font, new TranslatableComponent("dmlreforged.gui.deep_learner.when_placed"), leftStart, top + (spacing*4), 0xFFFFFF);
		drawString(pose, font, new TranslatableComponent("dmlreforged.gui.deep_learner.in_order"), leftStart, top + (spacing*6), 0xFFFFFF);
		drawString(pose, font, new TranslatableComponent("dmlreforged.gui.deep_learner.killing_blow"), leftStart, top + (spacing*7), 0xFFFFFF);		
	}
	
    private void renderMetaDataText(MobMetaData meta, int left, int top, ItemStack stack, PoseStack pose) {
        DecimalFormat f = new DecimalFormat("0.#");
		int leftStart = getGuiLeft() - 32;
        int topStart = top - 40;
        int spacing = 12;
        drawString(pose, font, "Name", leftStart, topStart + spacing, 0x55FFFF);
        drawString(pose, font,  "The " + meta.getName(), leftStart, topStart + (spacing *  2), 0xFFFFFF);
        drawString(pose, font, "Information", leftStart, topStart + (spacing *  3), 0xFFFFFF);
        String mobTrivia[] = meta.getMobTrivia();
        for (int i = 0; i < mobTrivia.length; i++) {
            drawString(pose, font, mobTrivia[i], leftStart, topStart + (spacing * 3) + ((i + 1) * 12), 0xFFFFFF);
        }
        MutableComponent dataModelTier = DataModelHelper.getTierName(stack, false);
        MutableComponent nextTier = DataModelHelper.getTierName(stack, true);
        String pluralMobName = DataModelHelper.getMobMetaData(stack).getPluralName();        
        int totalKills = DataModelHelper.getTotalKillCount(stack);
        double killsToNextTier = DataModelHelper.getKillsToNextTier(stack);
        drawString(pose, font, new TranslatableComponent("dmlreforged.tiers.tier", dataModelTier), leftStart, topStart + (spacing * 8), 0xFFFFFF);
        drawString(pose, font, pluralMobName + " defeated: " + totalKills, leftStart, topStart + (spacing * 9), 0xFFFFFF);
        if(DataModelHelper.getTier(stack) != DeepMobLearning.DATA_MODEL_MAXIMUM_TIER) {
            drawString(pose, font, new TranslatableComponent("dmlreforged.tiers.tier_next", f.format(killsToNextTier), nextTier), leftStart, topStart + (spacing * 10), 0xFFFFFF);
        } else {
            drawString(pose, font, "Maximum tier achieved", leftStart, topStart + (spacing * 10), 0xFFFFFF);
        }
        // Draw heart
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
		RenderSystem.setShaderTexture(0, base);
        blit(pose, left + 154, topStart + (spacing * 2) - 2, 0, 140, 9, 9);
        drawString(pose, font, "Life points", left + 154, topStart + spacing, 0x55FFFF);
        int numOfHearts = meta.getNumberOfHearts();
        if(numOfHearts == 0) {
            // Obfuscate if hearts is 0, use for models with multiple mobmetas
            drawString(pose, font, "�k10�r", left + 164, topStart + (spacing * 2) - 1, 0xFFFFFF);
        } else {
            drawString(pose, font, "" + meta.getNumberOfHearts(), left + 164, topStart + (spacing * 2) - 1, 0xFFFFFF);
        }
    }
   
    private void renderCycleButtons(PoseStack pose, int left, int top) {
        // Draw the buttons
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
		RenderSystem.setShaderTexture(0, extras);
		imgBtnPrev = new ImageButton(getGuiLeft() - 84, getGuiTop() + 70, 24, 24, 99, 0, 24, extras, 255, 255, btn -> {
        	if(validDataModels.size() > 1) {
        		this.currentItem = nextItemIndex();
        	}
        });
		imgBtnNext = new ImageButton(getGuiLeft() - 110, getGuiTop() + 70, 24, 24, 75, 0, 24, extras, 255, 255, btn -> {
        	if(validDataModels.size() > 1) {
        		this.currentItem = previousItemIndex();
        	}
        });
		addRenderableWidget(imgBtnNext);
        addRenderableWidget(imgBtnPrev); 
    }
    
    private void renderMobDisplayBox(PoseStack pose, int left, int top) {
        // Draw the mob display box
    	RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
		RenderSystem.setShaderTexture(0, extras);
        blit(pose, left -123, top-36, 0, 0, 75, 101);
    }
    
	
	@Override
	protected void renderLabels(PoseStack p_97808_, int p_97809_, int p_97810_) {
	}
	
    public NonNullList<ItemStack> getItemStacks() {
        NonNullList<ItemStack> list = NonNullList.create();        
        int numOfSlots  = DeepMobLearning.DEEP_LEARNER_INTERNAL_SLOTS_SIZE;
        for (int i = 0; i < numOfSlots; i++) {
            list.add(i, getMenu().getSlot(i).getItem());
        }
        return list;
    }
     
    @Override
    public Component getTitle() {
    	return super.getTitle();
    }
    
    @Override
    protected void slotClicked(Slot slot, int dragType, int index, ClickType clickTypeIn) {
    	this.validDataModels = DataModelHelper.getValidFromList(ItemDeepLearner.getContainedItems(deepLearner));
        if(validDataModels.size() >= 1 && currentItem < validDataModels.size()) {
            this.meta = DataModelHelper.getMobMetaData(validDataModels.get(currentItem));
            renderMetaDataText(meta, getGuiLeft(), getGuiTop(), validDataModels.get(currentItem), pose);
            if(slot!= null && slot.getItem().isEmpty()) {
            	nextItemIndex();
            }
        }
        getMenu().getDeepInv().setChanged();
    	super.slotClicked(slot, dragType, index, clickTypeIn);
    }
    
    @SuppressWarnings("deprecation")
	public static void renderEntityInInventory(int xPos, int yPos, int scale, float partialTicks, MobMetaData meta, Level world) {
        PoseStack posestack = RenderSystem.getModelViewStack();
        posestack.pushPose();
        posestack.translate((double)xPos, (double)yPos + Math.sin(world.getGameTime()/13.0d)*3.0d, 1050.0D);
        Quaternion quaternion1;
        Quaternion quaternion;
        LivingEntity entity= meta.getEntity(world);;
        if(meta instanceof DragonMeta) {
        	posestack.scale(0.19F, 0.19F, -0.19F);
        	quaternion1 = Vector3f.XP.rotationDegrees(10.0F);
        	quaternion = Vector3f.ZP.rotationDegrees(180.0F);
        	posestack.translate((double)xPos-67, (double)yPos - 200 + Math.sin(world.getGameTime()/13.0d)*3.0d, 1050.0D);
        }else if(meta instanceof WitherMeta) {
        	posestack.scale(0.7F, 0.7F, -0.7F);
        	quaternion1 = Vector3f.XP.rotationDegrees(180.0F);
        	quaternion = Vector3f.ZP.rotationDegrees(0.0F);
        	posestack.translate((double)(xPos-68), (double)(yPos- 87 + Math.sin(world.getGameTime()/13.0d)*3.0d), 1050.0D);
        }else if(meta instanceof GhastMeta) {
        	posestack.scale(0.33F, 0.33F, -0.33F);
        	quaternion1 = Vector3f.XP.rotationDegrees(180.0F);
        	quaternion = Vector3f.ZP.rotationDegrees(0.0F);
        	posestack.translate((double)(xPos-68), (double)(yPos- 188 + Math.sin(world.getGameTime()/13.0d)*0.001d), 0.0D);
        }else if(meta instanceof SpiderMeta) {
        	posestack.scale(1.02F, 1.02F, -1.02F);
        	quaternion1 = Vector3f.XP.rotationDegrees(180.0F);
        	quaternion = Vector3f.ZP.rotationDegrees(0.0F);
        	posestack.translate((double)(xPos-68), (double)(yPos- 110 + Math.sin(world.getGameTime()/13.0d)*3.0d), 0.0D);
        }else if(meta instanceof WitchMeta) {
        	posestack.scale(1.2F, 1.2F, -1.2F);
        	quaternion1 = Vector3f.XP.rotationDegrees(180.0F);
        	quaternion = Vector3f.ZP.rotationDegrees(0.0F);
        	posestack.translate((double)(xPos-68), (double)(yPos- 97 + Math.sin(world.getGameTime()/13.0d)*0.001d), 0.0D);
        }else if(meta instanceof EndermanMeta) {
        	posestack.scale(1.02F, 1.02F, -1.02F);
        	quaternion1 = Vector3f.XP.rotationDegrees(180.0F);
        	quaternion = Vector3f.ZP.rotationDegrees(0.0F);
        	posestack.translate((double)(xPos-68), (double)(yPos- 99 + Math.sin(world.getGameTime()/13.0d)*0.00001d), 0.0D);
        }else if(meta instanceof SlimeMeta) {
        	posestack.scale(2F, 2F, -2F);
        	quaternion1 = Vector3f.XP.rotationDegrees(180.0F);
        	quaternion = Vector3f.ZP.rotationDegrees(0.0F);
        	posestack.translate((double)(xPos-68), (double)(yPos- 110 + Math.sin(world.getGameTime()/13.0d)*3.0d), 0.0D);
        }else {
        	posestack.scale(1.2F, 1.2F, -1.2F);
        	quaternion1 = Vector3f.XP.rotationDegrees(180.0F);
        	quaternion = Vector3f.ZP.rotationDegrees(0.0F);
        }      	
        RenderSystem.applyModelViewMatrix();
        PoseStack posestack1 = new PoseStack();
        posestack1.translate(0.0D, 0.0D, 1000.0D);
        posestack1.scale((float)scale, (float)scale, (float)scale);   
        quaternion.mul(quaternion1);
        posestack1.mulPose(quaternion); 
        entity.yBodyRot =  0.0f + (float) (world.getGameTime()*1.2d);
        entity.yHeadRot =  0.0f + (float) (world.getGameTime()*1.2d);
        if(entity instanceof WitherBoss wither) {
        	wither.yRotHeads[0] = 0.0f + (float) (world.getGameTime()*1.2d);
        	wither.yRotHeads[1] = 0.0f + (float) (world.getGameTime()*1.2d);
        }
        Lighting.setupForEntityInInventory();    
        EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        entityrenderdispatcher.setRenderShadow(false);
        MultiBufferSource.BufferSource multibuffersource$buffersource = Minecraft.getInstance().renderBuffers().bufferSource();
        RenderSystem.runAsFancy(() -> {
           entityrenderdispatcher.render(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, posestack1, multibuffersource$buffersource, 15728880);
        });
        multibuffersource$buffersource.endBatch();
        entityrenderdispatcher.setRenderShadow(true);
        entity.yBodyRot = 0.0F;
        entity.yHeadRot = 0.0f;
        posestack.popPose();
        RenderSystem.applyModelViewMatrix();
        Lighting.setupFor3DItems();
     }
    
    private int nextItemIndex() {
        int result;      
        // If last in list go back to start of list
        if(currentItem == validDataModels.size() - 1) {
            result = 0;
        } else {
            result = currentItem + 1;
        }
        return result;
    }

    private int previousItemIndex() {
    	int result;
        // If first in list
        if(currentItem == 0) {
            if(validDataModels.size() > 1) {
                result = validDataModels.size() - 1;
            } else {
                result = 0;
            }
        } else {
            result = currentItem - 1;
        }

        return result;
    }
}
