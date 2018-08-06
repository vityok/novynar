package org.bb.vityok.novinar.core;



import java.time.Duration;


/** Possible update/refresh periods for feeds.
 *
 * User has only the following options to chose from.
 */
public enum UpdatePeriod
{
    MINUTES_15 ("minutes15", "15 minutes", Duration.ofMinutes(15)),
    MINUTES_30 ("minutes30", "30 minutes", Duration.ofMinutes(30)),
    HOURS_1 ("hours1", "1 hour", Duration.ofHours(1)),
    HOURS_3 ("hours3", "3 hours", Duration.ofHours(3)),
    HOURS_12 ("hours12", "12 hours", Duration.ofHours(12)),
    DAYS_1 ("days1", "1 day", Duration.ofDays(1)),
    DAYS_2 ("days2", "2 days", Duration.ofDays(2)),
    DAYS_7 ("days7", "7 days", Duration.ofDays(7)),
    NEVER ("never", "never", null);

    private final String code;
    private final String title;
    private final Duration dur;

    public static final UpdatePeriod DEFAULT_UPDATE_PERIOD = HOURS_3;

    /** We are using our own, manually-defined codes instead of
     * compiler-provided names.
     *
     * @see name()
     * @see toString()
     */
    UpdatePeriod(String code, String title, Duration dur) {
        this.code = code;
        this.title = title;
        this.dur = dur;
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

    /** Computer representation of the corresponding duration. */
    public Duration getDuration() {
        return dur;
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
