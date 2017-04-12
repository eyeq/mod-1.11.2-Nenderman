package eyeq.nenderman.client.renderer.entity;

import eyeq.util.client.renderer.EntityRenderResourceLocation;
import net.minecraft.client.renderer.entity.RenderEnderman;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.util.ResourceLocation;

import static eyeq.nenderman.Nenderman.MOD_ID;

public class RenderNenderman extends RenderEnderman {
    protected static final ResourceLocation textures = new EntityRenderResourceLocation(MOD_ID, "nenderman");

    public RenderNenderman(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityEnderman entity) {
        return textures;
    }
}
