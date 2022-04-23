package net.mgcup.dkdmdoor.block;

import net.mgcup.dkdmdoor.init.ModBlocks;
import net.mgcup.dkdmdoor.init.ModItems;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Random;

public class BlockDokodemoDoor extends BlockDoor  {

    public BlockDokodemoDoor(Material materialIn) {
        super(materialIn);
        this.setLightLevel(0.75f);

        if (materialIn == Material.WOOD) this.setSoundType(SoundType.WOOD);
        else if (materialIn == Material.IRON) this.setSoundType(SoundType.METAL);
        else if (materialIn == Material.ROCK) this.setSoundType(SoundType.STONE);
    }

    @Override
    public MapColor getMapColor(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        if (state.getBlock() == ModBlocks.WOODEN_TELEPORTER_BLOCK) {
            return MapColor.MAGENTA;
        }
        else if (state.getBlock() == ModBlocks.IRON_TELEPORTER_BLOCK) {
            return MapColor.IRON;
        }
        else if (state.getBlock() == ModBlocks.STONE_TELEPORTER_BLOCK) {
            return MapColor.BLUE;
        }

        return super.getMapColor(state, worldIn, pos);
    }

    @Override
    public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state) {
        return new ItemStack(this.getItem());
    }

    private Item getItem() {
        if (this == ModBlocks.WOODEN_TELEPORTER_BLOCK) {
            return ModItems.WOODEN_TELEPORTER;
        }
        else if (this == ModBlocks.STONE_TELEPORTER_BLOCK) {
            return ModItems.STONE_TELEPORTER;
        }

        return ModItems.IRON_TELEPORTER;
    }

    private int getCloseSound()
    {
        return this.blockMaterial == Material.IRON ? 1011 : 1012;
    }

    private int getOpenSound()
    {
        return this.blockMaterial == Material.IRON ? 1005 : 1006;
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        BlockPos bottom = state.getValue(HALF) == BlockDoor.EnumDoorHalf.LOWER ? pos : pos.down();
        IBlockState iBlockState = pos.equals(bottom) ? state : worldIn.getBlockState(bottom);

        if (iBlockState.getBlock() != this) {
            return false;
        }
        else {
            state = iBlockState.cycleProperty(OPEN);
            worldIn.setBlockState(bottom, state, 10);
            worldIn.markBlockRangeForRenderUpdate(bottom, pos);
            worldIn.playEvent(playerIn, ((Boolean)state.getValue(OPEN)).booleanValue() ? this.getOpenSound() : this.getCloseSound(), pos, 0);
            return true;
        }
    }

    @Override
    public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        super.randomDisplayTick(stateIn, worldIn, pos, rand);

        if (!isOpen(worldIn, pos)) {
            return;
        }

        for (int i = 0; i < 4; i++) {
            // worldIn.spawnParticle(EnumParticleTypes.PORTAL, 0, 0, 0, 0, 0, 0, );
        }
    }

    public String getLocalizedName()
    {
        return I18n.translateToLocal((this.getUnlocalizedName() + ".name").replaceAll("tile", "item").replaceAll("_block", ""));
    }
}
