package us.sparknetwork.base.hooks;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import us.sparknetwork.base.BasePlugin;

public class ProtocolLibHook {

    ProtocolManager manager;

    public void addListeners(BasePlugin plugin) {
         manager = ProtocolLibrary.getProtocolManager();
         manager.addPacketListener(new PacketAdapter(plugin, PacketType.Play.Client.TAB_COMPLETE) {
             @Override
             public void onPacketReceiving(PacketEvent event) {
                 if(event.getPlayer() != null && !event.getPlayer().hasPermission("base.tabcomplete")){
                     String[] args =  event.getPacket().getStrings().read(0).substring(1).split(" ");
                     if(args.length == 0){
                         event.setCancelled(true);
                         return;
                     }
                     if (args.length == 1 ){
                         switch (args[0]){
                             case "ver":
                             case "version":
                             case "bukkit:version":
                             case "bukkit:ver":
                             case "help":
                             case "about":
                             case "?":
                             case "bukkit:help":
                             case "bukkit:about":
                             case "bukkit:?":
                                event.setCancelled(true);
                                return;
                         }
                     }
                 }
             }
         });
    }

}
