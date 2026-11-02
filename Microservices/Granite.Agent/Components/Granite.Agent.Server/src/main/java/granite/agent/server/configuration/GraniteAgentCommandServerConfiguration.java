//////////////////////////////////////////////////////////////////////////////
// GraniteAgentCommandServerConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.agent.server.configuration;

import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import granite.agent.server.agent.AgentServiceTokenSource;
import granite.agent.server.agent.AgentSystemPrompt;
import granite.agent.server.agent.AgentTurnRunner;
import granite.agent.server.agent.ClaudeAgentProvider;
import granite.agent.server.agent.GetHelpArticleTool;
import granite.agent.server.agent.IAgentProvider;
import granite.agent.server.agent.IAgentTool;
import granite.agent.server.agent.IDelegatedTokenSource;
import granite.agent.server.agent.SearchHelpContentTool;
import granite.agent.server.agent.SessionDelegatedTokenSource;
import granite.agent.server.agent.UnavailableAgentProvider;
import granite.agent.server.application.AgentCommandService;
import granite.agent.server.application.ITransactionalAgentCommandService;
import granite.agent.server.domain.IConversationRepository;
import granite.agent.server.platform.CommandPlatformConfiguration;
import granite.agent.server.platform.OutboxEventAgentEventSender;
import granite.agent.service.event.IAgentEventSender;
import granite.content.client.requestreply.ContentQueryServiceClient;
import granite.content.client.requestreply.IContentQueryServiceClient;
import granite.session.client.requestreply.ISessionCommandServiceClient;
import granite.session.client.requestreply.SessionCommandServiceClient;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import org.apache.logging.log4j.LogManager;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.web.context.annotation.RequestScope;
import strata.outbox.core.repository.IOutboxEventRepository;

import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;

@Configuration
@Import({CommandPlatformConfiguration.class})
public
class GraniteAgentCommandServerConfiguration
    extends AbstractGraniteAgentServerConfiguration
{
    public static final String CLIENT_ID     = "granite.agent.service-client.id";
    public static final String CLIENT_SECRET = "granite.agent.service-client.secret";
    public static final String SSL_BUNDLE    = "granite.agent.ssl-bundle";

    @Autowired
    private Environment                environment;

    @Autowired
    private ObjectProvider<SslBundles> sslBundles;

    @Bean
    @RequestScope
    public IAgentEventSender
    agentEventSender(IOutboxEventRepository repository)
    {
        return new OutboxEventAgentEventSender(repository);
    }

    @Bean
    @Scope("singleton")
    public IAgentProvider
    agentProvider(Environment environment)
    {
        String key = environment.getProperty("granite.agent.claude.api-key","");

        if (key.isBlank())
        {
            LogManager.getLogger(getClass()).warn("granite.agent.claude.api-key is not set; the virtual assistant is unavailable");
            return new UnavailableAgentProvider();
        }

        return
            new ClaudeAgentProvider(
                AnthropicOkHttpClient
                    .builder()
                    .apiKey(key)
                    .timeout(Duration.parse(environment.getProperty("granite.agent.claude.timeout","PT2M")))
                    .maxRetries(environment.getProperty("granite.agent.claude.max-retries",Integer.class,2))
                    .build(),
                environment.getProperty("granite.agent.claude.model",ClaudeAgentProvider.DEFAULT_MODEL),
                environment.getProperty("granite.agent.claude.max-tokens",Long.class,ClaudeAgentProvider.DEFAULT_MAX_TOKENS),
                environment.getProperty("granite.agent.claude.effort","medium"));
    }

    @Bean
    @Lazy
    @Scope("singleton")
    public Supplier<String>
    agentServiceTokenSource(Environment environment)
    {
        return
            new AgentServiceTokenSource(
                () -> new SessionCommandServiceClient(createClientBuilder(),environment.getProperty("session-command-service.base-url")),
                Clock.systemUTC(),
                environment.getProperty(CLIENT_ID,"granite-agent"),
                environment.getProperty(CLIENT_SECRET,""));
    }

    @Bean
    @Scope("singleton")
    public IDelegatedTokenSource
    delegatedTokenSource(Environment environment,@Lazy Supplier<String> agentServiceTokenSource)
    {
        return
            new SessionDelegatedTokenSource(
                () -> new SessionCommandServiceClient(createServiceClientBuilder(agentServiceTokenSource),environment.getProperty("session-command-service.base-url")),
                environment.getProperty(CLIENT_ID,"granite-agent"),
                environment.getProperty(CLIENT_SECRET,""));
    }

    @Bean
    @Scope("singleton")
    public AgentTurnRunner
    agentTurnRunner(IAgentProvider agentProvider,Clock clock,Environment environment,@Lazy Supplier<String> agentServiceTokenSource)
    {
        return
            new AgentTurnRunner(
                agentProvider,
                agentTools(environment,agentServiceTokenSource),
                agentSystemPrompt(),
                clock,
                environment.getProperty("granite.agent.max-steps",Integer.class,AgentTurnRunner.DEFAULT_MAX_STEPS));
    }

    @Bean
    @RequestScope
    public ITransactionalAgentCommandService
    agentCommandService(
        IConversationRepository repository,
        IAgentEventSender       sender,
        ModelMapper             mapper,
        Clock                   clock,
        AgentTurnRunner         agentTurnRunner,
        IDelegatedTokenSource   delegatedTokenSource)
    {
        return new AgentCommandService(repository,sender,mapper,clock,agentTurnRunner,delegatedTokenSource);
    }

    protected List<IAgentTool>
    agentTools(Environment environment,Supplier<String> serviceToken)
    {
        Supplier<IContentQueryServiceClient> content =
            () -> new ContentQueryServiceClient(createServiceClientBuilder(serviceToken),environment.getProperty("content-query-service.base-url"));

        return List.of(new SearchHelpContentTool(content),new GetHelpArticleTool(content));
    }

    protected String
    agentSystemPrompt()
    {
        return AgentSystemPrompt.GRANITE;
    }

    protected ClientBuilder
    createServiceClientBuilder(Supplier<String> serviceToken)
    {
        ClientRequestFilter bearer = context -> context.getHeaders().putSingle(HttpHeaders.AUTHORIZATION,"Bearer " + serviceToken.get());

        return createClientBuilder().register(bearer);
    }

    protected ClientBuilder
    createClientBuilder()
    {
        SslBundles bundles = sslBundles == null ? null : sslBundles.getIfAvailable();

        if (bundles == null)
            return ClientBuilder.newBuilder();

        return
            ClientBuilder
                .newBuilder()
                .sslContext(bundles.getBundle(environment.getProperty(SSL_BUNDLE,"granite-services")).createSslContext());
    }

    @Override
    protected String
    getServicePathPattern() { return "/agent-command-service/**"; }
}

//////////////////////////////////////////////////////////////////////////////
