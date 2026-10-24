//////////////////////////////////////////////////////////////////////////////
// ConflictDetector.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import jakarta.persistence.PersistenceException;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.TransactionException;

public
class ConflictDetector
{
    public
    ConflictDetector() {}

    public boolean
    isConflict(Throwable exception)
    {
        for (Throwable cause = exception; cause != null; cause = cause.getCause())
        {
            if (cause instanceof ConcurrentLoginException)
                return true;

            if (cause instanceof PersistenceException)
                return true;

            if (cause instanceof DataAccessException)
                return true;

            if (cause instanceof TransactionException)
                return true;

            if (cause.getCause() == cause)
                return false;
        }

        return false;
    }
}

//////////////////////////////////////////////////////////////////////////////
