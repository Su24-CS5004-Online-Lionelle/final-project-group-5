package group5.view;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;

/** Helper class to set uniform font for all View components in a JFrame */
public class AppFont {

    /** Private constructor to prevent instantiation. */
    private AppFont() {

    }

    /**
     * Sets the font for all components in the View.
     *
     * @param font
     */
    public static void setAppFont(FontUIResource font) {
        java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof javax.swing.plaf.FontUIResource) {
                UIManager.put(key, font);
            }
        }
    }
}
