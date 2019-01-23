package us.sparknetwork.base.chat;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
public class BaseChatFormat implements ChatFormat {

    private final String name;
    private int priority;

    private String chatFormat;

    private String permission;

    private boolean usePlaceholderApi;
    private boolean allowRelationalPlaceholders;

    public BaseChatFormat(Map<String, Object> map) {
        name = (String) map.get("name");
        priority = (int) map.get("priority");
        chatFormat = (String) map.get("chat-format");
        permission = (String) map.getOrDefault("permission", "base.chatformat." + name);
        usePlaceholderApi = (boolean) map.getOrDefault("use-placeholderapi", false);
        allowRelationalPlaceholders = (boolean) map.getOrDefault("allow-relational-placeholders", false);
    }

    public BaseChatFormat(ChatFormat format){
        this.name = format.getName();
        this.priority = format.getPriority();
        this.chatFormat = format.getChatFormat();
        this.permission = format.getPermission();
        this.usePlaceholderApi = format.isUsePlaceholderApi();
        this.allowRelationalPlaceholders = format.isAllowRelationalPlaceholders();
    }

    public BaseChatFormat(String name, int priority) {
        this.name = name;
        this.priority = priority;
        chatFormat = "[\"\",{\"text\":\"{prefix}{displayName}{suffix}\"},{\"text\":\": \",\"color\":\"dark_gray\"},{\"text\":\"{chat}\",\"color\":\"gray\"}]";
        permission = "default";
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new LinkedHashMap<>();

        map.put("name", name);
        map.put("priority", priority);
        map.put("chat-format", chatFormat);
        map.put("permission", permission);
        map.put("use-placeholderapi", usePlaceholderApi);
        map.put("allow-relational-placeholders", allowRelationalPlaceholders);

        return map;
    }
}
