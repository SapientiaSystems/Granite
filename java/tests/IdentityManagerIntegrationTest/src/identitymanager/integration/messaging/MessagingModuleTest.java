package identitymanager.integration.messaging;

import static org.junit.Assert.*;
import identitymanager.integration.mapper.MapperModule;
import strata1.injector.container.Container;
import strata1.injector.container.IContainer;
import strata1.injector.property.PropertiesModule;
import strata1.integrator.messaging.IMessagingSession;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public abstract
class MessagingModuleTest
{
    private MessagingModule itsTarget;

    @Before
    public void 
    setUp() throws Exception
    {
        itsTarget = createTarget();
    }

    @After
    public void 
    tearDown() throws Exception
    {
        itsTarget = null;
    }

    @Test
    public void 
    testInitialize()
    {
        IContainer container = new Container();
        
        new PropertiesModule( createProperties() ).initialize( container );
        new MapperModule().initialize( container );
        itsTarget.initialize( container );
        
        assertNotNull( 
            container.getInstance( 
                IMessagingSession.class,
                "CommandSession1" ) );
        assertNotNull( 
            container.getInstance( 
                IMessagingSession.class,
                "EventSession1" ) );
        assertNotNull( 
            container.getInstance( 
                IMessagingSession.class,
                "CommandSession2" ) );
        assertNotNull( 
            container.getInstance( 
                IMessagingSession.class,
                "EventSession2" ) );
    }

    private Properties
    createProperties()
    {
        Properties  properties = new Properties();
        InputStream input = 
            this
                .getClass()
                .getResourceAsStream( 
                    "/identitymanager/integration/messaging/identitymanagerintegrationtest.properties" );
        
        try
        {
            if ( input == null )
                throw new IllegalStateException( "input stream is null" );
            
            properties.load( input );
        }
        catch (IOException e)
        {
            throw new IllegalStateException(e);
        }
        
        return properties;
    }
    

    protected abstract MessagingModule 
    createTarget();
}

// ##########################################################################
