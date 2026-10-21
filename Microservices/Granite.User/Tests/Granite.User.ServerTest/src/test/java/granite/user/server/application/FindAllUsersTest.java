//////////////////////////////////////////////////////////////////////////////
// FindAllUsersTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.server.application;

import granite.user.server.domain.IUserRepository;
import granite.user.server.domain.User;
import granite.user.service.requestreply.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Tag("CommitStage")
public
class FindAllUsersTest
{
    private IUserRepository  repository;
    private UserQueryService subject;

    @BeforeEach
    public void
    setUp()
    {
        repository = Mockito.mock(IUserRepository.class);
        subject =
            new UserQueryService(
                repository,
                new ModelMapper().registerModule(new MappingModule()));
    }

    @Test
    public void
    findAllUsersReturnsEveryUserOrderedById()
    {
        Mockito
            .when(repository.findAllUsers())
            .thenReturn(
                List.of(
                    new User().setPrimaryId(30L).setUserName("carol@example.com"),
                    new User().setPrimaryId(10L).setUserName("alice@example.com"),
                    new User().setPrimaryId(20L).setUserName("bob@example.com")));

        FindAllUsersReply reply =
            subject
                .findAllUsers(new FindAllUsersRequest())
                .toCompletableFuture()
                .join();

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertEquals(3,reply.getUsers().size());
        assertEquals(10L,reply.getUsers().get(0).getUserId());
        assertEquals(20L,reply.getUsers().get(1).getUserId());
        assertEquals(30L,reply.getUsers().get(2).getUserId());
    }

    @Test
    public void
    findAllUsersOnEmptyRepositoryReturnsEmptyList()
    {
        Mockito.when(repository.findAllUsers()).thenReturn(List.of());

        FindAllUsersReply reply =
            subject
                .findAllUsers(new FindAllUsersRequest())
                .toCompletableFuture()
                .join();

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertNotNull(reply.getUsers());
        assertTrue(reply.getUsers().isEmpty());
    }
}

//////////////////////////////////////////////////////////////////////////////
