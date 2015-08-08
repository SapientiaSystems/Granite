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
class UserIdentityRepositoryTest
{
    private IUserIdentityRepository itsTarget;
    private Set<String>             itsUserIds;
    
    @Before
    public void 
    setUp() throws Exception
    {
        itsTarget   = createTarget();
        itsUserIds = new HashSet<String>();
        
        assertNotNull( itsTarget );
    }

    @After
    public void 
    tearDown() throws Exception
    {
        IUnitOfWork unitOfWork = itsTarget.getUnitOfWork();
        
        for (String userId : itsUserIds)
        {
            IUserIdentity user = itsTarget.getUserIdentity( userId );
            
            itsTarget.removeUserIdentity( user );  
        }
        
        unitOfWork.commit();
        itsUserIds.clear();
        itsUserIds = null;
        itsTarget = null;
    }

    @Test
    public void 
    testInsertUserIdentity1()
        throws Exception
    {
        IUnitOfWork   unitOfWork = itsTarget.getUnitOfWork();
        IUserIdentity expected   = new UserIdentity();
        IUserIdentity actual     = null;
        
        expected = itsTarget.insertUserIdentity( expected );
        itsUserIds.add( expected.getUserId() );
        unitOfWork.commit();
        assertTrue( itsTarget.hasUserIdentity( expected.getUserId() ));
        
        actual = itsTarget.getUserIdentity( expected.getUserId() );
        assertEquals( actual.getUserId(),expected.getUserId() );
    }

    @Test
    public void 
    testInsertUserIdentity2()
        throws Exception
    {
        IUnitOfWork        unitOfWork = itsTarget.getUnitOfWork();
        IUserIdentity expected   = new UserIdentity();
        IUserIdentity actual     = null;
        
        expected.setUserName( "johndoe" );
        assertFalse( itsTarget.hasUserIdentityWithUserName( expected.getUserName() ));
        expected = itsTarget.insertUserIdentity( expected );
        itsUserIds.add( expected.getUserId() );
        unitOfWork.commit();
        assertTrue( itsTarget.hasUserIdentityWithUserName( expected.getUserName() ));
        
        actual = itsTarget.getUserIdentity( expected.getUserId() );
        assertEquals( actual.getUserId(),expected.getUserId() );
        assertEquals( actual.getUserName(),expected.getUserName() );
    }

    @Test
    public void 
    testInsertUserIdentity3()
        throws Exception
    {
        IUnitOfWork        unitOfWork = itsTarget.getUnitOfWork();
        IUserIdentity expected   = new UserIdentity();
        IUserIdentity actual     = null;
       
        expected.setUserName( "johndoe" );
        expected.setPassword( "xxxxxxxxxxxx" );
        expected.setEmailAddress( new EmailAddress("johndoe@mail.com") );
        expected.setName( new PersonName( "John","Xavier","Doe" ) );
        expected.setPostalAddress( new PostalAddress("123", "Main Street", "Small Town", "CA", "USA", "90123") );
        expected.setPhoneNumber( new PhoneNumber("1", "123", "555-5555") );
        expected.setSecurityQuestion( new SecurityQuestion(1, "Mother's maiden name", "Smith") );
        assertFalse( itsTarget.hasUserIdentityWithUserName( expected.getUserName() ));
        expected = itsTarget.insertUserIdentity( expected );
        itsUserIds.add( expected.getUserId() );
        unitOfWork.commit();
        assertTrue( itsTarget.hasUserIdentityWithUserName( expected.getUserName() ));
        
        actual = itsTarget.getUserIdentity( expected.getUserId() );
        assertEquals( actual.getUserId(),expected.getUserId() );
        assertEquals( actual.getUserId(),expected.getUserId() );
        assertEquals( actual.getUserName(),expected.getUserName() );
        assertEquals( actual.getPassword(),expected.getPassword() );
        assertEquals( actual.getEmailAddress(),expected.getEmailAddress() );
        assertEquals( actual.getName(),expected.getName() );
        assertEquals( actual.getPostalAddress(),expected.getPostalAddress() );
        assertEquals( actual.getPhoneNumber(),expected.getPhoneNumber() );
        assertEquals( actual.getSecurityQuestion(),expected.getSecurityQuestion() );
    }

    @Test
    public void 
    testUpdateUserIdentity()
        throws Exception
    {
        IUnitOfWork   unitOfWork = itsTarget.getUnitOfWork();
        IUserIdentity expected   = new UserIdentity();
        IUserIdentity actual     = null;
        
        expected.setUserName( "johndoe" );
        assertFalse( itsTarget.hasUserIdentityWithUserName( expected.getUserName() ));
        expected = itsTarget.insertUserIdentity( expected );
        itsUserIds.add( expected.getUserId() );
        unitOfWork.commit();
        unitOfWork = itsTarget.getUnitOfWork();
        assertTrue( itsTarget.hasUserIdentityWithUserName( expected.getUserName() ));
        
        actual = itsTarget.getUserIdentity( expected.getUserId() );
        assertEquals( actual.getUserId(),expected.getUserId() );
        assertEquals( actual.getUserName(),expected.getUserName() );
        
        expected = actual;
        actual = null;
        expected.setName(  new PersonName("John","Doe") );
        expected.setEmailAddress( new EmailAddress( "johndoe@mail.com" ) );
        
        expected = itsTarget.updateUserIdentity( expected );
        unitOfWork.commit();
        actual = itsTarget.getUserIdentity( expected.getUserId() );
        assertEquals( actual.getUserId(),expected.getUserId() );
        assertEquals( actual.getUserName(),expected.getUserName() );
        assertEquals( actual.getName(),expected.getName() );
        assertEquals( actual.getEmailAddress(),expected.getEmailAddress() );
    }

    @Test
    public void 
    testRemoveUserIdentity()
        throws Exception
    {
        IUnitOfWork   unitOfWork = itsTarget.getUnitOfWork();
        IUserIdentity expected   = new UserIdentity();
        IUserIdentity actual     = null;
        
        expected.setUserName( "johndoe" );
        assertFalse( itsTarget.hasUserIdentityWithUserName( expected.getUserName() ));
        expected = itsTarget.insertUserIdentity( expected );
        unitOfWork.commit();
        unitOfWork = itsTarget.getUnitOfWork();
        assertTrue( itsTarget.hasUserIdentity( expected.getUserId() ));
        actual = itsTarget.getUserIdentity( expected.getUserId() );
        assertNotNull( actual );
        itsTarget.removeUserIdentity( actual );
        unitOfWork.commit();
        assertFalse( itsTarget.hasUserIdentityWithUserName( "johndoe" ));
    }

    protected abstract IUserIdentityRepository
    createTarget();

}

// ##########################################################################
