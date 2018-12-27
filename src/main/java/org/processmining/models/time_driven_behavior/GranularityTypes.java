package org.processmining.models.time_driven_behavior;

import java.util.concurrent.TimeUnit;

/**
 * Created by Ivan Shugurov on 17.09.2014.
 */
public enum GranularityTypes
{
    SECONDS_5
            {
                @Override
                public long getPrecision()
                {
                    return TimeUnit.SECONDS.toMillis(5);
                }

                @Override
                public String toString()
                {
                    return "Round timestamp to 5 seconds";
                }
            },
    SECONDS_10
            {
                @Override
                public long getPrecision()
                {
                    return TimeUnit.SECONDS.toMillis(10);
                }

                @Override
                public String toString()
                {
                    return "Round timestamp to 10 seconds";
                }
            },
    SECONDS_15
            {
                @Override
                public long getPrecision()
                {
                    return TimeUnit.SECONDS.toMillis(15);
                }

                @Override
                public String toString()
                {
                    return "Round timestamp to 15 seconds";
                }
            },
    SECONDS_30
            {
                @Override
                public String toString()
                {
                    return "Round timestamp to 30 seconds";
                }

                @Override
                public long getPrecision()
                {
                    return TimeUnit.SECONDS.toMillis(30);
                }

            },
    MINUTES_1
            {
                @Override
                public String toString()
                {
                    return "Round timestamp to 1 minute";
                }

                @Override
                public long getPrecision()
                {
                    return TimeUnit.MINUTES.toMillis(1);
                }
            },
    MINUTES_5
            {
                @Override
                public String toString()
                {
                    return "Round timestamp to 5 minutes";
                }

                @Override
                public long getPrecision()
                {
                    return TimeUnit.MINUTES.toMillis(5);
                }
            },
    MINUTES_10
            {
                @Override
                public String toString()
                {
                    return "Round timestamp to 10 minutes";
                }

                @Override
                public long getPrecision()
                {
                    return TimeUnit.MINUTES.toMillis(10);
                }
            },
    MINUTES_15
            {
                @Override
                public String toString()
                {
                    return "Round timestamp to 15 minutes";
                }

                @Override
                public long getPrecision()
                {
                    return TimeUnit.MINUTES.toMillis(15);
                }
            },
    MINUTES_30
            {
                @Override
                public String toString()
                {
                    return "Round timestamp to 30 minutes";
                }

                @Override
                public long getPrecision()
                {
                    return TimeUnit.MINUTES.toMillis(30);
                }
            },
    HOUR_1
            {
                @Override
                public String toString()
                {
                    return "Round timestamp to 1 hour";
                }

                @Override
                public long getPrecision()
                {
                    return TimeUnit.HOURS.toMillis(1);
                }
            };

    public abstract long getPrecision();

    @Override
    public abstract String toString();
}
