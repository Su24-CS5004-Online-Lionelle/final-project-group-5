package group5.controller;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Triple;

import group5.model.filter.Operations;
import group5.model.IModel;
import group5.model.MovieData;
import group5.model.beans.MBeans;
import group5.view.FilterPane;
import group5.view.IView;

/**
 * Controller class implementation for the program.
 */
public class Controller implements IController, IFeature {
    /**
     * The model object representing the movie database.
     */
    IModel model;
    /**
     * The view object representing the user interface.
     */
    IView view;

    /**
     * Constructor for the controller.
     *
     * @param model the model object representing the movie database
     * @param view  the view object representing the user interface
     */
    public Controller(IModel model, IView view) {

        System.out.println("[Controller] Controller constructor called");
        this.model = model;
        this.view = view;

        // bindFeatures accept an IFeature interface, which is the controller itself
        view.bindFeatures(this);

        // source table is initialized in the constructor
        // load user-defined lists
        model.loadWatchList();

        // load user-defined list into model and view
        for (int i = 0; i < model.getUserListCount(); i++) {
            view.addUserTable(model.getUserListName(i));
            view.setUserTableRecords(model.getRecords(i), i);
        }

        // setup source table records - only after all the user lists are loaded
        // no filters applied on initialization
        view.setSourceTableRecords(model.getRecords(), getWatchlistNames(), getRecordUserListMatrix(model.getRecords()));
        view.getFilterPane().setMovies(model.getRecords(), true);
    }

    /**
     * {@inheritDoc}
     * <br>
     * Implemented to validate the list index and call the model for removal.
     * Switches to the source table after removal.
     */
    @Override
    public void deleteWatchlist(int userListIndex) {
        System.out.println("[Controller] Request to delete watchlist " + userListIndex);
        if (userListIndex < 0 || userListIndex >= model.getUserListCount()) {
            System.out.println("[Controller] Error deleting watchlist: index out of bounds");
        } else {
            int deletedWatchlistIdx = model.deleteWatchList(userListIndex);
            if (deletedWatchlistIdx < 0) {
                view.showAlertDialog(String.valueOf(ErrorMessage.ERROR), String.valueOf(ErrorMessage.DELETE_WATCHLIST));
                return;
            }

            // update source table because of new RecordUserListMatrix
            // (not necessary because the table will be updated on tab change in current implementation,
            // kept for safety in case of future changes)
            view.setSourceTableRecords(model.getRecords(), getWatchlistNames(), getRecordUserListMatrix(model.getRecords()));
            view.setActiveTab(0);
        }
    }

    /**
     * {@inheritDoc}
     * <br>
     * Implemented to validate the list name and call the model for creation.
     * @param name the name of the new watchlist.
     */
    @Override
    public void createWatchlist(String name) {
        String[] existingLists = this.getWatchlistNames();
        if (Arrays.asList(existingLists).contains(name)) {
            System.out.println("[Controller] Error creating new watchlist: \"" + name + "\" already exists");
            return;
        }

        int newListIdx = model.createNewWatchList(name);
        if (newListIdx < 0) {
            view.showAlertDialog(String.valueOf(ErrorMessage.ERROR), String.valueOf(ErrorMessage.CREATE_WATCHLIST));
            return;
        }
        view.addUserTable(name);
        // setUserTableRecords is not absolutely necessary because table will be updated on tab change
        // view.setUserTableRecords(model.getRecords(newListIdx), newListIdx);
    }

    /**
     * {@inheritDoc}
     * <br>
     * Implemented to handle export of user-defined watchlists only.
     * @param filepath the path to the file where the watchlist will be
     *                 exported.
     */
    @Override
    public void exportListToFile(String filepath) {
        if (view.getActiveTab() == 0) {
            System.out.println("[Controller] Not allowed to export source table");
        } else if (view.getActiveTab() > 0) {
            System.out.println("[Controller] User requested to export watchlist to " + filepath);
            model.saveWatchList(filepath, view.getActiveTab() - 1);
        }
    }

