package us.sparknetwork.base;

public class LangConfigurations {

    public static String tl(I18n i18n, String path){
        if(i18n == null){
            throw new IllegalStateException("I18n instance is null!");
        }
        return i18n.translate(path);
    }

    public static String parseVisibility(I18n i18n, boolean bool){
        return bool ? tl(i18n,"visible") : tl(i18n,"invisible");
    }

    public static String convertBoolean(I18n i18n, boolean bool){
        return bool ? tl(i18n,"true") : tl(i18n,"false");
    }
}
