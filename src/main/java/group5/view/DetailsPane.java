package group5.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import group5.controller.IFeature;
import group5.model.beans.MBeans;
import group5.model.formatters.MBeansLoader;

/**
 * A JPanel class to display details of a media.
 */
public class DetailsPane extends JPanel {

    /**
     * Default width.
     */
    private static final int DEFAULT_WIDTH = 300;

    /**
     * Default height.
     */
    private static final int DEFAULT_HEIGHT = 600;

    /**
     * Default color, light gray.
     */
    private static final Color DEFAULT_COLOR = new Color(230, 230, 230);

    /**
     * Holds scroll pane.
     */
    private JScrollPane scrollPane;

    /**
     * Holds details panel inside scroll pane.
     */
    private JPanel detailsPanel;

    /**
     * Holds TextPane object for media Title.
     */
    private JTextPane mediaTitle;

    /**
     * Holds media image Label obejct.
     */
    private JLabel mediaImage;

    /**
     * Holds lists of JTextArea for each media details.
     */
    private List<JTextArea> mediaDetails = new ArrayList<>();

    /**
     * Holds check box to display watched status.
     */
    private JCheckBox watchedBox;

    /**
     * Holds JTextField for user rating.
     */
    private JTextField userRating;

    /**
     * Holds the current media object being displayed.
     */
    private MBeans currentMedia = null;

    /**
     * Constructor to create a DetailsPane object.
     *
     * Setup all the components contained inside this panel.
     */
    DetailsPane() {
        super();
        this.setLayout(new BorderLayout());
        this.setMinimumSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
        this.setPreferredSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
        this.detailsPanel = new JPanel();
        this.detailsPanel.setLayout(new BoxLayout(this.detailsPanel, BoxLayout.Y_AXIS));
        this.detailsPanel.setBackground(Color.WHITE);
        this.scrollPane = new JScrollPane(this.detailsPanel);
        this.scrollPane.setBorder(BorderFactory.createEmptyBorder(1, 0, 1, 0));
        this.add(this.scrollPane, BorderLayout.CENTER);
        this.initContent();
    }

