package group5.view;

import group5.controller.IFeature;
import group5.model.beans.MBeans;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;

/** Contains and passes filters to controller for selected movie list */
public class FilterPane extends JPanel implements ActionListener, FocusListener {

    /** List of movies. */
    private List<MBeans> movies = new ArrayList<>();

    // Panels
    /** Panel containing filters. */
    private JPanel filterPanel = new JPanel(new GridBagLayout());
    /** Panel containing buttons. */
    private JPanel buttonPanel  = new JPanel(new GridLayout(0, 2));

    // Filters
    /** Title filter. */
    private JTextField titleFilter = new JTextField();
    /** Genre filter. */
    private JComboBox<String> genreFilter = new JComboBox();
    /** MPA Rating filter. */
    private JComboBox<String> mpaRatingFilter = new JComboBox();
    /** Director filter. */
    private JTextField directorFilter = new JTextField();
    /** Actor filter. */
    private JTextField actorFilter = new JTextField();
    /** Writer filter. */
    private JTextField writerFilter = new JTextField();
    /** Language filter. */
    private JComboBox<String> languageFilter = new JComboBox();

    // Range filters
    /** Year released min of range. */
    private JTextField releasedFrom = new JTextField();
    /** Year released max of range. */
    private JTextField releasedTo = new JTextField();
    /** IMDB Rating min of range. */
    private JTextField imdbRatingFrom = new JTextField();
    /** IMDB Rating max of range. */
    private JTextField imdbRatingTo = new JTextField();
    /** Box Office Earnings min of range. */
    private JTextField boxOfficeEarningsFrom = new JTextField();
    /** Box Office Earnings max of range. */
    private JTextField boxOfficeEarningsTo = new JTextField();

    /** Set containing all text filters. */
    private Set<JTextField> textFilters = new HashSet<JTextField>();
    /** Set containing all dropdown filters. */
    private Set<JComboBox<String>> dropdownFilters = new HashSet<JComboBox<String>>();
    /** Set containing all range filters. */
    private Set<JTextField> rangeFilters = new HashSet<JTextField>();

    // Range filters min/max placeholders
    /** Min and max of year released data in movies. */
    private String[] releasedRange = new String[] {"", ""};
    /** Min and max of IMDB rating data in movies. */
    private String[] imdbRatingRange = new String[] {"", ""};
    /** Min and max of Box office earnings data in movies. */
    private String[] boxOfficeRange = new String[] {"", ""};

    // Buttons
    /** Apply filters button */
    private JButton applyFilterButton = new JButton("Apply Filters");
    /** Clear filters button */
    private JButton clearFilterButton = new JButton("Clear Filters");

    // GridBagConstraints & grid row int
    /** Class global GridBagConstraints. */
    private GridBagConstraints gbc = new GridBagConstraints();
    /** Filter row to place filters descending using gbc.gridy */
    private int filterRow = 0;

