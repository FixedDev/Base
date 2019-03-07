package us.sparknetwork.base.listeners;

import com.google.inject.Inject;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import us.sparknetwork.base.I18n;
import us.sparknetwork.base.event.PunishmentEvent;
import us.sparknetwork.base.punishment.Punishment;
import us.sparknetwork.base.punishment.PunishmentManager;
import us.sparknetwork.base.punishment.PunishmentType;
import us.sparknetwork.utils.DateUtil;
import us.sparknetwork.utils.ListenableFutureUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class PunishmentListener implements Listener {

    @Inject
    private PunishmentManager manager;
    @Inject
    private I18n i18n;

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPunishment(PunishmentEvent event) {
        Punishment punishment = event.getPunishment();

        if (punishment.getType() == PunishmentType.BAN) {
            OfflinePlayer punished = Bukkit.getOfflinePlayer(punishment.getPunishedId());

            String banType = i18n.translate("punishment.banned");

            if (punishment.isIpPunishment()) {
                banType = i18n.translate("punishment.ipbanned");
            }

            if (punished.isOnline()) {
                if (!punishment.isPermanent() && punishment.getEndDate() != null) {
                    punished.getPlayer().kickPlayer(i18n.format("punishment.temporally.kick.message",
                            banType,
                            punishment.getIssuerName(),
                            DateUtil.getHumanReadableDate(Instant.now().until(punishment.getEndDate(), ChronoUnit.MILLIS), i18n),
                            punishment.getReason()));
                } else {
                    punished.getPlayer().kickPlayer(i18n.format("punishment.kick.message",
                            banType,
                            punishment.getIssuerName(),
                            punishment.getReason()));
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onJoin(PlayerLoginEvent e) {
        Player player = e.getPlayer();

        Punishment punish = manager.getPunishmentSync(PunishmentType.BAN, player.getUniqueId(), e.getAddress().getHostAddress());

        if (punish == null) {
            return;
        }

        String banType = i18n.translate("punishment.banned");

        if (punish.isIpPunishment()) {
            banType = i18n.translate("punishment.ipbanned");
        }

        // The punish.endDate == null is just because SonarLint complains that endDate can be null
        // Even if punish.isPermanent is the same that punish.endDate == null
        if (punish.isPermanent() || punish.getEndDate() == null) {
            e.disallow(PlayerLoginEvent.Result.KICK_BANNED,
                    ChatColor.RED + i18n.format("punishment.kick.message",
                            banType,
                            punish.getIssuerName(),
                            punish.getReason()));

            return;
        }

        long banSecondsLeft = Instant.now().until(punish.getEndDate(), ChronoUnit.SECONDS);

        if (banSecondsLeft <= 0) {
            punish.setActive(false);
            manager.savePunishment(punish);

            if (e.getResult() == PlayerLoginEvent.Result.ALLOWED || e.getResult() == PlayerLoginEvent.Result.KICK_BANNED) {
                e.allow();
            }
            return;
        }

        e.disallow(PlayerLoginEvent.Result.KICK_BANNED,
                ChatColor.RED + i18n.format("punishment.temporal.kick.message",
                        banType,
                        punish.getIssuerName(),
                        DateUtil.getHumanReadableDate(banSecondsLeft * 1000, i18n),
                        punish.getReason()));

    }

}
