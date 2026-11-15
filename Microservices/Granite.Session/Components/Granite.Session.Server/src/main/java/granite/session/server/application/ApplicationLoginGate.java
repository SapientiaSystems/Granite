//////////////////////////////////////////////////////////////////////////////
// ApplicationLoginGate.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import granite.foundation.core.security.ApplicationRolesManager;
import granite.session.server.domain.Session;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public
class ApplicationLoginGate
    implements IApplicationLoginGate
{
    public static final String       CLIENT_ADMIN_APPLICATIONS_PROPERTY = "granite.client.admin-applications";
    public static final List<String> DEFAULT_CLIENT_ADMIN_APPLICATIONS  = List.of("client");

    private final Set<String>           applications;
    private final Set<String>           clientAdminApplications;
    private final ILoginGate            graniteGate;
    private final Map<String,ILoginGate> gates;

    public
    ApplicationLoginGate(
        Collection<String>      applications,
        ILoginGate              graniteGate,
        Map<String,ILoginGate>  gates)
    {
        this(applications,DEFAULT_CLIENT_ADMIN_APPLICATIONS,graniteGate,gates);
    }

    public
    ApplicationLoginGate(
        Collection<String>      applications,
        Collection<String>      clientAdminApplications,
        ILoginGate              graniteGate,
        Map<String,ILoginGate>  gates)
    {
        Objects.requireNonNull(applications,"applications");
        Objects.requireNonNull(clientAdminApplications,"clientAdminApplications");
        Objects.requireNonNull(graniteGate,"graniteGate");
        Objects.requireNonNull(gates,"gates");

        this.applications            = names(applications);
        this.clientAdminApplications = names(clientAdminApplications);
        this.graniteGate             = graniteGate;
        this.gates                   = Map.copyOf(gates);
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

        if (clientAdminApplications.contains(name))
            return
                authorities.contains(ApplicationRolesManager.CLIENT_ADMIN)
                && gates
                    .getOrDefault(name,new RequiredAuthoritiesLoginGate(List.of(ApplicationRolesManager.CLIENT_ADMIN)))
                    .permits(authorities);

        ILoginGate own = Session.DEFAULT_APPLICATION.equals(name) ? null : gates.get(name);

        if (own != null)
            return own.permits(authorities);

        Set<String> kept = withoutClientAdmin(authorities);

        return (!kept.isEmpty() || !authorities.contains(ApplicationRolesManager.CLIENT_ADMIN)) && graniteGate.permits(kept);
    }

    private Set<String>
    names(Collection<String> values)
    {
        Set<String> result = new LinkedHashSet<>();

        for (String value : values)
            if (value != null && !value.isBlank())
                result.add(value.trim());

        return Set.copyOf(result);
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
