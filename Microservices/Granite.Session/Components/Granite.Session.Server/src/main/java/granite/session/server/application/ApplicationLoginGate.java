//////////////////////////////////////////////////////////////////////////////
// ApplicationLoginGate.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import granite.foundation.core.security.ApplicationRolesManager;
import granite.session.server.domain.Session;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public
class ApplicationLoginGate
    implements IApplicationLoginGate
{
    private final Set<String>           applications;
    private final ILoginGate            graniteGate;
    private final Map<String,ILoginGate> gates;

    public
    ApplicationLoginGate(
        Collection<String>      applications,
        ILoginGate              graniteGate,
        Map<String,ILoginGate>  gates)
    {
        Objects.requireNonNull(applications,"applications");
        Objects.requireNonNull(graniteGate,"graniteGate");
        Objects.requireNonNull(gates,"gates");

        Set<String> known = new LinkedHashSet<>();

        for (String application : applications)
            if (application != null && !application.isBlank())
                known.add(application.trim());

        this.applications = Set.copyOf(known);
        this.graniteGate  = graniteGate;
        this.gates        = Map.copyOf(gates);
    }

    @Override
    public boolean
    isKnown(String application)
    {
        return applications.contains(application == null ? Session.DEFAULT_APPLICATION : application);
    }

    @Override
    public boolean
    permits(String application,Collection<String> authorities)
    {
        String name = application == null ? Session.DEFAULT_APPLICATION : application;

        if (!applications.contains(name) || authorities == null)
            return false;

        if (Session.DEFAULT_APPLICATION.equals(name))
        {
            Set<String> kept = withoutClientAdmin(authorities);

            return (!kept.isEmpty() || !authorities.contains(ApplicationRolesManager.CLIENT_ADMIN)) && graniteGate.permits(kept);
        }

        return gates.getOrDefault(name,graniteGate).permits(authorities);
    }

    private Set<String>
    withoutClientAdmin(Collection<String> authorities)
    {
        Set<String> kept = new LinkedHashSet<>(authorities);

        kept.remove(ApplicationRolesManager.CLIENT_ADMIN);
        return kept;
    }
}

//////////////////////////////////////////////////////////////////////////////
