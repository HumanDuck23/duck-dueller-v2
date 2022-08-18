package best.spaghetcodes.duckdueller.mixins;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.network.handshake.FMLHandshakeMessage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Mixin(FMLHandshakeMessage.ModList.class)
public class MixinModList {

    @Shadow(remap = false)
    private Map<String, String> modTags;

    @Inject(method = "<init>(Ljava/util/List;)V", at = @At("RETURN"), remap = false)
    public void removeMod(List<ModContainer> modContainerList, CallbackInfo ci) {
        if (!Minecraft.getMinecraft().isSingleplayer()) {
            System.out.println("Removing mod from handshake...");
            this.modTags.keySet().removeIf(key -> Objects.equals(key, "duckdueller"));
        }
    }

}
