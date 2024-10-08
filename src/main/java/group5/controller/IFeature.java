package group5.controller;

import group5.model.beans.MBeans;

/**
 * Interface for the feature set and event handlers for the application.
 */
public interface IFeature {

    /**
     * Prompt the view to show the details of the selected entry. Designed to be
     * triggered by an action listener in the view upon table selection.
     *
     * @param record the user selected movie record.
     */
    void handleTableSelection(MBeans record);

    /**
     * Imports a user-defined watchlist from a specified file.
     *
     * @param filepath the path to the file containing the list of MBeans to be
     * added.
     */
    void importListFromFile(String filepath);

    /**
     * Exports the current list of MBeans to a specified file.
     *
     * @param filepath the path where the list of MBeans will be exported.
     */
    void exportListToFile(String filepath);

    /**
     * Removes a specific record from a user's watch list.
     *
     * @param mbean the MBean to be removed.
     * @param userListIndex the index in the user's watch list where the MBean
     * is located.
     */
    void removeFromWatchlist(MBeans mbean, int userListIndex);

    /**
     * Adds a specific record to a user's watch list.
     *
     * @param mbean the MBean to be added to the watch list.
     * @param userListIndex the index in the user's watch list where the MBean
     * should be added.
     */
    void addToWatchlist(MBeans mbean, int userListIndex);

    /**
     * Creates a new watch list with the specified name.
     *
     * @param name the name of the new watch list.
     */
    void createWatchlist(String name);

    /**
     * Deletes a watch list at the specified index.
     *
     * @param userListIndex the index of the watch list to be deleted.
     */
    void deleteWatchlist(int userListIndex);

    /**
     * Changes the rating of a specific MBean.
     *
     * @param mbean the MBean whose rating is to be changed.
     * @param rating the new rating to be assigned to the MBean.
     */
    void changeRating(MBeans mbean, double rating);

    /**
     * Changes the watched status of a specific MBean.
     *
     * @param record the MBean whose watched status is to be changed.
     * @param watched the new watched status to be assigned to the MBean.
     * @param caller the view component triggering this method call.
     */
    void changeWatchedStatus(MBeans record, boolean watched, String caller);

    /**
     * Handles application of filters triggered by the user.
     */
    void applyFilters();

    /**
     * Clears all applied filters and refreshes the views.
     */
    void clearFiltersAndReloadRecords();

    /**
     * Handles the change of the active tab in the view.
     *
     * @param tabIndex the index of the new active tab
     */
    void handleTabChange(int tabIndex);
}
