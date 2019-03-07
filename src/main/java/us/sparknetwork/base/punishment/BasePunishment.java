package us.sparknetwork.base.punishment;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.beans.ConstructorProperties;
import java.time.Instant;
import java.util.UUID;

@JsonSerialize(as = Punishment.class)
public class BasePunishment implements Punishment {

    private @NotNull String id;

    private @NotNull UUID issuerId;
    private @NotNull String issuerName;

    private @NotNull UUID punishedId;
    private @Nullable String punishedName;
    private @Nullable String punishedIp;

    private @NotNull PunishmentType type;

    private @NotNull String reason;

    private @NotNull Instant issuedDate;
    private @Nullable Instant endDate;

    private boolean ipPunishment;

    private boolean active;

    private boolean silent;

    @ConstructorProperties({"_id","issuerId", "issuerName", "punishedId", "punishedName", "punishedIp", "type", "reason", "issuedDate", "endDate", "ipPunishment", "active", "silent"})
    @JsonCreator
    BasePunishment(@NotNull String id, @NotNull UUID issuerId, @NotNull String issuerName, @NotNull UUID punishedId, @Nullable String punishedName, @Nullable String punishedIp, @NotNull PunishmentType type, @NotNull String reason, @NotNull Instant issuedDate, @Nullable Instant endDate, boolean ipPunishment, boolean active, boolean silent) {
        this.id = id;
        this.issuerId = issuerId;
        this.issuerName = issuerName;
        this.punishedId = punishedId;
        this.punishedName = punishedName;
        this.punishedIp = punishedIp;
        this.type = type;
        this.reason = reason;
        this.issuedDate = issuedDate;
        this.endDate = endDate;
        this.ipPunishment = ipPunishment;
        this.silent = silent;
    }

    @Override
    public @NotNull String getId() {
        return id;
    }

    @Override
    public @NotNull UUID getIssuerId() {
        return issuerId;
    }

    @Override
    public @NotNull String getIssuerName() {
        return issuerName;
    }

    @Override
    public @NotNull UUID getPunishedId() {
        return punishedId;
    }

    @Override
    public @Nullable String getPunishedName() {
        return punishedName;
    }

    @Override
    public @Nullable String getPunishedIp() {
        return punishedIp;
    }

    @Override
    public @NotNull PunishmentType getType() {
        return type;
    }

    @Override
    public @NotNull String getReason() {
        return reason;
    }

    @Override
    public @NotNull Instant getIssuedDate() {
        return issuedDate;
    }

    @Override
    public @Nullable Instant getEndDate() {
        return endDate;
    }

    @Override
    public boolean isIpPunishment() {
        return ipPunishment;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public boolean isSilent() {
        return silent;
    }

    @Override
    public void setActive(boolean active) {
        this.active = active;
    }

}
