package us.sparknetwork.base;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import lombok.Getter;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

public enum StaffPriority {

    NONE(0, "base.staffpriority.none"),
    LOWEST(1, "base.staffpriority.lowest"),
    LOW(2, "base.staffpriority.low"),
    MEDIUM(3, "base.staffpriority.medium"),
    NORMAL(4, "base.staffpriority.normal"),
    HiGH(5, "base.staffpriority.high"),
    HIGHEST(6, "base.staffpriority.highest");

    private static final ImmutableMap<Integer, StaffPriority> BY_ID;

    static {
        Builder<Integer, StaffPriority> builder = new Builder<>();
        for (StaffPriority sp : values()) {
            builder.put(sp.priority, sp);
        }
        BY_ID = builder.build();
    }

    @Getter
    final int priority;
    @Getter
    final String permission;

    StaffPriority(int prior, String permission) {
        this.priority = prior;
        this.permission = permission;
    }

    public static StaffPriority getByLevel(int level) {
        return BY_ID.get(level);
    }

    public static StaffPriority getByCommandSender(CommandSender sender) {
        if(sender instanceof ConsoleCommandSender){
            return HIGHEST;
        }

        for (StaffPriority sp : values()) {
            if (sp == NONE)
                continue;
            if (sender.hasPermission(sp.getPermission())) {
                return sp;
            }
        }
        return NONE;

    }

    public boolean isMoreThan(StaffPriority sp) {
        return this.priority > sp.priority;
    }

}
