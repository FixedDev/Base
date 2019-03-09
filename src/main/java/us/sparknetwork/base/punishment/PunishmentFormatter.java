package us.sparknetwork.base.punishment;

import com.google.inject.Inject;
import org.bukkit.Bukkit;
import us.sparknetwork.base.I18n;
import us.sparknetwork.utils.DateUtil;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

public class PunishmentFormatter {
    @Inject
    private I18n i18n;

    public String getPunishmentMessage(Punishment punishment) {
        String punishmentTypePath = "";
        String messagePath = "punishment.message";

        if (punishment.getType() == PunishmentType.WARN || punishment.getType() == PunishmentType.STRIKE) {
            messagePath = "punishment.warn.message";
            punishmentTypePath = "punishment.warn.warning";

            if (punishment.getType() == PunishmentType.STRIKE) {
                punishmentTypePath = "punishment.warn.strike";
            }

            return i18n.format(messagePath, punishment.getPunishedName(), i18n.translate(punishmentTypePath), punishment.getIssuerName(), punishment.getReason());
        }

        if (punishment.isPermanent()) {
            switch (punishment.getType()) {
                case KICK:
                    punishmentTypePath = "punishment.kicked";
                    break;
                case BAN:
                    if (punishment.isIpPunishment()) {
                        punishmentTypePath = "punishment.ipbanned";
                        break;
                    }
                    punishmentTypePath = "punishment.banned";
                    break;
                case MUTE:
                    punishmentTypePath = "punishment.muted";
                    break;
            }
        } else {
            messagePath = "punishment.temporal.message";
            switch (punishment.getType()) {
                case KICK:
                    punishmentTypePath = "punishment.kicked";
                    break;
                case BAN:
                    if (punishment.isIpPunishment()) {
                        punishmentTypePath = "punishment.ipbanned";
                        break;
                    }
                    punishmentTypePath = "punishment.banned";
                    break;
                case MUTE:
                    punishmentTypePath = "punishment.muted";
                    break;
            }
        }

        if (punishment.isPermanent()) {
            return i18n.format(messagePath, punishment.getPunishedName(), i18n.translate(punishmentTypePath), punishment.getIssuerName(), punishment.getReason());
        }

        return i18n.format(messagePath,
                punishment.getPunishedName(),
                i18n.translate(punishmentTypePath),
                punishment.getIssuerName(),
                DateUtil.getHumanReadableDate(ZonedDateTime.now().until(punishment.getEndDate(), ChronoUnit.MILLIS), i18n),
                punishment.getReason());
    }

    public void broadcastPunishmentMessage(Punishment punishment) {
        String message = getPunishmentMessage(punishment);

        if (punishment.isSilent()) {
            Bukkit.broadcast(message, "base.punishment.silent.see");

            return;
        }

        if (punishment.getType() == PunishmentType.STRIKE) {
            Bukkit.broadcast(message, "base.command.strike.see");

            return;
        }

        Bukkit.broadcastMessage(message);
    }
}
