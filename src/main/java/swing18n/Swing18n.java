package swing18n;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Swing18n {
    private static final ResourceBundle actualResources = ResourceBundle.getBundle(Swing18n.class.getName());
    private static final String internationalisingXX = actualResources.getString("InternationalisingXX");
    private static final String done = actualResources.getString("Done");
    private static final String theTranslationsHaveBeenCopiedToTheClipboardPleaseEmailToXX = actualResources.getString("TheTranslationsHaveBeenCopiedToTheClipboardPleaseEmailToXX");
    private static final String enterTheTranslationsForTheEnglishTextOnTheLeftInTheCorrespondingBoxesOnTheRight = actualResources.getString("EnterTheTranslationsForTheEnglishTextOnTheLeftInTheCorrespondingBoxesOnTheRight");
    private static final String leaveAnyUnknownTranslationsBlank = actualResources.getString("LeaveAnyUnknownTranslationsBlank");
    
    public final JFrame frame;

    public Swing18n(String appName, final Locale locale, final String email, Class<?>... classes) {
        frame = new JFrame(String.format(internationalisingXX, appName));
        final JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(0, 2));
        panel.add(new JLabel(enterTheTranslationsForTheEnglishTextOnTheLeftInTheCorrespondingBoxesOnTheRight, SwingConstants.CENTER));
        panel.add(new JLabel(leaveAnyUnknownTranslationsBlank, SwingConstants.CENTER));
        panel.add(new JLabel(Locale.ENGLISH.getDisplayLanguage(), SwingConstants.CENTER));
        panel.add(new JLabel(locale.getDisplayLanguage() + " (" + locale.toString() + ')', SwingConstants.CENTER));
        if (locale.equals(Locale.ENGLISH))
            throw new IllegalArgumentException("Not valid for English");

        final Map<String, JTextField> edits = new HashMap<String, JTextField>();
        final Color color1 = Color.white;
        final Color color2 = new Color(220, 220, 240);
        Color color = color1;

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
                    final JLabel englishLabel = new JLabel("       " + english.getProperty(key.toString()));
                    englishLabel.setOpaque(true);
                    englishLabel.setBackground(color);
                    panel.add(englishLabel);
                    final JTextField field = new JTextField(20);
                    field.setBackground(color);
                    color = color.equals(color1) ? color2 : color1;
                    panel.add(field);
                    edits.put(clazz.getName() + ' ' + key, field);
                }
        }
        final JButton copyToClipboard = new JButton(done);
        panel.add(copyToClipboard);
        frame.add(new JScrollPane(panel));
        copyToClipboard.addActionListener(new ActionListener() {
            @Override
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
        final Toolkit toolkit = Toolkit.getDefaultToolkit();
        final Dimension screenSize = toolkit.getScreenSize();
        final Insets screenInsets = toolkit.getScreenInsets(frame.getGraphicsConfiguration());
        if (panel.getPreferredSize().width > screenSize.getWidth() - screenInsets.left - screenInsets.right)
            panel.setPreferredSize(new Dimension((int) (screenSize.getWidth() - screenInsets.left - screenInsets.right), panel.getPreferredSize().height));
        frame.pack();
        if (frame.getHeight() > screenSize.height - screenInsets.bottom - screenInsets.top || frame.getWidth() > screenSize.width - screenInsets.left - screenInsets.right)
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    public static List<Class<?>> classesWithPropertyFiles(final String packageName) {
        final List<Class<?>> classes = new ArrayList<Class<?>>();
        final String[] classPath = System.getProperty("java.class.path").split(";");
        for (String cp : classPath) {
            final File outerFile = new File(cp);
            if (outerFile.isDirectory()) {
                new Object() {
                    void recurse(File file) {
                        for (File f : file.listFiles())
                            if (f.isDirectory())
                                recurse(f);
                            else {
                                if (f.getAbsolutePath().startsWith(outerFile.getAbsolutePath() + File.separator + packageName.replaceAll("\\.", Matcher.quoteReplacement(File.separator)))) {
                                    if (f.getName().endsWith(".properties")) {
                                        final String fullPathMinusDotClass = f.getAbsolutePath().substring(0, f.getAbsolutePath().length() - ".properties".length());
                                        final String pathMinusAbsoluteParts = fullPathMinusDotClass.substring(outerFile.getAbsolutePath().length() + 1);
                                        try {
                                            classes.add(Class.forName(pathMinusAbsoluteParts.replaceAll(Pattern.quote(File.separator), ".")));
                                        } catch (ClassNotFoundException e) {
                                        }
                                    }
                                }
                            }
                    }
                }.recurse(outerFile);
            } else {
                try {
                    final JarFile jarFile = new JarFile(outerFile);
                    final Enumeration<JarEntry> enumeration = jarFile.entries();
                    while (enumeration.hasMoreElements()) {
                        final JarEntry entry = enumeration.nextElement();
                        if (entry.getName().endsWith(".properties") && entry.getName().startsWith(packageName.replaceAll("/", ".")))
                            try {
                                classes.add(Class.forName(entry.getName().substring(0, entry.getName().length() - ".properties".length())));
                            } catch (ClassNotFoundException e) {
                                throw new RuntimeException(e);
                            }
                    }
                } catch (IOException e) {
                    System.err.println(outerFile);
                    e.printStackTrace();
                }
            }
        }
        return classes;
    }

    public static void main(String[] args) {
        final List<Class<?>> classes = classesWithPropertyFiles("swing18n");
        final Swing18n i18n = new Swing18n("Swing18n", new Locale("es", "ES", "es"), "ricky.clarkson@gmail.com", classes.toArray(new Class<?>[classes.size()]));
        i18n.frame.setVisible(true);
        i18n.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
