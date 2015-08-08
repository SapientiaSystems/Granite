package identitymanager.application.useridentity;

import static org.junit.Assert.*;
import identitymanager.service.shared.UserIdentityDescriptor;
import identitymanager.service.useridentity.FinishCreatingUserIdentityReply;
import identitymanager.service.useridentity.FinishCreatingUserIdentityRequest;
import identitymanager.service.useridentity.FinishUpdatingUserIdentityReply;
import identitymanager.service.useridentity.FinishUpdatingUserIdentityRequest;
import identitymanager.service.useridentity.PersonNameData;
import identitymanager.service.useridentity.StartCreatingUserIdentityReply;
import identitymanager.service.useridentity.StartCreatingUserIdentityRequest;
import identitymanager.service.useridentity.StartUpdatingUserIdentityReply;
import identitymanager.service.useridentity.StartUpdatingUserIdentityRequest;
import identitymanager.service.useridentity.UpdateUserIdentityReply;
import identitymanager.service.useridentity.UpdateUserIdentityRequest;
import identitymanager.service.useridentity.UserIdentityData;
import identitymanager.domain.useridentity.IUserIdentity;
import identitymanager.domain.useridentity.IUserIdentityEvent;
import identitymanager.common.utility.UtilityModule;
import identitymanager.common.value.PersonName;
import strata1.injector.container.Container;
import strata1.injector.container.IContainer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public 
class UserIdentityFactoryTest
{
    private IUserIdentityFactory itsTarget;
    
    @Before
    public void 
    setUp() throws Exception
    {
        IContainer container = new Container();
        
        new UtilityModule().initialize( container );
        new UserIdentityAppModule().initialize( container );
        itsTarget = container.getInstance( IUserIdentityFactory.class );
    }

    @After
    public void 
    tearDown() throws Exception
    {
        itsTarget = null;
    }

    @Test
    public void 
    testCreateUserIdentityReplyStartCreatingUserIdentityRequest()
    {
        StartCreatingUserIdentityRequest request = null;    
        StartCreatingUserIdentityReply   reply = null;
                        
        request = new StartCreatingUserIdentityRequest();
        
        request.setDuplicate( false );
        request.setRequestId( "XXXXXXX" );
        request.setReturnAddress( "ZZZZZZZZZ" );
        request.setSessionId( "1234567" );
        
        reply = itsTarget.createUserIdentityReply( request );
        assertEquals( reply.getOriginatingRequestId(),request.getRequestId() );
        assertFalse( reply.isDuplicate() );
        assertNotNull( reply.getTimestamp() );
    }

    @Test
    public void 
    testCreateUserIdentityReplyFinishCreatingUserIdentityRequest()
    {
        FinishCreatingUserIdentityRequest request = null;    
        FinishCreatingUserIdentityReply   reply = null;
                        
        request = new FinishCreatingUserIdentityRequest();
        
        request.setDuplicate( false );
        request.setRequestId( "XXXXXXX" );
        request.setReturnAddress( "ZZZZZZZZZ" );
        request.setSessionId( "1234567" );
        
        reply = itsTarget.createUserIdentityReply( request );
        assertEquals( reply.getOriginatingRequestId(),request.getRequestId() );
        assertFalse( reply.isDuplicate() );
        assertNotNull( reply.getTimestamp() );
    }

    @Test
    public void 
    testCreateUserIdentityReplyStartUpdatingUserIdentityRequest()
    {
        StartUpdatingUserIdentityRequest request = null;    
        StartUpdatingUserIdentityReply   reply = null;
                        
        request = new StartUpdatingUserIdentityRequest();
        
        request.setDuplicate( false );
        request.setRequestId( "XXXXXXX" );
        request.setReturnAddress( "ZZZZZZZZZ" );
        request.setSessionId( "1234567" );
        
        reply = itsTarget.createUserIdentityReply( request );
        assertEquals( reply.getOriginatingRequestId(),request.getRequestId() );
        assertFalse( reply.isDuplicate() );
        assertNotNull( reply.getTimestamp() );
    }

    @Test
    public void 
    testCreateUserIdentityReplyFinishUpdatingUserIdentityRequest()
    {
        FinishUpdatingUserIdentityRequest request = null;    
        FinishUpdatingUserIdentityReply   reply = null;
                        
        request = new FinishUpdatingUserIdentityRequest();
        
        request.setDuplicate( false );
        request.setRequestId( "XXXXXXX" );
        request.setReturnAddress( "ZZZZZZZZZ" );
        request.setSessionId( "1234567" );
        
        reply = itsTarget.createUserIdentityReply( request );
        assertEquals( reply.getOriginatingRequestId(),request.getRequestId() );
        assertFalse( reply.isDuplicate() );
        assertNotNull( reply.getTimestamp() );
    }

