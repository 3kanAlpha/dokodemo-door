package net.mgcup.dkdmdoor.block;

import net.mgcup.dkdmdoor.DokodemoDoorMod;
import net.mgcup.dkdmdoor.LocationFixer;
import net.mgcup.dkdmdoor.init.ModBlocks;
import net.mgcup.dkdmdoor.init.ModItems;
import net.mgcup.dkdmdoor.network.PacketTeleportationSound;
import net.mgcup.dkdmdoor.util.DoorDataManager;
import net.mgcup.dkdmdoor.util.ServerLogManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockStaticLiquid;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDoor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.chunk.Chunk;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class BlockDokodemoDoor extends BlockDoor  {
    private static final int TRAVEL_LIMIT = 29999872;
    private static final int TRAVEL_LIMIT_SAFE = 8300000; // 8300000にするとマイクラがバグる
    private static final int TRAVEL_LIMIT_STONE = 10000;

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

    /**
     * 基本BlockDoorと同じ動作だが、BlockのMaterialに関係なく手で開閉することが可能になっている
     * @param worldIn ブロックの存在するWorld
     * @param pos 右クリックされたブロックの座標
     * @param state 右クリックされたブロックのBlockState
     * @param playerIn 右クリックしたEntityPlayer
     * @param hand
     * @param facing
     * @param hitX
     * @param hitY
     * @param hitZ
     * @return ドアが正しく開閉された場合にtrueを返す
     */
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

    /**
     * ドアの下にあるブロックが壊れてもアイテム化しないように変更
     * @param state
     * @param worldIn
     * @param pos
     * @param blockIn
     * @param fromPos
     */
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

    /**
     * Entityがドアを通過したかどうかチェックする
     * @param worldIn ドアが存在するWorld
     * @param pos ドアが置かれている座標
     * @param state ドアのBlockState
     */
    private void checkIfEntityPassed(World worldIn, BlockPos pos, IBlockState state) {
        if (!isOpen(worldIn, pos) || !(worldIn.provider instanceof WorldProviderSurface)) return;

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

    /**
     * ドアをEntityが通過したときに実行される。テレポート先が既に決定されているか否かによって処理を分岐する。
     * @param worldIn ドアが存在するWorld
     * @param pos ドア下部の座標
     * @param state ドア下部のBlockState
     * @param entity ドアを通過したEntity
     */
    private void activateTeleporting(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase entity) {
        DokodemoDoorMod.logger.debug("Door Teleportation System Activated!");

        BlockPos dest = DokodemoDoorMod.saveHandler.getManager().getDestination(pos);

        // if (dest != null) DokodemoDoorMod.logger.debug(String.format("Destination: %s", dest.toString()));

        if (dest == null) {
            this.teleportSomewhere(worldIn, pos, state, entity);
        }
        else {
            this.teleportToDestination(worldIn, pos, state, entity);
        }

        DokodemoDoorMod.saveHandler.saveDoorInfo(DokodemoDoorMod.saveHandler.getManager().getDoorData());
    }

    /**
     * まだ行き先の決定されていないドアの出口をランダムに決定する。
     * @param worldIn
     * @param pos
     * @param state
     * @param entity
     */
    private void teleportSomewhere(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase entity) {
        double destX, destZ;

        if (state.getMaterial() != Material.ROCK) {
            destX = (worldIn.rand.nextDouble() - worldIn.rand.nextDouble()) * TRAVEL_LIMIT_SAFE;
            destZ = (worldIn.rand.nextDouble() - worldIn.rand.nextDouble()) * TRAVEL_LIMIT_SAFE;
        }
        else {
            destX = pos.getX() + (worldIn.rand.nextDouble() * 0.9d + 0.1d) * TRAVEL_LIMIT_STONE * (worldIn.rand.nextBoolean() ? -1 : 1);
            destZ = pos.getZ() + (worldIn.rand.nextDouble() * 0.9d + 0.1d) * TRAVEL_LIMIT_STONE * (worldIn.rand.nextBoolean() ? -1 : 1);
        }

        Chunk chunk = getChunk(worldIn, destX, destZ);
        BlockPos dest = findSpawnpointInChunk(chunk, getFacing(worldIn, pos).getOpposite());

        if (dest == null) {
            DokodemoDoorMod.logger.warn("Cannot locate the spawn point.");
            return;
        }

        DokodemoDoorMod.saveHandler.getManager().addEntry(pos.toImmutable(), dest.toImmutable(), state.getMaterial() != Material.WOOD);

        this.teleportEntity(worldIn, pos, state, entity, dest);
    }

    /**
     * 既にドアの行き先が決定されている場合に呼ばれる。ドアの接続先を取得してそこへEntityを飛ばす。
     * @param worldIn
     * @param pos
     * @param state
     * @param entity
     */
    private void teleportToDestination(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase entity) {
        DoorDataManager manager = DokodemoDoorMod.saveHandler.getManager();
        BlockPos dest = manager.getDestination(pos.toImmutable());

        Chunk chunk = getChunk(worldIn, dest.getX(), dest.getZ());

        this.teleportEntity(worldIn, pos, state, entity, dest);
    }

    /**
     * 与えられた座標にEntityをテレポートさせる。
     * @param worldIn
     * @param doorPos
     * @param doorState
     * @param entity テレポートさせるEntity
     * @param dest テレポート先の座標
     */
    private void teleportEntity(World worldIn, BlockPos doorPos, IBlockState doorState, EntityLivingBase entity, BlockPos dest) {
        if (entity.isRiding()) {
            entity.dismountRidingEntity();
        }

        BlockPos offset = dest.offset(getFacing(worldIn, doorPos));

        // このままだと窒息しないチェックをしていないので、LocationFixerで処理をする
        entity.setPositionAndUpdate(offset.getX() + 0.5d, dest.getY() + 0.5d, offset.getZ() + 0.5d);

        // ドアがなんらかの原因で壊れていた場合、再設置する
        if (doorState.getMaterial() != Material.WOOD && worldIn.getBlockState(dest).getBlock() != this) {
            if (worldIn.getBlockState(dest.down()).getBlock() == Blocks.AIR) {
                worldIn.setBlockState(dest.down(), Blocks.STONEBRICK.getDefaultState());
            }

            boolean flag = doorState.getValue(HINGE) == EnumHingePosition.RIGHT;
            ItemDoor.placeDoor(worldIn, dest, getFacing(worldIn, doorPos).getOpposite(), this, flag);
            // バグ: 耕地などの上にドアが設置されない？
        }

        if (entity instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) entity;

            // これが無いとチャンクが読み込まれるまでの間に落下して地面に埋まる
            LocationFixer.add(player, offset);

            double distance = doorPos.getDistance(dest.getX(), dest.getY(), dest.getZ());
            player.sendMessage(new TextComponentTranslation("dkdmdoor.distance", String.format("%.1f", distance / 1000.0d)));

            // ネザーポータルの音をテレポートした人のみに聞こえるように再生
            DokodemoDoorMod.packetPipeline.sendTo(new PacketTeleportationSound(), player);
        }

        ServerLogManager.printServerLog(worldIn.getMinecraftServer(), String.format("%s teleported from (%d, %d, %d) to (%d, %d, %d)",
                entity.getName(), doorPos.getX(), doorPos.getY(), doorPos.getZ(), dest.getX(), dest.getY(), dest.getZ()));

        entity.fallDistance = 0.0f;
        entity.motionY = 0.0f;
        entity.onGround = true;
    }

    /**
     * 与えられたChunk内から、ドアを設置可能な空間を探す。
     * @param chunkIn テレポート先のChunk
     * @param offset
     * @return ドアを設置可能なスペースが存在する場合その座標を返す。そうでない場合nullを返す。
     */
    @Nullable
    private static BlockPos findSpawnpointInChunk(Chunk chunkIn, EnumFacing offset) {
        final int CHUNK_SIZE = 16;
        BlockPos vertex1 = new BlockPos(chunkIn.x * CHUNK_SIZE, 52, chunkIn.z * CHUNK_SIZE);
        int y = chunkIn.getTopFilledSegment() + CHUNK_SIZE - 1;
        BlockPos vertex2 = new BlockPos(chunkIn.x * CHUNK_SIZE + CHUNK_SIZE - 1, y, chunkIn.z * CHUNK_SIZE + CHUNK_SIZE - 1);
        BlockPos spawn = null;
        double minDistance = 0.0d;

        for (BlockPos b : BlockPos.getAllInBox(vertex1, vertex2)) {
            IBlockState state = chunkIn.getBlockState(b);
            boolean flag = chunkIn.getBlockState(b.up(1)).getMaterial().isReplaceable() && chunkIn.getBlockState(b.up(2)).getBlock() == Blocks.AIR;

            if ((state.isNormalCube() || state.getBlock() instanceof BlockStaticLiquid) && flag) {
                double blockDistance = b.distanceSqToCenter(chunkIn.x * CHUNK_SIZE + 8,63, chunkIn.z * CHUNK_SIZE + 8);

                if (spawn == null || blockDistance < minDistance) {
                    spawn = (state.getBlock() instanceof BlockStaticLiquid ? b.up(10) : b).up();
                    minDistance = blockDistance;
                }
            }
        }

        return spawn;
    }

    private static Chunk getChunk(World worldIn, double blockX, double blockZ) {
        return worldIn.getChunkProvider().provideChunk(MathHelper.floor(blockX / 16.0d), MathHelper.floor(blockZ / 16.0d));
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

    /**
     * ドアが壊れた場合、ドア間の接続を切る。
     * @param worldIn 壊されたドアが存在したWorld
     * @param pos 壊されたドアが存在していた座標
     * @param state 壊されたドアのBlockState
     */
    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        super.breakBlock(worldIn, pos, state);

        if (worldIn.isRemote) return;

        if (state.getValue(HALF) == EnumDoorHalf.UPPER) return;

        DoorDataManager manager = DokodemoDoorMod.saveHandler.getManager();

        if (manager.hasDestination(pos.toImmutable())) {
            manager.removeEntry(pos.toImmutable(), state.getMaterial() != Material.WOOD);
            DokodemoDoorMod.saveHandler.saveDoorInfo(manager.getDoorData());
        }
    }

    // --- Teleportation System End ---
}
