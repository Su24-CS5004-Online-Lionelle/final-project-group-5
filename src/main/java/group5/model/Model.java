package group5.model;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.File;
import java.io.InputStream;
import java.nio.file.StandardCopyOption;

import group5.model.Filter.FilterHandler;
import group5.model.Filter.IFilterHandler;
import group5.model.beans.MBeans;
import group5.model.formatters.Formats;
import group5.model.formatters.MBeansFormatter;
import group5.model.formatters.MBeansLoader;
import group5.model.net.apiFunctionality.MovieAPIHandler;


public class Model implements IModel {

    /**
     * List of MBeans representing the source databse list.
     */
    private Set<MBeans> sourceList;

    /**
     * List of watchLists where each holds a list of reference to source list
     * MBeans.
     */
    private List<IMovieList> watchLists;

    /**
     * IFilterHandler object.
     */
    private IFilterHandler filterHandler;

    /**
     * Holds last used filter parameters.
     */
    private List<List<String>> filter;

    /**
     * Model class constructor.
     */
    public Model() {
        loadSourceData();
        this.watchLists = new ArrayList<>();
        this.filterHandler = new FilterHandler();
    }

    /**
     * {@inheritDoc}
     *
     * Stores the source list in the sourceList field.
     */
    @Override
    public void loadSourceData() {
        System.out.println("Load source database from" + DEFAULT_DATA);
        if (!Files.exists(Paths.get(DEFAULT_DATA))) {
            System.out.println("Model: Source data not found, creating new source list from back up resources.");
            try {
                Path sourcePath = new File(DEFAULT_DATA).toPath();
                InputStream backupData = (getClass().getClassLoader().getResourceAsStream("source_bak.json"));
                Files.copy(backupData, sourcePath, StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception e) {
                System.out.println("Error loading backup data");
            }
        }
        this.sourceList = MBeansLoader.loadMediasFromFile(DEFAULT_DATA, Formats.JSON);

    }

    /**
     * {@inheritDoc}
     *
     * Create a set of MBeans where each item is a reference to same MBeans in
     * source list. Pass set to MovieList constructor to create a new watch
     * list. Add the new watch list to the watchLists list.
     *
     * @param filename The file to load the watch list from.
     */
    @Override
    public void loadWatchList(String filename) {

        // Create a substring of file name to use as list name
        int lastSeparator = Math.max(filename.lastIndexOf("\\"), filename.lastIndexOf("/"));
        int lastDot = filename.lastIndexOf('.');
        String name = filename.substring(lastSeparator + 1, lastDot);

        Set<MBeans> externalList = MBeansLoader.loadMediasFromFile(filename, Formats.JSON);
        // Create a list of sourcelist references by mapping externalList to sourceList
        Set<MBeans> mapped = externalList.stream()
                .map(externalBean
                        -> this.sourceList.stream()
                        .filter(localBean -> localBean.equals(externalBean))
                        .findFirst()
                        .orElse(null))
                .collect(Collectors.toSet());
        IMovieList watchList = new MovieList(name, mapped);
        this.watchLists.add(watchList);
    }

    @Override
    public void createNewWatchList(String name) {
        IMovieList watchList = new MovieList(name);
        this.watchLists.add(watchList);
    }

    @Override
    public Stream<MBeans> getRecords() {
        if (this.filter == null) {
            return this.sourceList.stream();
        }
        return filterHandler.filter(this.filter, this.sourceList.stream());
    }

    @Override
    public Stream<MBeans> getRecords(int userListId) {
        if (this.filter == null) {
            return this.watchLists.get(userListId).getMovieList();
        }
        return filterHandler.filter(this.filter, this.watchLists.get(userListId).getMovieList());
    }

    @Override
    public Stream<MBeans> getRecords(List<List<String>> filters) {
        // TODO the method to implement new movies is made but not integrated
        this.setFilter(filters);
        return getRecords();
    }

    @Override
    public Stream<MBeans> getRecords(int userListId, List<List<String>> filters) {
        this.setFilter(filters);
        return getRecords(userListId);
    }

    /**
     * adds new movies to the list.
     *
     * @param filters
     * @return the list of movies with more added.
     */
    public Stream<MBeans> addNewMBeans(List<List<String>> filters) {
        Stream<MBeans> movieStream = this.getRecords();
        String value1 = null;
        String value2 = null;
        String title = null;
        Set<MBeans> beansToAdd = null;

        // Iterate through filters to find title and year values
        for (List<String> afilter : filters) {
            if (afilter.get(0).equalsIgnoreCase("title")) {
                title = afilter.get(2);
            } else if (afilter.get(0).equalsIgnoreCase("year")) {
                if (value1 == null) {
                    value1 = afilter.get(2);
                } else {
                    value2 = afilter.get(2);
                }
            }
        }

        // Ensure that value2 is not null
        if (value2 == null) {
            value2 = value1;
        }

        // Fetch new MBeans if title is present
        if (title != null) {
            if (value1 != null && value2 != null) {
                if (value1.equals(value2)) {
                    beansToAdd = new HashSet<>(MovieAPIHandler.getMoreSourceBeans(title, value1));
                } else {
                    String yearRange = value1 + "-" + value2;
                    beansToAdd = new HashSet<>(MovieAPIHandler.getMoreSourceBeans(title, yearRange));
                }
            }
        }

        // Add the new MBeans to the existing movie set
        if (beansToAdd != null) {
            Set<MBeans> movieSet = movieStream.collect(Collectors.toSet());
            movieSet.addAll(beansToAdd);
            return movieSet.stream();
        }

        return movieStream;
    }

    @Override
    public void saveWatchList(String filename, int userListId) {
        // Get file extension
        String formatStr = filename.substring(filename.lastIndexOf("."), filename.length());
        Formats format = Formats.containsValues(formatStr.toUpperCase());
        try {
            MBeansFormatter.writeMediasToFile(this.getRecords(userListId).collect(Collectors.toList()),
                    new FileOutputStream(filename), format);
        } catch (Exception e) {
            System.out.println("Error writing to file");
        }
    }

    /**
     * {@inheritDoc}
     *
     * Add the media to the watch list by adding a reference to the media in the
     * source list.
     */
    @Override
    public void addToWatchList(MBeans media, int userListId) {
        MBeans sourceMedia = this.getMatchedObjectFromSource(media);
        this.watchLists.get(userListId).addToList(sourceMedia);
    }

    @Override
    public void removeFromWatchList(MBeans media, int userListId) {
        this.watchLists.get(userListId).removeFromList(media);
    }

    @Override
    public void updateWatched(MBeans media, boolean watched) {
        this.getMatchedObjectFromSource(media).setWatched(watched);
        this.updateSourceList();
    }

    @Override
    public void updateUserRating(MBeans media, double rating) {
        this.getMatchedObjectFromSource(media).setMyRating(rating);
        this.updateSourceList();
    }

    @Override
    public void updateSourceList() {
        try {
            MBeansFormatter.writeMediasToFile(this.getRecords().collect(Collectors.toList()),
                    new FileOutputStream(DEFAULT_DATA), Formats.JSON);
        } catch (Exception e) {
            System.out.println("Error writing to file");
        }
    }

    @Override
    public String getUserListName(int userListId) {
        return this.watchLists.get(userListId).getListName();
    }

    @Override
    public int getUserListCount() {
        return this.watchLists.size();
    }

    @Override
    public int[] getUserListIndicesForRecord(MBeans record) {
        int[] indices = IntStream.range(0, this.watchLists.size())
                .filter(i -> this.watchLists.get(i).containsMedia(record))
                .toArray();
        return indices;
    }

    @Override
    public void clearFilter() {
        this.filter = null;
    }

    /**
     * Set and store the current filter to be used later.
     *
     * @param filter filter parameters to store.
     */
    private void setFilter(List<List<String>> filter) {
        this.filter = filter;
    }

    /**
     * Get the object reference of the MBeans that matched given media inside
     * the source list.
     *
     * @param media
     * @return MBeans object reference of the media with the same imdbID
     */
    private MBeans getMatchedObjectFromSource(MBeans media) {
        return this.sourceList.stream()
                .filter(bean -> bean.equals(media))
                .findFirst()
                .orElse(null);
    }

    /**
     * Main method to test the model.
     */
    public static void main(String[] args) {
        Model model = new Model();
    }

}
