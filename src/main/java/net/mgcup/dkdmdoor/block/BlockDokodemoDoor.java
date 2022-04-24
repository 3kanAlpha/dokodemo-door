package net.mgcup.dkdmdoor.block;

import net.mgcup.dkdmdoor.init.ModBlocks;
import net.mgcup.dkdmdoor.init.ModItems;
import net.minecraft.block.Block;
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

    // Door is not broken if the door is in air
    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        // if the lower part is broken
        if (state.getValue(HALF) == BlockDoor.EnumDoorHalf.UPPER) {
            BlockPos blockpos = pos.down();
            IBlockState iblockstate = worldIn.getBlockState(blockpos);

            if (iblockstate.getBlock() != this)
            {
                worldIn.setBlockToAir(pos);
            }
            else if (blockIn != this)
            {
                iblockstate.neighborChanged(worldIn, blockpos, blockIn, fromPos);
            }
        }
        else {
            boolean isBroken = false;
            BlockPos blockpos1 = pos.up();
            IBlockState iblockstate1 = worldIn.getBlockState(blockpos1);

            if (iblockstate1.getBlock() != this)
            {
                worldIn.setBlockToAir(pos);
                isBroken = true;
            }

            if (isBroken)
            {
                if (!worldIn.isRemote)
                {
                    this.dropBlockAsItem(worldIn, pos, state, 0);
                }
            }
            else
            {
                boolean flag = worldIn.isBlockPowered(pos) || worldIn.isBlockPowered(blockpos1);

                if (blockIn != this && (flag || blockIn.getDefaultState().canProvidePower()) && flag != ((Boolean)iblockstate1.getValue(POWERED)).booleanValue())
                {
                    worldIn.setBlockState(blockpos1, iblockstate1.withProperty(POWERED, Boolean.valueOf(flag)), 2);

                    if (flag != ((Boolean)state.getValue(OPEN)).booleanValue())
                    {
                        worldIn.setBlockState(pos, state.withProperty(OPEN, Boolean.valueOf(flag)), 2);
                        worldIn.markBlockRangeForRenderUpdate(pos, pos);
                        worldIn.playEvent((EntityPlayer)null, flag ? this.getOpenSound() : this.getCloseSound(), pos, 0);
                    }
                }
            }
        }
    }

    @Override
    public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        super.randomDisplayTick(stateIn, worldIn, pos, rand);

        if (!isOpen(worldIn, pos)) {
            return;
        }

        int numOfParticles = 4;

        int posX = pos.getX();
        int posY = pos.getY();
        int posZ = pos.getZ();

        for (int i = 0; i < numOfParticles; i++) {
            double startX = posX + rand.nextDouble();
            double startY = posY + rand.nextDouble();
            double startZ = posZ + rand.nextDouble();

            double speedX = (rand.nextDouble() - 0.5);
            double speedY = (rand.nextDouble() - 0.5);
            double speedZ = (rand.nextDouble() - 0.5);

            EnumFacing facing = getFacing(worldIn, pos);
            double mag = 2.0d;
            double varSpeed = (0.5 + rand.nextDouble()) * 0.5;

            switch (facing) {
                case NORTH:
                    speedZ = rand.nextDouble() * mag;
                    startZ += speedZ * varSpeed;
                    break;
                case SOUTH:
                    speedZ = rand.nextDouble() * -mag;
                    startZ += speedZ * varSpeed;
                    break;
                case WEST:
                    speedX = rand.nextDouble() * mag;
                    startX += speedX * varSpeed;
                    break;
                case EAST:
                    speedX = rand.nextDouble() * -mag;
                    startX += speedX * varSpeed;
                    break;
                default:
                    break;
            }

            worldIn.spawnParticle(EnumParticleTypes.PORTAL, startX, startY, startZ, speedX, speedY, speedZ);
        }
    }

    public String getLocalizedName()
    {
        return I18n.translateToLocal((this.getUnlocalizedName() + ".name").replaceAll("tile", "item").replaceAll("_block", ""));
    }
}
