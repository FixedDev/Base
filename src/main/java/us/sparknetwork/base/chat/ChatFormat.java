package us.sparknetwork.base.chat;

import net.kyori.text.TextComponent;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;
import us.sparknetwork.utils.EasyKyoriComponent;

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

    default TextComponent constructJsonMessage() {
        EasyKyoriComponent prefix = new EasyKyoriComponent();

        prefix.append(getPrefix());

        if (!getPrefixTooltip().isEmpty()) {
            EasyKyoriComponent hoverComponent = new EasyKyoriComponent();

            boolean first = true;
            for (String line : getPrefixTooltip()) {
                if (first) {
                    first = false;

                    hoverComponent.append(line);
                    continue;
                }
                hoverComponent.appendWithNewLine(line);
            }

            prefix.setHoverShowText(hoverComponent.build());
        }

        switch (getPrefixClickAction()) {
            case OPEN_URL:
                prefix.setClickOpenUrl(getPrefixClickActionContent());
                break;
            case EXECUTE_COMMAND:
                prefix.setClickRunCommand(getPrefixClickActionContent());
                break;
            case SUGGEST_COMMAND:
                prefix.setClickSuggestCommand(getPrefixClickActionContent());
                break;
            default:
            case NONE:
                break;
        }

        EasyKyoriComponent playerName = new EasyKyoriComponent();

        playerName.append(getPlayerName());

        if (!getPlayerNameTooltip().isEmpty()) {
            EasyKyoriComponent hoverComponent = new EasyKyoriComponent();

            boolean first = true;
            for (String line : getPlayerNameTooltip()) {
                if (first) {
                    first = false;

                    hoverComponent.append(line);
                    continue;
                }
                hoverComponent.appendWithNewLine(line);
            }

            playerName.setHoverShowText(hoverComponent.build());
        }


        switch (getPlayerNameClickAction()) {
            case OPEN_URL:
                playerName.setClickOpenUrl(getPrefixClickActionContent());
                break;
            case EXECUTE_COMMAND:
                playerName.setClickRunCommand(getPrefixClickActionContent());
                break;
            case SUGGEST_COMMAND:
                playerName.setClickSuggestCommand(getPrefixClickActionContent());
                break;
            default:
            case NONE:
                break;
        }

        EasyKyoriComponent suffix = new EasyKyoriComponent();

        suffix.append(getSuffix());


        if (!getSuffixTooltip().isEmpty()) {
            EasyKyoriComponent hoverComponent = new EasyKyoriComponent();

            boolean first = true;
            for (String line : getSuffixTooltip()) {
                if (first) {
                    first = false;

                    hoverComponent.append(line);
                    continue;
                }
                hoverComponent.appendWithNewLine(line);
            }

            suffix.setHoverShowText(hoverComponent.build());
        }


        switch (getSuffixClickAction()) {
            case OPEN_URL:
                suffix.setClickOpenUrl(getPrefixClickActionContent());
                break;
            case EXECUTE_COMMAND:
                suffix.setClickRunCommand(getPrefixClickActionContent());
                break;
            case SUGGEST_COMMAND:
                suffix.setClickSuggestCommand(getPrefixClickActionContent());
                break;
            default:
            case NONE:
                break;
        }

        return prefix.append(playerName).append(suffix).build();
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
