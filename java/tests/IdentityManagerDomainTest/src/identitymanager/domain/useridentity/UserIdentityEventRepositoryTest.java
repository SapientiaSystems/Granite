package identitymanager.domain.useridentity;

import static org.junit.Assert.*;
import identitymanager.common.value.EmailAddress;
import identitymanager.common.value.PersonName;
import identitymanager.common.value.PhoneNumber;
import identitymanager.common.value.PostalAddress;
import identitymanager.common.value.SecurityQuestion;
import strata1.persistence.repository.IUnitOfWork;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.util.HashSet;
import java.util.Set;

public abstract
class UserIdentityEventRepositoryTest
{
    private IUserIdentityEventRepository itsTarget;
    private Set<Long>                    itsEventIds;
    
    @Before
    public void 
    setUp() throws Exception
    {
        itsTarget   = createTarget();
        itsEventIds = new HashSet<Long>();
        
        assertNotNull( itsTarget );
    }

    @After
    public void 
    tearDown() throws Exception
    {
        IUnitOfWork unitOfWork = itsTarget.getUnitOfWork();
        
        for (Long eventId : itsEventIds)
        {
            IUserIdentityEvent event = itsTarget.getUserIdentityEvent( eventId );
            
            itsTarget.removeUserIdentityEvent( event );  
        }
        
        unitOfWork.commit();
        itsEventIds.clear();
        itsEventIds = null;
        itsTarget = null;
    }

    @Test
    public void 
    testInsertUserIdentityEvent1()
        throws Exception
    {
        IUnitOfWork        unitOfWork = itsTarget.getUnitOfWork();
        IUserIdentityEvent expected   = new UserIdentityEvent();
        IUserIdentityEvent actual     = null;
        
        expected = itsTarget.insertUserIdentityEvent( expected );
        itsEventIds.add( expected.getEventId() );
        unitOfWork.commit();
        assertTrue( itsTarget.hasUserIdentityEvent( expected.getEventId() ));
        
        actual = itsTarget.getUserIdentityEvent( expected.getEventId() );
        assertEquals( actual.getEventId(),expected.getEventId() );
    }

    @Test
    public void 
    testInsertUserIdentityEvent2()
        throws Exception
    {
        IUnitOfWork        unitOfWork = itsTarget.getUnitOfWork();
        IUserIdentityEvent expected   = new UserIdentityEvent();
        IUserIdentityEvent actual     = null;
        
        expected.setUserName( "johndoe" );
        assertFalse( itsTarget.hasUserIdentityEventWithUserName( expected.getUserName() ));
        expected = itsTarget.insertUserIdentityEvent( expected );
        itsEventIds.add( expected.getEventId() );
        unitOfWork.commit();
        assertTrue( itsTarget.hasUserIdentityEventWithUserName( expected.getUserName() ));
        
        actual = itsTarget.getUserIdentityEvent( expected.getEventId() );
        assertEquals( actual.getEventId(),expected.getEventId() );
        assertEquals( actual.getUserName(),expected.getUserName() );
    }