    /** Public constructor. */
    public FilterPane() {
        super(new BorderLayout());

        // name components to enable switch statements
        setComponentNames();

        // configure gbc
        gbc.insets = new Insets(5, 4, 5, 4);
        updateGBC(null, null, null, null, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL);

        // add border padding to button panel
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0,0,5,0));
        // add panels
        add(filterPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.SOUTH);

        // add filters
        addFilter(FilterLabels.TITLE.getFilterLabel(), titleFilter);
        addFilter(FilterLabels.GENRE.getFilterLabel(), genreFilter);
        addFilter(FilterLabels.MPA_RATING.getFilterLabel(), mpaRatingFilter);
        // add range filters
        addRangeFilter(FilterLabels.RELEASED.getFilterLabel(), releasedFrom, releasedTo, releasedRange[0],
                releasedRange[1]);
        addRangeFilter(FilterLabels.IMDB_RATING.getFilterLabel(), imdbRatingFrom, imdbRatingTo, imdbRatingRange[0],
                imdbRatingRange[1]);
        addRangeFilter(FilterLabels.BOX_OFFICE_EARNINGS.getFilterLabel(), boxOfficeEarningsFrom, boxOfficeEarningsTo,
                boxOfficeRange[0], boxOfficeRange[1]);
        // add remaining filters
        addFilter(FilterLabels.DIRECTOR.getFilterLabel(), directorFilter);
        addFilter(FilterLabels.ACTOR.getFilterLabel(), actorFilter);
        addFilter(FilterLabels.WRITER.getFilterLabel(), writerFilter);
        addFilter(FilterLabels.LANGUAGE.getFilterLabel(), languageFilter);

        // add buttons
        buttonPanel.add(applyFilterButton);
        buttonPanel.add(clearFilterButton);

        // add action listeners
        titleFilter.addActionListener(this);
        directorFilter.addActionListener(this);
        actorFilter.addActionListener(this);
        writerFilter.addActionListener(this);
        applyFilterButton.addActionListener(this);
        clearFilterButton.addActionListener(this);
    }

    /* Getters -------------------------------------------------------------------------------------------------------*/
    /**
     * Returns user entry in title filter.
     *
     * @return movie title entered by user
     */
    public String getFilteredTitle() {
        return titleFilter.getText();
    }

    /**
     * Returns user selection in genre filter.
     *
     * @return genre selected by user
     */
    public String getFilteredGenre() {
        try {
            return genreFilter.getSelectedItem().toString();
        } catch (NullPointerException e) {
            return "";
        }
    }

    /**
     * Returns user selection in MPA rating filter.
     *
     * @return MPA rating selected by user
     */
    public String getFilteredMpaRating() {
        try {
            return mpaRatingFilter.getSelectedItem().toString();
        } catch (NullPointerException e) {
            return "";
        }
    }

    /**
     * Returns the lower bound user entry in date released filter.
     *
     * @return lower bound release of date range entered by user
     */
    public String getFilteredReleasedMin() {
        return releasedFrom.getText();
    }

    /**
     * Returns the upper bound user entry in date released filter.
     *
     * @return upper bound release of date range entered by user
     */
    public String getFilteredReleasedMax() {
        return releasedTo.getText();
    }

    /**
     * Returns the lower bound user entry in the IMDB Rating filter.
     *
     * @return lower bound of the IMDB Rating range entered by user
     */
    public String getFilteredImdbRatingMin() {
        return imdbRatingFrom.getText();
    }

    /**
     * Returns the upper bound user entry in the IMDB Rating filter.
     *
     * @return upper bound of the IMDB Rating range entered by user
     */
    public String getFilteredImdbRatingMax() {
        return imdbRatingTo.getText();
    }

    /**
     * Returns the lower bound user entry in the Box Office Earnings filter.
     *
     * @return lower bound of the Box Office Earnings range entered by user
     */
    public String getFilteredBoxOfficeEarningsMin() {
        try {
            return boxOfficeEarningsFrom.getText().equalsIgnoreCase("N/A") ?
                    boxOfficeEarningsFrom.getText() : formatFromMillions(boxOfficeEarningsFrom.getText());
        } catch (NumberFormatException e) {
            return "";
        }
    }

    /**
     * Returns the upper bound user entry in the Box Office Earnings filter.
     *
     * @return upper bound of the Box Office Earnings range entered by user
     */
    public String getFilteredBoxOfficeEarningsMax() {
        try {
            return boxOfficeEarningsTo.getText().equalsIgnoreCase("N/A") ?
                    boxOfficeEarningsTo.getText() : formatFromMillions(boxOfficeEarningsTo.getText());
        } catch (NumberFormatException e) {
            return "";
        }
    }

    /**
     * Returns user entry in the director filter.
     *
     * @return director name entered by user
     */
    public String getFilteredDirectorFilter() {
        return directorFilter.getText();
    }

    /**
     * Returns user entry in the actor filter.
     *
     * @return actor name entered by user
     */
    public String getFilteredActorFilter() {
        return actorFilter.getText();

    }

    /**
     * Returns user entry in the writer filter.
     *
     * @return writer name entered by user
     */
    public String getFilteredWriterFilter() {
        return writerFilter.getText();
    }

    /**
     * Returns user selection in the language filter.
     *
     * @return language selected by user
     */
    public String getFilteredLanguageFilter() {
        try {
            return languageFilter.getSelectedItem().toString();
        } catch (NullPointerException e) {
            return "";
        }
    }

    /* FilterPane Setup Methods --------------------------------------------------------------------------------------*/

    public void setMovies(Stream<MBeans> movies) {
        setMovies(movies, false);
    }

    /**
     * Sets the movies list of this FilterPane instance based on the MBeans in an input Stream.
     * Resets filter ranges and options based on MBeans in the new movies list.
     *
     * @param movies a Stream of movies to replace the current movies list
     */
    public void setMovies(Stream<MBeans> movies, boolean clearFilters) {
        List<MBeans> moviesList = movies.toList();
        this.movies = moviesList;

        if(!moviesList.isEmpty()) {
            // reset filter ranges and clear filter options
            setRangeFilterRanges();
            resetComboBoxOptions();
            refreshPlaceholders();
        } else {
            clearFilterOptions();
        }
    }

    /**
     * Sets component names to enable conditions through switch statements.
     */
    private void setComponentNames() {
        // set filter names
        titleFilter.setName(Filters.TITLE.getFilterName());
        genreFilter.setName(Filters.GENRE.getFilterName());
        mpaRatingFilter.setName(Filters.MPA_RATING.getFilterName());

        // set range filter names
        releasedFrom.setName(Filters.RELEASED_FROM.getFilterName());
        releasedTo.setName(Filters.RELEASED_TO.getFilterName());
        imdbRatingFrom.setName(Filters.IMDB_RATING_FROM.getFilterName());
        imdbRatingTo.setName(Filters.IMDB_RATING_TO.getFilterName());
        boxOfficeEarningsFrom.setName(Filters.BOX_OFFICE_EARNINGS_FROM.getFilterName());
        boxOfficeEarningsTo.setName(Filters.BOX_OFFICE_EARNINGS_TO.getFilterName());

        // set remaining filter names
        directorFilter.setName(Filters.DIRECTOR.getFilterName());
        actorFilter.setName(Filters.ACTOR.getFilterName());
        writerFilter.setName(Filters.WRITER.getFilterName());
        languageFilter.setName(Filters.LANGUAGE.getFilterName());
    }

    /**
     * Sets the min and max of each filter range based on MBeans in the
     * movies list.
     */
    private void setRangeFilterRanges() {
        // set filter ranges
        releasedRange = getIntFilterRange(MBeans::getYear);
        imdbRatingRange = getDoubleFilterRange(MBeans::getImdbRating);
        boxOfficeRange = getIntFilterRange(MBeans::getBoxOffice);

        // format box office range as US currency
        for (int i = 0; i < boxOfficeRange.length; i++) {
            boxOfficeRange[i] = formatAsCurrency(boxOfficeRange[i]);
        }
    }

    /**
     * Updates FilterPane's GridBagContraints to account for the placement of filters and labels.
     *
     * @param x gbc.gridx
     * @param y gbc.gridy
     * @param width gbc.gridwidth
     * @param weightx gbc.weightx
     * @param anchor gbc.anchor
     * @param fill gbc.fill
     */
    private void updateGBC(Integer x, Integer y, Integer width, Integer weightx,
                           Integer anchor, Integer fill) {
        // change gbc only if param is not null
        gbc.gridx = (x != null) ? x : gbc.gridx;
        gbc.gridy = (y != null) ? y : gbc.gridy;
        gbc.gridwidth = (width != null) ? width : gbc.gridwidth;
        gbc.weightx = (weightx != null) ? weightx : gbc.weightx;
        gbc.anchor = (anchor != null) ? anchor : gbc.anchor;
        gbc.fill = (fill != null) ? fill : gbc.fill;
    }

    /**
     * Adds a JLabel to the FilterPane.
     *
     * @param filterTitle the test displayed by the label
     */
    private void addLabel(String filterTitle) {
        // update gbc
        updateGBC(0, filterRow, 4, 1, GridBagConstraints.WEST, null);

        // add label
        filterPanel.add(new JLabel(filterTitle), gbc);

        // increment filter row
        filterRow++;
    }

    /**
     * Adds filters (JTextFields and JComboBoxes) to the FilterPane.
     *
     * @param filterTitle the text displayed above the filter
     * @param filter the filter object
     */
    private void addFilter(String filterTitle, Object filter) {
        // add filter label
        addLabel(filterTitle);

        // configure combo boxes
        if (filter instanceof JComboBox) {
            configureComboBox((JComboBox<String>) filter);
        } else if (filter instanceof JTextField){
            textFilters.add((JTextField) filter);
        }

        // update gbc
        updateGBC(0, filterRow, 4, 1, null, GridBagConstraints.HORIZONTAL);

        // add component
        filterPanel.add((Component) filter, gbc);

        // increment filter row
        filterRow++;
    }

    /**
     * Adds a filter to the FilterPane where users enter the min and max of a range of values.
     *
     * @param filterTitle the String of text displayed above the filter
     * @param filterFrom a JTextField where users enter the minimum of the range
     * @param filterTo a JTextField where users enter the maximum of the range
     * @param rangeMin a String representing the minimum value of the filter range
     * @param rangeMax a String representing the maximum value of the filter range
     */
    private void addRangeFilter(String filterTitle, JTextField filterFrom,
                                JTextField filterTo, String rangeMin, String rangeMax) {
        // add title to panel
        addLabel(filterTitle);

        // create "From" and "To" labels with italic font
        JLabel fromLabel = new JLabel(FilterLabels.FROM.getFilterLabel());
        italicizeFont(fromLabel);
        JLabel toLabel = new JLabel(FilterLabels.TO.getFilterLabel());
        italicizeFont(toLabel);

        // update gbc and add "From" label
        updateGBC(0, filterRow, 1, 0, GridBagConstraints.WEST, GridBagConstraints.NONE);
        filterPanel.add(fromLabel, gbc);

        // update gbc and add "From" text field
        updateGBC(1, null, null, 1, null, GridBagConstraints.HORIZONTAL);
        // set font italic
        italicizeFont(filterFrom);
        // add min as placeholder
        setPlaceholder(filterFrom, rangeMin);
        // add focus listener and add filter to panel
        filterFrom.setColumns(6);
        filterFrom.addFocusListener(this);
        filterPanel.add(filterFrom, gbc);
        // add filter to set of range filters
        rangeFilters.add(filterFrom);

        // update gbc and add "To" label
        updateGBC(2, null, null, 0, null, GridBagConstraints.NONE);
        filterPanel.add(toLabel, gbc);

        // update gbc and add "To" text field
        updateGBC(3, null, null, 1, null, GridBagConstraints.HORIZONTAL);
        // set font italic
        italicizeFont(filterTo);
        // add max as placeholder
        setPlaceholder(filterTo, rangeMax);
        // add focus listener and add filter to panel
        filterTo.addFocusListener(this);
        filterTo.setColumns(6);
        filterPanel.add(filterTo, gbc);
        // add filter to set of range filters
        rangeFilters.add(filterTo);

        // increment filter row
        filterRow++;
    }

    /**
     * Sets placeholder text in a JTextField to the String value passed as a param.
     *
     * @param textField the JTextField receiving the placeholder text
     * @param value a String containing the placeholder text
     */
    private void setPlaceholder(JTextField textField, String value) {
        textField.setText(value);
    }

    /* Helper Methods ------------------------------------------------------------------------------------------------*/
    /**
     * Italicizes the font in the object passed.
     *
     * @param object the object with text to italicize
     */
    private void italicizeFont(Object object) {
        // italicize JLabel text
        if (object instanceof JLabel) {
            JLabel label = (JLabel) object;
            Font font = label.getFont();
            Font italicFont = font.deriveFont(Font.ITALIC);
            label.setFont(italicFont);
        }

        // italicize JTextField text
        if (object instanceof JTextField) {
            JTextField textField = (JTextField) object;
            Font font = textField.getFont();
            Font italicFont = font.deriveFont(Font.ITALIC);
            textField.setFont(italicFont);
        }
    }

    /**
     * Find and returns the minimum and maximum values in a double returning getter from a
     * stream of the movies list.
     *
     * @param fieldFunction the double returning getter method
     * @return a list of Strings containing the min value at the first idx and the max value at the second
     */
    private String[] getDoubleFilterRange(ToDoubleFunction<MBeans> fieldFunction) {
        // find max/min
        OptionalDouble maxValue = movies.stream().mapToDouble(fieldFunction).max();
        OptionalDouble minValue = movies.stream().mapToDouble(fieldFunction).min();

        // convert to string
        String maxValueString = maxValue.isPresent() ? Double.toString(maxValue.getAsDouble()) : "No Max";
        String minValueString = minValue.isPresent() ? Double.toString(minValue.getAsDouble()) : "No Min";

        return new String[] {minValueString, maxValueString};
    }

    /**
     * Find and returns the minimum and maximum values in an int returning getter from a
     * stream of the movies list.
     *
     * @param fieldFunction the int returning getter method
     * @return a list of Strings containing the min value at the first idx and the max value at the second
     */
    private String[] getIntFilterRange(ToIntFunction<MBeans> fieldFunction) {
        // find max/min
        OptionalInt maxValue = movies.stream().mapToInt(fieldFunction).max();
        OptionalInt minValue = movies.stream().mapToInt(fieldFunction).min();

        // convert to string
        String maxValueString = maxValue.isPresent() ? Integer.toString(maxValue.getAsInt()) : "No Max";
        String minValueString = minValue.isPresent() ? Integer.toString(minValue.getAsInt()) : "No Min";

       String[] range = {minValueString, maxValueString};

       // replace -1 with N/A if box office earning data is missing
       for (int i = 0; i < range.length; i++) {
           if (Integer.parseInt(range[i]) < 0) {
               range[i] = "N/A";
           }
       }

       return range;
    }

    /**
     * Returns a string formatted as US currency (in millions of dollars) from a double parsable String.
     *
     * @param value a String representing a dollar value
     * @return a String containing the dollar value of the input param formatted as US currency in millions of dollars
     */
    private String formatAsCurrency(String value) {

        if (value.equalsIgnoreCase("N/A")) {
            return value;
        }

        // set currency formatter to US locale
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);

        // convert to string and remove trailing .00
        String currencyStringDouble = currencyFormatter.format(Double.parseDouble(value) / 1_000_000);
        String currencyInMillions = new String(currencyStringDouble + "M");

        return currencyInMillions;
    }

    /**
     * Converts and returns a String containing a dollar value in millions of dollars to a String representing that
     * same value as an unformatted number.
     *
     * @param value a String representing a dollar value in millions of dollars
     * @return a String representing the input param dollar value as an unformatted number
     */
    private String formatFromMillions(String value) {
        // strip $ and M if present
        String processedValue = value.replaceAll("[^0-9.]", "");
        double processedDouble  = Double.parseDouble(processedValue);

        // convert to double from millions and return as String
        Double expandedDouble = processedDouble * 1_000_000;
        return String.format("%.0f", expandedDouble);
    }

    /**
     * Sets the options inside a JComboBox (dropdown) filter based on unique MBean attributes.
     *
     * @param comboBox the dropdown filter in which to add options
     */
    private void configureComboBox(JComboBox<String> comboBox) {
        // initialize empty tree sets
        Set<String> uniqueGenres = new TreeSet<>();
        Set<String> uniqueMpaRatings = new TreeSet<>();
        Set<String> uniqueLanguages = new TreeSet<>();

        Filters filter = getFilterByEnum(comboBox.getName());

        switch (filter) {
            case GENRE:
                // add options to set
                for (MBeans movie : movies) {
                    for (String genre : movie.getGenre()) {
                        uniqueGenres.add(genre);
                    }
                }
                // add elements to combo box
                for (String genre : uniqueGenres) {
                    comboBox.addItem(genre);
                }
                break;
            case MPA_RATING:
                // add options to set
                for (MBeans movie : movies) {
                    uniqueMpaRatings.add(movie.getRated());
                }
                // add elements to combo box
                for (String mpaRating : uniqueMpaRatings) {
                    comboBox.addItem(mpaRating);
                }
                break;
            case LANGUAGE:
                // add options to set
                for (MBeans movie : movies) {
                    // unpack options from string list
                    for (String language : movie.getLanguage()) {
                        uniqueLanguages.add(language);
                    }
                }
                // add elements to combo box
                for (String language : uniqueLanguages) {
                    comboBox.addItem(language);
                }
                break;
            default:
                System.out.println("Invalid text field passed");
        }
        // deselect options in combobox
        comboBox.setSelectedIndex(-1);
        // add combobox to set of dropdown filters
        dropdownFilters.add(comboBox);
    }

    public void clearFilterOptions() {
        for (JComboBox filter : dropdownFilters) {
            filter.removeAllItems();
        }
        for (JTextField filter : textFilters) {
            filter.setText("");
        }
        for (JTextField filter : rangeFilters) {
            filter.setText("");
        }
    }
    /**
     * Resets all JComboBox options in the FilterPane based on the FilterPane's movies list.
     */
    public void resetComboBoxOptions() {
        for (JComboBox filter : dropdownFilters) {
            Object selectedItem = filter.getSelectedItem();
            filter.removeAllItems();
            configureComboBox(filter);
            filter.setSelectedItem(selectedItem);
        }
    }

    public void resetTextFilters() {
        for (JTextField filter : textFilters) {
            filter.setText("");
        }
    }

    public void resetRangeFilters() {
        for (JTextField filter : rangeFilters) {
            resetPlaceholder(filter);
        }
    }

    /**
     * Clears all filters in the FilterPane of user selections.
     */
    public void resetFilterOptions() {
        // reset options in dropdown filters
        resetComboBoxOptions();
        resetTextFilters();

        // reset placeholders for ranges
        setPlaceholder(releasedFrom, releasedRange[0]);
        setPlaceholder(releasedTo, releasedRange[1]);
        setPlaceholder(imdbRatingFrom, imdbRatingRange[0]);
        setPlaceholder(imdbRatingTo, imdbRatingRange[1]);
        setPlaceholder(boxOfficeEarningsFrom, boxOfficeRange[0]);
        setPlaceholder(boxOfficeEarningsTo, boxOfficeRange[1]);
    }


    public void refreshPlaceholders() {
      releasedFrom.setText(releasedRange[0]);
      releasedTo.setText(releasedRange[1]);
      imdbRatingFrom.setText(imdbRatingRange[0]);
      imdbRatingTo.setText(imdbRatingRange[1]);




      boxOfficeEarningsFrom.setText(boxOfficeRange[0]);
      boxOfficeEarningsTo.setText(boxOfficeRange[1]);
    }

    /**
     * Resets the placeholder text in a JTextField to the min and max of the initial range.
     *
     * @param textField the JTextField to reset its placeholder
     */
    public void resetPlaceholder(JTextField textField) {
        Filters filter = getFilterByEnum(textField.getName());
        switch (filter) {
            case RELEASED_FROM:
                if (releasedFrom.getText().isEmpty()) {
                    releasedFrom.setText(releasedRange[0]);
                }
                break;
            case RELEASED_TO:
                if (releasedTo.getText().isEmpty()) {
                    releasedTo.setText(releasedRange[1]);
                }
                break;
            case IMDB_RATING_FROM:
                if (imdbRatingFrom.getText().isEmpty()) {
                    imdbRatingFrom.setText(imdbRatingRange[0]);
                }
                break;
            case IMDB_RATING_TO:
                if (imdbRatingTo.getText().isEmpty()) {
                    imdbRatingTo.setText(imdbRatingRange[1]);
                }
                break;
            case BOX_OFFICE_EARNINGS_FROM:
                if (boxOfficeEarningsFrom.getText().isEmpty() || boxOfficeEarningsFrom.getText().startsWith("-")) {
                    boxOfficeEarningsFrom.setText(boxOfficeRange[0]);
                } else if (boxOfficeEarningsFrom.getText().equalsIgnoreCase("N/A")){
                    boxOfficeEarningsFrom.setText("N/A");
                } else {
                    String processedBoxOfficeEarningsMin = boxOfficeEarningsFrom.getText()
                            .replaceAll("[^0-9.]", "");
                    try {
                        double BoxOfficeEarningsMinDouble = Double.parseDouble(processedBoxOfficeEarningsMin);
                        boxOfficeEarningsFrom.setText(
                                formatAsCurrency(formatFromMillions(boxOfficeEarningsFrom.getText())));
                    } catch (NumberFormatException e) {
                        setPlaceholder(boxOfficeEarningsFrom, boxOfficeRange[0]);
                    }
                }
                break;
            case BOX_OFFICE_EARNINGS_TO:
                if (boxOfficeEarningsTo.getText().isEmpty() || boxOfficeEarningsTo.getText().startsWith("-")) {
                    boxOfficeEarningsTo.setText(boxOfficeRange[1]);
                } else if (boxOfficeEarningsTo.getText().equalsIgnoreCase("N/A")){
                    boxOfficeEarningsTo.setText("N/A");
                } else {
                    String processedBoxOfficeEarningsMax = boxOfficeEarningsTo.getText()
                            .replaceAll("[^0-9.]", "");
                    try {
                        double BoxOfficeEarningsMaxDouble = Double.parseDouble(processedBoxOfficeEarningsMax);
                        boxOfficeEarningsTo.setText(
                                formatAsCurrency(formatFromMillions(boxOfficeEarningsTo.getText())));
                    } catch (NumberFormatException e) {
                        setPlaceholder(boxOfficeEarningsTo, boxOfficeRange[1]);
                    }
                }
                break;
            default:
                System.out.println("Invalid text field passed");
        }
    }

    /**
     * Returns the Filters enum associated with a filter name passed as a String.
     *
     * @param filter the String name of a filter in the FilterPane
     * @return the Filters enum associated with the String filter
     */
    private Filters getFilterByEnum(String filter) {
        for (Filters filters : Filters.values()) {
            if (filters.getFilterName().equalsIgnoreCase(filter)) {
                return filters;
            }
        }
        String error = String.format("Unsupported filter: '%s'", filter);
        System.out.println(error);
        return null;
    }

    /* Actions -------------------------------------------------------------------------------------------------------*/
    /**
     * Invoked when an action occurs.
     *
     * @param e the event to be processed
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        // @Wing decide how you want apply/clear message sent to BaseView/controller and I can reconfigure

        if (e.getSource() == applyFilterButton) {
            System.out.println("Filters applied");

            System.out.println("Title: " + getFilteredTitle());
            System.out.println("Genre: " + getFilteredGenre());
            System.out.println("Rating: " + getFilteredMpaRating());
            System.out.println("Released From: " + getFilteredReleasedMin());
            System.out.println("Released To: " + getFilteredReleasedMax());
            System.out.println("IMDB From: " + getFilteredImdbRatingMin());
            System.out.println("IMDB To: " + getFilteredImdbRatingMax());
            System.out.println("Box Office From: " + getFilteredBoxOfficeEarningsMin());
            System.out.println("Box Office To: " + getFilteredBoxOfficeEarningsMax());
            System.out.println("Director: " + getFilteredDirectorFilter());
            System.out.println("Actor: " + getFilteredActorFilter());
            System.out.println("Writer: " + getFilteredWriterFilter());
            System.out.println("Language: " + getFilteredLanguageFilter());

        } else if (e.getSource() == clearFilterButton) {
            System.out.println("Filters cleared");
            if (!movies.isEmpty()) {
                resetFilterOptions();
            }
        }
    }

    /**
     * Invoked when a component gains the keyboard focus.
     *
     * @param e the event to be processed
     */
    @Override
    public void focusGained(FocusEvent e) {
        // clear text when focus is gained
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
        // reset placeholders when focus is lost
        JTextField textField = (JTextField) e.getSource();
        resetPlaceholder(textField);
    }

    /**
     * Triggers actions outside of the FilterPane from event actions performed by FilterPane components.
     *
     * @param features action tiggered by the FilterPane
     */
    public void bindFeatures(IFeature features) {
        applyFilterButton.addActionListener(e -> features.applyFilters());
        clearFilterButton.addActionListener(e -> features.clearFilters());
    }
}

