package gay.khitiara.stonetorch.forge;

import dev.architectury.platform.forge.EventBuses;
import gay.khitiara.stonetorch.StoneTorch;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(StoneTorch.MOD_ID)
public class StoneTorchForge {
    public StoneTorchForge() {
		// Submit our event bus to let architectury register our content on the right time
        EventBuses.registerModEventBus(StoneTorch.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        StoneTorch.init();
    }
}