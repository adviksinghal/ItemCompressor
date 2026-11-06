package com.ic.client;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ItemCompressorClient implements ClientModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("itemcompressor");

	@Override
	public void onInitializeClient() {
		LOGGER.info("ItemCompressor loaded! Suppressing duplicate item draw calls on client.");
	}
}