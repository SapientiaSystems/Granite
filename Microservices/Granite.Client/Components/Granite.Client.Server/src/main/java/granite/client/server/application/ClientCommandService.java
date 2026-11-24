//////////////////////////////////////////////////////////////////////////////
// ClientCommandService.java
//////////////////////////////////////////////////////////////////////////////

package granite.client.server.application;

import granite.client.server.domain.Client;
import granite.client.server.domain.IClientEvent;
import granite.client.server.domain.IClientEventObserver;
import granite.client.server.domain.IClientRepository;
import granite.client.service.event.ClientEvent;
import granite.client.service.event.IClientEventSender;
import granite.client.service.requestreply.*;
import granite.foundation.core.security.ApplicationRolesManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.modelmapper.ModelMapper;
import strata.foundation.core.concurrent.CurrentThreadExecutor;
import strata.foundation.core.event.EventIdentifiersData;
import strata.foundation.core.event.StandardEventType;
import strata.foundation.core.transfer.ExceptionData;

import java.security.MessageDigest;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

public
class ClientCommandService
    implements ITransactionalClientCommandService,IClientEventObserver
{
    public static final int    MAX_NAME_LENGTH        = 100;
    public static final int    MAX_DESCRIPTION_LENGTH = 1000;
    public static final String NOT_FOUND              = "not found";
    public static final String INVALID_CLIENT         = "invalid client";
    public static final String SUBJECT_PREFIX         = "client:";

    private static final byte[] UNKNOWN_HASH = ClientCredentials.hash("unknown-client");

    private final IClientRepository          repository;
    private final IClientEventSender         sender;
    private final ModelMapper                mapper;
    private final ClientAuthorityPolicy      policy;
    private final Clock                      clock;
    private final Supplier<Optional<String>> callerSubject;
    private final ClientIdGenerator          idGenerator;
    private final Logger                     logger;

    public
    ClientCommandService(
        IClientRepository          r,
        IClientEventSender         s,
        ModelMapper                m,
        ApplicationRolesManager    roles,
        Clock                      c,
        Supplier<Optional<String>> subject)
    {
        this(r,s,m,roles,c,subject,new ClientIdGenerator(ClientIdGenerator.DEFAULT_PREFIX));
    }

    public
    ClientCommandService(
        IClientRepository          r,
        IClientEventSender         s,
        ModelMapper                m,
        ApplicationRolesManager    roles,
        Clock                      c,
        Supplier<Optional<String>> subject,
        ClientIdGenerator          generator)
    {
        repository    = r;
        sender        = s;
        mapper        = m;
        policy        = new ClientAuthorityPolicy(roles);
        clock         = c;
        callerSubject = subject;
        idGenerator   = generator;
        logger        = LogManager.getLogger(ClientCommandService.class);
    }

    @Override
    public CompletionStage<CreateClientReply>
    createClient(CreateClientRequest request)
    {
        logger.debug("createClient({})",request != null ? request.getRequestId() : null);
        return
            CompletableFuture
                .supplyAsync(() -> doCreate(request),new CurrentThreadExecutor())
                .exceptionally(e -> createFailure(request,e));
    }

    @Override
    public CompletionStage<UpdateClientsReply>
    updateClients(UpdateClientsRequest request)
    {
        logger.debug("updateClients({})",request != null ? request.getRequestId() : null);
        return
            CompletableFuture
                .supplyAsync(() -> doUpdate(request),new CurrentThreadExecutor())
                .exceptionally(e -> updateFailure(request,e));
    }

    @Override
    public CompletionStage<RotateClientSecretReply>
    rotateClientSecret(RotateClientSecretRequest request)
    {
        logger.debug("rotateClientSecret({})",request != null ? request.getRequestId() : null);
        return
            CompletableFuture
                .supplyAsync(
                    () ->
                        rotate(
                            new RotateClientSecretReply(request),
                            request.getClientId(),
                            client -> client.setSecretExpiresAt(request.getSecretExpiresAt())),
                    new CurrentThreadExecutor())
                .exceptionally(e -> rotateFailure(new RotateClientSecretReply(request),e));
    }

    @Override
    public CompletionStage<RotateClientSecretReply>
    rotateOwnedClientSecret(RotateOwnedClientSecretRequest request)
    {
        logger.debug("rotateOwnedClientSecret({})",request != null ? request.getRequestId() : null);
        return
            CompletableFuture
                .supplyAsync(
                    () ->
                        rotate(
                            ownedReply(request),
                            request.getClientId(),
                            request.getOwnerUserId()),
                    new CurrentThreadExecutor())
                .exceptionally(e -> rotateFailure(ownedReply(request),e));
    }

    @Override
    public CompletionStage<RotateOwnClientSecretReply>
    rotateOwnClientSecret(RotateOwnClientSecretRequest request)
    {
        logger.debug("rotateOwnClientSecret({})",request != null ? request.getRequestId() : null);
        return
            CompletableFuture
                .supplyAsync(() -> doRotateOwn(request),new CurrentThreadExecutor())
                .exceptionally(
                    e ->
                    {
                        logger.info("<rotateOwnClientSecret> exception reply: {}",rootMessage(e));
                        return
                            new RotateOwnClientSecretReply(request)
                                .setSuccess(false)
                                .setFailureMessage("Unable to rotate client secret");
                    });
    }

    @Override
    public CompletionStage<DestroyClientsReply>
    destroyClients(DestroyClientsRequest request)
    {
        logger.debug("destroyClients({})",request != null ? request.getRequestId() : null);
        return
            CompletableFuture
                .supplyAsync(() -> doDestroy(request),new CurrentThreadExecutor())
                .exceptionally(
                    e ->
                    {
                        logger.info("<destroyClients> exception reply: {}",rootMessage(e));
                        return
                            new DestroyClientsReply(request)
                                .setSuccess(false)
                                .setFailureMessage("Unable to destroy clients: " + rootMessage(e))
                                .setException(ExceptionData.of(e))
                                .setClientIds(List.of());
                    });
    }

    @Override
    public CompletionStage<VerifyClientCredentialsReply>
    verifyClientCredentials(VerifyClientCredentialsRequest request)
    {
        logger.debug("verifyClientCredentials({})",request != null ? request.getRequestId() : null);
        return
            CompletableFuture
                .supplyAsync(() -> doVerify(request),new CurrentThreadExecutor())
                .exceptionally(
                    e ->
                    {
                        logger.info("<verifyClientCredentials> exception reply: {}",rootMessage(e));
                        return
                            new VerifyClientCredentialsReply(request)
                                .setSuccess(false)
                                .setValid(false)
                                .setFailureMessage("unable to verify client");
                    });
    }

    @Override
    public void
    onEvent(IClientEvent event)
    {
        switch (event.getName())
        {
            case "ClientCreated":
                sender.send(toServiceEvent(event,StandardEventType.CREATED));
                break;

            case "ClientUpdated":
                sender.send(toServiceEvent(event,StandardEventType.UPDATED));
                break;

            case "ClientDestroyed":
                sender.send(toServiceEvent(event,StandardEventType.DESTROYED));
                break;

            default:
                logger.error("Encountered unknown event type: {}",event.getName());
                break;
        }
    }

    @Override
    public void
    onException(Exception e)
    {
        logger.error("Observing ClientEvents and caught exception: {}",e.getMessage());
    }

    protected CreateClientReply
    doCreate(CreateClientRequest request)
    {
        Objects.requireNonNull(request,"request");

        CreateClientData data = request.getClient();

        Objects.requireNonNull(data,"validation failed: client was not provided");

        String      name        = validateName(data.getName());
        String      description = validateDescription(data.getDescription());
        Set<String> authorities = policy.validateAdded(Set.of(),data.getAuthorities());

        if (repository.findByName(name).isPresent())
            throw new IllegalArgumentException("validation failed: name is already in use");

        String secret = ClientCredentials.newSecret();
        Client client =
            new Client()
                .setClientId(idGenerator.newClientId())
                .setName(name)
                .setDescription(description)
                .setAuthorities(authorities)
                .setOwners(data.getOwners())
                .setEnabled(data.isEnabled())
                .setSecretSha256(ClientCredentials.hash(secret))
                .setSecretExpiresAt(data.getSecretExpiresAt());

        repository.save(client).attach(this).notifyCreated();
        logger.info("<createClient> created client {}",client.getClientId());
        return
            new CreateClientReply(request)
                .setSuccess(true)
                .setSuccessMessage("Client: " + client.getClientId() + " created")
                .setClient(mapper.map(client,ClientData.class))
                .setClientSecret(secret);
    }

    protected UpdateClientsReply
    doUpdate(UpdateClientsRequest request)
    {
        Objects.requireNonNull(request,"request");
        Objects.requireNonNull(request.getClients(),"validation failed: clients were not provided");

        if (request.getClients().isEmpty())
            throw new IllegalArgumentException("validation failed: clients were empty");

        List<Client>   targets = new ArrayList<>();
        List<Runnable> changes = new ArrayList<>();
        Set<String>    names   = new LinkedHashSet<>();

        for (UpdateClientData data : request.getClients())
        {
            Objects.requireNonNull(data.getClientId(),"validation failed: clientId was not provided");

            Client client =
                repository
                    .findByClientId(data.getClientId())
                    .orElseThrow(() -> new IllegalArgumentException(NOT_FOUND));

            if (data.getVersion() != null
                && client.getVersion() != null
                && data.getVersion().longValue() != client.getVersion().longValue())
                throw new IllegalArgumentException("validation failed: client was changed by someone else");

            String      name        = validateName(data.getName());
            String      description = validateDescription(data.getDescription());
            Set<String> authorities = policy.validateAdded(client.getAuthorities(),data.getAuthorities());

            if (!names.add(name))
                throw new IllegalArgumentException("validation failed: name is repeated");

            Optional<Client> sameName = repository.findByName(name);

            if (sameName.isPresent() && !sameName.get().getClientId().equals(client.getClientId()))
                throw new IllegalArgumentException("validation failed: name is already in use");

            targets.add(client);
            changes.add(
                () ->
                    client
                        .setName(name)
                        .setDescription(description)
                        .setAuthorities(authorities)
                        .setOwners(data.getOwners())
                        .setEnabled(data.isEnabled())
                        .setSecretExpiresAt(data.getSecretExpiresAt()));
        }

        changes.forEach(Runnable::run);

        List<ClientData> updated = new ArrayList<>();

        for (Client client : targets)
            updated.add(mapper.map(repository.save(client).attach(this).notifyUpdated(),ClientData.class));

        return
            new UpdateClientsReply(request)
                .setSuccess(true)
                .setSuccessMessage("Updated " + updated.size() + " client(s)")
                .setClients(updated);
    }

    protected RotateClientSecretReply
    rotate(RotateClientSecretReply reply,String clientId,java.util.function.Consumer<Client> extra)
    {
        Optional<Client> found = clientId == null ? Optional.empty() : repository.findByClientId(clientId);

        if (found.isEmpty())
            return notFound(reply);

        return completeRotation(reply,found.get(),extra);
    }

    protected RotateClientSecretReply
    rotate(RotateClientSecretReply reply,String clientId,Long ownerUserId)
    {
        Optional<Client> found = clientId == null ? Optional.empty() : repository.findByClientId(clientId);

        if (found.isEmpty() || !found.get().isOwnedBy(ownerUserId))
            return notFound(reply);

        return completeRotation(reply,found.get(),client -> {});
    }

    protected RotateOwnClientSecretReply
    doRotateOwn(RotateOwnClientSecretRequest request)
    {
        RotateOwnClientSecretReply reply   = new RotateOwnClientSecretReply(request);
        String                     subject = callerSubject.get().orElse(null);

        if (subject == null || !subject.startsWith(SUBJECT_PREFIX))
            return reply.setSuccess(false).setFailureMessage(NOT_FOUND);

        Optional<Client> found = repository.findByClientId(subject.substring(SUBJECT_PREFIX.length()));

        if (found.isEmpty())
            return reply.setSuccess(false).setFailureMessage(NOT_FOUND);

        Client client = found.get();
        String secret = ClientCredentials.newSecret();

        client.setSecretSha256(ClientCredentials.hash(secret));
        repository.save(client).attach(this).notifyUpdated();
        logger.info("<rotateOwnClientSecret> rotated secret of client {}",client.getClientId());
        return
            reply
                .setSuccess(true)
                .setSuccessMessage("Client secret rotated")
                .setClientId(client.getClientId())
                .setClientSecret(secret);
    }

    protected DestroyClientsReply
    doDestroy(DestroyClientsRequest request)
    {
        Objects.requireNonNull(request,"request");
        Objects.requireNonNull(request.getClientIds(),"validation failed: clientIds were not provided");

        if (request.getClientIds().isEmpty())
            throw new IllegalArgumentException("validation failed: clientIds were empty");

        List<String> destroyed = new ArrayList<>();

        for (String clientId : request.getClientIds())
        {
            Optional<Client> found = clientId == null ? Optional.empty() : repository.findByClientId(clientId);

            if (found.isEmpty())
                continue;

            Client client = found.get().attach(this);

            repository.delete(client);
            client.notifyDestroyed();
            destroyed.add(clientId);
        }

        boolean all = destroyed.size() == request.getClientIds().size();

        return
            new DestroyClientsReply(request)
                .setSuccess(all)
                .setSuccessMessage(all ? "Destroyed " + destroyed.size() + " client(s)" : null)
                .setFailureMessage(all ? null : "Unable to destroy " + (request.getClientIds().size() - destroyed.size()) + " client(s)")
                .setClientIds(destroyed);
    }

    protected VerifyClientCredentialsReply
    doVerify(VerifyClientCredentialsRequest request)
    {
        Objects.requireNonNull(request,"request");

        VerifyClientCredentialsReply reply    = new VerifyClientCredentialsReply(request);
        String                       clientId = request.getClientId();
        String                       secret   = request.getClientSecret() == null ? "" : request.getClientSecret();
        Optional<Client>             found    = clientId == null ? Optional.empty() : repository.findByClientId(clientId);
        Instant                      now      = Instant.now(clock);
        byte[]                       stored   = found.map(Client::getSecretSha256).orElse(UNKNOWN_HASH);
        boolean                      matches  = MessageDigest.isEqual(stored == null ? UNKNOWN_HASH : stored,ClientCredentials.hash(secret));
        boolean                      valid    =
            found.isPresent()
            && found.get().getSecretSha256() != null
            && matches
            && found.get().isEnabled()
            && found.get().isSecretCurrent(now);

        if (!valid)
            return reply.setSuccess(false).setValid(false).setFailureMessage(INVALID_CLIENT);

        Client client = found.get();

        recordTokenIssued(client.getClientId(),now);
        return
            reply
                .setSuccess(true)
                .setValid(true)
                .setClientId(client.getClientId())
                .setAuthorities(policy.mintable(client.getAuthorities()));
    }

    private void
    recordTokenIssued(String clientId,Instant at)
    {
        try
        {
            repository.recordTokenIssued(clientId,at);
        }
        catch (RuntimeException e)
        {
            logger.debug("Unable to record token issue time: {}",e.getClass().getSimpleName());
        }
    }

    private RotateClientSecretReply
    completeRotation(RotateClientSecretReply reply,Client client,java.util.function.Consumer<Client> extra)
    {
        String secret = ClientCredentials.newSecret();

        client.setSecretSha256(ClientCredentials.hash(secret));
        extra.accept(client);
        repository.save(client).attach(this).notifyUpdated();
        logger.info("<rotateClientSecret> rotated secret of client {}",client.getClientId());
        return
            reply
                .setSuccess(true)
                .setSuccessMessage("Client secret rotated")
                .setClient(mapper.map(client,ClientData.class))
                .setClientSecret(secret);
    }

    private RotateClientSecretReply
    ownedReply(RotateOwnedClientSecretRequest request)
    {
        RotateClientSecretReply reply = new RotateClientSecretReply();

        if (request != null)
            reply.setOriginatingRequestId(request.getRequestId());

        return reply;
    }

    private RotateClientSecretReply
    notFound(RotateClientSecretReply reply)
    {
        return reply.setSuccess(false).setFailureMessage(NOT_FOUND);
    }

    private RotateClientSecretReply
    rotateFailure(RotateClientSecretReply reply,Throwable e)
    {
        logger.info("<rotateClientSecret> exception reply: {}",rootMessage(e));
        return
            reply
                .setSuccess(false)
                .setFailureMessage("Unable to rotate client secret: " + rootMessage(e))
                .setException(ExceptionData.of(e));
    }

    private CreateClientReply
    createFailure(CreateClientRequest request,Throwable e)
    {
        logger.info("<createClient> exception reply: {}",rootMessage(e));
        return
            new CreateClientReply(request)
                .setSuccess(false)
                .setFailureMessage("Unable to create client: " + rootMessage(e))
                .setException(ExceptionData.of(e));
    }

    private UpdateClientsReply
    updateFailure(UpdateClientsRequest request,Throwable e)
    {
        logger.info("<updateClients> exception reply: {}",rootMessage(e));
        return
            new UpdateClientsReply(request)
                .setSuccess(false)
                .setFailureMessage("Unable to update clients: " + rootMessage(e))
                .setException(ExceptionData.of(e))
                .setClients(List.of());
    }

    private String
    validateName(String name)
    {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("validation failed: name was blank");

        String trimmed = name.trim();

        if (trimmed.length() > MAX_NAME_LENGTH)
            throw new IllegalArgumentException("validation failed: name is longer than " + MAX_NAME_LENGTH + " characters");

        return trimmed;
    }

    private String
    validateDescription(String description)
    {
        if (description != null && description.length() > MAX_DESCRIPTION_LENGTH)
            throw new IllegalArgumentException("validation failed: description is longer than " + MAX_DESCRIPTION_LENGTH + " characters");

        return description;
    }

    private String
    rootMessage(Throwable e)
    {
        Throwable cause = e;

        while (cause.getCause() != null && cause != cause.getCause())
            cause = cause.getCause();

        return cause.getMessage() == null ? cause.getClass().getSimpleName() : cause.getMessage();
    }

    private ClientEvent
    toServiceEvent(IClientEvent domainEvent,StandardEventType eventType)
    {
        return
            ClientEvent
                .newBuilder()
                .setEventType(eventType)
                .setIdentifiers(
                    EventIdentifiersData
                        .newBuilder()
                        .setEventId(UUID.randomUUID().toString())
                        .setCorrelationId(domainEvent.getCorrelationId())
                        .setTimestamp(domainEvent.getTimestamp())
                        .build())
                .setSourceType(domainEvent.getSource().getClass().getCanonicalName())
                .setSourceId(domainEvent.getSource().getPrimaryId())
                .build();
    }
}

//////////////////////////////////////////////////////////////////////////////
