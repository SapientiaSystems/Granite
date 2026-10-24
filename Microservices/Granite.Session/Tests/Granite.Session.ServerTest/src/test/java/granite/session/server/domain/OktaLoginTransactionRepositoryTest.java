//////////////////////////////////////////////////////////////////////////////
// OktaLoginTransactionRepositoryTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.domain;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import strata.server.spring.inject.RequestAttributeMap;
import strata.server.spring.unitofwork.ISpringUnitOfWorkManager;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TestConfiguration.class})
public
class OktaLoginTransactionRepositoryTest
{
    @Autowired
    private IOktaLoginTransactionRepository target;

    @Autowired
    private ISpringUnitOfWorkManager unitOfWorkManager;

    private TransactionTemplate transaction;

    @BeforeEach
    public void
    setUp()
    {
        RequestContextHolder.setRequestAttributes(new RequestAttributeMap());
        transaction = new TransactionTemplate(unitOfWorkManager);
    }

    @AfterEach
    public void
    tearDown()
    {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    public void
    savedTransactionIsFoundByHashWithAllFields()
    {
        String  hash    = hash();
        Instant expires = Instant.parse("2026-10-24T12:05:00Z");

        transaction.executeWithoutResult(status -> target.save(newTransaction(hash,expires)));

        OktaLoginTransaction found =
            transaction
                .execute(status -> target.findByHashedTransactionId(hash))
                .orElseThrow();

        assertNotNull(found.getPrimaryId());
        assertEquals("state-1",found.getState());
        assertEquals("nonce-1",found.getNonce());
        assertEquals("verifier-1",found.getCodeVerifier());
        assertEquals("/users",found.getReturnTo());
        assertEquals("device-1",found.getDeviceId());
        assertEquals(expires,found.getExpires());
    }

    @Test
    public void
    unknownHashIsEmpty()
    {
        Optional<OktaLoginTransaction> found =
            transaction.execute(status -> target.findByHashedTransactionId(hash()));

        assertTrue(found.isEmpty());
    }

    @Test
    public void
    deletedTransactionIsGone()
    {
        String hash = hash();

        transaction.executeWithoutResult(status -> target.save(newTransaction(hash,Instant.now().plusSeconds(300))));
        transaction.executeWithoutResult(
            status -> target.delete(target.findByHashedTransactionId(hash).orElseThrow()));

        assertTrue(transaction.execute(status -> target.findByHashedTransactionId(hash)).isEmpty());
    }

    @Test
    public void
    findExpiredBeforeReturnsOnlyExpired()
    {
        String  expired = hash();
        String  live    = hash();
        Instant cutoff  = Instant.parse("2030-01-01T00:00:00Z");

        transaction.executeWithoutResult(
            status ->
            {
                target.save(newTransaction(expired,cutoff.minusSeconds(1)));
                target.save(newTransaction(live,cutoff.plusSeconds(300)));
            });

        List<String> hashes =
            transaction
                .execute(status -> target.findExpiredBefore(cutoff))
                .stream()
                .map(OktaLoginTransaction::getHashedTransactionId)
                .toList();

        assertTrue(hashes.contains(expired));
        assertTrue(!hashes.contains(live));
    }

    @Test
    public void
    duplicateHashIsRejected()
    {
        String hash = hash();

        transaction.executeWithoutResult(status -> target.save(newTransaction(hash,Instant.now().plusSeconds(300))));

        assertThrows(
            RuntimeException.class,
            () ->
                transaction.executeWithoutResult(
                    status -> target.save(newTransaction(hash,Instant.now().plusSeconds(300)))));
    }

    private OktaLoginTransaction
    newTransaction(String hash,Instant expires)
    {
        return
            new OktaLoginTransaction()
                .setHashedTransactionId(hash)
                .setState("state-1")
                .setNonce("nonce-1")
                .setCodeVerifier("verifier-1")
                .setReturnTo("/users")
                .setDeviceId("device-1")
                .setExpires(expires);
    }

    private String
    hash()
    {
        return (UUID.randomUUID().toString() + UUID.randomUUID()).replace("-","").substring(0,64);
    }
}

//////////////////////////////////////////////////////////////////////////////
