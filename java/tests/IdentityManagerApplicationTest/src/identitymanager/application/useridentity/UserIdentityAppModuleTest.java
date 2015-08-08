package identitymanager.application.useridentity;

import static org.junit.Assert.*;
import identitymanager.service.useridentity.IUserIdentityService;
import identitymanager.domain.useridentity.UserIdentityDomainModule;
import identitymanager.serverplatform.hibernatepersistence.HibernatePersistenceModule;
import identitymanager.serverplatform.javamail.JavaMailModule;
import identitymanager.common.utility.UtilityModule;
import strata1.common.logger.ILogger;
import strata1.common.logger.Logger;
import strata1.common.logger.PrintWriterLogEntryProcessor;
import strata1.injector.container.Binder;
import strata1.injector.container.Container;
import strata1.injector.container.IContainer;
import strata1.injector.property.PropertiesModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Properties;

public 
class UserIdentityAppModuleTest
{
    private IContainer            itsContainer;
    private UserIdentityAppModule itsTarget;
    
    @Before
    public void 
    setUp() throws Exception
    {
        itsContainer = new Container();
        itsTarget    = new UserIdentityAppModule();
        
        itsContainer.insertBinding( Binder.bindType( ILogger.class ).toInstance(createLogger()));
        
        new PropertiesModule(createProperties()).initialize(itsContainer);
        new HibernatePersistenceModule().initialize( itsContainer );
        new UtilityModule().initialize( itsContainer );
        new JavaMailModule().initialize( itsContainer );
        new UserIdentityDomainModule().initialize( itsContainer );
    }

    @After
    public void 
    tearDown() throws Exception
    {
        itsTarget    = null;
        itsContainer = null;
    }

    @Test
    public void 
    testInitialize()
    {
        itsTarget.initialize( itsContainer );
        
        assertNotNull( itsContainer.getInstance( IUserIdentityService.class,"UserIdentityServiceImp" ) );
    }

    private ILogger 
    createLogger()
    {
        ILogger logger = new Logger();
        
        logger.attachProcessor( new PrintWriterLogEntryProcessor(new PrintWriter(System.out)) );
        return logger;
    }

    private Properties
    createProperties()
    {
        Properties  properties = new Properties();
        InputStream input = 
            this
                .getClass()
                .getResourceAsStream( 
                    "/identitymanager/application/useridentity/useridentitytest.properties" );
        
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
    
}

// ##########################################################################