    /**
     * {@inheritDoc}
     * <br>
     * Implemented to prompt the model to load the watchlist from the specified file.
     * Creates a new tab and switches to it if the import is successful.
     * Pops up an alert dialog if the import fails.
     * @param filepath the path to the file containing the list of records to be imported.
     */
    @Override
    public void importListFromFile(String filepath) {
        System.out.println("[Controller] User requested to import watchlist from " + filepath);
        int newWatchlistIdx = model.loadWatchList(filepath);
        if (newWatchlistIdx < 0) {
            Path path = Paths.get(filepath);
            String fileName = path.getFileName().toString();
            switch (newWatchlistIdx) {
                case -1:
                    view.showAlertDialog(String.valueOf(ErrorMessage.ERROR), ErrorMessage.NAME_CLASH.getErrorMessage(fileName));
                    break;
                case -2:
                    view.showAlertDialog(String.valueOf(ErrorMessage.ERROR), ErrorMessage.IMPORT_WATCHLIST.getErrorMessage(fileName));
                    break;
                default:
                    break;
            }
            return;
        }
        view.addUserTable(model.getUserListName(newWatchlistIdx));
        // Set records for the new table (this call isn't necessary because the table will be updated on tab change)
        view.setUserTableRecords(model.getRecords(newWatchlistIdx), newWatchlistIdx);
        // Update the source table because of new RecordUserListMatrix
        view.setSourceTableRecords(model.getRecords(), getWatchlistNames(), getRecordUserListMatrix(model.getRecords()));
        view.setActiveTab(newWatchlistIdx + 1);

    }


    /**
     * {@inheritDoc}
     * <br>
     * Implemented to show the details of the selected entry in the details pane.
     * @param record the MBean to be displayed in the details pane.
     */
    @Override
    public void handleTableSelection(MBeans record) {
        System.out.println("[Controller] showRecordDetails called");
        if (record == null) {
            return;
        }
        view.setDetailsPaneEntry(record);
    }

    /**
     * {@inheritDoc}
     * <br>
     * Implemented to apply the current filters and update the tables.
     */
    @Override
    public void applyFilters() {
        view.clearTableSelection();
        List<List<String>> filters = getFilterOptions();
        int currTabIdx = view.getActiveTab();
        List<MBeans> recordList;
        if (currTabIdx == 0) {
            // Source table: fetch API + apply filters + update filter ranges
            try {
                model.addNewMBeans(filters, null);
            } catch (Exception e) {
                System.out.println("[Controller] Failed to fetch new records: Operating in offline mode");
            }
            recordList = model.getRecords(filters).collect(Collectors.toList());
            view.setSourceTableRecords(recordList.stream(), getWatchlistNames(), getRecordUserListMatrix(recordList.stream()));

            // this filter range has to be set without any filters
            view.getFilterPane().setMovies(model.getAllRecords(), true);

        } else {
            // User table: apply filters only
            recordList = model.getRecords(currTabIdx - 1, filters).collect(Collectors.toList());
            view.setUserTableRecords(recordList.stream(), currTabIdx - 1);
        }
    }

    /**
     * {@inheritDoc}
     * <br>
     * Implemented to clear the filters and reload the records.
     */
    @Override
    public void clearFiltersAndReloadRecords() {
        model.clearFilter();
        view.getFilterPane().resetFilterOptions();
        view.getFilterPane().clearFilterOptions();
        int currTabIdx = view.getActiveTab();
        List<MBeans> recordList = getRecordsForActiveTab().toList();
        if (currTabIdx == 0) {
            view.setSourceTableRecords(recordList.stream(), getWatchlistNames(), getRecordUserListMatrix(recordList.stream()));
            view.getFilterPane().setMovies(recordList.stream(), true);
        } else {
            view.setUserTableRecords(recordList.stream(), currTabIdx - 1);
            view.getFilterPane().setMovies(recordList.stream());
        }
    }

