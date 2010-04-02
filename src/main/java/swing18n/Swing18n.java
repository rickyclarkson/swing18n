package swing18n;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.io.IOException;
import java.util.Locale;
import java.util.Properties;
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
                final Properties english = new Properties();
                final Properties foreign = new Properties();

                try {
                    english.load(clazz.getResource(clazz.getSimpleName() + ".properties").openStream());
                    final String foreignName = clazz.getSimpleName() + '_' + locale.getLanguage() + ".properties";
                    System.err.println(foreignName);
                    foreign.load(clazz.getResource(foreignName).openStream());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                for (Object key: english.keySet())
                    if (!foreign.containsKey(key))
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
