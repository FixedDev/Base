package us.sparknetwork.base.punishment;

import org.jetbrains.annotations.Nullable;

public enum PunishmentType {
    BAN(null),
    MUTE(null),
    KICK(null),
    WARN(null),
    STRIKE(WARN),
    IPBAN(BAN);

    private PunishmentType superType;

    PunishmentType(@Nullable PunishmentType superType){
        this.superType = superType;
    }

    @Nullable
    public PunishmentType getSuperType() {
        return superType;
    }
}
