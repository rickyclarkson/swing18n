package swing18n;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.util.Locale;
import java.util.ResourceBundle;

public class Swing18n {
    ResourceBundle actualResources = ResourceBundle.getBundle(Swing18n.class.getName());
    String internationalisingXX = actualResources.getString("InternationalisingXX");

    JFrame frame;
    public Swing18n(String appName, Locale locale, Class<?>... classes) {
        frame = new JFrame(String.format(internationalisingXX, appName));
        final JTextArea area = new JTextArea(40, 80);
        if (!locale.equals(Locale.ENGLISH))
            for (Class<?> clazz: classes) {
                final ResourceBundle localised = ResourceBundle.getBundle(clazz.getName(), locale);
                final ResourceBundle english = ResourceBundle.getBundle(clazz.getName(), Locale.ENGLISH);
                for (String key: english.keySet())
                    if (localised.getString(key).equals(english.getString(key)))
                        area.setText(area.getText() + clazz + '.' + key + " has no translation into " + locale + '\n');
            }
        frame.add(new JScrollPane(area));
        frame.pack();
    }

    public static void main(String[] args) {
        final Swing18n i18n = new Swing18n("Swing18n", new Locale("es", "ES", "es"), Swing18n.class);
        i18n.frame.setVisible(true);
        i18n.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
