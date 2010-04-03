package swing18n;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class Swing18n {
    private static final ResourceBundle actualResources = ResourceBundle.getBundle(Swing18n.class.getName());
    private static final String internationalisingXX = actualResources.getString("InternationalisingXX");
    private static final String copyToClipboardText = actualResources.getString("CopyToClipboard");
    private static final String theTranslationsHaveBeenCopiedToTheClipboardPleaseEmailToXX = actualResources.getString("TheTranslationsHaveBeenCopiedToTheClipboardPleaseEmailToXX");
    public final JFrame frame;

    public Swing18n(String appName, final Locale locale, final String email, Class<?>... classes) {
        frame = new JFrame(String.format(internationalisingXX, appName));
        final JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(0, 2));
        panel.add(new JLabel(Locale.ENGLISH.getDisplayLanguage()));
        panel.add(new JLabel(locale.getDisplayLanguage()));
        if (locale.equals(Locale.ENGLISH))
            throw new IllegalArgumentException("Not valid for English");

        final Map<String, JTextField> edits = new HashMap<String, JTextField>();
        for (Class<?> clazz: classes) {
            final Properties english = new Properties();
            final Properties foreign = new Properties();

            try {
                english.load(clazz.getResource(clazz.getSimpleName() + ".properties").openStream());
                final String foreignName = clazz.getSimpleName() + '_' + locale.getLanguage() + ".properties";
                final URL foreignResource = clazz.getResource(foreignName);
                if (foreignResource != null)
                    foreign.load(foreignResource.openStream());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            for (Object key: english.keySet())
                if (!foreign.containsKey(key)) {
                    panel.add(new JLabel(english.getProperty(key.toString())));
                    final JTextField field = new JTextField(20);
                    panel.add(field);
                    edits.put(clazz + " " + key, field);
                }
        }
        frame.add(new JScrollPane(panel));
        final JButton copyToClipboard = new JButton(copyToClipboardText);
        copyToClipboard.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                final StringBuilder builder = new StringBuilder();
                builder.append(locale.getDisplayLanguage()).append(" (").append(locale.getLanguage()).append(")\n");
                for (Entry<String, JTextField> entry: edits.entrySet()) {
                    final String value = entry.getValue().getText();
                    if (!value.isEmpty())
                        builder.append(entry.getKey()).append(' ').append(value).append('\n');
                }
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(builder.toString()), null);
                JOptionPane.showMessageDialog(frame, String.format(theTranslationsHaveBeenCopiedToTheClipboardPleaseEmailToXX, email));
            }
        });
        frame.add(copyToClipboard, BorderLayout.SOUTH);
        frame.pack();
    }

    public static void main(String[] args) throws IOException {
        String[] classPath = System.getProperty("java.class.path").split(";");
        for (String cp: classPath) {
            File file = new File(cp);
            if (file.isDirectory()) {
                new Object() {
                    void recurse(File file) {
                        for (File f: file.listFiles())
                            if (f.isDirectory())
                                recurse(f);
                            else
                                System.out.println(f);
                    }
                }.recurse(file);
            }
            else {
                JarFile jarFile = new JarFile(file);
                Enumeration<JarEntry> enumeration = jarFile.entries();
                while (enumeration.hasMoreElements()) {
                    JarEntry entry = enumeration.nextElement();
                    System.out.println(entry.getName());
                }
            }
        }
        
        final Swing18n i18n = new Swing18n("Swing18n", new Locale("es", "ES", "es"), "ricky.clarkson@gmail.com", Swing18n.class);
        i18n.frame.setVisible(true);
        i18n.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
