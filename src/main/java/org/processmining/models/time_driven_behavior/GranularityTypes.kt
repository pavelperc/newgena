package org.processmining.models.time_driven_behavior

import java.util.concurrent.TimeUnit

/**
 * Created by Ivan Shugurov on 17.09.2014.
 * Rounds timestamp.
 */
enum class GranularityTypes(val precision: Long) {
    
    SECONDS_5(TimeUnit.SECONDS.toMillis(5)),
    SECONDS_10(TimeUnit.SECONDS.toMillis(10)),
    SECONDS_15(TimeUnit.SECONDS.toMillis(15)),
    SECONDS_30(TimeUnit.SECONDS.toMillis(30)),
    MINUTES_1(TimeUnit.MINUTES.toMillis(1)),
    MINUTES_5(TimeUnit.MINUTES.toMillis(5)),
    MINUTES_10(TimeUnit.MINUTES.toMillis(10)),
    MINUTES_15(TimeUnit.MINUTES.toMillis(15)),
    MINUTES_30(TimeUnit.MINUTES.toMillis(30)),
    HOUR_1(TimeUnit.HOURS.toMillis(1)),
}
