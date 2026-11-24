//////////////////////////////////////////////////////////////////////////////
// TestCallerSubject.java
//////////////////////////////////////////////////////////////////////////////

package granite.client.server.application;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public
class TestCallerSubject
    implements Supplier<Optional<String>>
{
    private static final AtomicReference<String> CURRENT = new AtomicReference<>();

    public static void
    set(String subject)
    {
        CURRENT.set(subject);
    }

    @Override
    public Optional<String>
    get()
    {
        return Optional.ofNullable(CURRENT.get());
    }
}

//////////////////////////////////////////////////////////////////////////////
