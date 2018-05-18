package org.bb.vityok.novinar;

/** Possible update/refresh periods for feeds.
 *
 * User has only the following options to chose from.
 */
public enum UpdatePeriod
{
    MINUTES_15 ("minutes15", "15 minutes"),
    MINUTES_30 ("minutes30", "30 minutes"),
    HOURS_1 ("hours1", "1 hour"),
    HOURS_3 ("hours3", "3 hours"),
    HOURS_12 ("hours12", "12 hours"),
    DAYS_1 ("days1", "1 day"),
    DAYS_2 ("days2", "2 days"),
    DAYS_7 ("days7", "7 days"),
    NEVER ("never", "never");

    private final String code;
    private final String title;

    public static final UpdatePeriod DEFAULT_UPDATE_PERIOD = HOURS_3;

    /** We are using our own, manually-defined codes instead of
     * compiler-provided names.
     *
     * @see name()
     * @see toString()
     */
    UpdatePeriod(String code, String title) {
        this.code = code;
        this.title = title;
    }


    /** Returns title for this enum value.
     *
     * @see getCode()
     */
    @Override
    public String toString() {
        return title;
    }

    public String getCode() {
        return code;
    }

    public static UpdatePeriod fromString(String str) {
        UpdatePeriod[] periods = values();
        if (str != null && (! str.isEmpty())) {
            for (int i = 0; i < periods.length; i++) {
                if (periods[i].code.equals(str)) {
                    return periods[i];
                }
            }
        }
        return null;
    }

}