    /**
     * {@inheritDoc}
     * Implemented to call the view to display the user interface.
     */
    @Override
    public void go() {
        System.out.println("[Controller] Controller.go() called");
        view.display();
    }

    /**
     * {@inheritDoc}
     * <br>
     * @param record the MBean to be removed from the watchlist.
     * @param userListIndex the index in the user's watchlist where the MBean
     *                      should be removed.
     */
    public void removeFromWatchlist(MBeans record, int userListIndex) {
        System.out.println("[Controller] removeFromWatchList called to remove " + record.getTitle() + " from user list index " + userListIndex);
        if (view.getActiveTab() > 0) {
            // only clears selection if the current tab is the affected watchlist
            view.clearTableSelection();
        }
        model.removeFromWatchList(record, userListIndex);

        // Update the filter pane if the current tab is the affected user list
        if (view.getActiveTab() > 0) {
            if (model.getRecords(userListIndex).count() == 0) { // if the resultant list is empty...
                // ...then clear filters in the filter pane and model
                model.clearFilter();
                view.getFilterPane().resetFilterOptions();
                view.getFilterPane().clearFilterOptions();
            }
            // this filter range has to be set without any filters
            view.getFilterPane().setMovies(model.getAllRecords(userListIndex), false);
        }

        view.setSourceTableRecords(model.getRecords(), getWatchlistNames(), getRecordUserListMatrix(model.getRecords()));
        view.setUserTableRecords(model.getRecords(userListIndex), userListIndex);
    }

    /**
     * Add a record to the user's watch list.
     *
     * @param record        the MBean to be added to the watch list.
     * @param userListIndex the index in the user's watch list where the MBean
     *                      should be added.
     */
    public void addToWatchlist(MBeans record, int userListIndex) {
        System.out.println("[Controller] addToWatchList called to add " + record.getTitle() + " to user list index " + userListIndex);
        model.addToWatchList(record, userListIndex);
        view.setSourceTableRecords(model.getRecords(), getWatchlistNames(), getRecordUserListMatrix(model.getRecords()));
        view.setUserTableRecords(model.getRecords(userListIndex), userListIndex); // This is not absolutely necessary
        // Since adding to a list is done from the source tab only, there's no need to update the filter pane
    }

    /**
     * A private method to get the records for the active tab for convenience.
     *
     * @return a stream of MBeans
     */
    private Stream<MBeans> getRecordsForActiveTab() {
        int currentTab = view.getActiveTab();
        if (currentTab == 0) {
            return model.getRecords();
        } else {
            return model.getRecords(currentTab - 1);
        }
    }

    /**
     * Changes the rating of a specific MBean. No view updates are triggered
     * since the rating is not displayed in the table.
     *
     * @param record the MBean whose rating is to be changed.
     * @param rating the new rating to be assigned to the MBean.
     */
    public void changeRating(MBeans record, double rating) {
        System.out.println("[Controller] Changing rating for " + record.getTitle() + " to " + rating);
        model.updateUserRating(record, rating);
    }

    public void changeWatchedStatus(MBeans record, boolean watched, String caller) {
        model.updateWatched(record, watched);
        if (caller.equalsIgnoreCase("detailsPane")) {   // If caller is detailsPane, update the listPane
            System.out.println("[Controller] Changed Watched Status: Updating listPane from detailsPane");
            if (view.getActiveTab() == 0) {
                // If the current tab is the source table, update the source table
                view.setSourceTableRecords(getRecordsForActiveTab(),
                        getWatchlistNames(),
                        getRecordUserListMatrix(getRecordsForActiveTab()));
            } else if (getRecordsForActiveTab().anyMatch(r -> r == record)) {
                // If the record is in the active user list, update the user list
                view.setUserTableRecords(getRecordsForActiveTab(), view.getActiveTab() - 1);
            }
        } else if (caller.equalsIgnoreCase("listPane")) {   // If the caller is listPane, update the detailsPane
            System.out.println("[Controller] Changed Watched Status: Updating detailsPane from listPane");
            // Update the details pane if the record is currently displayed
            if (view.getDetailsPane().getCurrentMedia() == record) {
                view.setDetailsPaneEntry(record);
            }
        }
    }

