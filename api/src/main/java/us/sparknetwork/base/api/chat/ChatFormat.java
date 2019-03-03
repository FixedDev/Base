package us.sparknetwork.base.api.chat;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;
import us.sparknetwork.base.api.util.JsonMessage;

import java.util.List;

public interface ChatFormat extends ConfigurationSerializable {
    String getFormatName();

    int getPriority();

    @NotNull
    String getPrefix();

    @NotNull
    ClickAction getPrefixClickAction();

    @NotNull
    String getPrefixClickActionContent();

    @NotNull
    List<String> getPrefixTooltip();

    @NotNull
    String getPlayerName();

    @NotNull
    ClickAction getPlayerNameClickAction();

    @NotNull
    String getPlayerNameClickActionContent();

    @NotNull
    List<String> getPlayerNameTooltip();

    @NotNull
    String getSuffix();

    @NotNull
    ClickAction getSuffixClickAction();

    @NotNull
    String getSuffixClickActionContent();

    @NotNull
    List<String> getSuffixTooltip();

    @NotNull
    String getChatColor();

    @NotNull
    String getPermission();

    default JsonMessage constructJsonMessage() {
        JsonMessage.JsonStringBuilder builder = new JsonMessage().append(getPrefix());

        if (!getPrefixTooltip().isEmpty()) {
            builder.setHoverAsTooltip(getPrefixTooltip().toArray(new String[0]));
        }

        switch (getPrefixClickAction()) {
            case OPEN_URL:
                builder.setClickAsURL(getPrefixClickActionContent());
                break;
            case EXECUTE_COMMAND:
                builder.setClickAsExecuteCmd(getPrefixClickActionContent());
                break;
            case SUGGEST_COMMAND:
                builder.setClickAsSuggestCmd(getPrefixClickActionContent());
                break;
            default:
            case NONE:
                break;
        }

        builder = builder.save().append(getPlayerName());

        if (!getPlayerNameTooltip().isEmpty()) {
            builder.setHoverAsTooltip(getPlayerNameTooltip().toArray(new String[0]));
        }

        switch (getPlayerNameClickAction()) {
            case OPEN_URL:
                builder.setClickAsURL(getPlayerNameClickActionContent());
                break;
            case EXECUTE_COMMAND:
                builder.setClickAsExecuteCmd(getPlayerNameClickActionContent());
                break;
            case SUGGEST_COMMAND:
                builder.setClickAsSuggestCmd(getPlayerNameClickActionContent());
                break;
            default:
            case NONE:
                break;
        }

        builder = builder.save().append(getSuffix());

        if (!getSuffixTooltip().isEmpty()) {
            builder.setHoverAsTooltip(getSuffixTooltip().toArray(new String[0]));
        }

        switch (getSuffixClickAction()) {
            case OPEN_URL:
                builder.setClickAsURL(getSuffixClickActionContent());
                break;
            case EXECUTE_COMMAND:
                builder.setClickAsExecuteCmd(getSuffixClickActionContent());
                break;
            case SUGGEST_COMMAND:
                builder.setClickAsSuggestCmd(getSuffixClickActionContent());
                break;
            default:
            case NONE:
                break;
        }
        
        return builder.save();
    }

    boolean isUsePlaceholderApi();

    boolean isAllowRelationalPlaceholders();

    void setPriority(int priority);

    void setPrefix(@NotNull String prefix);

    void setPrefixClickAction(@NotNull ClickAction prefixClickAction);

    void setPrefixClickActionContent(@NotNull String prefixClickActionContent);

    void setPrefixTooltip(@NotNull List<String> prefixTooltip);

    void setPlayerName(@NotNull String playerName);

    void setPlayerNameClickAction(@NotNull ClickAction playerNameClickAction);

    void setPlayerNameClickActionContent(@NotNull String playerNameClickActionContent);

    void setPlayerNameTooltip(@NotNull List<String> playerNameTooltip);

    void setSuffix(@NotNull String suffix);

    void setSuffixClickAction(@NotNull ClickAction suffixClickAction);

    void setSuffixClickActionContent(@NotNull String suffixClickActionContent);

    void setSuffixTooltip(@NotNull List<String> suffixTooltip);

    void setPermission(@NotNull String permission);

    void setUsePlaceholderApi(boolean usePlaceholderApi);

    void setAllowRelationalPlaceholders(boolean allowRelationalPlaceholders);
}
