package group5.view;

import group5.controller.IFeature;
import group5.model.beans.MBeans;
import group5.model.formatters.Formats;
import group5.model.formatters.MBeansLoader;
import org.apache.commons.lang3.tuple.Pair;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;


/**
 * ListPaneV2 is ....... TODO: add description
 * BorderLayout.CENTER = a JTabbedPane containing the various lists
 * BorderLayout.SOUTH = a JPanel toolbar containing with buttons
 * <p>
 * TabbedPane citation: https://docs.oracle.com/javase/tutorial/uiswing/components/tabbedpane.html
 */

public class ListPaneV2 extends JPanel {

    private final String MAIN_TAB_NAME = "All Movies";
    private final String ADD_LIST_BUTTON_TEXT = "Import List";
    private final String DELETE_LIST_BUTTON_TEXT = "Delete List";
    private final String EXPORT_LIST_BUTTON_TEXT = "Export List";
    private final String MAIN_ACTION_BUTTON_TEXT = "Add/Remove";
    private final String WATCHLIST_ACTION_BUTTON_TEXT = "Remove";

    private final String NEW_LIST_POPUP_TITLE = "New Watchlist";
    private final String NEW_LIST_POPUP_PROMPT = "Enter the name of the new watchlist:";
    private final String NEW_LIST_ERROR_TITLE = "Error";
    private final String NEW_LIST_ERROR_PROMPT = "Watchlist '%s' already exists. Please choose another name.";

    JTable sourceTable;
    JButton importListButton;
    JButton deleteListButton;
    JButton exportListButton;
    JTabbedPane tabbedPane;

    MovieTableModel sourceTableModel;

    List<String> watchlistNames;
    Consumer<MBeans> tableSelectionHandler;
    BiConsumer<MBeans, Integer> removeFromListHandler;
    BiConsumer<MBeans, Integer> addToListHandler;
    BiConsumer<MBeans, Boolean> changeWatchedStatusHandler;
    Consumer<String> createListHandler;
    Consumer<Integer> deleteListHandler;
    Consumer<Integer> tabChangeHandler;
    Consumer<String> importListHandler;
    Consumer<String> exportListHandler;


    /**
     * List of movie models for user-defined lists
     * Each user-defined list will have its own model
     * Each tab will own a table with its own model
     */
    List<MovieTableModel> watchlistModels;
    List<JTable> watchlistTables;

    ListPaneV2() {
        super();

        // Set layout for the list pane
        this.setLayout(new BorderLayout());


        // Setting up the tabbed pane, and adding listener for tab change
        tabbedPane = new JTabbedPane();
        this.add(tabbedPane, BorderLayout.CENTER);

        // Create the main table
        createSourceTableTab();
        watchlistModels = new ArrayList<>();
        watchlistTables = new ArrayList<>();
        watchlistNames = new ArrayList<>();


        // Create panel for add and export buttons below the table
        JPanel bottomButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        importListButton = new JButton(ADD_LIST_BUTTON_TEXT);
        exportListButton = new JButton(EXPORT_LIST_BUTTON_TEXT);
        deleteListButton = new JButton(DELETE_LIST_BUTTON_TEXT);
        deleteListButton.setEnabled(false);
        bottomButtonPanel.add(importListButton);
        bottomButtonPanel.add(exportListButton);
        bottomButtonPanel.add(deleteListButton);

        // Add button panel to the bottom of the list panel
        this.add(bottomButtonPanel, BorderLayout.SOUTH);

    }


    public int getActiveTab() {
        return tabbedPane.getSelectedIndex();
    }


    public JTable getActiveTable() {
        int currentTab = tabbedPane.getSelectedIndex();
        if (currentTab == 0) {
            return sourceTable;
        } else {
            return watchlistTables.get(currentTab - 1);
        }
    }

    public MovieTableModel getActiveTableModel() {
        int currentTab = tabbedPane.getSelectedIndex();
        if (currentTab == 0) {
            return sourceTableModel;
        } else {
            return watchlistModels.get(currentTab - 1);
        }
    }


