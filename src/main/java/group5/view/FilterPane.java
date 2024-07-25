package group5.view;

import group5.controller.IFeature;
import group5.model.beans.MBeans;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;

import static group5.model.formatters.MBeansLoader.loadMBeansFromAPI;

/** Contains and passes filters to controller for selected movie list */
public class FilterPane extends JPanel implements ActionListener, FocusListener {

    /** List of movies. */
    private List<MBeans> movies = new ArrayList<>();

    private List<Object> filterList = new ArrayList<>();

    /** Title filter. */
    private JTextField titleFilter = new JTextField();
    /** Content Type filter. */
    private JComboBox contentTypeFilter = new JComboBox();
    /** Genre filter. */
    private JComboBox genreFilter = new JComboBox();
    /** MPA Rating filter. */
    private JComboBox mpaRatingFilter = new JComboBox();


    private JTextField releasedFrom = new JTextField();
    private JTextField releasedTo = new JTextField();
    private JTextField imdbRatingFrom = new JTextField();
    private JTextField imdbRatingTo = new JTextField();
    private JTextField boxOfficeEarningsFrom = new JTextField();
    private JTextField boxOfficeEarningsTo = new JTextField();

    /** Director filter. */
    private JTextField directorFilter = new JTextField();
    /** Actor filter. */
    private JTextField actorFilter = new JTextField();
    /** Writer filter. */
    private JTextField writerFilter = new JTextField();
    /** Language filter. */
    private JComboBox languageFilter = new JComboBox();
    /** Country of Origin filter. */
    private JComboBox countryOfOriginFilter = new JComboBox();

    /** Apply filters button */
    private JButton applyFilterButton = new JButton("Apply Filters");
    /** Clear filters button */
    private JButton clearFilterButton = new JButton("Clear Filters");
    /** Class global GridBagConstraints. */
    private GridBagConstraints gbc = new GridBagConstraints();
    /** Filter row to place filters descending using gbc.gridy */
    private int filterRow = 0;

    private JPanel filterPanel = new JPanel(new GridBagLayout());
    private JPanel buttonPanel  = new JPanel(new GridLayout(0, 2));


    private String[] releasedRange;
    private String[] imdbRatingRange;
    private String[] boxOfficeRange;


    /** Public constructor. */
    public FilterPane(List<MBeans> movies) {
        super(new BorderLayout());

        if (movies != null) {
            this.movies = movies;
        }

        setMovies();
        setRangeFilterRanges();
        setComponentNames();

        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        add(filterPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.SOUTH);

        addFilter("Title:", titleFilter);
        addFilter("Content Type:", contentTypeFilter);
        addFilter("Genre(s):", genreFilter);
        addFilter("MPA Rating:", mpaRatingFilter);


        addRangeFilter("Released:", releasedFrom, releasedTo, releasedRange[0], releasedRange[1]);
        addRangeFilter("IMDB Rating:", imdbRatingFrom, imdbRatingTo, imdbRatingRange[0], imdbRatingRange[1]);
        addRangeFilter("Box Office Earnings:", boxOfficeEarningsFrom, boxOfficeEarningsTo,
                boxOfficeRange[0], boxOfficeRange[1]);
        addFilter("Director(s):", directorFilter);
        addFilter("Actor(s)", actorFilter);
        addFilter("Writer(s)", writerFilter);
        addFilter("Language(s):", languageFilter);
        addFilter("Country of Origin:", countryOfOriginFilter);

        titleFilter.addActionListener(this);
        directorFilter.addActionListener(this);
        actorFilter.addActionListener(this);
        writerFilter.addActionListener(this);

        buttonPanel.add(applyFilterButton);
        buttonPanel.add(clearFilterButton);

        applyFilterButton.addActionListener(this);
        clearFilterButton.addActionListener(this);
    }

    public String getFilteredTitle() {
        return titleFilter.getText();
    }

    public String getFilteredContentType() {
        return contentTypeFilter.getSelectedItem().toString();
    }

    public String getFilteredGenre() {
        return genreFilter.getSelectedItem().toString();
    }

    public String getFilteredMpaRating() {
        return mpaRatingFilter.getSelectedItem().toString();
    }

    public String getFilteredReleasedMin() {
        return releasedFrom.getText();
    }

    public String getFilteredReleasedMax() {
        return releasedTo.getText();
    }

    public String getFilteredImdbRatingMin() {
        return imdbRatingFrom.getText();
    }

    public String getFilteredImdbRatingMax() {
        return imdbRatingTo.getText();
    }

