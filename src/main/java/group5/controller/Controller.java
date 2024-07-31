package group5.controller;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.swing.JOptionPane;

import group5.model.Filter.Operations;
import group5.model.IModel;
import group5.model.MovieData;
import group5.model.beans.MBeans;
import group5.model.formatters.Formats;
import group5.view.FilterPane;
import group5.view.IView;
import org.apache.commons.lang3.tuple.Triple;

import java.util.ArrayList;
import java.util.List;

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
     * @param view  the view object representing the user interface
     */
    public Controller(IModel model, IView view) {

        System.out.println("[Controller] Controller constructor called");
        this.model = model;
        this.view = view;

        model.loadSourceData();
        model.loadWatchList("./data/samples/watchlist.json");
        model.loadWatchList("./data/samples/abc.json");
        model.loadWatchList("./data/samples/avatar.json");
        model.loadWatchList("./data/samples/lol.json");
        model.loadWatchList("./data/samples/titanic_only.json");
        // bindFeatures accept an IFeature interface, which is the controller itself
        view.bindFeatures(this);

        // setup source table records
        view.setSourceTableRecordsV2(model.getRecords(), getWatchlistNames(), getRecordUserListMatrixV2(model.getRecords()));

        // no filters applied on initialization
        view.getFilterPane().setMovies(model.getRecords());

        // load user-defined list into model and view
        for (int i = 0; i < model.getUserListCount(); i++) {
            view.addUserTable(model.getUserListName(i));
            view.setUserTableRecords(model.getRecords(i), i);
        }
    }


    @Override
    public void deleteWatchlist(int userListIndex) {
        System.out.println("[Controller] Request to delete watchlist " + userListIndex);
        if (userListIndex < 0 || userListIndex >= model.getUserListCount()) {
            System.out.println("[Controller] Error deleting watchlist: index out of bounds");
        } else {
            // model.deleteWatchList(userListIndex);
        }
    }

    @Override
    public void createWatchlist(String name) {
        String existingLists[] = this.getWatchlistNames();
        if (Arrays.stream(existingLists).anyMatch(list -> list.equals(name))) {
            System.out.println("[Controller] Error creating new watchlist: \"" + name + "\" already exists");
            return;
        }
        model.createNewWatchList(name);
        int newListIdx = model.getUserListCount() - 1; // the new list would be the last one
        view.addUserTable(name);
        view.setUserTableRecords(model.getRecords(newListIdx), newListIdx);
    }

    @Override
    public void exportListToFile(String filepath) {
        String currentTab = "\"SourceTable\"";
        if (view.getCurrentTab() > 0) {
            currentTab = "\"UserTable " + (view.getCurrentTab() - 1 + "\"");
        }
        JOptionPane.showMessageDialog(null, "Exporting data from " + currentTab + " to " + filepath);
        if (view.getCurrentTab() == 0) {
            JOptionPane.showMessageDialog(null, "I currently do not have the ability to export from the main list.");
        } else if (view.getCurrentTab() > 0) {
            model.saveWatchList(filepath, view.getCurrentTab() - 1);
        }
    }

    @Override
    public void importListFromFile(String filepath) {
        // TODO Auto-generated method stub
        System.out.println("[Controller] importListFromFile called");
        model.loadWatchList(filepath);
        view.addUserTable(model.getUserListName(model.getUserListCount() - 1));
        view.setUserTableRecords(model.getRecords(model.getUserListCount() - 1), model.getUserListCount() - 1);

        // throw new UnsupportedOperationException("[Controller.java] Unimplemented method 'addListFromFile'");
    }

    @Override
    public void showRecordDetails(MBeans record) {
        System.out.println("[Controller] showRecordDetails called");
        view.setDetailsPaneEntry(record);
    }


    @Override
    public void applyFilters() {
        List<List<String>> filters = getFilterOptions();
        int currTabIdx = view.getCurrentTab();
        List<MBeans> recordList;
        if (currTabIdx == 0) {
            recordList = model.getRecords(filters).collect(Collectors.toList());
            view.setSourceTableRecordsV2(recordList.stream(), getWatchlistNames(), getRecordUserListMatrixV2(recordList.stream()));
        } else {
            recordList = model.getRecords(currTabIdx - 1, filters).collect(Collectors.toList());
            view.setUserTableRecords(recordList.stream(), currTabIdx - 1);
        }
        // note that we do not update the filter range here
    }

    /**
     * Clear the filters in the FilterPane.
     */
    @Override
    public void clearFilters() {
        model.clearFilter();
        view.getFilterPane().resetFilterOptions();
        view.getFilterPane().clearFilterOptions();
        int currTabIdx = view.getCurrentTab();
        List<MBeans> recordList;
        if (currTabIdx == 0) {
            recordList = model.getRecords().collect(Collectors.toList());
            view.setSourceTableRecordsV2(recordList.stream(), getWatchlistNames(), getRecordUserListMatrixV2(recordList.stream()));
        } else {
            recordList = model.getRecords(currTabIdx - 1).collect(Collectors.toList());
            view.setUserTableRecords(recordList.stream(), currTabIdx - 1);
        }
        view.getFilterPane().setMovies(recordList.stream());
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
     * @param record        the MBean to be removed.
     * @param userListIndex the index in the user's watch list where the MBean is located.
     */
    public void removeFromWatchList(MBeans record, int userListIndex) {
        System.out.println("[Controller] removeFromWatchList called to remove " + record.getTitle() + " from user list index " + userListIndex);

        model.removeFromWatchList(record, userListIndex);
        view.setUserTableRecords(model.getRecords(userListIndex), userListIndex);


        view.setSourceTableRecordsV2(model.getRecords(), getWatchlistNames(), getRecordUserListMatrixV2(model.getRecords()));
        // Update the filter pane if the current tab is the affected user list
        if (view.getCurrentTab() - 1 == userListIndex) {
            view.getFilterPane().setMovies(model.getRecords(userListIndex));
        }
    }

    public void addToWatchlist(MBeans record, int userListIndex) {
        System.out.println("[Controller] addToWatchList called to add " + record.getTitle() + " to user list index " + userListIndex);
        model.addToWatchList(record, userListIndex);
        view.setSourceTableRecordsV2(model.getRecords(), getWatchlistNames(), getRecordUserListMatrixV2(model.getRecords()));
        view.setUserTableRecords(model.getRecords(userListIndex), userListIndex);
        // Since adding to a list is done from the source tab only, there is no need to update the filter pane
    }


    /**
     * A convenient method to get the records for the current view.
     * Specify whether to apply filters currently set.
     *
     * @return a stream of MBeans
     */
    private Stream<MBeans> getRecordsForCurrentView() {
        int currentTab = view.getCurrentTab();
        if (currentTab == 0) {
            return model.getRecords();
        } else {
            return model.getRecords(currentTab - 1);
        }
    }

    public void changeRating(MBeans record, double rating) {
        model.updateUserRating(record, rating);
        // TODO: Check if Views are correctly updated
    }

    public void changeWatchedStatus(MBeans record, boolean watched) {
        model.updateWatched(record, watched);
        // Update the table if the record is in the current table
        if (getRecordsForCurrentView().anyMatch(r -> r == record)) {
            if (view.getCurrentTab() == 0) {
                view.setSourceTableRecordsV2(getRecordsForCurrentView(),
                        getWatchlistNames(),
                        getRecordUserListMatrixV2(getRecordsForCurrentView()));
            } else {
                view.setUserTableRecords(getRecordsForCurrentView(), view.getCurrentTab() - 1);
            }
        }
        // Update the details pane if the record is currently displayed
        if (view.getDetailsPane().getCurrentMedia() == record) {
            view.setDetailsPaneEntry(record);
        }
    }


    public void handleTabChange(int tabIndex) {
        System.out.println("[Controller] Handling event: tab changed to " + tabIndex + " and updating filter pane range");
        clearFilters();
//        List<MBeans> recordList;
//
//        view.getFilterPane().resetFilterOptions();
//        view.getFilterPane().clearFilterOptions();
//        int currentTab = view.getCurrentTab();
//        if (currentTab == 0) {
//            recordList = model.getRecords().toList();
//            view.setSourceTableRecordsV2(recordList.stream(), getUserListNames(), getRecordUserListMatrix());
//        } else {
//            recordList = model.getRecords(currentTab - 1).toList(); // decrement by 1 to get the user-defined list index
//            view.setUserTableRecords(recordList.stream(), currentTab - 1);
//        }
//        // no filters applied on tab change because the filter pane is reset
//        view.getFilterPane().setMovies(recordList.stream());
    }

    /**
     * Private helper method to get a boolean matrix representing the user lists for each record.
     *
     * @return a 2D boolean array where each row represents a record and each column represents a user list
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
     * Method to get the names of the user lists.
     * Created to assist in constructing the watchlist dropbox menu.
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
     * Note that the current filter fields do not mean that the filters are committed by the user.
     *
     * @return a 3-column table of strings representing the filter options
     */
    private List<List<String>> getFilterOptions() {
        FilterPane filterPane = view.getFilterPane();

        List<List<String>> filters = new ArrayList<>();
        List<Triple<String, Operations, MovieData>> triples = new ArrayList<>();
        // Title
        triples.add(Triple.of(filterPane.getFilteredTitle(), Operations.CONTAINS, MovieData.TITLE));
        // Genre
        triples.add(Triple.of(filterPane.getFilteredGenre(), Operations.CONTAINS, MovieData.GENRE));
        // MPA Rating
        triples.add(Triple.of(filterPane.getFilteredMpaRating(), Operations.EQUALS, MovieData.MPA));
        // Released (Year)
        triples.add(Triple.of(filterPane.getFilteredReleasedMin(), Operations.GREATEROREQUAL, MovieData.RELEASED));
        triples.add(Triple.of(filterPane.getFilteredReleasedMax(), Operations.LESSOREQUAL, MovieData.RELEASED));
        // IMDB Rating
        triples.add(Triple.of(filterPane.getFilteredImdbRatingMin(), Operations.GREATEROREQUAL, MovieData.IMDB));
        triples.add(Triple.of(filterPane.getFilteredImdbRatingMax(), Operations.LESSOREQUAL, MovieData.IMDB));
        // FIXME: Box Office Earnings is Missing from MovieData
        // triples.add(Triple.of(filterPane.getFilteredBoxOfficeEarningsMin(), Operations.GREATEROREQUAL, MovieData.BOXOFFICE));
        // triples.add(Triple.of(filterPane.getFilteredBoxOfficeEarningsMax(), Operations.LESSOREQUAL, MovieData.BOXOFFICE));
        // Director
        triples.add(Triple.of(filterPane.getFilteredDirectorFilter(), Operations.CONTAINS, MovieData.DIRECTOR));
        // Actor
        triples.add(Triple.of(filterPane.getFilteredActorFilter(), Operations.CONTAINS, MovieData.ACTOR));
        // FIXME: Writer is Missing from MovieData
        // triples.add(Triple.of(filterPane.getFilteredWriterFilter(), Operations.CONTAINS, MovieData.WRITER));
        // Language
        triples.add(Triple.of(filterPane.getFilteredLanguageFilter(), Operations.CONTAINS, MovieData.LANGUAGE));


        for (Triple<String, Operations, MovieData> triple : triples) {
            if (!triple.getLeft().isEmpty()) {
                List<String> filter = new ArrayList<>();
                filter.add(triple.getRight().name());
                filter.add(triple.getMiddle().getOperator());
                filter.add(triple.getLeft());
                filters.add(filter);
            }
        }
        return filters;
    }


}
