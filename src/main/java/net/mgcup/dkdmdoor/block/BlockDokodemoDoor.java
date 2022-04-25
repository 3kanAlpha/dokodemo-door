package net.mgcup.dkdmdoor.block;

import net.mgcup.dkdmdoor.DokodemoDoorMod;
import net.mgcup.dkdmdoor.init.ModBlocks;
import net.mgcup.dkdmdoor.init.ModItems;
import net.mgcup.dkdmdoor.util.ServerLogManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

public class BlockDokodemoDoor extends BlockDoor  {
    private static final int TRAVEL_LIMIT = 29999872;

    public BlockDokodemoDoor(Material materialIn) {
        super(materialIn);
        this.setLightLevel(0.75f);
        this.setTickRandomly(false);

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

        if (!isOpen(worldIn, pos) || worldIn.provider.getDimension() != 0) {
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

    // --- Teleportation System Start ----


    @Override
    public int tickRate(World worldIn) {
        return 20;
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        super.updateTick(worldIn, pos, state, rand);

        if (worldIn.isRemote) return;

        this.checkIfEntityPassed(worldIn, pos, state);
    }

    @Override
    public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IBlockState state, Entity entityIn) {
        super.onEntityCollidedWithBlock(worldIn, pos, state, entityIn);

        if (worldIn.isRemote) return;

        this.checkIfEntityPassed(worldIn, pos, state);
    }

    private void checkIfEntityPassed(World worldIn, BlockPos pos, IBlockState state) {
        if (!isOpen(worldIn, pos) || worldIn.provider.getDimension() != 0) return;

        if (state.getValue(HALF) == EnumDoorHalf.UPPER) return;

        int posX = pos.getX();
        int posY = pos.getY();
        int posZ = pos.getZ();

        float f = 1.0f / 8.0f;
        AxisAlignedBB bb = new AxisAlignedBB(posX + f, posY, posZ + f, posX + 1 - f, posY + 0.5d, posZ + 1 - f);

        List<EntityLivingBase> list = worldIn.getEntitiesWithinAABB(EntityLivingBase.class, bb);

        if (list.size() > 0) {
            for (EntityLivingBase entity : list) {
                Vec3d dx = new Vec3d(entity.posX - entity.prevPosX, entity.posY - entity.prevPosY, entity.posZ - entity.prevPosZ);
                Vec3d norm = dx.normalize();
                double speed = dx.lengthVector(); // blocks per tick

                double threshSpeed = 0.1d;
                double threshAngle = 0.95d;

                boolean flag = (speed > threshSpeed);

                switch (getFacing(worldIn, pos)) {
                    case NORTH:
                        flag &= (norm.z < -threshAngle);
                        break;
                    case SOUTH:
                        flag &= (norm.z > threshAngle);
                        break;
                    case WEST:
                        flag &= (norm.x < -threshAngle);
                        break;
                    case EAST:
                        flag &= (norm.x > threshAngle);
                        break;
                    default:
                        break;
                }

                if (flag) {
                    activateTeleporting(worldIn, pos, state, entity);
                }
            }
        }
    }

    private void activateTeleporting(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase entity) {
        DokodemoDoorMod.logger.debug("Door Teleportation System Activated!");

        teleportSomewhere(worldIn, pos, state, entity);
    }

    private void teleportSomewhere(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase entity) {
        double destX = (worldIn.rand.nextDouble() - worldIn.rand.nextDouble()) * TRAVEL_LIMIT;
        double destZ = (worldIn.rand.nextDouble() - worldIn.rand.nextDouble()) * TRAVEL_LIMIT;

        BlockPos dest = new BlockPos(destX, entity.posY, destZ);

        worldIn.getChunkProvider().provideChunk(dest.getX() >> 4, dest.getZ() >> 4);

        if (entity instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) entity;

            player.dismountRidingEntity();
            player.connection.setPlayerLocation(destX, entity.posY, destZ, entity.rotationYaw, entity.rotationPitch);
            player.setRotationYawHead(entity.rotationYaw);

            double distance = pos.getDistance(dest.getX(), dest.getY(), dest.getZ());
            player.sendMessage(new TextComponentTranslation("dkdmdoor.distance", String.format("%.1f", distance / 1000.0d)));
        }
        else {
            entity.setLocationAndAngles(destX, entity.posY, destZ, entity.rotationYaw, entity.rotationPitch);
            entity.setRotationYawHead(entity.rotationYaw);
        }

        entity.fallDistance = 0.0f;
        entity.motionY = 0.0f;
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
        if (!world.isRemote) {
            String msg = String.format("%s has destroyed the travel door at (%d, %d, %d).",
                    player.getDisplayNameString(), pos.getX(), pos.getY(), pos.getZ());
            ServerLogManager.printServerLog(world.getMinecraftServer(), msg);
        }

        return super.removedByPlayer(state, world, pos, player, willHarvest);
    }

    @Override
    public void onBlockDestroyedByExplosion(World worldIn, BlockPos pos, Explosion explosionIn) {
        super.onBlockDestroyedByExplosion(worldIn, pos, explosionIn);

        if (!worldIn.isRemote) {
            String msg = String.format("The travel door at (%d, %d, %d) has been destroyed by explosion.",
                    pos.getX(), pos.getY(), pos.getZ());
            ServerLogManager.printServerLog(worldIn.getMinecraftServer(), msg);
        }
    }

    // remove the door info if the door is broken
    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        super.breakBlock(worldIn, pos, state);

        if (worldIn.isRemote) return;
    }

    // --- Teleportation System End ---
}
