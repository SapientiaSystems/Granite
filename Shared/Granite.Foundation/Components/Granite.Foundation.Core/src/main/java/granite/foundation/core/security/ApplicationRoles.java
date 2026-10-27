//////////////////////////////////////////////////////////////////////////////
// ApplicationRoles.java
//////////////////////////////////////////////////////////////////////////////

package granite.foundation.core.security;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

public
class ApplicationRoles
{
    public static final String ADMIN = "ROLE_ADMIN";

    private static final Pattern NAME_PATTERN = Pattern.compile("ROLE_[A-Z0-9_]+");

    private final List<RoleDefinition>       roles;
    private final Map<String,RoleDefinition> byName;
    private final String                     defaultRole;

    public
    ApplicationRoles(List<RoleDefinition> applicationRoles,String defaultRole)
    {
        Objects.requireNonNull(applicationRoles,"applicationRoles");
        Objects.requireNonNull(defaultRole,"defaultRole");

        List<RoleDefinition>       all   = new ArrayList<>();
        Map<String,RoleDefinition> map   = new HashMap<>();
        RoleDefinition             admin = new RoleDefinition(ADMIN,"Admin",Set.of());

        all.add(admin);
        map.put(ADMIN,admin);

        for (RoleDefinition role : applicationRoles)
        {
            if (!NAME_PATTERN.matcher(role.getName()).matches())
                throw new IllegalArgumentException("role name must match ROLE_[A-Z0-9_]+: " + role.getName());

            if (ADMIN.equals(role.getName()))
                throw new IllegalArgumentException(ADMIN + " is reserved");

            if (map.put(role.getName(),role) != null)
                throw new IllegalArgumentException("duplicate role " + role.getName());

            all.add(role);
        }

        for (RoleDefinition role : applicationRoles)
            for (String implied : role.getImpliedRoles())
            {
                if (ADMIN.equals(implied))
                    throw new IllegalArgumentException(role.getName() + " must not imply " + ADMIN);

                if (!map.containsKey(implied))
                    throw new IllegalArgumentException(role.getName() + " implies unknown role " + implied);
            }

        this.roles       = List.copyOf(all);
        this.byName      = map;
        this.defaultRole = defaultRole;

        for (RoleDefinition role : applicationRoles)
            if (reachesSelf(role.getName()))
                throw new IllegalArgumentException("implication cycle at " + role.getName());

        if (ADMIN.equals(defaultRole) || !map.containsKey(defaultRole))
            throw new IllegalArgumentException("default role " + defaultRole + " is not an application role");
    }

    public List<RoleDefinition>
    getRoles() { return roles; }

    public List<String>
    getRoleNames() { return roles.stream().map(RoleDefinition::getName).toList(); }

    public String
    getDefaultRole() { return defaultRole; }

    public boolean
    isRole(String name) { return byName.containsKey(name); }

    public Set<String>
    expand(Set<String> authorities)
    {
        if (authorities == null)
            return new LinkedHashSet<>();

        Set<String> result = new LinkedHashSet<>(authorities);

        for (String authority : authorities)
            if (byName.containsKey(authority))
                result.addAll(closure(authority));

        return result;
    }

    public Set<String>
    dependentsOf(String role)
    {
        Set<String> result = new LinkedHashSet<>();

        for (RoleDefinition candidate : roles)
            if (!candidate.getName().equals(role) && closure(candidate.getName()).contains(role))
                result.add(candidate.getName());

        return result;
    }

    private Set<String>
    closure(String role)
    {
        Set<String>   result = new LinkedHashSet<>();
        List<String>  queue  = new ArrayList<>(List.of(role));

        while (!queue.isEmpty())
        {
            String current = queue.remove(queue.size() - 1);

            if (result.add(current))
                queue.addAll(byName.get(current).getImpliedRoles());
        }

        return result;
    }

    private boolean
    reachesSelf(String role)
    {
        Set<String>  seen  = new HashSet<>();
        List<String> queue = new ArrayList<>(byName.get(role).getImpliedRoles());

        while (!queue.isEmpty())
        {
            String current = queue.remove(queue.size() - 1);

            if (current.equals(role))
                return true;

            if (seen.add(current))
                queue.addAll(byName.get(current).getImpliedRoles());
        }

        return false;
    }
}

//////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////
