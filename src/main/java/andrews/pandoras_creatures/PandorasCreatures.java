package andrews.pandoras_creatures;

import andrews.pandoras_creatures.config.PCConfigs;
import andrews.pandoras_creatures.item_groups.PCItemGroup;
import andrews.pandoras_creatures.network.PCNetwork;
import andrews.pandoras_creatures.objects.items.PCSpawnEggItem;
import andrews.pandoras_creatures.registry.PCBlocks;
import andrews.pandoras_creatures.registry.PCContainers;
import andrews.pandoras_creatures.registry.PCCrafting;
import andrews.pandoras_creatures.registry.PCEntities;
import andrews.pandoras_creatures.registry.PCFeatures;
import andrews.pandoras_creatures.registry.PCItems;
import andrews.pandoras_creatures.registry.PCRenderLayers;
import andrews.pandoras_creatures.registry.PCSounds;
import andrews.pandoras_creatures.registry.PCStructures;
import andrews.pandoras_creatures.registry.PCTileEntities;
import andrews.pandoras_creatures.registry.util.PCDispenserBehaviors;
import andrews.pandoras_creatures.registry.util.PCEntityAttributes;
import andrews.pandoras_creatures.util.Reference;
import andrews.pandoras_creatures.util.RehostedJarHandler;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.moddiscovery.ModFile;

@Mod(value = Reference.MODID)
public class PandorasCreatures
{
	public static PandorasCreatures instance;
	public static final ItemGroup PANDORAS_CREATURES_GROUP = new PCItemGroup();

	@SuppressWarnings("deprecation")
	public PandorasCreatures()
	{
		instance = this;
		final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		
		PCItems.ITEMS.register(modEventBus);
		PCBlocks.BLOCKS.register(modEventBus);
		PCSounds.SOUNDS.register(modEventBus);
		PCTileEntities.TILE_ENTITY_TYPES.register(modEventBus);
		PCContainers.CONTAINERS.register(modEventBus);
		PCEntities.ENTITY_TYPES.register(modEventBus);
//		PCParticles.PARTICLES.register(modEventBus); Will be used in the future
		PCFeatures.FEATURES.register(modEventBus);
		PCStructures.STRUCTURE_FEATURES.register(modEventBus);
		PCCrafting.RECIPES.register(modEventBus);
		
		DistExecutor.runWhenOn(Dist.CLIENT, () -> () ->
		{
			modEventBus.addListener(EventPriority.LOWEST, this::registerItemColors);
			modEventBus.addListener(EventPriority.LOWEST, this::setupClient);
		});
		modEventBus.addListener(EventPriority.LOWEST, this::setupCommon);
		
		PCConfigs.registerConfigs();
	}
	
	void setupCommon(final FMLCommonSetupEvent event)
	{	
		event.enqueueWork(() -> 
		{
			PCDispenserBehaviors.registerAll();
			PCEntityAttributes.putAttributes();
			PCFeatures.registerConfiguredFeatures();
			PCStructures.registerStructureFeaturesAndSeparation();
			PCEntities.registerEntityPlacementLogics();
		});
		//Thread Safe Stuff
		PCNetwork.setupMessages();
		ModFile file = ModList.get().getModFileById(Reference.MODID).getFile();
		new RehostedJarHandler(file, "pandoras_creatures-" + Reference.VERSION);
	}
	
	@OnlyIn(Dist.CLIENT)
	void setupClient(final FMLClientSetupEvent event)
	{
		event.enqueueWork(() -> 
		{
			PCRenderLayers.setBlockRenderLayers();
			PCItems.setupItemProperties();
			PCContainers.screenSetup();
			PCTileEntities.registerTileRenders();
//			PCParticles.registerParticleFactories(); Will be used in the future
		});
		//Thread Safe Stuff
		PCEntities.registerEntityRenders();
	}
	
	@OnlyIn(Dist.CLIENT)
	public void registerItemColors(ColorHandlerEvent.Item event)
	{
		for(RegistryObject<Item> items : PCItems.SPAWN_EGGS)
		{
			Item item = items.get();
			if(item instanceof PCSpawnEggItem)
			{
				event.getItemColors().register((itemColor, itemsIn) ->
				{
					return ((PCSpawnEggItem) item).getColor(itemsIn);
				}, item);
			}
		}
	}
}