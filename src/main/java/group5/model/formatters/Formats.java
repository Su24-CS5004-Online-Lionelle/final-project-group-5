package group5.model.formatters;

/** A list of format types allowed for export and import. */
public enum Formats {
    /** Different formatting options. */
    JSON, XML, CSV, PRETTY;

    /**
     * Helper function to check if a value is in the list of formats.
     *
     * @param value the value to check
     * @return the format if found, null otherwise
     */
    public static Formats containsValues(String value) {
        if (value.equalsIgnoreCase("txt")) {
            return Formats.PRETTY;
        }
        for (Formats format : Formats.values()) {
            if (format.toString().equalsIgnoreCase(value)) {
                return format;
            }
        }
        return null;
    }
}
