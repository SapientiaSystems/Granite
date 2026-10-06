//////////////////////////////////////////////////////////////////////////////
// UserRepository.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.server.domain;

import strata.foundation.core.collection.IMultiMap;
import strata.foundation.core.collection.ListValuedMultiMap;
import strata.server.core.repository.AbstractRepository;
import strata.server.core.unitofwork.IUnitOfWork;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public
class UserRepository
    extends AbstractRepository<Long,User>
    implements IUserRepository
{
    public
    UserRepository(IUnitOfWork uow)
    {
        super(User.class,"primaryId",uow);
        getUnitOfWork()
            .registerQuery(
                "findByFoo",
                "select e from User e where e.foo = :foo")
            .registerQuery(
                "findAllByIdIn",
                "select e from User e where e.primaryId IN :userIds")
            .registerQuery(
                "findAllByFooIn",
                "select e from User e where e.foo IN :foos");

    }

    @Override
    public Map<Long,Optional<User>>
    findAllByIdIn(Set<Long> ids)
    {
        Map<Long,Optional<User>> result =
            getUnitOfWork()
                .findManyByQuery(
                    User.class,
                    "findAllByIdIn",
                    Map.of("userIds",ids))
                .stream()
                .collect(
                    Collectors.toMap(
                        User::getPrimaryId,
                        Optional::ofNullable));

        ids
            .stream()
            .filter(id -> !result.containsKey(id))
            .forEach(id -> result.put(id,Optional.empty()));

        return result;

    }

    @Override
    public List<User>
    findAllByFoo(String foo)
    {
        return
            getUnitOfWork()
                .findManyByQuery(
                    User.class,
                    "findAllByFoo",
                    Map.of("foo",foo))
                .stream()
                .collect(Collectors.toList());
    }

    @Override
    public IMultiMap<String,User>
    findAllByFooIn(Set<String> foos)
    {
        ListValuedMultiMap<String,User> result =
            new ListValuedMultiMap<>();

        getUnitOfWork()
            .findManyByQuery(
                User.class,
                "findAllByFooIn",
                Map.of("foos",foos))
            .forEach(entity -> result.put(entity.getFoo(),entity));

        foos
            .stream()
            .filter(foo -> !result.containsKey(foo))
            .forEach(foo -> result.putAll(foo,List.of()));

        return result;
    }
}

//////////////////////////////////////////////////////////////////////////////
