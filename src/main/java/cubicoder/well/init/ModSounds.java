package cubicoder.well.init;

import javax.annotation.Nonnull;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

/**
 *
 * @author jbred
 *
 */
public final class ModSounds
{
    @Nonnull
    public static final SoundEvent CRANK = new SoundEvent(new ResourceLocation("well", "block.well.crank")).setRegistryName("well", "block.well.crank");
}
