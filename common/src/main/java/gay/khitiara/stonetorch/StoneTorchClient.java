package gay.khitiara.stonetorch;

import dev.architectury.event.events.client.ClientLifecycleEvent;
import dev.architectury.registry.client.rendering.RenderTypeRegistry;
import net.minecraft.client.renderer.RenderType;

public final class StoneTorchClient {
    public static void init() {
        ClientLifecycleEvent.CLIENT_SETUP.register(instance -> {
            RenderTypeRegistry.register(RenderType.cutout(), StoneTorch.StoneTorchBlock.get(), StoneTorch.StoneWallTorch.get());
        });
    }
}
