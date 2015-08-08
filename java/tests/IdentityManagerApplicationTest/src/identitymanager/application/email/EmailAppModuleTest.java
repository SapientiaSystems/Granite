package identitymanager.application.email;

import static org.junit.Assert.assertNotNull;
import strata1.injector.container.Container;
import strata1.injector.container.IContainer;
import strata1.injector.property.PropertiesModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public abstract 
class EmailAppModuleTest
{
    private IContainer     itsContainer;
    private EmailAppModule itsTarget;
    
    @Before
    public void 
    setUp() throws Exception
    {
        itsContainer = new Container();
        itsTarget = createTarget();
        
        new PropertiesModule(createProperties()).initialize(itsContainer);
    }

    @After
    public void 
    tearDown() throws Exception
    {
        itsTarget = null;
        itsContainer = null;
    }

    @Test
    public void 
    testInitialize()
    {
        itsTarget.initialize( itsContainer );  
        assertNotNull( itsContainer.getInstance( IEmailSession.class ) );
    }

    protected abstract EmailAppModule 
    createTarget();

    private Properties
    createProperties()
    {
        Properties  properties = new Properties();
        InputStream input = 
            this
                .getClass()
                .getResourceAsStream( 
                    "/identitymanager/application/email/emailtest.properties" );
        
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
