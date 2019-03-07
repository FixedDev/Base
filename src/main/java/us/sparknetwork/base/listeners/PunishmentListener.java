package us.sparknetwork.base.listeners;

import com.google.inject.Inject;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import us.sparknetwork.base.I18n;
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

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onJoin(PlayerLoginEvent e) {
        Player player = e.getPlayer();

        ListenableFutureUtils.addCallback(manager.getPunishment(PunishmentType.BAN, player.getUniqueId(), e.getAddress().getHostAddress()), punish -> {
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

        });
    }

}