    public String getFilteredBoxOfficeEarningsMin() {
        return boxOfficeEarningsFrom.getText();
    }

    public String getFilteredBoxOfficeEarningsMax() {
        return boxOfficeEarningsTo.getText();
    }

    public String getFilteredDirectorFilter() {
        return directorFilter.getText();
    }

    public String getFilteredActorFilter() {
        return actorFilter.getText();
    }

    public String getFilteredWriterFilter() {
        return writerFilter.getText();
    }

    public String getFilteredLanguageFilter() {
        return languageFilter.getSelectedItem().toString();
    }

    public String getFilteredCountryOfOriginFilter() {
        return countryOfOriginFilter.getSelectedItem().toString();
    }

    /**
     * Enables conditions through switch statements instead of if-else blocks.
     */
    private void setComponentNames() {
        titleFilter.setName("titleFilter");
        contentTypeFilter.setName("contentTypeFilter");
        genreFilter.setName("genreFilter");
        mpaRatingFilter.setName("mpaRatingFilter");

        releasedFrom.setName("releasedFrom");
        releasedTo.setName("releasedTo");
        imdbRatingFrom.setName("imdbRatingFrom");
        imdbRatingTo.setName("imdbRatingTo");
        boxOfficeEarningsFrom.setName("boxOfficeEarningsFrom");
        boxOfficeEarningsTo.setName("boxOfficeEarningsTo");

        directorFilter.setName("directorFilter");
        actorFilter.setName("actorFilter");
        writerFilter.setName("writerFilter");
        languageFilter.setName("languageFilter");
        countryOfOriginFilter.setName("countryOfOriginFilter");

    }

    private void setRangeFilterRanges() {
        releasedRange = getIntFilterRange(MBeans::getYear);
        imdbRatingRange = getDoubleFilterRange(MBeans::getImdbRating);
        boxOfficeRange = getStringFilterRange(MBeans::getBoxOffice);

        for (int i = 0; i < boxOfficeRange.length; i++) {
            boxOfficeRange[i] = formatAsCurrency(boxOfficeRange[i]);
        }
    }

    private void addLabel(String filterTitle) {
        // GridBagConstraints for the label (title)
        gbc.gridx = 0;
        gbc.gridy = filterRow;
        gbc.gridwidth = 4; // Span across all columns
        gbc.weightx = 1; // Label can grow horizontally
        gbc.anchor = GridBagConstraints.WEST;
        filterPanel.add(new JLabel(filterTitle), gbc);
        filterRow++;
    }

    private void addFilter(String filterTitle, Object filter) {
        // add filter label
        addLabel(filterTitle);

        // configure combo boxes
        if (filter instanceof JComboBox) {
            configureComboBox((JComboBox<String>) filter);
        }

        // update gbc
        gbc.gridx = 0;
        gbc.gridwidth = 4;
        gbc.gridy = filterRow;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // add component
        filterPanel.add((Component) filter, gbc);

        // increment filter row
        filterRow++;
    }

    private void italicizeFont(Object object) {
        if (object instanceof JLabel) {
            JLabel label = (JLabel) object;
            Font font = label.getFont();
            Font italicFont = font.deriveFont(Font.ITALIC);
            label.setFont(italicFont);
        } else if (object instanceof JTextField) {
            JTextField textField = (JTextField) object;
            Font font = textField.getFont();
            Font italicFont = font.deriveFont(Font.ITALIC);
            textField.setFont(italicFont);
        }
    }

    private String[] getDoubleFilterRange(ToDoubleFunction<MBeans> fieldFunction) {

        OptionalDouble maxValue = movies.stream().mapToDouble(fieldFunction).max();
        OptionalDouble minValue = movies.stream().mapToDouble(fieldFunction).min();

        String maxValueString = maxValue.isPresent() ? Double.toString(maxValue.getAsDouble()) : "No Max";
        String minValueString = minValue.isPresent() ? Double.toString(minValue.getAsDouble()) : "No Min";

        return new String[] {minValueString, maxValueString};
    }

    private String[] getIntFilterRange(ToIntFunction<MBeans> fieldFunction) {

        OptionalInt maxValue = movies.stream().mapToInt(fieldFunction).max();
        OptionalInt minValue = movies.stream().mapToInt(fieldFunction).min();

        String maxValueString = maxValue.isPresent() ? Integer.toString(maxValue.getAsInt()) : "No Max";
        String minValueString = minValue.isPresent() ? Integer.toString(minValue.getAsInt()) : "No Min";

        return new String[] {minValueString, maxValueString};
    }

