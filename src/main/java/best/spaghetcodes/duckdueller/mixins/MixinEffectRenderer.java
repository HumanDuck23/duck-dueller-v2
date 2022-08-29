package best.spaghetcodes.duckdueller.mixins;

import com.google.common.collect.Lists;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.particle.EntityParticleEmitter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ConcurrentModificationException;
import java.util.List;

@Mixin(EffectRenderer.class)
public abstract class MixinEffectRenderer {

    @Shadow protected abstract void updateEffectLayer(int layer);

    @Shadow private List<EntityParticleEmitter> particleEmitters;

    /**
     * @author Mojang
     * @reason Fix ConcurrentModificationException crash
     */
    @Overwrite
    public void updateEffects() {
        try {
            for (int i = 0; i < 4; ++i)
            {
                this.updateEffectLayer(i);
            }

            List<EntityParticleEmitter> list = Lists.newArrayList();

            for (EntityParticleEmitter entityparticleemitter : this.particleEmitters)
            {
                entityparticleemitter.onUpdate();

                if (entityparticleemitter.isDead)
                {
                    list.add(entityparticleemitter);
                }
            }

            this.particleEmitters.removeAll(list);
        }catch(final ConcurrentModificationException ignored) {
        }
    }

}
