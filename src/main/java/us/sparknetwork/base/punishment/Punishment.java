package us.sparknetwork.base.punishment;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.sparknetwork.base.datamanager.Model;

import java.time.ZonedDateTime;
import java.util.UUID;

@JsonDeserialize(as = BasePunishment.class)
public interface Punishment extends Model {
    @NotNull
    UUID getIssuerId();
    @NotNull
    String getIssuerName();

    @NotNull
    UUID getPunishedId();
    @Nullable
    String getPunishedName();
    @Nullable
    String getPunishedIp();

    @NotNull
    PunishmentType getType();

    @NotNull
    String getReason();

    @NotNull
    ZonedDateTime getIssuedDate();

    /**
     * @return null if the punishment is permanent
     */
    @Nullable
    ZonedDateTime getEndDate();

    boolean isIpPunishment();

    boolean isActive();

    boolean isSilent();

    void setActive(boolean active);

    @JsonIgnore
    default boolean isPermanent() {
        return getEndDate() == null;
    }
}
