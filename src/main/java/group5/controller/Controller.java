package group5.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Triple;

import group5.model.Filter.Operations;
import group5.model.IModel;
import group5.model.MovieData;
import group5.model.beans.MBeans;
import group5.view.FilterPane;
import group5.view.IView;

/**
 * Controller class for the program.
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
     * @param view the view object representing the user interface
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
        view.setSourceTableRecordsV2(model.getRecords(), getWatchlistNames(), getRecordUserListMatrixV2(model.getRecords()));
        view.getFilterPane().setMovies(model.getRecords(), true);
    }


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
            // view.setSourceTableRecordsV2(model.getRecords(), getWatchlistNames(), getRecordUserListMatrixV2(model.getRecords()));
            view.setActiveTab(0);
        }
    }

    /**
     * Create a new watchlist.
     *
     * @param name the name of the new watchlist.
     */
    @Override
    public void createWatchlist(String name) {
        String existingLists[] = this.getWatchlistNames();
        if (Arrays.stream(existingLists).anyMatch(list -> list.equals(name))) {
            System.out.println("[Controller] Error creating new watchlist: \"" + name + "\" already exists");
            return;
        }

        int newListIdx = model.createNewWatchList(name);
        if (newListIdx < 0) {
            view.showAlertDialog(String.valueOf(ErrorMessage.ERROR), String.valueOf(ErrorMessage.CREATE_WATCHLIST));
            return;
        }
        view.addUserTable(name);
        // Calls below are not absolutely necessary because table will be updated on tab change
        // view.setUserTableRecords(model.getRecords(newListIdx), newListIdx);
    }

    /**
     * Handles user-initiated export of a watchlist to a file.
     *
     * @param filepath the path to the file where the watchlist will be
     * exported.
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
     * Handles user-initiated import of a watchlist from a file.
     *
     * @param filepath the path to the file containing the watchlist.
     */
    @Override
    public void importListFromFile(String filepath) {
        System.out.println("[Controller] User requested to import watchlist from " + filepath);
        int newWatchlistIdx = model.loadWatchList(filepath);
        if (newWatchlistIdx < 0) {
            view.showAlertDialog(String.valueOf(ErrorMessage.ERROR), ErrorMessage.CREATE_WATCHLIST.getErrorMessage(filepath));
            return;
        }
        view.addUserTable(model.getUserListName(newWatchlistIdx));
        // Set records for the new table (this call isn't necessary because the table will be updated on tab change)
        view.setUserTableRecords(model.getRecords(newWatchlistIdx), newWatchlistIdx);
        // Update the source table because of new RecordUserListMatrix
        view.setSourceTableRecordsV2(model.getRecords(), getWatchlistNames(), getRecordUserListMatrixV2(model.getRecords()));
        view.setActiveTab(newWatchlistIdx + 1);

    }

    @Override
    public void handleTableSelection(MBeans record) {
        System.out.println("[Controller] showRecordDetails called");
        if (record == null) {
            return;
        }
        view.setDetailsPaneEntry(record);
    }

    /**
     * Handles user-initiated filtering of the records.
     */
    @Override
    public void applyFilters() {
        view.clearTableSelection();
        List<List<String>> filters = getFilterOptions();
        int currTabIdx = view.getActiveTab();
        List<MBeans> recordList;
        if (currTabIdx == 0) {
            // Source table: fetch API + apply filters + update filter ranges
            model.addNewMBeans(filters, null);
            recordList = model.getRecords(filters).collect(Collectors.toList());
            view.setSourceTableRecordsV2(recordList.stream(), getWatchlistNames(), getRecordUserListMatrixV2(recordList.stream()));

            // this filter range has to be set without any filters
            // this is a workaround to retrieve the full source list, and then restore the filters
            model.clearFilter();
            view.getFilterPane().setMovies(model.getRecords(), true);
            model.getRecords(filters);

        } else {
            // User table: apply filters only
            recordList = model.getRecords(currTabIdx - 1, filters).collect(Collectors.toList());
            view.setUserTableRecords(recordList.stream(), currTabIdx - 1);
        }
        // filter ranges are not updated, because the actual is unaltered
    }

    /**
     * Clear the filters in the FilterPane.
     */
    @Override
    public void clearFiltersAndReloadRecords() {
        model.clearFilter();
        view.getFilterPane().resetFilterOptions();
        view.getFilterPane().clearFilterOptions();
        int currTabIdx = view.getActiveTab();
        List<MBeans> recordList = getRecordsForCurrentView().toList();
        if (currTabIdx == 0) {
            view.setSourceTableRecordsV2(recordList.stream(), getWatchlistNames(), getRecordUserListMatrixV2(recordList.stream()));
            view.getFilterPane().setMovies(recordList.stream(), true);
        } else {
            view.setUserTableRecords(recordList.stream(), currTabIdx - 1);
            view.getFilterPane().setMovies(recordList.stream());
        }
    }

    /**
     * Main entry point for the controller.
     */
    @Override
    public void go() {
        System.out.println("[Controller] Controller.go() called");
        view.display();
    }

    /**
     * Remove a record from the user's watch list.
     *
     * @param record the MBean to be removed.
     * @param userListIndex the index in the user's watch list where the MBean
     * is located.
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
            view.getFilterPane().setMovies(model.getRecords(userListIndex), false);
        }

        view.setSourceTableRecordsV2(model.getRecords(), getWatchlistNames(), getRecordUserListMatrixV2(model.getRecords()));
        view.setUserTableRecords(model.getRecords(userListIndex), userListIndex);
    }

    /**
     * Add a record to the user's watch list.
     *
     * @param record the MBean to be added to the watch list.
     * @param userListIndex the index in the user's watch list where the MBean
     * should be added.
     */
    public void addToWatchlist(MBeans record, int userListIndex) {
        System.out.println("[Controller] addToWatchList called to add " + record.getTitle() + " to user list index " + userListIndex);
        model.addToWatchList(record, userListIndex);
        view.setSourceTableRecordsV2(model.getRecords(), getWatchlistNames(), getRecordUserListMatrixV2(model.getRecords()));
        view.setUserTableRecords(model.getRecords(userListIndex), userListIndex);
        // Since adding to a list is done from the source tab only, there's no need to update the filter pane
    }

    /**
     * A convenient method to get the records for the current view.
     *
     * @return a stream of MBeans
     */
    private Stream<MBeans> getRecordsForCurrentView() {
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

    public void changeWatchedStatusV2(MBeans record, boolean watched, String caller) {
        model.updateWatched(record, watched);
        if (caller.equalsIgnoreCase("detailsPane")) {   // If caller is detailsPane, update the listPane
            System.out.println("[Controller] Changed Watched Status: Updating listPane from detailsPane");
            if (view.getActiveTab() == 0) {
                // If the current tab is the source table, update the source table
                view.setSourceTableRecordsV2(getRecordsForCurrentView(),
                        getWatchlistNames(),
                        getRecordUserListMatrixV2(getRecordsForCurrentView()));
            } else if (getRecordsForCurrentView().anyMatch(r -> r == record)) {
                // If the record is in the active user list, update the user list
                view.setUserTableRecords(getRecordsForCurrentView(), view.getActiveTab() - 1);
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
    private boolean[][] getRecordUserListMatrixV2(Stream<MBeans> records) {
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
     * Method to get the names of the user lists. Created to assist in
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
     * Note that the current filter fields do not mean that the filters are
     * committed by the user, it is an indicator what is currently typed or
     * selected in the filter fields. The last committed filter options are
     * stored in the model.
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
