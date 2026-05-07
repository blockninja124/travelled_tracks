package com.blockninja.forge;

import com.blockninja.TravelledTracks;
import net.minecraftforge.fml.common.Mod;

@Mod(TravelledTracks.MOD_ID)
public final class TravelledTracksForge {
    public TravelledTracksForge() {
        // Run our common setup.
        TravelledTracks.init();
    }
}
