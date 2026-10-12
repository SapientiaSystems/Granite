//////////////////////////////////////////////////////////////////////////////
// FindUsersByExternalIdentityTest.java
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
class FindUsersByExternalIdentityTest
{
    private static final Long   USER_ID     = 101L;
    private static final String PROVIDER    = "google";
    private static final String EXTERNAL_ID = "1234567890";
    private static final String COMPOSITE   = "google:1234567890";

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
    returnsOneUserWhenExternalIdentityMatches()
    {
        Mockito
            .when(repository.findAllByExternalIdentity(COMPOSITE))
            .thenReturn(
                List.of(
                    new User()
                        .setPrimaryId(USER_ID)
                        .setUserName("alice@example.com")
                        .setExternalIdentity(COMPOSITE)));

        FindUsersByExternalIdentityReply reply =
            subject
                .findUsersByExternalIdentity(
                    new FindUsersByExternalIdentityRequest()
                        .setProvider(PROVIDER)
                        .setExternalId(EXTERNAL_ID))
                .toCompletableFuture()
                .join();

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertEquals(1,reply.getUsers().size());
        assertEquals(USER_ID,reply.getUsers().get(0).getUserId());
    }

    @Test
    public void
    returnsEmptyListWhenNoExternalIdentityMatches()
    {
        Mockito
            .when(repository.findAllByExternalIdentity(COMPOSITE))
            .thenReturn(List.of());

        FindUsersByExternalIdentityReply reply =
            subject
                .findUsersByExternalIdentity(
                    new FindUsersByExternalIdentityRequest()
                        .setProvider(PROVIDER)
                        .setExternalId(EXTERNAL_ID))
                .toCompletableFuture()
                .join();

        assertTrue(reply.isSuccess(),reply.getFailureMessage());
        assertNotNull(reply.getUsers());
        assertTrue(reply.getUsers().isEmpty());
    }

    @Test
    public void
    failsValidationWhenExternalIdIsNull()
    {
        FindUsersByExternalIdentityReply reply =
            subject
                .findUsersByExternalIdentity(
                    new FindUsersByExternalIdentityRequest()
                        .setProvider(PROVIDER)
                        .setExternalId(null))
                .toCompletableFuture()
                .join();

        assertFalse(reply.isSuccess());
        assertNotNull(reply.getFailureMessage());
        Mockito.verifyNoInteractions(repository);
    }
}

//////////////////////////////////////////////////////////////////////////////