    /**
     * Initialize and setup all the components template contained inside this
     * panel.
     */
    private void initContent() {
        this.addTitlePane();
        this.addImageLabel();
        this.addWatched();
        this.addDetailPane("Released");
        this.addDetailPane("Type");
        this.addDetailPane("MPA Rating");
        this.addDetailPane("Genre");
        this.addDetailPane("Runtime");
        this.addDetailPane("Director");
        this.addDetailPane("Actors");
        this.addDetailPane("Writer");
        this.addDetailPane("Plot");
        this.addDetailPane("Language");
        this.addDetailPane("Country");
        this.addDetailPane("Awards");
        this.addDetailPane("Metascore");
        this.addDetailPane("IMDB");
        this.addDetailPane("Box Office");
        this.addUserRating();

        this.scrollPane.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                // Dynamically resize media title and media details.
                reSize();
            }
        });
    }

    /**
     * Add vertical padding to the details panel.
     *
     * @param padding the padding size to add.
     */
    private void addVerticalPadding(int padding) {
        this.detailsPanel.add(Box.createRigidArea(new Dimension(0, padding)));
    }

    /**
     * Add title pane to the details panel.
     */
    private void addTitlePane() {
        this.mediaTitle = new JTextPane();
        this.mediaTitle.setFont(new Font("SansSerif", Font.BOLD, 30));
        System.out.println(this.mediaTitle.getFont().getFontName());
        this.mediaTitle.setBackground(DEFAULT_COLOR);
        this.mediaTitle.setEditable(false);

        // Set word wrap for title pane.
        StyledDocument doc = this.mediaTitle.getStyledDocument();
        SimpleAttributeSet center = new SimpleAttributeSet();
        StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
        doc.setParagraphAttributes(0, doc.getLength(), center, false);

        this.mediaTitle.setMinimumSize(new Dimension(DEFAULT_WIDTH, 30));
        this.detailsPanel.add(this.mediaTitle);
        this.addVerticalPadding(5);
    }

    /**
     * Add image label to the details panel.
     */
    private void addImageLabel() {
        this.mediaImage = new JLabel();
        this.mediaImage.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.mediaImage.setPreferredSize(new Dimension(200, 250));  // Scaling.
        this.mediaImage.setIcon(new ImageIcon(getClass().getClassLoader().getResource("no-image.jpg")));
        this.detailsPanel.add(this.mediaImage);
        this.addVerticalPadding(5);
    }

    /**
     * Add watched check box to the details panel.
     */
    private void addWatched() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        this.watchedBox = new JCheckBox("Watched");
        this.watchedBox.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.watchedBox.setBackground(DEFAULT_COLOR);
        this.watchedBox.setFocusPainted(false);  // Remove border around text.
        this.detailsPanel.add(this.watchedBox);
        this.addVerticalPadding(5);
        this.watchedBox.setEnabled(false); // Not editable until records is shown
    }

    /**
     * Add detail pane to the details panel.
     *
     * @param name the name of the detail.
     */
    private void addDetailPane(String name) {
        // JPanel to encased both elements.
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));

        // JPanel for detail title.
        JLabel label = new JLabel(name + ": ");
        label.setFont(new Font("SansSerif", Font.PLAIN, 12));
        label.setPreferredSize(new Dimension(75, 10));
        label.setVerticalAlignment(JLabel.TOP);
        label.setBorder(BorderFactory.createEmptyBorder(2, 4, 0, 0));

        // JTextArea for actual media details.
        JTextArea text = new JTextArea();
        text.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 6));
        text.setFont(new Font("SansSerif", Font.PLAIN, 12));
        text.setBackground(new Color(230, 230, 230));
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        text.setEditable(false);
        text.setMinimumSize(new Dimension(150, 20));
        this.mediaDetails.add(text);

        panel.add(label, BorderLayout.WEST);
        panel.add(text, BorderLayout.CENTER);
        this.detailsPanel.add(panel);
        this.addVerticalPadding(5);
    }

    /**
     * Add user rating to the details panel.
     */
    private void addUserRating() {
        // JPanel to encased both elements.
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));

        // JPanel for detail title.
        JLabel label = new JLabel("My Rating: ");
        label.setFont(new Font("SansSerif", Font.PLAIN, 12));
        label.setPreferredSize(new Dimension(75, 10));
        label.setVerticalAlignment(JLabel.TOP);
        label.setBorder(BorderFactory.createEmptyBorder(2, 4, 0, 0));

        // JTextField for actual media details.
        this.userRating = new JTextField();
        this.userRating.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 6));
        this.userRating.setFont(new Font("SansSerif", Font.PLAIN, 12));
        this.userRating.setBackground(new Color(230, 230, 230));
        this.userRating.setEditable(true);
        this.userRating.setMinimumSize(new Dimension(150, 20));
        this.userRating.setEnabled(false); // Not editable until records is shown

        panel.add(label, BorderLayout.WEST);
        panel.add(this.userRating, BorderLayout.CENTER);
        this.detailsPanel.add(panel);
        this.addVerticalPadding(5);
    }

    /**
     * Resize the media title and media details.
     *
     * Allows the media title and media details to be resized dynamically when
     * the window is resized.
     */
    private void reSize() {
        this.mediaTitle.setPreferredSize(null);
        this.mediaTitle.setSize(this.scrollPane.getSize());
        this.mediaTitle.setPreferredSize(new Dimension(this.scrollPane.getSize().width - 22,
                this.mediaTitle.getPreferredSize().height));
        for (JTextArea text : mediaDetails) {
            text.setSize(new Dimension(this.scrollPane.getWidth() - 114, text.getHeight()));
            this.scrollPane.revalidate();
        }
    }

    /**
     * Scale the image to fit the media image label.
     *
     * @param imgUrl the URL of the image to scale.
     * @return the scaled image.
     */
    private ImageIcon scaleImage(String imgStr) {
        try {
            URL imgUrl = new URL(imgStr);
            BufferedImage imageBig = ImageIO.read(imgUrl);
            Image image = imageBig.getScaledInstance(200, 250, Image.SCALE_SMOOTH);
            return new ImageIcon(image);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ImageIcon(getClass().getClassLoader().getResource("no-image.jpg"));
    }

    /**
     * Get the current media being displayed.
     *
     * @return the current media being displayed.
     */
    public MBeans getCurrentMedia() {
        return this.currentMedia;
    }


    public void refreshUserFields() {
        if (currentMedia != null) {
            this.userRating.setText(Double.toString(currentMedia.getMyRating()));
            this.watchedBox.setSelected(currentMedia.getWatched());
        }
    }

    /**
     * Set the media details to display.
     *
     * @param media the media to display.
     * @param userDetails whether the details are on source or watchlist tab.
     */
    public void setMedia(MBeans media) {
        this.currentMedia = media;
        this.mediaTitle.setText(media.getTitle());
        // TEMP FIX to handle the change in return type from getPoster()
        try {
            this.mediaImage.setIcon(this.scaleImage(media.getPoster()));
        } catch (Exception e) {
            this.mediaImage.setIcon(new ImageIcon(getClass().getClassLoader().getResource("no-image.jpg")));
            e.printStackTrace();
        }
        this.mediaDetails.get(0).setText(media.formattedDate());
        this.mediaDetails.get(1).setText(media.getType());
        this.mediaDetails.get(2).setText(media.getRated());
        this.mediaDetails.get(3).setText(String.join("\n", media.getGenre()));

        int runtime = media.getRuntime();
        if (runtime == -1) {
            this.mediaDetails.get(4).setText("N/A");
        } else {
            this.mediaDetails.get(4).setText(Integer.toString(runtime) + " minutes");
        }

        this.mediaDetails.get(5).setText(String.join("\n", media.getDirector()));
        this.mediaDetails.get(6).setText(String.join("\n", media.getActors()));
        this.mediaDetails.get(7).setText(String.join("\n", media.getWriter()));
        this.mediaDetails.get(8).setText(media.getPlot());
        this.mediaDetails.get(9).setText(String.join("\n", media.getLanguage()));
        this.mediaDetails.get(10).setText(String.join("\n", media.getCountry()));
        this.mediaDetails.get(11).setText(media.getAwards());

        int metascore = media.getMetascore();
        if (metascore == -1) {
            this.mediaDetails.get(12).setText("N/A");
        } else {
            this.mediaDetails.get(12).setText(Integer.toString(metascore));
        }

        double imdbRating = media.getImdbRating();
        if (imdbRating == -1) {
            this.mediaDetails.get(13).setText("N/A");
        } else {
            this.mediaDetails.get(13).setText(Double.toString(imdbRating));
        }

        this.mediaDetails.get(14).setText(media.formatBoxOfficeCurrency());
        this.userRating.setText(Double.toString(media.getMyRating()));
        this.watchedBox.setSelected(media.getWatched());
        SwingUtilities.invokeLater(() -> {
            this.reSize();
            this.scrollPane.getVerticalScrollBar().setValue(0); // Move to top
        });

        // Enable watched box and user rating box
        this.userRating.setEnabled(true);
        this.watchedBox.setEnabled(true);
    }

    /**
     * Add listeners to the watched check box and save rating button.
     */
    public void bindFeatures(IFeature features) {
        this.watchedBox.addActionListener(e -> {
            if (this.currentMedia != null) {
                // features.changeWatchedStatus(this.currentMedia, this.watchedBox.isSelected());
                features.changeWatchedStatusV2(this.currentMedia, this.watchedBox.isSelected(), "detailsPane");
            }
        });

        // 1. Action Listener - do something when `enter` pressed
        this.userRating.addActionListener(e -> {
            try {
                double rating = Double.parseDouble(this.userRating.getText());
                features.changeRating(this.currentMedia, rating);
                this.userRating.setText(Double.toString(rating));
            } catch (NumberFormatException ex) {
                System.out.println("Invalid rating format. Please enter a number.");
            }
        });
    }

    /**
     * Main method to test the DetailsPane.
     *
     * @param args
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 600);
        DetailsPane detailsPane = new DetailsPane();
        frame.add(detailsPane);
        MBeans media = MBeansLoader.loadMBeansFromAPI("The Matrix", "", "").get(0);
        //MBeans media = MBeansLoader.loadMediasFromFile("data/test/empty.json", Formats.JSON).iterator().next();
        System.out.println(media);
        System.out.println(media.getPoster());
        System.out.println(media.getReleased());
        detailsPane.setMedia(media);
        detailsPane.bindFeatures(null);
        frame.setVisible(true);
    }
}
