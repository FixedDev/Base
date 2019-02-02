package us.sparknetwork.base;

import us.sparknetwork.base.user.User;

public class LangConfigurations {

    public static String tl(I18n i18n, String path) {
        if (i18n == null) {
            throw new IllegalStateException("I18n instance is null!");
        }
        return i18n.translate(path);
    }

    public static String parseWhisperVisibility(I18n i18n, User.WhisperVisibility visibility) {
        switch (visibility) {
            case ALL:
                return tl(i18n, "whisper.visibility.all");
            case FRIENDS:
                return tl(i18n, "whisper.visibility.friends");
            case NONE:
                return tl(i18n, "whisper.visibility.none");
            default:
                return "";
        }
    }

    public static String parseVisibility(I18n i18n, boolean bool) {
        return bool ? tl(i18n, "visible") : tl(i18n, "invisible");
    }

    public static String convertBoolean(I18n i18n, boolean bool) {
        return bool ? tl(i18n, "true") : tl(i18n, "false");
    }
}
