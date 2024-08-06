package group5.model;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import group5.model.filter.FilterHandler;
import group5.model.filter.IFilterHandler;
import group5.model.beans.MBeans;
import group5.model.formatters.Formats;
import group5.model.formatters.MBeansFormatter;
import group5.model.formatters.MBeansLoader;
import group5.model.net.MovieAPIHandler;

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
     * Walk through a DEFAULT_WATCHLIST directory and find every.json file to load.
     */
    @Override
    public int loadWatchList() {
        try (Stream<Path> paths = Files.walk(Paths.get(DEFAULT_WATCHLIST))) {
            List<Path> pathList = paths.collect(Collectors.toList());
            for (Path path : pathList) {
                if (path.toString().endsWith(".json")) {
                    loadWatchList(path.toString());
                }
            }
        } catch (Exception e) {
            System.out.println("Error loading watchlist");
        }
        return this.getUserListCount();
    }

    /**
     * {@inheritDoc}
     *
     * Create a set of MBeans where each item is a reference to same MBeans in
     * source list. Pass set to MovieList constructor to create a new watch
     * list. Add the new watch list to the watchLists list.
     *
     * Create a new watch list file into default watchlist directory.
     *
     * @param filename The file to load the watch list from.
     */
    @Override
    public int loadWatchList(String filename) {

        // Create a substring of file name to use as list name
        int lastSeparator = Math.max(filename.lastIndexOf("\\"), filename.lastIndexOf("/"));
        int lastDot = filename.lastIndexOf('.');
        Formats format = Formats.containsValues(filename.substring(lastDot + 1));
        String name = filename.substring(lastSeparator + 1, lastDot);
        for (IMovieList watchList : this.watchLists) {
            if (watchList.getListName().equals(name)) {
                // Return -1 if watchlist with exact name already exists.
                return -1;
            }
        }

        Set<MBeans> externalList = MBeansLoader.loadMediasFromFile(filename, format);

        // Return -2 if file failed to load or loaded an empty file
        if (externalList == null || externalList.isEmpty()) {
            return -2;
        }

        // Create a list of sourcelist references by mapping externalList to sourceList
        boolean newItems = false;
        Set<MBeans> mapped = new HashSet<>();
        for (MBeans externalBean : externalList) {
            // Find if each loaded movie match any record in source.
            MBeans match = this.getMatchedObjectFromSource(externalBean);
            if (match == null) {
                // New item, add to source list
                this.sourceList.add(externalBean);
                newItems = true;  // Flag for update source file
                mapped.add(externalBean);
            } else {
                mapped.add(match);
            }
        }

        // Update source file if new items were added
        if (newItems) {
            saveSourceList();
        }

        // Create new watchlist
        IMovieList watchList = new MovieList(name, mapped);
        this.watchLists.add(watchList);
        int index = this.watchLists.size() - 1;
        // Write to local directory.
        this.saveWatchList(DEFAULT_WATCHLIST + "/" + this.getUserListName(index) + ".json", index);
        return index;
    }

    @Override
    public int createNewWatchList(String name) {
        for (IMovieList watchList : this.watchLists) {
            if (watchList.getListName().equals(name)) {
                // Return -1 if watchlist with exact name already exists.
                return -1;
            }
        }
        IMovieList watchList = new MovieList(name);
        this.watchLists.add(watchList);
        int index = this.watchLists.size() - 1;
        // Write to local directory.
        this.saveWatchList(DEFAULT_WATCHLIST + "/" + this.getUserListName(index) + ".json", index);
        return index;
    }

    @Override
    public int deleteWatchList(int userListId) {
        if (userListId < 0 || userListId >= this.watchLists.size()) {
            // Invalid index
            return -1;
        }
        String name = this.getUserListName(userListId);
        this.saveWatchList("./data/unused/" + name + ".json", userListId);
        this.watchLists.remove(userListId);
        File toDelete = new File(DEFAULT_WATCHLIST + "/" + name + ".json");
        toDelete.delete();
        return userListId;
    }

    @Override
    public Stream<MBeans> getAllRecords() {
        return this.sourceList.stream();
    }

    @Override
    public Stream<MBeans> getAllRecords(int userListId) {
        return this.watchLists.get(userListId).getMovieList();
    }

    @Override
    public Stream<MBeans> getRecords() {
        if (this.filter == null) {
            return this.getAllRecords();
        }
        return filterHandler.filter(this.filter, this.sourceList.stream());
    }

    @Override
    public Stream<MBeans> getRecords(int userListId) {
        if (this.filter == null) {
            return this.getAllRecords(userListId);
        }
        return filterHandler.filter(this.filter, this.watchLists.get(userListId).getMovieList());
    }

    @Override
    public Stream<MBeans> getRecords(List<List<String>> filters) {
        this.setFilter(filters);
        return getRecords();
    }

    @Override
    public Stream<MBeans> getRecords(int userListId, List<List<String>> filters) {
        this.setFilter(filters);
        return getRecords(userListId);
    }

    @Override
    public void addNewMBeans(List<List<String>> filters, Stream<MBeans> movieStream) {
        Map<String, String> filterValues = extractFilterValues(filters);
        String title = filterValues.get("title");
        if (title == null || title.isEmpty()) {
            return;
        } else {
            title = title.replace(" ", "+");
        }

        String year1 = filterValues.get("year1");
        String year2 = filterValues.get("year2");

        Set<MBeans> beansToAdd = fetchMBeans(title, year1, year2);

        if (beansToAdd != null) {
            updateSourceList(beansToAdd);
        }

    }

    @Override
    public Map<String, String> extractFilterValues(List<List<String>> filters) {
        Map<String, String> filterValues = new HashMap<>();
        String title = null;
        String year1 = null;
        String year2 = null;

        for (List<String> filter : filters) {
            if (filter.size() >= 3) { // Ensure filter has at least 3 elements
                String key = filter.get(0);
                String value = filter.get(2);

                if ("title".equalsIgnoreCase(key)) {
                    title = value;
                } else if ("released".equalsIgnoreCase(key)) {
                    if (year1 == null) {
                        year1 = value;
                    } else {
                        year2 = value;
                    }
                }
            }
        }

        // Add extracted values to the map
        if (title != null) {
            filterValues.put("title", title);
        }
        if (year1 != null) {
            filterValues.put("year1", year1);
        }
        if (year2 != null) {
            filterValues.put("year2", year2);
        }

        return filterValues;
    }

    @Override
    public Set<MBeans> fetchMBeans(String title, String year1, String year2) {
        if (year1 != null && year2 != null) {
            if (year1.equals(year2)) {
                return new HashSet<>(MovieAPIHandler.getMoreSourceBeans(title, year1));
            } else {
                String yearRange = year1 + "-" + year2;
                return new HashSet<>(MovieAPIHandler.getMoreSourceBeans(title, yearRange));
            }
        } else if (year1 != null) {
            return new HashSet<>(MovieAPIHandler.getMoreSourceBeans(title, year1));
        } else {
            return new HashSet<>(MovieAPIHandler.getMoreSourceBeans(title, null));
        }
    }

    @Override
    public void saveWatchList(String filename, int userListId) {
        // Get file extension
        String formatStr = filename.substring(filename.lastIndexOf(".") + 1, filename.length());
        Formats format = Formats.containsValues(formatStr);
        System.out.println(format);
        try {
            OutputStream out = new FileOutputStream(filename);
            MBeansFormatter.writeMediasToFile(this.watchLists.get(userListId).getMovieList().collect(Collectors.toSet()),
                    out, format);
            out.close();
        } catch (Exception e) {
            System.out.println("Error writing to file");
            e.printStackTrace();
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
        this.saveWatchList(DEFAULT_WATCHLIST + "/" + this.getUserListName(userListId) + ".json", userListId);
    }

    @Override
    public void removeFromWatchList(MBeans media, int userListId) {
        this.watchLists.get(userListId).removeFromList(media);
        this.saveWatchList(DEFAULT_WATCHLIST + "/" + this.getUserListName(userListId) + ".json", userListId);
    }

    @Override
    public void updateWatched(MBeans media, boolean watched) {
        this.getMatchedObjectFromSource(media).setWatched(watched);
        this.saveSourceList();
    }

    @Override
    public void updateUserRating(MBeans media, double rating) {
        this.getMatchedObjectFromSource(media).setMyRating(rating);
        this.saveSourceList();
    }

    @Override
    public void saveSourceList() {
        try {
            OutputStream out = new FileOutputStream(DEFAULT_DATA);
            MBeansFormatter.writeMediasToFile(this.sourceList, out, Formats.JSON);
            out.close();
        } catch (Exception e) {
            System.out.println("Error writing to file");
        }
    }

    @Override
    public void updateSourceList(Set<MBeans> moviesToAdd) {
        try {
            // Collect current records into a LinkedHashSet to maintain order and avoid duplicates
            Set<MBeans> currentList = new LinkedHashSet<>(this.sourceList);
            System.out.println("Current list size before adding new movies: " + currentList.size());
            System.out.println("New movies size: " + moviesToAdd.size());

            if (!moviesToAdd.isEmpty()) {
                System.out.println("Current list size before adding new MBeans: " + currentList.size());
                System.out.println("New MBeans size: " + moviesToAdd.size());

                // Add new MBeans to the current list
                currentList.addAll(moviesToAdd);
                System.out.println("Current list size after adding new MBeans: " + currentList.size());
                this.sourceList = currentList;
                // Update source file
                this.saveSourceList();
            }
        } catch (Exception e) {
            System.out.println("Error updating source list: " + e.getMessage());
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
    public int[] getUserListIndicesForRecord(MBeans record
    ) {
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
    public static void main(String[] args) throws IOException {
        Model model = new Model();
        model.loadWatchList();
        int watchList01 = model.loadWatchList("./bad.json");
        int watchList02 = model.loadWatchList("./bad.csv");
        int watchList03 = model.createNewWatchList("Movie List 4");
        System.out.println("Watchlist 01: " + watchList01);
        System.out.println("Watchlist 02: " + watchList02);
        System.out.println("Watchlist 03: " + watchList03);
    }

}