    public void handleTabChange(int tabIndex) {
        System.out.println("[Controller] Handling event: tab changed to " + tabIndex + " and updating filter pane range");
        view.clearTableSelection(); // this is to prevent inactive tabs from having selections
        clearFiltersAndReloadRecords();
    }

    /**
     * Private helper method to retrieve a boolean matrix representing the user
     * lists for each record.
     *
     * @return a 2D boolean array where each row represents a record and each
     * column represents a user list
     */
    private boolean[][] getRecordUserListMatrix(Stream<MBeans> records) {
        return records
                .map(record -> {
                    int[] indices = model.getUserListIndicesForRecord(record);
                    boolean[] result = new boolean[model.getUserListCount()];
                    for (int i = 0; i < model.getUserListCount(); i++) {
                        result[i] = Arrays.binarySearch(indices, i) >= 0;
                    }
                    return result;
                })
                .toArray(boolean[][]::new);
    }

    /**
     * Private method to get the names of the user lists. Created to assist in
     * constructing the watchlist dropbox menu.
     *
     * @return an array of strings representing the names of the user lists
     */
    private String[] getWatchlistNames() {
        return IntStream.range(0, model.getUserListCount())
                .mapToObj(model::getUserListName)
                .toArray(String[]::new);
    }

    /**
     * Get the current filter options from the FilterPane.
     * <p>
     * Note that the current filter fields is only an indicator of the current values in the filter fields.
     * The latest user-committed filter options are kept in the model.
     *
     * @return a 3-column table of strings representing the filter options
     */
    private List<List<String>> getFilterOptions() {
        FilterPane filterPane = view.getFilterPane();

        List<List<String>> filters = new ArrayList<>();
        List<Triple<String, Operations, MovieData>> triples = Arrays.asList(
                Triple.of(filterPane.getFilteredTitle(), Operations.CONTAINS, MovieData.TITLE),
                Triple.of(filterPane.getFilteredGenre(), Operations.CONTAINS, MovieData.GENRE),
                Triple.of(filterPane.getFilteredMpaRating(), Operations.EQUALS, MovieData.MPA),
                Triple.of(filterPane.getFilteredReleasedMin(), Operations.GREATEROREQUAL, MovieData.RELEASED),
                Triple.of(filterPane.getFilteredReleasedMax(), Operations.LESSOREQUAL, MovieData.RELEASED),
                Triple.of(filterPane.getFilteredImdbRatingMin(), Operations.GREATEROREQUAL, MovieData.IMDB),
                Triple.of(filterPane.getFilteredImdbRatingMax(), Operations.LESSOREQUAL, MovieData.IMDB),
                Triple.of(filterPane.getFilteredBoxOfficeEarningsMin(), Operations.GREATEROREQUAL, MovieData.BOXOFFICE),
                Triple.of(filterPane.getFilteredBoxOfficeEarningsMax(), Operations.LESSOREQUAL, MovieData.BOXOFFICE),
                Triple.of(filterPane.getFilteredDirectorFilter(), Operations.CONTAINS, MovieData.DIRECTOR),
                Triple.of(filterPane.getFilteredActorFilter(), Operations.CONTAINS, MovieData.ACTOR),
                Triple.of(filterPane.getFilteredWriterFilter(), Operations.CONTAINS, MovieData.WRITER),
                Triple.of(filterPane.getFilteredLanguageFilter(), Operations.CONTAINS, MovieData.LANGUAGE)
        );

        for (Triple<String, Operations, MovieData> triple : triples) {
            if (!triple.getLeft().isEmpty()) {
                filters.add(Arrays.asList(triple.getRight().name(), triple.getMiddle().getOperator(), triple.getLeft()));
            }
        }
        return filters;
    }

}
