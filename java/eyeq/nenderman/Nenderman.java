package eyeq.nenderman;

import eyeq.util.client.renderer.ResourceLocationFactory;
import eyeq.util.client.resource.ULanguageCreator;
import eyeq.util.client.resource.lang.LanguageResourceManager;
import eyeq.util.common.registry.UEntityRegistry;
import eyeq.util.world.biome.BiomeUtils;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import eyeq.nenderman.entity.monster.EntityNenderman;
import eyeq.nenderman.client.renderer.entity.RenderNenderman;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.File;
import java.util.List;

import static eyeq.nenderman.Nenderman.MOD_ID;

@Mod(modid = MOD_ID, version = "1.0", dependencies = "after:eyeq_util")
public class Nenderman {
    public static final String MOD_ID = "eyeq_nenderman";

    @Mod.Instance(MOD_ID)
    public static Nenderman instance;

    private static final ResourceLocationFactory resource = new ResourceLocationFactory(MOD_ID);

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        registerEntities();
        if(event.getSide().isServer()) {
            return;
        }
        registerEntityRenderings();
        createFiles();
    }

    public static void registerEntities() {
        UEntityRegistry.registerModEntity(resource, EntityNenderman.class, "Nenderman", 0, instance, 0x8B8B8B, 0x8B8B8B);
        List<Biome> biomes = BiomeUtils.getSpawnBiomes(EntityEnderman.class, EnumCreatureType.MONSTER);
        EntityRegistry.addSpawn(EntityNenderman.class, 6, 1, 3, EnumCreatureType.MONSTER, biomes.toArray(new Biome[0]));
    }

    @SideOnly(Side.CLIENT)
    public static void registerEntityRenderings() {
        RenderingRegistry.registerEntityRenderingHandler(EntityNenderman.class, RenderNenderman::new);
    }

    public static void createFiles() {
        File project = new File("../1.11.2-Nenderman");

        LanguageResourceManager language = new LanguageResourceManager();

        language.register(LanguageResourceManager.EN_US, EntityNenderman.class, "Nenderman");
        language.register(LanguageResourceManager.JA_JP, EntityNenderman.class, "ネンダーマン");

        ULanguageCreator.createLanguage(project, MOD_ID, language);
    }
}