/* enums -------------------------------------------------------------------------------------------------------------*/
/** Encapsulates the name of a FilterPane filter in an enum. */
enum Filters {

    /** Filters Enums. */
    TITLE("titleFilter"), GENRE("genreFilter"), MPA_RATING("mparatingFilter"),
    RELEASED_FROM("releasedFromFilter"), RELEASED_TO("releasedToFilter"),
    IMDB_RATING_FROM("imdbRatingFromFilter"), IMDB_RATING_TO("imdbRatingToFilter"),
    BOX_OFFICE_EARNINGS_FROM("boxOfficeEarningsFromFilter"),
    BOX_OFFICE_EARNINGS_TO("boxOfficeEarningsToFilter"),
    DIRECTOR("directorFilter"), ACTOR("actorFilter"),
    WRITER("writerFilter"), LANGUAGE("languageFilter");

    /** String representing the name of a FilterPane filter. */
    private final String filterName;

    /** Public constructor */
    Filters(String filterName) {
        this.filterName = filterName;
    }

    /**
     * Returns the String, representing a filter name, associated with a Filters enum.
     *
     * @return String, representing a filter name, associated with a Filters enum
     */
    public String getFilterName() {
        return filterName;
    }
}

/** Encapsulates the text in a JLabel, representing a FilterPane filter title, in an enum. */
enum FilterLabels {

    /** FilterLabels Enums. */
    TITLE("Title:"), GENRE("Genre:"), MPA_RATING("MPA Rating:"),
    RELEASED("Released:"), IMDB_RATING("IMDB Rating:"), BOX_OFFICE_EARNINGS("Box Office Earnings: ($ millions)"),
    DIRECTOR("Director:"), ACTOR("Actor:"), WRITER("Writer:"), LANGUAGE("Language:"), FROM("From:"), TO("To:");

    /** String representing the text in a JLabel above a FilterPane filter. */
    private final String filterLabel;

    /** Public constructor */
    FilterLabels(String filterLabel) {
        this.filterLabel = filterLabel;
    }

    /**
     * Returns the String, representing a filter title, associated with a FilterLabels enum.
     *
     * @return String, representing a filter title, associated with a FilterLabels enum
     */
    public String getFilterLabel() {
        return filterLabel;
    }
}
