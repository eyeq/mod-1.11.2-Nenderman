package eyeq.nenderman.entity.monster;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.BlockPistonMoving;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.state.BlockPistonStructureHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class EntityNenderman extends EntityEnderman {
    private static final DataParameter<EnumFacing> TARGET_FACE = EntityDataManager.createKey(EntityNenderman.class, DataSerializers.FACING);

    public EntityNenderman(World world) {
        super(world);
    }

    @Override
    protected void initEntityAI() {
        super.initEntityAI();

        List<EntityAITasks.EntityAITaskEntry> removeList = new ArrayList<>();
        int i = 0;
        for(EntityAITasks.EntityAITaskEntry taskEntry : this.tasks.taskEntries) {
            if(i == 5 || i == 6) {
                removeList.add(taskEntry);
            }
            i++;
        }
        for(EntityAITasks.EntityAITaskEntry taskEntry : removeList) {
            this.tasks.taskEntries.remove(taskEntry);
        }
        this.tasks.addTask(9, new EntityAIMoveBlock(this));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(25.0);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.5);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(TARGET_FACE, EnumFacing.DOWN);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setTargetFace(EnumFacing.getFront(compound.getByte("TargetFace")));
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setByte("TargetFace", (byte) this.getTargetFace().getIndex());
    }

    @Override
    protected void dropEquipment(boolean wasRecentlyHit, int lootingModifier) {
        dropItem(Item.getItemFromBlock(Blocks.PISTON), 1);
    }

    public void setTargetFace(EnumFacing facing) {
        this.dataManager.set(TARGET_FACE, facing);
    }

    public EnumFacing getTargetFace() {
        return this.dataManager.get(TARGET_FACE);
    }

    public class EntityAIMoveBlock extends EntityAIBase {
        private final EntityNenderman entity;

        public EntityAIMoveBlock(EntityNenderman entity) {
            this.entity = entity;
        }

        @Override
        public boolean shouldExecute() {
            EnumFacing facing;
            if(entity.getRNG().nextInt(20) == 0) {
                facing = EnumFacing.random(entity.getRNG());
                entity.setTargetFace(facing);
            } else {
                facing = entity.getTargetFace();
            }
            World world = entity.world;
            BlockPos pos = entity.getPosition().offset(facing);
            IBlockState state = world.getBlockState(pos);
            entity.setHeldBlockState(state);
            if(!entity.getEntityWorld().getGameRules().getBoolean("mobGriefing")) {
                return false;
            }
            if(entity.getRNG().nextInt(20) != 0) {
                return false;
            }
            return BlockPistonBase.canPush(state, world, pos, facing, true);
        }

        @Override
        public void updateTask() {
            IBlockState state = entity.getHeldBlockState();
            if(state == null) {
                return;
            }
            EnumFacing facing = entity.getTargetFace();
            BlockPos pos = entity.getPosition().offset(facing);

            boolean isExtending = !state.getProperties().containsKey(BlockPistonBase.EXTENDED);
            if(!isExtending) {
                world.setBlockToAir(pos.offset(facing));
            }
            BlockPistonStructureHelper pistonHelper = new BlockPistonStructureHelper(world, entity.getPosition(), facing, isExtending);
            if(!pistonHelper.canMove()) {
                return;
            }

            List<BlockPos> destroyList = pistonHelper.getBlocksToDestroy();
            List<BlockPos> moveList = pistonHelper.getBlocksToMove();
            IBlockState[] actuals = new IBlockState[moveList.size()];
            for(int i = 0; i < moveList.size(); i++) {
                BlockPos pos1 = moveList.get(i);
                actuals[i] = world.getBlockState(pos1).getActualState(world, pos1);
            }

            Block[] destroyedBlocks = new Block[destroyList.size()];
            for(int i = 0; i < destroyList.size(); i++) {
                BlockPos pos1 = destroyList.get(i);
                Block block = world.getBlockState(pos1).getBlock();
                float chance = block instanceof BlockSnow ? -1.0F : 1.0F;
                block.dropBlockAsItemWithChance(world, pos1, world.getBlockState(pos1), chance, 0);
                world.setBlockToAir(pos1);
                destroyedBlocks[i] = block;
            }

            Block[] movedBlocks = new Block[moveList.size()];
            for(int i = 0; i < moveList.size(); i++) {
                BlockPos pos1 = moveList.get(i);
                IBlockState state1 = world.getBlockState(pos1);
                world.setBlockToAir(pos1);

                pos1 = pos1.offset(facing);
                world.setBlockState(pos1, Blocks.PISTON_EXTENSION.getDefaultState().withProperty(BlockPistonBase.FACING, facing), 4);
                world.setTileEntity(pos1, BlockPistonMoving.createTilePiston(actuals[i], facing, isExtending, false));
                movedBlocks[i] = state1.getBlock();
            }

            for(int i = 0; i < destroyList.size(); i++) {
                BlockPos pos1 = destroyList.get(i);
                world.notifyNeighborsOfStateChange(pos1, destroyedBlocks[i], false);
            }
            for(int i = 0; i < moveList.size(); i++) {
                BlockPos pos1 = moveList.get(i);
                world.notifyNeighborsOfStateChange(pos1, movedBlocks[i], false);
            }
            if(isExtending) {
                BlockPos targetPos = pos.offset(facing);
                world.notifyNeighborsOfStateChange(targetPos, Blocks.PISTON_HEAD, false);
                world.notifyNeighborsOfStateChange(pos, state.getBlock(), false);
            }
        }
    }
}