    @Test
    public void 
    testCreateUserIdentityReplyUpdateUserIdentityRequest()
    {
        UpdateUserIdentityRequest request = null;    
        UpdateUserIdentityReply   reply = null;
                        
        request = new UpdateUserIdentityRequest();
        
        request.setDuplicate( false );
        request.setRequestId( "XXXXXXX" );
        request.setReturnAddress( "ZZZZZZZZZ" );
        request.setSessionId( "1234567" );
        
        reply = itsTarget.createUserIdentityReply( request );
        assertEquals( reply.getOriginatingRequestId(),request.getRequestId() );
        assertFalse( reply.isDuplicate() );
        assertNotNull( reply.getTimestamp() );
    }

    @Test
    public void 
    testCreateUserIdentityEvent()
    {
        PersonNameData                   nameData = null;
        UserIdentityData                 userData = null;
        StartCreatingUserIdentityRequest request  = null;
        IUserIdentityEvent               event    = null;
        
        nameData = new PersonNameData();
        userData = new UserIdentityData();
        request = new StartCreatingUserIdentityRequest();
        
        nameData.setFirstName( "Adam" );
        nameData.setLastName( "Smith" );
        
        userData.setUserName( "adamsmith" );
        userData.setEmailAddress( "adam@wealthofnations.com" );
        userData.setPersonName( nameData );
        
        request.setDuplicate( false );
        request.setRequestId( "XXXXXXX" );
        request.setReturnAddress( "ZZZZZZZZZ" );
        request.setSessionId( "1234567" );
        request.setUser( userData );
        
        event = itsTarget.createUserIdentityEvent( request );
        assertEquals( event.getUserName(),request.getUser().getUserName() );
        assertEquals( event.getEmailAddress().toString(),request.getUser().getEmailAddress() );
        assertEquals( event.getName().getFirstName(),request.getUser().getPersonName().getFirstName() );
        assertEquals( event.getName().getLastName(),request.getUser().getPersonName().getLastName() );
    }

    @Test
    public void 
    testCreateUserIdentity()
    {
        PersonNameData                   nameData = null;
        UserIdentityData                 userData = null;
        StartCreatingUserIdentityRequest request  = null;
        IUserIdentityEvent               event    = null;
        IUserIdentity                    user     = null;
        
        nameData = new PersonNameData();
        userData = new UserIdentityData();
        request = new StartCreatingUserIdentityRequest();
        
        nameData.setFirstName( "Adam" );
        nameData.setLastName( "Smith" );
        
        userData.setUserName( "adamsmith" );
        userData.setEmailAddress( "adam@wealthofnations.com" );
        userData.setPersonName( nameData );
        
        request.setDuplicate( false );
        request.setRequestId( "XXXXXXX" );
        request.setReturnAddress( "ZZZZZZZZZ" );
        request.setSessionId( "1234567" );
        request.setUser( userData );
        
        event = itsTarget.createUserIdentityEvent( request );
        user  = itsTarget.createUserIdentity( event );
        assertEquals( user.getUserName(),event.getUserName() );
        assertEquals( user.getEmailAddress(),event.getEmailAddress());
        assertEquals( user.getName(),event.getName() );
    }

    @Test
    public void 
    testCreateUserIdentityDescriptor()
    {
        PersonNameData                   nameData   = null;
        UserIdentityData                 userData   = null;
        StartCreatingUserIdentityRequest request    = null;
        IUserIdentityEvent               event      = null;
        IUserIdentity                    user       = null;
        UserIdentityDescriptor           descriptor = null;
        
        nameData = new PersonNameData();
        userData = new UserIdentityData();
        request = new StartCreatingUserIdentityRequest();
        
        nameData.setFirstName( "Adam" );
        nameData.setLastName( "Smith" );
        
        userData.setUserName( "adamsmith" );
        userData.setEmailAddress( "adam@wealthofnations.com" );
        userData.setPersonName( nameData );
        
        request.setDuplicate( false );
        request.setRequestId( "XXXXXXX" );
        request.setReturnAddress( "ZZZZZZZZZ" );
        request.setSessionId( "1234567" );
        request.setUser( userData );
        
        event      = itsTarget.createUserIdentityEvent( request );
        user       = itsTarget.createUserIdentity( event );
        
        user.setUserId( "XYZxyzXYZxyzXYZ" );
        descriptor = itsTarget.createUserIdentityDescriptor( user );
        
        assertEquals( descriptor.getUserId(),user.getUserId() );
        assertEquals( descriptor.getUserName(),user.getUserName() );
    }

}

// ##########################################################################
