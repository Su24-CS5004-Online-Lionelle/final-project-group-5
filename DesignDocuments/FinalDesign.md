# Movie List Manager: Final Design UML Class Diagram

```mermaid
classDiagram
    direction LR

    namespace MODEL {
        class IModel
        class Model
        class IMovieList
        class MovieData
        class MovieList
    }

    namespace VIEW {
        class IView
        class BaseView
        class FilterPane
        class FilterLabels
        class FiltersEnum
        class ListPane
        class MovieTableModel
        class MovieListSelectionHandler
        class ButtonRenderer
        class ButtonEditor
        class TableMode
        class TableColumn
        class MovieTableModelRecord
        class DetailsPane
        class AppFont
    }

    namespace CONTROLLER {
        class IController
        class IFeature
        class Controller
        class ErrorMessage
    }
    
    namespace BEANS {
        class MBeans
    }

    namespace FORMATTERS {
        class MovieXMLWrapper
        class Formats
        class MBeansDeserializer
        class MBeansSerializer
        class MBeansFormatter
        class MBeansLoader
        class IntSerializer
        class DoubleSerializer
        class DateSerializer
        class RuntimeSerializer
        class StringListSerializer
        class BoxOfficeSerializer
        class IntDeserializer
        class DoubleDeserializer
        class DateDeserializer
        class RuntimeDeserializer
        class StringListDeserializer
        class BoxOfficeDeserializer
    }

    namespace FILTER {
        class FilterHandler
        class FilterOperation
        class IFilterHandler
        class Operations
    }

    namespace NET {
        class APIBeans
        class MovieAPIHandler
    }
    
    namespace APP {
        class MovieListManager
    }

    Controller ..|> IController: implements
    Controller ..|> IFeature: implements
    Controller --> IModel: uses
    Controller --> IView: uses
    Controller --> ErrorMessage: uses
    Controller --> MBeans: uses
    Controller --> FilterPane: uses
    Controller --> Operations: uses
    Controller --> MovieData: uses
    
    MBeans --> MBeansDeserializer: uses
    MBeans --> MBeansSerializer: uses

    FilterHandler ..|> IFilterHandler: implements
    FilterHandler --> MBeans: uses
    FilterHandler --> MovieData: uses
    FilterHandler --> Operations: uses
    FilterOperation --> MBeans: uses
    FilterOperation --> MovieData: uses
    FilterOperation --> Operations: uses

    MBeansDeserializer --|> IntDeserializer : contains
    MBeansDeserializer --|> DoubleDeserializer : contains
    MBeansDeserializer --|> DateDeserializer : contains
    MBeansDeserializer --|> RuntimeDeserializer : contains
    MBeansDeserializer --|> StringListDeserializer : contains
    MBeansDeserializer --|> BoxOfficeDeserializer : contains
    MBeansSerializer --|> IntSerializer : contains
    MBeansSerializer --|> DoubleSerializer : contains
    MBeansSerializer --|> DateSerializer : contains
    MBeansSerializer --|> RuntimeSerializer : contains
    MBeansSerializer --|> StringListSerializer : contains
    MBeansSerializer --|> BoxOfficeSerializer : contains
    MBeansFormatter --> MBeans: uses
    MBeansFormatter --> Formats: uses
    MBeansLoader --> MBeans: uses
    MBeansLoader --> Formats: uses
    MovieXMLWrapper --> MBeans: uses

    APIBeans --> MBeansDeserializer: uses
    APIBeans --> MBeansSerializer: uses
    MovieAPIHandler --> MBeans: uses
    MovieAPIHandler --> APIBeans: uses

    Model ..|> IModel: implements
    Model --> MovieData: uses
    Model --> IMovieList: uses
    Model --> IFilterHandler: uses
    Model --> MBeans: uses
    Model --> MBeansLoader: uses
    Model --> MovieAPIHandler: uses
    Model --> Formats: uses
    Model --> MBeansFormatter: uses
    MovieList ..|> IMovieList: implements
    MovieList --> MBeans: uses
    MovieList --> MBeansFormatter: uses

    BaseView ..|> IView: implements
    BaseView --> AppFont: uses
    BaseView --> IFeature: binds
    BaseView --> MBeans: uses
    BaseView --> FilterPane: contains
    BaseView --> ListPane: contains
    BaseView --> DetailsPane: contains
    DetailsPane --> MBeans: uses
    DetailsPane --> IFeature: binds
    FilterPane --> MBeans: uses
    FilterPane --> FiltersEnum: uses
    FilterPane --> FilterLabels: uses
    FilterPane --> IFeature: binds
    ListPane --> MBeans: uses
    ListPane --> IFeature: binds
    ListPane --> MovieTableModel: uses
    ListPane --> MovieTableModelRecord: uses
    ListPane --> MovieListSelectionHandler: uses
    ListPane --> ButtonRenderer: uses
    ListPane --> ButtonEditor: uses
    ListPane --> TableMode: uses
    ListPane --> TableColumn: uses
    MovieTableModel --> MBeans: uses
    MovieTableModel --> TableMode: uses
    MovieTableModel --> TableColumn: uses
    MovieTableModel --> MovieTableModelRecord: uses
    ButtonRenderer --> TableMode: uses
    ButtonEditor --> MBeans: uses
    ButtonEditor --> TableMode: uses
    ButtonEditor --> MovieTableModelRecord: uses
    MovieTableModelRecord --> MBeans: uses

    MovieListManager --> IModel: uses
    MovieListManager --> IView: uses
    MovieListManager --> IController: uses

    class MovieListManager { 
        - MovieListManager()
        - main(String[] args): void$
    }
    
    class IModel {
        <<interface>>
        + DEFAULT_DATA: String
        + DEFAULT_WATCHLIST: String
        + DEFAULT_UNUSED: String
        + loadSourceData(): void
        + loadWatchList(): int
        + loadWatchList(String filename): int
        + createNewWatchList(String name): int
        + deleteWatchList(int userListId): int
        + getAllRecords(): Stream~MBeans~
        + getAllRecords(int userListId): Stream~MBeans~
        + getRecords(): Stream~MBeans~
        + getRecords(int userListId): Stream~MBeans~
        + getRecords(List~List~String~~ filters): Stream~MBeans~
        + getRecords(int userListId, List~List~String~~ filters): Stream~MBeans~
        + saveWatchList(String filename, int userListId): void
        + addToWatchList(MBeans media, int userListId): void
        + removeFromWatchList(MBeans media, int userListId): void
        + updateWatched(MBeans media, boolean watched): void
        + updateUserRating(MBeans media, double rating): void
        + updateSourceList(): void
        + updateSourceList(Set~MBeans~ moviesToAdd): void
        + getUserListName(int userListId): String
        + getUserListCount(): int
        + getUserListIndicesForRecord(MBeans record): int[]
        + clearFilter(): void
        + addNewMBeans(List~List~String~~ filters, Stream~MBeans~ movieStream): void
        + extractFilterValues(List~List~String~~ filters): Map~String, String~
        + fetchMBeans(String title, String year1, String year2): Set~MBeans~
    }
    
    class IView {
        <<interface>>
        + setDetailsPaneEntry(MBeans record): void
        + clearTableSelection(): void
        + setSourceTableRecords(Stream~MBeans~ records, String[] watchlistNames, boolean[][] recordWatchlistMatrix): void
        + setUserTableRecords(Stream~MBeans~ records, int userListId): void
        + addUserTable(String watchlistName): void
        + bindFeatures(IFeature features): void
        + getFilterPane(): FilterPane
        + getDetailsPane(): DetailsPane
        + display(): void
        + getActiveTab(): int
        + setActiveTab(int tabIdx): void
        + showAlertDialog(String title, String message): void
    }
    
    class IController {
        <<interface>>
        + go(): void
    }
    
    class IFeature {
        <<interface>>
        + handleTableSelection(MBeans record): void
        + importListFromFile(String filepath): void
        + exportListToFile(String filepath): void
        + removeFromWatchlist(MBeans mbean, int userListIndex): void
        + addToWatchlist(MBeans mbean, int userListIndex): void
        + createWatchlist(String name): void
        + deleteWatchlist(int userListIndex): void
        + changeRating(MBeans mbean, double rating): void
        + changeWatchedStatus(MBeans record, boolean watched, String caller): void
        + applyFilters(): void
        + clearFiltersAndReloadRecords(): void
        + handleTabChange(int tabIndex): void
    }
    
    class BaseView {
        - APP_TITLE: String$
        - DEFAULT_WIDTH: int$
        - DEFAULT_HEIGHT: int$
        - DEFAULT_FONT_SIZE: int$
        + FilterPane
        + ListPane
        + DetailsPane
        + BaseView()
    }

    class Model {
        - sourceList: Set~MBeans~
        - watchLists: List~IMovieList~
        - filterHandler: IFilterHandler
        - filter: List~List~String~~
        + Model()
        + setFilter(List~List~String~~ filter): void
        + getMatchedObjectFromSource(MBeans media): MBeans
    }
    
    class MovieData {
        <<enumeration>>
         TITLE
         NUMBER
         GENRE
         DIRECTOR
         ACTOR
         LANGUAGE
         WRITER
         MPA
         IMDB
         USER
         RELEASED
         RUNTIME
         BOXOFFICE
         HASWATCHED
        - columnTitle: String
        + MovieData(String columnTitle)
        + getColumnTitle(): String
        + fromColumnName(String columnName): MovieData$
        + fromString(String title): MovieData$
    }
    
    class IMovieList {
        <<interface>>
        + getListName(): String
        + getMovieList(): Stream ~MBeans~
        + clear(): void
        + count(): int
        + saveMovie(String filename, Formats format): void
        + addToList(MBeans media): void
        + removeFromList(MBeans media): void
        + containsMedia(MBeans media): boolean
    }
    
    class MovieList {
        - movieList: Set~MBeans~
        - name: String
        + MovieList(String name)
        + MovieList(String name, Set~MBeans~ movieList)
    }
    
    class Controller {
        + IModel
        + IView
        + Controller(IModel model, IView view)
        - getRecordsForActiveTab(): Stream~MBeans~
        - getRecordUserListMatrix(Stream~MBeans~ records): boolean[][]
        - getWatchlistNames(): String[]
        - getFilterOptions(): List~List~String~~
    }
    
    class ErrorMessage {
        <<enumeration>>
         ERROR
         DELETE_WATCHLIST
         CREATE_WATCHLIST
        NAME_CLASH
         IMPORT_WATCHLIST
        - errorMessage: String
        + ErrorMessage(String errorMessage)
        + getErrorMessage(String fileName): String
    }
    
    class MovieXMLWrapper {
        - movie: Collection~MBeans~
        + MovieXMLWrapper(Collection~MBeans~ movies)
    }
    
    class Formats {
        <<enumeration>>
         JSON
         XML
         CSV
         PRETTY
        + containsValues(String value): Formats$
    }
    
    class MBeansDeserializer {
        <<utility>>
        + IntDeserializer
        + DoubleDeserializer
        + DateDeserializer
        + RuntimeDeserializer
        + StringListDeserializer
        + BoxOfficeDeserializer
    }
    
    class IntDeserializer {
        <<utility>>
        + deserialize(JsonParser p, DeserializationContext ctxt): Integer
    }
    
    class DoubleDeserializer {
        <<utility>>
        + deserialize(JsonParser p, DeserializationContext ctxt): Double
    }

    class DateDeserializer {
        <<utility>>
        + deserialize(JsonParser p, DeserializationContext ctxt): LocalDate
    }

    class RuntimeDeserializer {
        <<utility>>
        + deserialize(JsonParser p, DeserializationContext ctxt): Integer
    }

    class StringListDeserializer {
        <<utility>>
        + deserialize(JsonParser p, DeserializationContext ctxt): List~String~
    }

    class BoxOfficeDeserializer {
        <<utility>>
        + deserialize(JsonParser p, DeserializationContext ctxt): Integer
    }
    
    class MBeansSerializer {
        <<utility>>
        + IntSerializer
        + DoubleSerializer
        + DateSerializer
        + RuntimeSerializer
        + StringListSerializer
        + BoxOfficeSerializer
    }
    
    class IntSerializer {
        <<utility>>
        + serialize(Integer value, JsonGenerator gen, SerializerProvider serializers): void
    }

    class DoubleSerializer {
        <<utility>>
        + serialize(Double value, JsonGenerator gen, SerializerProvider serializers): void
    }

    class DateSerializer {
        <<utility>>
        + serialize(LocalDate date, JsonGenerator gen, SerializerProvider serializers): void
    }

    class RuntimeSerializer {
        <<utility>>
        + serialize(Integer runtime, JsonGenerator gen, SerializerProvider serializers): void
    }

    class StringListSerializer {
        <<utility>>
        + serialize(List~String~ genre, JsonGenerator gen, SerializerProvider serializers): void
    }

    class BoxOfficeSerializer {
        <<utility>>
        + serialize(Integer boxOffice, JsonGenerator gen, SerializerProvider serializers): void
    }
    
    class MBeansFormatter {
        - MBeansFormatter()
        - writeMediasToTXT(Collection~MBeans~records, OutputStream out): void$
        - writeMediasToXML(Collection~MBeans~ records, OutputStream out): void$
        - writeMediasToJSON(Collection~MBeans~ records, OutputStream out): void$
        - writeMediasToCSV(Collection~MBeans~ records, OutputStream out): void$
        - writeMediasToFile(Collection~MBeans~ records, OutputStream out, Formats format): void$
    }
    
    class MBeansLoader {
        - MBeansLoader()
        + loadMediasFromJSON(String filename): Set~MBeans~$
        + loadMediasFromCSV(String filename): Set~MBeans~$
        + loadMediasFromFile(String filename, Formats format): Set~MBeans~$
    }
    
    class AppFont {
        <<utility>>
        - AppFont()
        + setAppFont(FontUIResource font): void$
    }
    
    class DetailsPane {
        - DEFAULT_WIDTH: int$
        - DEFAULT_HEIGHT: int$
        - DEFAULT_COLOR: Color$
        - scrollPane: JScrollPane
        - detailsPanel: JPanel
        - mediaTitle: JTextPane
        - mediaImage: JLabel
        - mediaDetails: List~JTextArea~
        - watchedBox: JcheckBox
        - userRating: JTextField
        - currentMedia: MBeans
        + DetailsPane()
        - initContent(): void
        - addVerticalPadding(int padding): void
        - addTitlePane(): void
        - addImageLabel(): void
        - addWatched(): void
        - addDetailPane(String name): void
        - addUserRating(): void
        - reSize(): void
        - scaleImage(String imgStr): ImageIcon
        - getCurrentMedia(): MBeans
        - refreshUserFields(): void
        + setMedia(MBeans media): void
        + bindFeatures(IFeature features): void
        - setUserRatingField(): void
    }
    
    class FilterPane {
        - DEFAULT_WIDTH: int
        - DEFAULT_HEIGHT: int
        - movies: Set~MBeans~
        - moviesIsSourceList: boolean
        - filterPanel: JPanel
        - buttonPanel: JPanel
        - titleFilter: JTextField
        - genreFilter: JComboBox
        - mpaRatingFilter: JComboBox
        - directorFilter: JTextField
        - actorFilter: JTextField
        - writerFilter: JTextField
        - languageFilter: JComboBox
        - releasedFrom: JTextField
        - releasedTo: JTextField
        - imdbRatingFrom: JTextField
        - imdbRatingTo: JTextField
        - boxOfficeEarningsFrom: JTextField
        - boxOfficeEarningsTo: JTextField
        - textFilters: Set~JTextField~
        - dropDownFilters: Set~JComboBox~String~~
        - rangeFilterMap: Map~JTextField~, ~String~
        - applyFiltersButton: JButton
        - clearFiltersButton: JButton
        - gbc: GridBagConstrains
        - filterRow: int
        + FilterPane(): String
        + getFilteredTitle(): String
        + getFilteredGenre(): String
        + getFilteredMpaRating(): String
        + getFilteredReleasedMin(): String
        + getFilteredReleasedMax(): String
        + getFilteredImdbRatingMin(): String
        + getFilteredImdbRatingMax(): String
        + getFilteredBoxOfficeEarningsMin(): String
        + getFilteredBoxOfficeEarningsMax(): String
        + getFilteredDirectorFilter(): String
        + getFilteredActorFilter(): String
        + getFilteredWriterFilter(): String
        + getFilteredLanguageFilter(): String
        + setMovies(Stream~MBeans~ movies): void
        + setMovies(Stream~MBeans~ movies, boolean isSourceList): void
        - setComponentNames(): void
        - setRangeFilterRanges(): void
        - updateGBC(Integer x, Integer y, Integer width, Integer weightx, Integer anchor, Integer fill): void
        - addLabel(String filterTitle): void
        - addFilter(String filterTitle, Object filter): void
        + addRangeFilter(String filterTitle, JTextField filterFrom, JTextField filterTo): void
        - setPlaceholder(JTextField textField, String value): void
        - italicizeFont(Object object): void
        - getDoubleFilterRange(ToDoubleFunction~MBeans~ fieldFunction, JTextField from, JTextField to): void
        - getIntFilterRange(ToIntFunction~MBeans~ fieldFunction, JTextField from, JTextField to): void
        - formatAsCurrency(String value, String minOrMax): String
        - formatFromMillions(String value): String
        - configureComboBox(JComboBox~String~ comboBox): void
        - clearFilterOptions(): void
        - resetComboBoxOptions(): void
        - resetTextFilters(): void
        - resetFilterOptions(): void
        - refreshPlaceholders(String updateOrClearPlaceholders): void
        - resetPlaceholder(JTextField textField): void
        - reformatBoxOfficeEarnings(JTextField boxOfficeEarnings): void
        - getFilterByEnum(String filter): Filters
        + actionPerformed(ActionEvent e): void
        + focusGained(FocusEvent e): void
        + focusLost(FocusEvent e): void
        + bindFeatures(IFeature features): void
    }
    
    class FiltersEnum {
        <<enumeration>>
         TITLE
         GENRE
         MPA_RATING
         RELEASED_FROM
         RELEASED_TO
         IMDB_RATING_FROM
         IMDB_RATING_TO
         BOX_OFFICE_EARNINGS_FROM
         BOX_OFFICE_EARNINGS_TO
         DIRECTOR
         ACTOR
         WRITER
         LANGUAGE
        - filterName: String
        + Filters(String filterName)
        + getFilterName(): String
    }
    
    class FilterLabels {
        <<enumeration>>
         TITLE
         GENRE
         MPA_RATING
         RELEASED
         IMDB_RATING
         BOX_OFFICE_EARNINGS
         DIRECTOR
         ACTOR
         WRITER
         LANGUAGE
         FROM
         TO
        - filterLabel: String
        + FilterLabels(String filterLabel)
        + getFilterLabel(): String
    }
    
    class ListPane {
        - MAIN_TAB_NAME: String
        - ADD_LIST_BUTTON_TEXT: String
        - DELETE_LIST_BUTTON_TEXT: String
        - EXPORT_LIST_BUTTON_TEXT: String
        - MAIN_ACTION_BUTTON_TEXT: String
        - WATCHLIST_ACTION_BUTTON_TEXT: String
        - NEW_LIST_POPUP_TITLE: String
        - NEW_LIST_POPUP_PROMPT: String
        + sourceTable: JTable
        + importListButton: JButton
        + deleteListButton: JButton
        + exportListButton: JButton
        + tabbedPane: JTabbedPane
        + sourceTableModel: MovieTableModel
        + tableSelectionHandler: Consumer~MBeans~
        + removeFromListHandler: BiConsumer~MBeans, Integer~
        + addToListHandler: BiConsumer~MBeans, Integer~
        + changeWatchedStatusHandler: TriConsumer~MBeans, Boolean, String~
        + createListHandler: Consumer~String~
        + deleteListHandler: Consumer~Integer~
        + tabChangeHandler: Consumer~Integer~
        + importListHandler: Consumer~String~
        + exportListHandler: Consumer~String~
        + SORTING_ENABLED: Boolean
        + watchlistModels: List~MovieTableModel~
        + watchlistTables: List~JTable~
        + ListPane()
        + setActiveTab(int index): void
        + getActiveTab(): int
        + getActiveTable(): JTable
        + getActiveTableModel(): MovieTableModel
        + createTableTab(String name, TableMode tableMode): void
        + removeUserTable(int userListId): void
        + createSourceTableTab(): void
        + createUserTableTab(String tableName): void
        + localImportListHandler(): void
        + localExportListHandler(): void
        + setSourceTable(Stream~MBeans~ records, String[] watchlistNames, boolean[][] recordWatchlistMatrix): void
        + setUserTable(Stream~MBeans~ recordStream, int watchlistIndex): void
        + bindFeatures(IFeature features): void
        + localTabChangeHandler(): void
        + getCurrentTable(): JTable
        + localDeleteListHandler(): void
    }
    
    class MovieTableModel {
        - columnNames: String[]
        - movieTableModelRecords: List~MovieTableModelRecord~
        - tableMode: TableMode
        + MovieTableModel(TableMode tableMode)
        + getRecordAt(int row): MBeans
        + getTableMode(): TableMode
        + setMovieTableModelRecords(List~MovieTableModelRecord~ movieTableModelRecords): void
        + getColumnCount(): int
        + getRowCount(): int
        + getColumnName(int col): String
        + getValueAt(int row, int col): Object
        + getColumnClass(int col): Class
        + isCellEditable(int row, int col): boolean
        + setValueAt(Object value, int row, int col): void
    }
    
    class MovieListSelectionHandler {
        + valueChanged(ListSelectionEvent e) void
    }
    
    class ButtonRenderer {
        - tableMode: TableMode
        + ButtonRenderer(TableMode tableMode)
        + getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column): Component
    }
    
    class ButtonEditor {
        - button: JButton
        - label: String
        - record: MBeans
        - isPushed: boolean
        - tableMode: TableMode
        - movieTableModelRecord: MovieTableModelRecord
        - tickIcon: Icon$
        + ButtonEditor(TableMode tableMode)
        + getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column): Component
        + getCellEditorValue(): Object
        + stopCellEditing(): boolean
    }
    
    class TableMode {
        <<enumeration>>
        MAIN
        WATCHLIST
    }
    
    class TableColumn {
        TITLE
        YEAR
        GENRE
        RUNTIME
        WATCHED
        WATCHLIST
        - name: String
        + TableColumn(String name)
        + getName(): String
        + getIndex(): int
    }
    
    class MovieTableModelRecord {
        - record: MBeans
        - userListIndeces: boolean[]
        - userListNames: String[]
        + MovieTableModelRecord(MBeans record, String[] userListNames, boolean[] userListIndices)
        + MovieTableModelRecord(MBeans record)
        + getRecord(): MBeans
        + getUserListIndices(): boolean[]
        + getUserListNames(): String[]
    }
    
    class MBeans {
        - title: String
        - year: int
        - type: String
        - rated: String
        - released: LocalDate
        - runtime: int
        - genre: List~String~
        - director: List~String~
        - writer: List~String~
        - actors: List~String~
        - plot: String
        - language: List~String~
        - country: List~String~
        - awards: String
        - poster: String
        - metascore: int
        - imdbRating: double
        - boxOffice: int
        - id: String
        - watched: boolean
        - myRating: double
        + MBeans()
        + MBeans(String title, int year, String type, String rated, LocalDate released, int runtime, List~String~ genre,List~String~ director, List~String~ writer, List~String~ actors, String plot, List~String~ language,List~String~ country, String awards, String poster, int metascore, double imdbRating, int boxOffice, String id, boolean watched, double myRating)
        + getTitle(): String
        + getYear(): int
        + getType(): String
        + getRated(): String
        + getReleased(): LocalDate
        + getRuntime(): int
        + getGenre(): List~String~
        + getDirector(): List~String~
        + getWriter(): List~String~
        + getActors(): List~String~
        + getPlot(): String
        + getLanguage(): List~String~
        + getCountry(): List~String~
        + getAwards(): String
        + getPoster(): String
        + getMetascore(): int
        + getImdbRating(): double
        + getBoxOffice(): int
        + getID(): String
        + getWatched(): boolean
        + getMyRating(): double
        + setTitle(String title): void
        + setYear(int year): void
        + setType(String type): void
        + setRated(String rated): void
        + setReleased(LocalDate released): void
        + setRuntime(int runtime): void
        + setGenre(List~String~ genre): void
        + setDirector(List~String~ director): void
        + setWriter(List~String~ writer): void
        + setActors(List~String~ actors): void
        + setPlot(String plot): void
        + setLanguage(List~String~ language): void
        + setCountry(List~String~ country): void
        + setAwards(String awards): void
        + setPoster(String poster): void
        + setMetascore(int metascore): void
        + setImdbRating(double imdbRating): void
        + setBoxOffice(int boxOffice)
        + setID(String id): void: void
        + setWatched(boolean watched): void
        + setMyRating(double myRating): void
        + toString(): String
        + formatBoxOfficeCurrency(): String
        + equals(Object o): boolean
        + hashCode(): int
        + formattedDate(): String
    }
    
    class IFilterHandler {
        <<interface>>
        + filter(List~List~String~~ filter, Stream~MBeans~ beanStream): Stream~MBeans~
    }
    
    class FilterHandler {
        + FilterHandler()
        + makeAndApplySingleFilter(List~MBeans~ beans, List~String~ filter): Stream~MBeans~$
    }
    
    class FilterOperation {
        <<utility>>
        - FilterOperation()
        + getFilter(MBeans movie, MovieData filterOn, Operations op, String val): boolean$
        + filterList(List~String~ strList, Operations op, String val): boolean$
        + filterString(String field, Operations op, String val): boolean$
        + filterInt(int field, Operations op, String val): boolean$
        + filterDouble(double field, Operations op, String val): boolean$
        + filterBoolean(boolean field, Operations op, String val): boolean$
    }
    
    class Operations {
        <<enumeration>>
        EQUALS
        CONTAINS
        NOTEQUAL
        GREATERTHAN
        LESSTHAN
        GREATEROREQUAL
        LESSOREQUAL
        - operator: String
        + Operations(String value)
        + getOperator(): String
        + getOperatorFromStr(String str): Operations$
    }
    
    class APIBeans {
        - title: String
        - year: int
        - id: String
        + APIBeans()
        + APIBeans(String title, int year, String id)
        + getTitle(): String
        + getYear(): int
        + getID(): String
        + setTitle(String title): void
        + setYear(int year): void
        + setID(String id): void
        + toString(): String
        + equals(Object o): boolean
        + hashCode(): int
    }
    
    class MovieAPIHandler {
        - API_ENDPOINT: String$
        - API_KEY: String$
        + MovieAPIHandler()
        + getMoreSourceBeans(String title, String yearRange): List ~MBeans~$
        + getMovieListFromAPI(String title): List~APIBeans~$
        + getMovie(String imdbID): MBeans$
        + handleErrorResponse(HttpURLConnection conn): void$
        + parseAPITitle(InputStream inputStream): List~APIBeans~$
        + parseMovieFromAPI(InputStream inputStream): MBeans$
    }
```
