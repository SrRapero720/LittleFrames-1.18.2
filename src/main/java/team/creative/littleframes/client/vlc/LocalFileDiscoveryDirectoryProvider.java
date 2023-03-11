
package team.creative.littleframes.client.vlc;

import java.nio.file.Path;

import net.minecraftforge.fml.loading.FMLPaths;
import me.srrapero720.waterframes.vlcj.binding.support.runtime.RuntimeUtil;
import me.srrapero720.waterframes.vlcj.factory.discovery.provider.DiscoveryDirectoryProvider;

public class LocalFileDiscoveryDirectoryProvider implements DiscoveryDirectoryProvider {
    
    @Override
    public int priority() {
        return 2;
    }
    
    @Override
    public boolean supported() {
        return true;
    }
    
    @Override
    public String[] directories() {
        Path vlc = FMLPaths.GAMEDIR.get().resolve("vlc");
        if (RuntimeUtil.isNix())
            vlc = vlc.resolve("linux");
        else if (RuntimeUtil.isMac())
            vlc = vlc.resolve("mac");
        else {
            boolean is64 = System.getProperty("sun.arch.data.model").equals("64");
            vlc = vlc.resolve("windows_" + (is64 ? "x64" : "x86"));
        }
        return new String[] { vlc.toString() };
    }
}