    @Test
    public void 
    testInsertUserIdentityEvent3()
        throws Exception
    {
        IUnitOfWork        unitOfWork = itsTarget.getUnitOfWork();
        IUserIdentityEvent expected   = new UserIdentityEvent();
        IUserIdentityEvent actual     = null;
       
        expected.setEventKind( UserIdentityEventKind.CREATE_USERIDENTITY );
        expected.setStateKind( UserIdentityStateKind.PENDING );
        expected.setUserId( "XXXXXXXXXXXXXXXXXX" );
        expected.setUserName( "johndoe" );
        expected.setPassword( "xxxxxxxxxxxx" );
        expected.setEmailAddress( new EmailAddress("johndoe@mail.com") );
        expected.setEmailVerification( "123456" );
        expected.setName( new PersonName( "John","Xavier","Doe" ) );
        expected.setPostalAddress( new PostalAddress("123", "Main Street", "Small Town", "CA", "USA", "90123") );
        expected.setPhoneNumber( new PhoneNumber("1", "123", "555-5555") );
        expected.setPhoneVerification( "234567" );
        expected.setSecurityQuestion( new SecurityQuestion(1, "Mother's maiden name", "Smith") );
        assertFalse( itsTarget.hasUserIdentityEventWithUserName( expected.getUserName() ));
        expected = itsTarget.insertUserIdentityEvent( expected );
        itsEventIds.add( expected.getEventId() );
        unitOfWork.commit();
        assertTrue( itsTarget.hasUserIdentityEventWithUserName( expected.getUserName() ));
        
        actual = itsTarget.getUserIdentityEvent( expected.getEventId() );
        assertEquals( actual.getEventId(),expected.getEventId() );
        assertEquals( actual.getEventKind(),expected.getEventKind() );
        assertEquals( actual.getStateKind(),expected.getStateKind() );
        assertEquals( actual.getUserId(),expected.getUserId() );
        assertEquals( actual.getUserName(),expected.getUserName() );
        assertEquals( actual.getPassword(),expected.getPassword() );
        assertEquals( actual.getEmailAddress(),expected.getEmailAddress() );
        assertEquals( actual.getEmailVerification(),expected.getEmailVerification() );
        assertEquals( actual.getName(),expected.getName() );
        assertEquals( actual.getPostalAddress(),expected.getPostalAddress() );
        assertEquals( actual.getPhoneNumber(),expected.getPhoneNumber() );
        assertEquals( actual.getPhoneVerification(),expected.getPhoneVerification() );
        assertEquals( actual.getSecurityQuestion(),expected.getSecurityQuestion() );
    }

    @Test
    public void 
    testUpdateUserIdentityEvent()
        throws Exception
    {
        IUnitOfWork        unitOfWork = itsTarget.getUnitOfWork();
        IUserIdentityEvent expected   = new UserIdentityEvent();
        IUserIdentityEvent actual     = null;
        
        expected.setUserName( "johndoe" );
        assertFalse( itsTarget.hasUserIdentityEventWithUserName( expected.getUserName() ));
        expected = itsTarget.insertUserIdentityEvent( expected );
        itsEventIds.add( expected.getEventId() );
        unitOfWork.commit();
        unitOfWork = itsTarget.getUnitOfWork();
        assertTrue( itsTarget.hasUserIdentityEventWithUserName( expected.getUserName() ));
        
        actual = itsTarget.getUserIdentityEvent( expected.getEventId() );
        assertEquals( actual.getEventId(),expected.getEventId() );
        assertEquals( actual.getUserName(),expected.getUserName() );
        
        expected = actual;
        actual = null;
        expected.setName(  new PersonName("John","Doe") );
        expected.setEmailAddress( new EmailAddress( "johndoe@mail.com" ) );
        
        expected = itsTarget.updateUserIdentityEvent( expected );
        unitOfWork.commit();
        actual = itsTarget.getUserIdentityEvent( expected.getEventId() );
        assertEquals( actual.getEventId(),expected.getEventId() );
        assertEquals( actual.getUserName(),expected.getUserName() );
        assertEquals( actual.getName(),expected.getName() );
        assertEquals( actual.getEmailAddress(),expected.getEmailAddress() );
    }

    @Test
    public void 
    testRemoveUserIdentityEvent()
        throws Exception
    {
        IUnitOfWork        unitOfWork = itsTarget.getUnitOfWork();
        IUserIdentityEvent expected   = new UserIdentityEvent();
        IUserIdentityEvent actual     = null;
        
        expected.setUserName( "johndoe" );
        assertFalse( itsTarget.hasUserIdentityEventWithUserName( expected.getUserName() ));
        expected = itsTarget.insertUserIdentityEvent( expected );
        unitOfWork.commit();
        unitOfWork = itsTarget.getUnitOfWork();
        assertTrue( itsTarget.hasUserIdentityEvent( expected.getEventId() ));
        actual = itsTarget.getUserIdentityEvent( expected.getEventId() );
        assertNotNull( actual );
        itsTarget.removeUserIdentityEvent( actual );
        unitOfWork.commit();
        assertFalse( itsTarget.hasUserIdentityEventWithUserName( "johndoe" ));
    }

    protected abstract IUserIdentityEventRepository
    createTarget();

}

// ##########################################################################
