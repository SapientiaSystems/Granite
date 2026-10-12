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
                "findAllByIdIn",
                "select e from User e where e.primaryId IN :userIds")
            .registerQuery(
                "findByUserName",
                "select e from User e where e.userName = :userName")
            .registerQuery(
                "findAllByUserNameIn",
                "select e from User e where e.userName IN :userNames")
            .registerQuery(
                "findAllByExternalIdentity",
                "select e from User e where e.externalIdentity = :externalIdentity");

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
    public Optional<User>
    findByUserName(String userName)
    {
        return
            getUnitOfWork()
                .findOneByQuery(
                    User.class,
                    "findByUserName",
                    Map.of("userName",userName));
    }

    @Override
    public List<User>
    findAllByExternalIdentity(String externalIdentity)
    {
        return
            getUnitOfWork()
                .findManyByQuery(
                    User.class,
                    "findAllByExternalIdentity",
                    Map.of("externalIdentity",externalIdentity))
                .stream()
                .collect(Collectors.toList());
    }

    @Override
    public IMultiMap<String,User>
    findAllByUserNameIn(Set<String> userNames)
    {
        ListValuedMultiMap<String,User> result =
            new ListValuedMultiMap<>();

        getUnitOfWork()
            .findManyByQuery(
                User.class,
                "findAllByUserNameIn",
                Map.of("userNames",userNames))
            .forEach(entity -> result.put(entity.getUserName(),entity));

        userNames
            .stream()
            .filter(userName -> !result.containsKey(userName))
            .forEach(userName -> result.putAll(userName,List.of()));

        return result;
    }
}

//////////////////////////////////////////////////////////////////////////////