    private String[] getStringFilterRange(Function<MBeans, String> fieldFunction) {

        OptionalDouble maxValue = movies.stream()
                .map(fieldFunction)
                .mapToDouble(this::customParseDouble)
                .filter(v -> v!= Double.MIN_VALUE)
                .max();

        OptionalDouble minValue = movies.stream()
                .map(fieldFunction)
                .mapToDouble(this::customParseDouble)
                .filter(v -> v!= Double.MIN_VALUE)
                .min();

        String maxValueString = maxValue.isPresent() ? Double.toString(maxValue.getAsDouble()) : "No Max";
        String minValueString = minValue.isPresent() ? Double.toString(minValue.getAsDouble()) : "No Min";

        return new String[] {minValueString, maxValueString};
    }

    private double customParseDouble(String value) {
        try {
            String processedValue = value.replaceAll("\\D", "");
            return Double.parseDouble(processedValue);
        } catch (NumberFormatException e) {
            return Double.MIN_VALUE;
        }
    }

    private String formatAsCurrency(String value) {
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);

        String currencyStringDouble = currencyFormatter.format(Double.parseDouble(value));
        String currencyStringInt = currencyStringDouble.substring(0, currencyStringDouble.length() - 3);

        return currencyStringInt;
    }


    private void addRangeFilter(String filterTitle, JTextField filterFrom,
                                JTextField filterTo, String rangeMin, String rangeMax) {
        addLabel(filterTitle);
        JLabel fromLabel = new JLabel("From:");
        italicizeFont(fromLabel);
        JLabel toLabel = new JLabel("To:");
        italicizeFont(toLabel);

        // "From" label gbc
        gbc.gridx = 0;
        gbc.gridy = filterRow;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        filterPanel.add(fromLabel, gbc);

        // "From" text field gbc
        gbc.gridx = 1;
        gbc.weightx = 1; // Extra space for text field
        gbc.fill = GridBagConstraints.HORIZONTAL;
        italicizeFont(filterFrom);
        setPlaceholder(filterFrom, rangeMin);
        filterFrom.addFocusListener(this);
        filterPanel.add(filterFrom, gbc);

        // "To" label gbc
        gbc.gridx = 2;
        gbc.weightx = 0; // No extra space for label
        gbc.fill = GridBagConstraints.NONE; // Reset fill
        filterPanel.add(toLabel, gbc);

        // "To" text field gbc
        gbc.gridx = 3;
        gbc.weightx = 1; // Extra space for text field
        gbc.fill = GridBagConstraints.HORIZONTAL;
        italicizeFont(filterTo);
        setPlaceholder(filterTo, rangeMax);
        filterTo.addFocusListener(this);
        filterPanel.add(filterTo, gbc);

        // increment filter row
        filterRow++;
    }

    private void configureComboBox(JComboBox<String> comboBox) {

        if (comboBox == contentTypeFilter) {
            // unique filter options, tree set preserves insertion order
            Set<String> uniqueContentType = new TreeSet<>();

            for (MBeans movie : movies) {
                uniqueContentType.add(movie.getType());
            }
            for (String contentType : uniqueContentType) {
                comboBox.addItem(contentType);
            }
        } else if (comboBox == genreFilter) {
            // unique filter options, tree set preserves insertion order
            Set<String> uniqueGenres = new TreeSet<>();

            for (MBeans movie : movies) {
                uniqueGenres.add(movie.getGenre());
            }
            for (String genre : uniqueGenres) {
                comboBox.addItem(genre);
            }
        } else if (comboBox == mpaRatingFilter) {
            // unique filter options, tree set preserves insertion order
            Set<String> uniqueMpaRatings = new TreeSet<>();

            for (MBeans movie : movies) {
                uniqueMpaRatings.add(movie.getRated());
            }
            for (String mpaRating : uniqueMpaRatings) {
                comboBox.addItem(mpaRating);
            }
        } else if (comboBox == languageFilter) {
            // unique filter options, tree set preserves insertion order
            Set<String> uniqueLanguages = new TreeSet<>();

            for (MBeans movie : movies) {
                for (String language : movie.getLanguage()) {
                    uniqueLanguages.add(language);
                }
            }
            for (String language : uniqueLanguages) {
                comboBox.addItem(language);
            }
        } else if (comboBox == countryOfOriginFilter) {
            // unique filter options, tree set preserves insertion order
            Set<String> uniqueCountries = new TreeSet<>();

            for (MBeans movie : movies) {
                for (String country : movie.getCountry()) {
                    uniqueCountries.add(country);
                }
            }
            for (String country : uniqueCountries) {
                comboBox.addItem(country);
            }
        }

        // deselect options in combobox
        comboBox.setSelectedIndex(-1);
    }

    private void setMovies() {
        MBeans theMatrix;
        MBeans titanic;

        theMatrix = loadMBeansFromAPI("The Matrix", "1999", "movie");
        titanic = loadMBeansFromAPI("Titanic", "1997", "movie");
        movies.add(theMatrix);
        movies.add(titanic);
    }

    /**
     * Invoked when an action occurs.
     *
     * @param e the event to be processed
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == applyFilterButton) {
            System.out.println("Filters applied");
        } else if (e.getSource() == clearFilterButton) {
            System.out.println("Filters cleared");
            titleFilter.setText("");
            contentTypeFilter.setSelectedIndex(-1);
            genreFilter.setSelectedIndex(-1);
            mpaRatingFilter.setSelectedIndex(-1);
            setPlaceholder(releasedFrom, releasedRange[0]);
            setPlaceholder(releasedTo, releasedRange[1]);
            setPlaceholder(imdbRatingFrom, imdbRatingRange[0]);
            setPlaceholder(imdbRatingTo, imdbRatingRange[1]);
            setPlaceholder(boxOfficeEarningsFrom, boxOfficeRange[0]);
            setPlaceholder(boxOfficeEarningsTo, boxOfficeRange[1]);
            directorFilter.setText("");
            actorFilter.setText("");
            writerFilter.setText("");
            languageFilter.setSelectedIndex(-1);
            countryOfOriginFilter.setSelectedIndex(-1);
        }
    }

    private void setPlaceholder(JTextField textField, String value) {
        textField.setText(value);
    }

    /**
     * Invoked when a component gains the keyboard focus.
     *
     * @param e the event to be processed
     */
    @Override
    public void focusGained(FocusEvent e) {
        JTextField textField = (JTextField) e.getSource();
        textField.setText("");
    }

    /**
     * Invoked when a component loses the keyboard focus.
     *
     * @param e the event to be processed
     */
    @Override
    public void focusLost(FocusEvent e) {
        JTextField textField = (JTextField) e.getSource();
        resetPlaceholder(textField);
    }

    private boolean isValidInput(JTextField textField) {
        switch (textField.getName()) {
            case "titleFilter":
                String filterInput = textField.getText();
                break;
        }
        return true;
    }

    public void resetPlaceholder(JTextField textField) {

        switch (textField.getName()) {
            case "releasedFrom":
                if (releasedFrom.getText().isEmpty()) {
                    releasedFrom.setText(releasedRange[0]);
                }
                break;
            case "releasedTo":
                if (releasedTo.getText().isEmpty()) {
                    releasedTo.setText(releasedRange[1]);
                }
                break;
            case "imdbRatingFrom":
                if (imdbRatingFrom.getText().isEmpty()) {
                    imdbRatingFrom.setText(imdbRatingRange[0]);
                }
                break;
            case "imdbRatingTo":
                if (imdbRatingTo.getText().isEmpty()) {
                    imdbRatingTo.setText(imdbRatingRange[1]);
                }
                break;
            case "boxOfficeEarningsFrom":
                if (boxOfficeEarningsFrom.getText().isEmpty()) {
                    boxOfficeEarningsFrom.setText(boxOfficeRange[0]);
                } else {
                    boxOfficeEarningsFrom.setText(formatAsCurrency(boxOfficeEarningsFrom.getText()));
                }
                break;
            case "boxOfficeEarningsTo":
                if (boxOfficeEarningsTo.getText().isEmpty()) {
                    boxOfficeEarningsTo.setText(boxOfficeRange[1]);
                } else {
                    boxOfficeEarningsTo.setText(formatAsCurrency(boxOfficeEarningsTo.getText()));
                }
                break;
            default:
                System.out.println("Invalid text field passed");
        }
    }

    public void bindFeatures(IFeature features) {

    }

    public static void main(String[] args) {

        /* Keeping this here so you can see how I was viewing the filter pane while building. */

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setMinimumSize(new Dimension(340, 120));

        FilterPane filterPane = new FilterPane(null);

        JScrollPane scrollPane = new JScrollPane(filterPane);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        frame.getContentPane().add(scrollPane);

        frame.setSize(800, 800);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        //

    }
}
