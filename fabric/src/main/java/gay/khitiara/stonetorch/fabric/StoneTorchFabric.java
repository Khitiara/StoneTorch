package gay.khitiara.stonetorch.fabric;

import gay.khitiara.stonetorch.fabriclike.StoneTorchFabricLike;
import net.fabricmc.api.ModInitializer;

public class StoneTorchFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        StoneTorchFabricLike.init();
    }
}