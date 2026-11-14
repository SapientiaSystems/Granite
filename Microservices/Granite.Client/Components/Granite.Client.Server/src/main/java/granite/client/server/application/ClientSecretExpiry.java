//////////////////////////////////////////////////////////////////////////////
// ClientSecretExpiry.java
//////////////////////////////////////////////////////////////////////////////

package granite.client.server.application;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

public
class ClientSecretExpiry
{
    private
    ClientSecretExpiry() {}

    public static Instant
    fromDate(LocalDate date)
    {
        return
            date == null
                ? null
                : date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
    }
}

//////////////////////////////////////////////////////////////////////////////
