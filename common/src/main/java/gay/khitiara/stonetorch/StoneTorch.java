package gay.khitiara.stonetorch;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;

@SuppressWarnings("UnstableApiUsage")
public final class StoneTorch {
    public static final String MOD_ID = "stonetorch";

    public static final DeferredRegister<Block> BlocksReg = DeferredRegister.create(MOD_ID, Registries.BLOCK);
    public static final RegistrySupplier<Block> StoneTorchBlock = BlocksReg.register("stone_torch", () -> new TorchBlock(
            BlockBehaviour.Properties.of()
                    .noCollission()
                    .instabreak().lightLevel(x -> 12)
                    .sound(SoundType.STONE)
                    .pushReaction(PushReaction.DESTROY),
            ParticleTypes.FLAME
    ));
    public static final RegistrySupplier<Block> StoneWallTorch = BlocksReg.register("stone_wall_torch", () -> new WallTorchBlock(
            BlockBehaviour.Properties.of()
                    .noCollission()
                    .instabreak().lightLevel(x -> 12)
                    .sound(SoundType.STONE)
                    .dropsLike(StoneTorchBlock.get())
                    .pushReaction(PushReaction.DESTROY),
            ParticleTypes.FLAME
    ));

    public static final DeferredRegister<Item> ItemsReg = DeferredRegister.create(MOD_ID, Registries.ITEM);
    public static final RegistrySupplier<Item> StoneStick = ItemsReg.register("stone_stick", () -> new Item(
            new Item.Properties().arch$tab(CreativeModeTabs.INGREDIENTS)));
    public static final RegistrySupplier<Item> StoneTorch = ItemsReg.register("stone_torch", () ->
            new StandingAndWallBlockItem(StoneTorchBlock.get(), StoneWallTorch.get(),
                    new Item.Properties().arch$tab(CreativeModeTabs.FUNCTIONAL_BLOCKS), Direction.DOWN));

    public static void init() {
        BlocksReg.register();
        ItemsReg.register();

        EnvExecutor.runInEnv(Env.CLIENT, () -> StoneTorchClient::init);
    }
}

