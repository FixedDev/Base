package us.sparknetwork.utils;

import net.kyori.text.Component;
import net.kyori.text.TextComponent;
import net.kyori.text.event.ClickEvent;
import net.kyori.text.event.HoverEvent;
import net.kyori.text.serializer.gson.GsonComponentSerializer;
import net.kyori.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.logging.Level;

public class EasyKyoriComponent {

    private TextComponent.Builder builder;

    public EasyKyoriComponent() {
        builder = TextComponent.builder();
    }

    private EasyKyoriComponent(@NotNull TextComponent.Builder builder) {
        this.builder = builder;
    }

    @NotNull
    public EasyKyoriComponent appendWithNewLine(@NotNull String content){
        return appendWithNewLine(LegacyComponentSerializer.INSTANCE.deserialize(content));
    }

    @NotNull
    public EasyKyoriComponent append(@NotNull String content) {
        return append(LegacyComponentSerializer.INSTANCE.deserialize(content));
    }

    @NotNull
    public EasyKyoriComponent appendWithNewLine(@NotNull Component component){
        return addNewLine().append(component);
    }

    @NotNull
    public EasyKyoriComponent append(@NotNull Component component) {
        builder = builder.append(component);

        return this;
    }

    @NotNull
    public EasyKyoriComponent addNewLine(){
        builder = builder.append(Component.newline());

        return this;
    }

    @NotNull
    public EasyKyoriComponent append(@NotNull EasyKyoriComponent kyoriText){
        return new EasyKyoriComponent(builder.append(kyoriText.builder.build()));
    }

    @NotNull
    public EasyKyoriComponent setHoverShowText(@NotNull String content) {
        return setHoverShowText(LegacyComponentSerializer.INSTANCE.deserialize(content));
    }

    @NotNull
    public EasyKyoriComponent setHoverShowText(@NotNull Component component) {
        return setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, component));
    }

    @NotNull
    public EasyKyoriComponent setHoverShowItem(@NotNull ItemStack item) {
        return setHoverShowItem(GsonComponentSerializer.INSTANCE.deserialize(convertItemStackToJson(item)));
    }

    @NotNull
    public EasyKyoriComponent setHoverShowItem(@NotNull Component component) {
        return setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, component));
    }

    @NotNull
    public EasyKyoriComponent setHoverEvent(@NotNull HoverEvent event) {
        builder = builder.hoverEvent(event);

        return this;
    }

    @NotNull
    public EasyKyoriComponent setClickRunCommand(@NotNull String command) {
        return setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
    }

    @NotNull
    public EasyKyoriComponent setClickSuggestCommand(@NotNull String command){
        return setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command));
    }

    @NotNull
    public EasyKyoriComponent setClickOpenUrl(@NotNull String url){
        return setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
    }

    @NotNull
    public EasyKyoriComponent setClickEvent(@NotNull ClickEvent event) {
        builder = builder.clickEvent(event);

        return this;
    }


    public TextComponent build() {
        return builder.build();
    }

    /**
     * Created by sainttx
     * Taken from here: https://www.spigotmc.org/threads/tut-item-tooltips-with-the-chatcomponent-api.65964/
     *
     * @param itemStack
     * @return
     */
    private String convertItemStackToJson(ItemStack itemStack) {
        // ItemStack methods to get a net.minecraft.server.ItemStack object for serialization
        Class<?> craftItemStackClazz = ReflectionUtil.getOBCClass("inventory.CraftItemStack");
        Method asNMSCopyMethod = ReflectionUtil.getMethod(craftItemStackClazz, "asNMSCopy", ItemStack.class);

        // NMS Method to serialize a net.minecraft.server.ItemStack to a valid Json string
        Class<?> nmsItemStackClazz = ReflectionUtil.getNMSClass("ItemStack");
        Class<?> nbtTagCompoundClazz = ReflectionUtil.getNMSClass("NBTTagCompound");
        Method saveNmsItemStackMethod = ReflectionUtil.getMethod(nmsItemStackClazz, "save", nbtTagCompoundClazz);

        Object nmsNbtTagCompoundObj; // This will just be an empty NBTTagCompound instance to invoke the saveNms method
        Object nmsItemStackObj; // This is the net.minecraft.server.ItemStack object received from the asNMSCopy method
        Object itemAsJsonObject; // This is the net.minecraft.server.ItemStack after being put through saveNmsItem method

        try {
            nmsNbtTagCompoundObj = nbtTagCompoundClazz.newInstance();
            nmsItemStackObj = asNMSCopyMethod.invoke(null, itemStack);
            itemAsJsonObject = saveNmsItemStackMethod.invoke(nmsItemStackObj, nmsNbtTagCompoundObj);
        } catch (Throwable t) {
            Bukkit.getLogger().log(Level.SEVERE, "failed to serialize itemstack to nms item", t);
            return null;
        }

        // Return a string representation of the serialized object
        return itemAsJsonObject.toString();
    }
}
