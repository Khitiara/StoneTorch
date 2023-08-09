package gay.khitiara.stonetorch.quilt;

import gay.khitiara.stonetorch.fabriclike.StoneTorchFabricLike;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;

public class StoneTorchQuilt implements ModInitializer {
    @Override
    public void onInitialize(ModContainer container) {
        StoneTorchFabricLike.init();
    }
}