    private void createTableTab(String name, TableMode tableMode) {
        MovieTableModel targetModel;
        JTable targetTable;
        String tabName = tableMode == TableMode.MAIN ? MAIN_TAB_NAME : name;

        if (tableMode == TableMode.MAIN) {
            // Error checking: there should only be one main table
            if (sourceTableModel != null || sourceTable != null || tabbedPane.getTabCount() > 0) {
                throw new IllegalArgumentException("[ListPaneV2] Error: Main table is already constructed!");
            }
        }
        targetModel = new MovieTableModel(tableMode);
        targetTable = new JTable(targetModel);

        // Enable sorting and disable sorting for the action column
        if (0 == 1) {
            TableRowSorter<TableModel> sorter = new TableRowSorter<>(targetTable.getModel());
            targetTable.setRowSorter(sorter);
            sorter.setSortable(TableColumn.WATCHLIST.getIndex(), false);
        }

        // Setting the column widths
        List<Pair<TableColumn, Integer>> columnMaxWidths = List.of(
                // Pair.of(TableColumn.TITLE, 150),
                Pair.of(TableColumn.YEAR, 35),
                Pair.of(TableColumn.WATCHED, 50),
                Pair.of(TableColumn.WATCHLIST, 100),
                Pair.of(TableColumn.RUNTIME, 50)
        );
        for (Pair<TableColumn, Integer> pair : columnMaxWidths) {
            // targetTable.getColumnModel().getColumn(pair.getLeft().getIndex()).setPreferredWidth(pair.getRight());
            targetTable.getColumnModel().getColumn(pair.getLeft().getIndex()).setMaxWidth(pair.getRight());
        }


        if (tableMode == TableMode.MAIN) {
            sourceTableModel = targetModel;
            sourceTable = targetTable;
        } else {
            watchlistModels.add(targetModel);
            watchlistTables.add(targetTable);
        }

        targetTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        targetTable.getSelectionModel().addListSelectionListener(new MovieListSelectionHandler());

        targetTable.getColumn("Watchlist").setCellRenderer(new ButtonRenderer(tableMode));
        targetTable.getColumn("Watchlist").setCellEditor(new ButtonEditor(tableMode));
        JScrollPane newScrollPane = new JScrollPane(
                targetTable,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        tabbedPane.addTab(tabName, null, newScrollPane, tabName);
    }


    private void createSourceTableTab() {
        createTableTab(MAIN_TAB_NAME, TableMode.MAIN);
    }

    public void createUserTableTab(String tableName) {
        createTableTab(tableName, TableMode.WATCHLIST);
    }


    public void setUserTableRecords(Stream<MBeans> recordStream, int watchlistIndex) {
        List<MBeans> records = recordStream.toList();
                if (tabbedPane.getTabCount() - 2 < watchlistIndex) {
            throw new IllegalArgumentException("User-defined list index out of bounds");
        }
        MovieTableModel targetUserListModel = watchlistModels.get(watchlistIndex);
        List<MovieTableModelRecord> recordsWithMetadata = new ArrayList<>();
        for (MBeans record : records) {
            recordsWithMetadata.add(new MovieTableModelRecord(record));
        }
        targetUserListModel.setRecordsWithMetadata(recordsWithMetadata);
    }



    private void localImportListHandler() {
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter fileFilters = new FileNameExtensionFilter(
                "JSON or CSV (*.json;*.csv)", "json", "csv");
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setFileFilter(fileFilters);
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
        fileChooser.setDialogTitle("Import List");

        int userSelection = fileChooser.showOpenDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            importListHandler.accept(fileChooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void localExportListHandler() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
        fileChooser.setDialogTitle("Export List");
        fileChooser.setFileFilter(new FileNameExtensionFilter("XML (*.xml)", "xml"));
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("JSON (*.json)", "json"));
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("CSV (*.csv)", "csv"));
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Text (*.txt)", "txt"));
        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            exportListHandler.accept(fileChooser.getSelectedFile().getAbsolutePath() + "." + ((FileNameExtensionFilter) fileChooser.getFileFilter()).getExtensions()[0]);
        }
    }

    public void setSourceTableRecordsV2(Stream<MBeans> records, String[] watchlistNames, boolean[][] recordWatchlistMatrix) {
        System.out.println("[ListPaneV2] setMainTableRecords called");
        List<MovieTableModelRecord> recordsWithMetadata = new ArrayList<>();
        List<MBeans> recordsList = records.toList();
        for (int i = 0; i < recordsList.size(); i++) {
            recordsWithMetadata.add(new MovieTableModelRecord(recordsList.get(i), watchlistNames, recordWatchlistMatrix[i]));
        }
        sourceTableModel.setRecordsWithMetadata(recordsWithMetadata);
    }

    public void bindFeatures(IFeature features) {
        System.out.println("[ListPaneV2] bindFeatures");
        importListButton.addActionListener(e -> localImportListHandler());
        importListHandler = features::importListFromFile;
        exportListHandler = features::exportListToFile;
        exportListButton.addActionListener(e -> localExportListHandler());
        tableSelectionHandler = features::showRecordDetails;
        removeFromListHandler = features::removeFromWatchList;
        addToListHandler = features::addToWatchlist;
        changeWatchedStatusHandler = features::changeWatchedStatus;
        tabChangeHandler = features::handleTabChange;
        tabbedPane.addChangeListener(e -> localTabChangeHandler());
        createListHandler = features::createWatchlist;
        deleteListHandler = features::deleteWatchlist;
        deleteListButton.addActionListener(e -> localDeleteListHandler());
    }


    private void localTabChangeHandler() {
        deleteListButton.setEnabled(!(tabbedPane.getSelectedIndex() == 0));
        tabChangeHandler.accept(tabbedPane.getSelectedIndex());
    }

    private void localDeleteListHandler() {
        // Pop up a dialog to confirm deletion
        int currWatchlistIdx = tabbedPane.getSelectedIndex() - 1;
        String listName = watchlistNames.get(currWatchlistIdx);
        int dialogResult = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete the list \"" + listName + "\"?", "Warning", JOptionPane.YES_NO_OPTION);
        System.out.println("[ListPaneV2] Delete list dialog result: " + dialogResult);
    }


    public void setWatchlistNames(String[] watchlistNames) {
        this.watchlistNames = List.of(watchlistNames);
    }


    class MovieTableModel extends AbstractTableModel {
        private String[] columnNames = Arrays.stream(TableColumn.values())
                .map(TableColumn::getName)
                .toArray(String[]::new);

        private List<MBeans> records;
        private List<MovieTableModelRecord> movieTableModelRecords;
        private TableMode tableMode;


        MovieTableModel(TableMode tableMode) {
            this.tableMode = tableMode;
        }

        /**
         * Get the record at the specified row.
         * <br>
         * This method is for self-referencing from the table,
         * so we can do something like: table.getModel().getRecordAt(row) in an action listener
         *
         * @param row the row index
         * @return MBeans the movie record
         */
        public MBeans getRecordAt(int row) {
            return movieTableModelRecords.get(row).getRecord();
        }

        public TableMode getTableMode() {
            return tableMode;
        }

//        public void setRecords(List<MBeans> records) {
//            this.records = records;
//            fireTableDataChanged();
//        }

        public void setRecordsWithMetadata(List<MovieTableModelRecord> movieTableModelRecords) {
            this.movieTableModelRecords = movieTableModelRecords;
            fireTableDataChanged();
        }

        public int getColumnCount() {
            return columnNames.length;
        }

        public int getRowCount() {
            if (movieTableModelRecords == null) {
                return 0;
            }
            return movieTableModelRecords.size();
        }

        public String getColumnName(int col) {
            return columnNames[col];
        }

        public Object getValueAt(int row, int col) {
            MovieTableModelRecord movieTableModelRecord = movieTableModelRecords.get(row);
            MBeans record = movieTableModelRecord.getRecord();
            TableColumn column = TableColumn.values()[col];
            switch (column) {
                case TITLE:
                    return record.getTitle();
                case YEAR:
                    return ((Integer) record.getYear()).toString();
                case WATCHED:
                    return record.getWatched();
                case WATCHLIST:
                    return movieTableModelRecord;
                case GENRE:
                    return String.join(", ", record.getGenre());
                case RUNTIME:
                    int hours = record.getRuntime() / 60;
                    int minutes = record.getRuntime() % 60;
                    return String.format("%dh %dm", hours, minutes);
                default:
                    return "AN_ERROR_OCCURRED";
            }
        }

        /*
         * JTable uses this method to determine the default renderer/
         * editor for each cell.  If we didn't implement this method,
         * then the last column would contain text ("true"/"false"),
         * rather than a checkbox.
         */
        public Class getColumnClass(int col) {
            TableColumn column = TableColumn.values()[col];
            switch (column) {
                case WATCHED:
                    return Boolean.class;
                case WATCHLIST:
                    return movieTableModelRecords.getClass();
                default:
                    return String.class;
            }
        }

        /*
         * Don't need to implement this method unless your table's
         * editable.
         */
        public boolean isCellEditable(int row, int col) {
            // The data/cell address is constant, even when are rearranged onscreen.
            TableColumn column = TableColumn.values()[col];
            switch (column) {
                case WATCHED, WATCHLIST:
                    return true;
                default:
                    return false;
            }
        }

        /*
         * This handles any changes to the table's data.
         * Changes in watched status are handled here.
         */
        public void setValueAt(Object value, int row, int col) {
            // System.out.println("[ListPaneV2] setValueAt: " + value + " at row: " + row + " col: " + col);
            // data[row][col] = value;
            if (col == TableColumn.WATCHED.getIndex()) {
                MBeans record = this.getRecordAt(row);
                System.out.println("[ListPaneV2] Setting watched status of " + record.getTitle() + " to " + !record.getWatched());
                // calling the handler to update the Model
                changeWatchedStatusHandler.accept(record, !record.getWatched());
            }
            // ideally, the Model has been updated at this point
            // since the MBeans in this model points to the same MBeans in the Model
            // there is no need to re-set the records for this table model
            fireTableCellUpdated(row, col);
        }


    }


    class MovieListSelectionHandler implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent e) {
            boolean isAdjusting = e.getValueIsAdjusting();
            if (!isAdjusting) {
                ListSelectionModel lsm = (ListSelectionModel) e.getSource();
                if (!lsm.isSelectionEmpty()) {
                    // Find out which indexes are selected.
                    int tableIndex = lsm.getSelectedIndices()[0];
                    // Convert the table index to the model index, since the table is sorted
                    // Model order is unaffected by sorting in table-level sorting
                    int modelIndex = getActiveTable().convertRowIndexToModel(tableIndex);
                    tableSelectionHandler.accept(getActiveTableModel().getRecordAt(modelIndex));
                }
            }
        }
    }

    /**
     * ButtonRenderer is a custom TableCellRenderer for rendering a JButton in a JTable
     * This button is only for appearance and does not have any action listener and function
     */
    class ButtonRenderer extends JButton implements TableCellRenderer {
        private TableMode tableMode;

        public ButtonRenderer(TableMode tableMode) {
            this.tableMode = tableMode;
            setOpaque(true);
            setFocusPainted(false);
            setBorderPainted(false);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            switch (this.tableMode) {
                case MAIN:
                    setText(MAIN_ACTION_BUTTON_TEXT);
                    break;
                case WATCHLIST:
                    setText(WATCHLIST_ACTION_BUTTON_TEXT);
                    break;
                default:
                    setText("AN_ERROR_OCCURRED");
            }
            return this;
        }
    }

    class ButtonEditor extends DefaultCellEditor {

        protected JButton button;
        private String label;
        private MBeans record;
        private boolean isPushed;
        private TableMode tableMode;
        private JPopupMenu editMenu;
        private List<String> userListNames;
        private MovieTableModelRecord movieTableModelRecord;

        private static Icon tickIcon;

        static {
            // Create an ImageIcon from the PNG file
            ImageIcon imageIcon = new ImageIcon(ButtonEditor.class.getClassLoader().getResource("tick.png"));

            // Optionally, you can scale the image if needed
            Image image = imageIcon.getImage(); // Transform it
            Image scaledImage = image.getScaledInstance(10, 10, Image.SCALE_SMOOTH); // Scale it to 32x32 pixels
            tickIcon = new ImageIcon(scaledImage); // Transform it back to an ImageIcon
        }


        public ButtonEditor(TableMode tableMode) {
            // This is a workaround, since DefaultCellEditor only accepts JCheckBox, JComboBox or JTextField
            // The JCheckBox is unused and hidden
            super(new JCheckBox());

            button = new JButton();
            this.tableMode = tableMode;

            switch (this.tableMode) {
                case MAIN:
                    label = MAIN_ACTION_BUTTON_TEXT;
                    break;
                case WATCHLIST:
                    label = WATCHLIST_ACTION_BUTTON_TEXT;
                    break;
                default:
                    label = "AN_ERROR_OCCURRED";
            }
            button.setOpaque(true);

            button.addMouseListener(new MouseAdapter() {
                public void mouseReleased(MouseEvent e) {
                    switch (tableMode) {
                        case MAIN:
                            System.out.println("[ButtonEditor] Adding/removing record \"" + record.getTitle() + "\"" + " to/from watchlist");
                            JPopupMenu editMenu = new JPopupMenu("Edit");

                            String[] userListNames = movieTableModelRecord.getUserListNames();
                            boolean[] userListIndices = movieTableModelRecord.getUserListIndices();

                            for (int i = 0; i < userListNames.length; i++) {
                                JMenuItem item;
                                int idx = i;
                                if (userListIndices[i]) {
                                    item = new JMenuItem(userListNames[i], tickIcon);
                                    item.addActionListener(event -> {
                                        removeFromListHandler.accept(record, idx);
                                    });
                                } else {
                                    item = new JMenuItem(userListNames[i], null);
                                    item.addActionListener(event -> {
                                        addToListHandler.accept(record, idx);
                                    });
                                }
                                editMenu.add(item);
                            }
                            JMenuItem createNewListItem = new JMenuItem("Add To New List");
                            createNewListItem.addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    System.out.println("Create new list clicked");
                                    createNewListItem.setSelected(false);
                                    // create a pop-up dialog to get the name of the new list
                                    String newListName = JOptionPane.showInputDialog(null, NEW_LIST_POPUP_PROMPT, NEW_LIST_POPUP_TITLE, JOptionPane.QUESTION_MESSAGE);
                                    if (newListName != null) {
                                        System.out.println("New list name: " + newListName);
                                        if (newListName.length() > 0) {
                                            for (String list : userListNames) {
                                                if (list.equals(newListName)) {
                                                    JOptionPane.showMessageDialog(
                                                            null,
                                                            String.format(NEW_LIST_ERROR_PROMPT, newListName),
                                                            NEW_LIST_ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
                                                    return;
                                                }
                                            }
                                            createListHandler.accept(newListName);
                                            addToListHandler.accept(record, userListNames.length);
                                        }
                                    }
                                }

                            });
                            editMenu.addSeparator();
                            editMenu.add(createNewListItem);
                            editMenu.show(e.getComponent(), e.getX(), e.getY());
                            break;
                        case WATCHLIST:
                            int currUserTableIndex = tabbedPane.getSelectedIndex() - 1;
                            removeFromListHandler.accept(movieTableModelRecord.getRecord(), currUserTableIndex);
                            break;
                        default:
                            System.out.println("[ButtonEditor] AN_ERROR_OCCURRED");
                    }
                    fireEditingStopped();
                }
            });

