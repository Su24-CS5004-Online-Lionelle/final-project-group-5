package group5.model;

import java.util.List;
import java.util.stream.Stream;

import group5.model.beans.MBeans;

/**
 * Interface for the Model component of the application.
 */
public interface IModel {

    /**
     * String file path to default database location.
     */
    public static final String DEFAULT_DATA = "./data/source/source.json";  // Path to default movie DB here

    /**
     * Load the source data from the default location.
     */
    void loadSourceData();

    /**
     * Load a watch list from a file.
     *
     * @param filename The file to load the watch list from.
     */
    void loadWatchList(String filename);

    /**
     * Create new empty watch list.
     *
     * @param name The name of the new watch list.
     */
    void createNewWatchList(String name);

    /**
     * Get the source list of media as stream.
     *
     * @return stream of MBeans representing the source list.
     */
    Stream<MBeans> getRecords();

    /**
     * Get the watch list by id as stream.
     *
     * @param userListId The identifier of the watch list to get.
     * @return stream of MBeans representing the watch list.
     */
    Stream<MBeans> getRecords(int userListId);

    /**
     * Get the source list, filter it and return as a stream.
     *
     * @param filter The filter to apply to the source list.
     * @return stream of MBeans representing the filtered source list.
     */
    Stream<MBeans> getRecords(List<List<String>> filters);

    /**
     * Get the watch list by id, filter it and return as a stream.
     *
     * @param userListId The identifier of the watch list to get.
     * @param filters The filters to apply to the watch list.
     * @return stream of MBeans representing the filtered watch list.
     */
    Stream<MBeans> getRecords(int userListId, List<List<String>> filters);

    /**
     * Save the watch list to a file.
     *
     * @param filename string of file path to save the watch list to.
     * @param userListId The identifer of the watch list to save.
     */
    void saveWatchList(String filename, int userListId/*, UserListIdentifier userListId*/);

    /**
     * Add a media to the watch list.
     *
     * @param media The media to add to the watch list.
     * @param userListId The identifier of the watch list to add the media to.
     */
    void addToWatchList(MBeans media, int userListId);

    /**
     * Remove a media from the watch list.
     *
     * @param media The media to remove from the watch list.
     * @param userListId The identifier of the watch list to add the media to.
     */
    void removeFromWatchList(MBeans media, int userListId);

    /**
     * Update user watched status of a media.
     *
     * @param media The media to remove from the watch list.
     * @param watched Boolean to set the watched status of the media.
     */
    void updateWatched(MBeans media, boolean watched);

    /**
     * Update user rating of a media.
     *
     * @param media The media to remove from the watch list.
     * @param rating value to set the user rating of the media.
     */
    void updateUserRating(MBeans media, double rating);

    /**
     * Update local source list file to reflect the curent state in the application.
     */
    void updateSourceList();

    /**
     * Get the name of watch list.
     *
     * @return the name of watch list
     */
    String getUserListName(int userListId);

    /**
     * Get the amount of user created watch list.
     *
     * @return the amount of user created watch list
     */
    int getUserListCount();

    /**
     * Get the list of user created watch list index.
     *
     * @param record The record to get the watch list indices for.
     * @return the list of user created watch list
     */
    int[] getUserListIndicesForRecord(MBeans record);

    /**
     * Clear the current filter
     */
    void clearFilter();
}
