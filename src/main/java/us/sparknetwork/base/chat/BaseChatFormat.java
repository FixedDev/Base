package us.sparknetwork.base.chat;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static us.sparknetwork.base.chat.ClickAction.NONE;

@Getter
@Setter
public class BaseChatFormat implements ChatFormat {

    @NotNull
    private final String formatName;
    private int priority;

    @NotNull
    private String prefix = "";
    @NotNull
    private ClickAction prefixClickAction = NONE;
    @NotNull
    private String prefixClickActionContent = "";
    @NotNull
    private List<String> prefixTooltip = new ArrayList<>();

    @NotNull
    private String playerName = "{displayName}";
    @NotNull
    private ClickAction playerNameClickAction = NONE;
    @NotNull
    private String playerNameClickActionContent = "";
    @NotNull
    private List<String> playerNameTooltip = new ArrayList<>();

    @NotNull
    private String suffix = "";
    @NotNull
    private ClickAction suffixClickAction = NONE;
    @NotNull
    private String suffixClickActionContent = "";
    @NotNull
    private List<String> suffixTooltip = new ArrayList<>();

    @NotNull
    private String chatColor;

    @NotNull
    private String permission;

    private boolean usePlaceholderApi;
    private boolean allowRelationalPlaceholders;

    @SuppressWarnings("unchecked")
    public BaseChatFormat(@NotNull Map<String, Object> map) {
        formatName = (String) map.get("name");
        priority = (int) map.get("priority");

        prefix = (String) map.getOrDefault("prefix", "");
        prefixClickAction = ClickAction.valueOf((String) map.getOrDefault("prefix-click-action", "NONE"));
        prefixClickActionContent = (String) map.getOrDefault("prefix-click-action-content", "");
        prefixTooltip = (List<String>) map.getOrDefault("prefix-tooltip", prefixTooltip);

        playerName = (String) map.getOrDefault("player-name", "");
        playerNameClickAction = ClickAction.valueOf((String) map.getOrDefault("player-name-click-action", "NONE"));
        playerNameClickActionContent = (String) map.getOrDefault("player-name-click-action-content", "");
        playerNameTooltip = (List<String>) map.getOrDefault("player-name-tooltip", prefixTooltip);

        suffix = (String) map.getOrDefault("suffix", "");
        suffixClickAction = ClickAction.valueOf((String) map.getOrDefault("suffix-click-action", "NONE"));
        suffixClickActionContent = (String) map.getOrDefault("suffix-click-action-content", "");
        suffixTooltip = (List<String>) map.getOrDefault("suffix-tooltip", suffixTooltip);

        chatColor = (String) map.getOrDefault("chat-color", "");

        permission = (String) map.getOrDefault("permission", "base.chatformat." + formatName);
        usePlaceholderApi = (boolean) map.getOrDefault("use-placeholderapi", false);
        allowRelationalPlaceholders = (boolean) map.getOrDefault("allow-relational-placeholders", false);
    }

    public BaseChatFormat(@NotNull ChatFormat format) {
        this.formatName = format.getFormatName();
        this.priority = format.getPriority();
        this.permission = format.getPermission();
        this.usePlaceholderApi = format.isUsePlaceholderApi();
        this.allowRelationalPlaceholders = format.isAllowRelationalPlaceholders();
    }

    public BaseChatFormat(@NotNull String formatName, int priority) {
        this.formatName = formatName;
        this.priority = priority;
        permission = "default";
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new LinkedHashMap<>();

        map.put("name", formatName);
        map.put("priority", priority);

        map.put("prefix", prefix);
        map.put("prefix-click-action", prefixClickAction.toString());
        map.put("prefix-click-action-content", prefixClickActionContent);
        map.put("prefix-tooltip", prefixTooltip);

        map.put("player-name", playerName);
        map.put("player-name-click-action", playerNameClickAction.toString());
        map.put("player-name-click-action-content", playerNameClickActionContent);
        map.put("player-name-tooltip", playerNameTooltip);

        map.put("suffix", suffix);
        map.put("suffix-click-action", suffixClickAction.toString());
        map.put("suffix-click-action-content", suffixClickActionContent);
        map.put("suffix-tooltip", suffixTooltip);

        map.put("chat-color", chatColor);

        map.put("permission", permission);
        map.put("use-placeholderapi", usePlaceholderApi);
        map.put("allow-relational-placeholders", allowRelationalPlaceholders);

        return map;
    }
}