//            button.addActionListener(new ActionListener() {
//                @Override
//                public void actionPerformed(ActionEvent e) {
//                    //
//                }
//            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            if (value instanceof MovieTableModelRecord) {
                movieTableModelRecord = (MovieTableModelRecord) value;
                record = movieTableModelRecord.getRecord();
            } else {
                movieTableModelRecord = null;
                System.out.println("[ButtonEditor] getTableCellEditorComponent: value is not MovieTableModelRecord");
                // TODO: throw an exception
            }
            button.setText(label);
            isPushed = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (isPushed) {
                // System.out.println("[getCellEditorValue] " + record.getTitle() + " clicked");
            }
            isPushed = false;
            return label;
        }

        @Override
        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }
    }


    /**
     * Main method to test the DetailsPane.
     *
     * @param args
     */
    public static void main(String[] args) {
//        JFrame frame = new JFrame();
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.setSize(500, 600);
//        ListPaneV2 listPaneV2 = new ListPaneV2();
//        frame.add(listPaneV2);
//
//        String sampleDataPath = "data/samples/source.json";
//        Set<MBeans> records = MBeansLoader.loadMediasFromFile(sampleDataPath, Formats.JSON);
//        listPaneV2.setSourceTableRecords(records.stream());
//
//        listPaneV2.setVisible(true);
//        frame.setVisible(true);
    }


    /**
     * Idea: What about AbstractMovieTableModel -> SourceTableModel and UserTableModel extends AbstractMovieTableModel
     */
    enum TableMode {
        MAIN,
        WATCHLIST
    }

    /**
     * Enum class for the columns in the table
     * Column order and titles are defined and can be changed here
     */
    enum TableColumn {
        TITLE("Title"),
        YEAR("Year"),
        GENRE("Genre"),
        RUNTIME("Runtime"),
        WATCHED("Watched"),
        WATCHLIST("Watchlist");

        private final String name;


        TableColumn(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public int getIndex() {
            return this.ordinal();
        }
    }

    /**
     * This contains all information needed to render a row in the table.
     * The UserList fields are required for the sourceList to construct
     * the dropdown menu for watchlist management.
     * Admittedly, this looks counter-intuitive for now.
     * Makes use of composition pattern instead of inheritance with MBeans
     * because we want to retain the original MBeans reference instead of copying and constructing new objects.
     */
    class MovieTableModelRecord {
        private MBeans record;
        private boolean[] userListIndices = null;
        private String[] userListNames = null;

        public MovieTableModelRecord(MBeans record, String[] userListNames, boolean[] userListIndices) {
            this.record = record;
            this.userListNames = userListNames;
            this.userListIndices = userListIndices;
        }

        public MovieTableModelRecord(MBeans record) {
            this.record = record;
        }

        public MBeans getRecord() {
            return record;
        }

        public boolean[] getUserListIndices() {
            return userListIndices;
        }

        public String[] getUserListNames() {
            return userListNames;
        }
    }


}


