package pl.gpwpoid.origin.ui.views.company;

import lombok.Getter;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public enum ChartTimeRange {
    FIVE_MINUTES("5m", 5, ChronoUnit.MINUTES, ChronoUnit.SECONDS, 5),
    THIRTY_MINUTES("30m", 30, ChronoUnit.MINUTES, ChronoUnit.SECONDS, 30),
    ONE_HOUR("1h", 1, ChronoUnit.HOURS, ChronoUnit.MINUTES, 1),
    ONE_DAY("1d", 1, ChronoUnit.DAYS, ChronoUnit.MINUTES, 24),
    ONE_WEEK("1t", 1, ChronoUnit.WEEKS, ChronoUnit.HOURS, 3),
    ONE_MONTH("1m", 1, ChronoUnit.MONTHS, ChronoUnit.HOURS, 12);

    @Getter
    private final String label;
    private final long amountToSubtract;
    private final ChronoUnit unitToSubtract;
    @Getter
    private final ChronoUnit groupingUnit;
    @Getter
    private final int intervalAmount;

    ChartTimeRange(String label, long amountToSubtract, ChronoUnit unitToSubtract, ChronoUnit groupingUnit, int intervalAmount) {
        this.label = label;
        this.amountToSubtract = amountToSubtract;
        this.unitToSubtract = unitToSubtract;
        this.groupingUnit = groupingUnit;
        this.intervalAmount = intervalAmount;
    }


    public LocalDateTime getStartTime(LocalDateTime now) {
        return now.minus(amountToSubtract, unitToSubtract);
    }

    public LocalDateTime truncate(LocalDateTime time) {
        if (intervalAmount == 1) {
            return time.truncatedTo(groupingUnit);
        }

        switch (groupingUnit) {
            case SECONDS: {
                long totalSecondsOfDay = time.toLocalTime().toSecondOfDay();
                long truncatedTotalSeconds = (totalSecondsOfDay / intervalAmount) * intervalAmount;
                return time.truncatedTo(ChronoUnit.DAYS).plusSeconds(truncatedTotalSeconds);
            }
            case MINUTES: {
                long totalMinutesOfDay = time.getHour() * 60L + time.getMinute();
                long truncatedTotalMinutes = (totalMinutesOfDay / intervalAmount) * intervalAmount;
                return time.truncatedTo(ChronoUnit.DAYS).plusMinutes(truncatedTotalMinutes);
            }
            case HOURS: {
                int hour = time.getHour();
                int truncatedHour = (hour / intervalAmount) * intervalAmount;
                return time.withHour(truncatedHour).truncatedTo(ChronoUnit.HOURS);
            }
            default:
                return time.truncatedTo(groupingUnit);
        }
    }
}
