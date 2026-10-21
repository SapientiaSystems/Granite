//////////////////////////////////////////////////////////////////////////////
// SessionTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.domain;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import strata.foundation.core.mapper.IMapper;
import strata.foundation.core.value.EmailAddress;
import strata.foundation.core.value.GeoLocation;
import strata.foundation.core.value.PersonName;
import strata.foundation.core.value.PhoneNumber;
import strata.foundation.core.value.PostalAddress;
import strata.foundation.core.value.PostalCode;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class SessionTest
{
    @AfterEach
    public void
    tearDown()
    {
        Session.getRegistry()
            .unregister(String.class,UUID.class)
            .unregister(UUID.class,String.class)
            .unregister(Integer.class,String.class)
            .unregister(String.class,EmailAddress.class)
            .unregister(EmailAddress.class,String.class)
            .unregister(String.class,PhoneNumber.class)
            .unregister(PhoneNumber.class,String.class)
            .unregister(String.class,PostalCode.class)
            .unregister(PostalCode.class,String.class)
            .unregister(String.class,PersonName.class)
            .unregister(PersonName.class,String.class)
            .unregister(String.class,PostalAddress.class)
            .unregister(PostalAddress.class,String.class)
            .unregister(String.class,GeoLocation.class)
            .unregister(GeoLocation.class,String.class);
    }

    @Test
    public void
    testUserIdDefaultsToNull()
    {
        assertNull(new Session().getUserId());
    }

    @Test
    public void
    testAccessTokenDefaultsToNull()
    {
        assertNull(new Session().getAccessToken());
    }

    @Test
    public void
    testExpiresDefaultsToNull()
    {
        assertNull(new Session().getExpires());
    }

    @Test
    public void
    testDeviceIdDefaultsToNull()
    {
        assertNull(new Session().getDeviceId());
    }

    @Test
    public void
    testAuthoritiesDefaultsToEmptySet()
    {
        Session target = new Session();

        assertNotNull(target.getAuthorities());
        assertTrue(target.getAuthorities().isEmpty());
    }

    @Test
    public void
    testValuesDefaultsToEmptyMap()
    {
        Session target = new Session();

        assertNotNull(target.getValues());
        assertTrue(target.getValues().isEmpty());
    }

    @Test
    public void
    testSetUserIdFluentlyAndRead()
    {
        Session target = new Session();

        assertSame(target,target.setUserId(42L));
        assertEquals(42L,target.getUserId());
    }

    @Test
    public void
    testSetAccessTokenFluentlyAndRead()
    {
        Session target = new Session();

        assertSame(target,target.setAccessToken("jwt.abc.def"));
        assertEquals("jwt.abc.def",target.getAccessToken());
    }

    @Test
    public void
    testRefreshTokenHashesDefaultToNull()
    {
        Session target = new Session();

        assertNull(target.getRefreshTokenHash());
        assertNull(target.getPreviousRefreshTokenHash());
    }

    @Test
    public void
    testRotateRefreshTokenHashMovesCurrentToPrevious()
    {
        Session target = new Session().setRefreshTokenHash("hash-1");

        assertSame(target,target.rotateRefreshTokenHash("hash-2"));
        assertEquals("hash-2",target.getRefreshTokenHash());
        assertEquals("hash-1",target.getPreviousRefreshTokenHash());

        target.rotateRefreshTokenHash("hash-3");

        assertEquals("hash-3",target.getRefreshTokenHash());
        assertEquals("hash-2",target.getPreviousRefreshTokenHash());
    }

    @Test
    public void
    testSetDeviceIdFluentlyAndRead()
    {
        Session target = new Session();

        assertSame(target,target.setDeviceId("device-123"));
        assertEquals("device-123",target.getDeviceId());
    }

    @Test
    public void
    testSetAuthoritiesFluentlyAndRead()
    {
        Session     target      = new Session();
        Set<String> authorities = new LinkedHashSet<>(Set.of("admin","user"));

        assertSame(target,target.setAuthorities(authorities));
        assertEquals(authorities,target.getAuthorities());
    }

    @Test
    public void
    testSetSessionIdFluentlyAndRead()
    {
        Session target = new Session();

        assertSame(target,target.setSessionId(99L));
        assertEquals(99L,target.getSessionId());
        assertEquals(99L,target.getPrimaryId());
    }

    @Test
    public void
    testSetExpiresFiresSessionUpdated()
    {
        Session           target   = new Session();
        CapturingObserver observer = new CapturingObserver();

        target.attach(observer);
        target.setExpires(Instant.parse("2026-10-06T12:00:00Z"));

        assertEquals(1,observer.events.size());
        assertTrue(observer.events.get(0) instanceof SessionUpdated);
        assertEquals("SessionUpdated",observer.events.get(0).getName());
    }

    @Test
    public void
    testSetValueFluentlyAndReadByType()
    {
        Session target = new Session();

        assertSame(target,target.setValue("cart-id","c-1"));
        assertEquals(Optional.of("c-1"),target.getValue(String.class,"cart-id"));
    }

    @Test
    public void
    testSetValueStoresStringAsIs()
    {
        Session target = new Session().setValue("name","alice");

        assertEquals("alice",target.getValues().get("name"));
    }

    @Test
    public void
    testSetValueStoresIntegerAsIs()
    {
        Session target = new Session().setValue("count",42);

        assertEquals(42,target.getValues().get("count"));
    }

    @Test
    public void
    testSetValueOfComplexWithoutMapperFallsBackToToString()
    {
        UUID    uuid   = UUID.randomUUID();
        Session target = new Session().setValue("id",uuid);

        assertEquals(uuid.toString(),target.getValues().get("id"));
    }

    @Test
    public void
    testSetValueOfComplexWithMapperStringifiesViaMapper()
    {
        IMapper<String,UUID> toText = u -> "uuid[" + u.toString() + "]";
        UUID                 uuid   = UUID.randomUUID();
        Session              target = new Session();

        Session.getRegistry().register(String.class,UUID.class,toText);
        target.setValue("id",uuid);

        assertEquals("uuid[" + uuid.toString() + "]",target.getValues().get("id"));
    }

    @Test
    public void
    testGetValueOnMissingKeyReturnsEmpty()
    {
        assertTrue(new Session().getValue(String.class,"missing").isEmpty());
    }

    @Test
    public void
    testGetValueOnTypeMismatchReturnsEmpty()
    {
        Session target = new Session().setValue("count","not-a-number");

        assertTrue(target.getValue(Integer.class,"count").isEmpty());
    }

    @Test
    public void
    testGetValueReturnsStoredStringWhenTypeIsString()
    {
        Session target = new Session().setValue("name","alice");

        assertEquals(Optional.of("alice"),target.getValue(String.class,"name"));
    }

    @Test
    public void
    testGetValueReturnsStoredNumberWhenTypeMatches()
    {
        Session target = new Session().setValue("count",42);

        assertEquals(Optional.of(42),target.getValue(Integer.class,"count"));
    }

    @Test
    public void
    testGetValueUsesMapperToConvertStoredStringToType()
    {
        IMapper<Integer,String> parseInt = Integer::valueOf;
        Session                 target   = new Session();

        Session.getRegistry().register(Integer.class,String.class,parseInt);
        target.setValue("count","42");

        assertEquals(Optional.of(42),target.getValue(Integer.class,"count"));
    }

    @Test
    public void
    testGetValueOfComplexRoundTripsThroughRegisteredMappers()
    {
        IMapper<String,UUID> toText   = Object::toString;
        IMapper<UUID,String> fromText = UUID::fromString;
        UUID                 uuid     = UUID.randomUUID();
        Session              target   = new Session();

        Session
            .getRegistry()
            .register(String.class,UUID.class,toText)
            .register(UUID.class,String.class,fromText);
        target.setValue("id",uuid);

        assertEquals(Optional.of(uuid),target.getValue(UUID.class,"id"));
    }

    @Test
    public void
    testGetValueOfComplexWithoutReverseMapperReturnsEmpty()
    {
        UUID    uuid   = UUID.randomUUID();
        Session target = new Session().setValue("id",uuid);

        assertTrue(target.getValue(UUID.class,"id").isEmpty());
    }

    @Test
    public void
    testHasValueReturnsTrueForPresentKey()
    {
        Session target = new Session().setValue("cart-id","c-1");

        assertTrue(target.hasValue("cart-id"));
    }

    @Test
    public void
    testHasValueReturnsFalseForAbsentKey()
    {
        assertFalse(new Session().hasValue("missing"));
    }

    @Test
    public void
    testGetMapperRegisterStoresReqistryWithStringDestination()
    {
        Session              target = new Session();
        IMapper<String,UUID> mapper = Object::toString;

        Session.getRegistry().register(String.class,UUID.class,mapper);

        assertTrue(Session.getRegistry().has(String.class,UUID.class));
    }

    @Test
    public void
    testGetMapperRegisterStoresReqistryWithStringSource()
    {
        Session              target = new Session();
        IMapper<UUID,String> mapper = UUID::fromString;

        Session.getRegistry().register(UUID.class,String.class,mapper);

        assertTrue(Session.getRegistry().has(UUID.class,String.class));
    }

    @Test
    public void
    testGetRegistryRegisterThrowsWhenNeitherSourceNorDestinationIsString()
    {
        Session               target = new Session();
        IMapper<Integer,Long> mapper = Long::intValue;

        assertThrows(
            IllegalArgumentException.class,
            () -> Session.getRegistry().register(Integer.class,Long.class,mapper));
    }

    @Test
    public void
    testGetRegistryUnregisterRemovesMapping()
    {
        Session              target = new Session();
        IMapper<String,UUID> mapper = Object::toString;

        Session
            .getRegistry()
            .register(String.class,UUID.class,mapper)
            .unregister(String.class,UUID.class);

        assertFalse(Session.getRegistry().has(String.class,UUID.class));
    }

    @Test
    public void
    testGetRegistryHasReturnsFalseWhenAbsent()
    {
        assertFalse(Session.getRegistry().has(String.class,UUID.class));
    }

    @Test
    public void
    testSetAndGetValueRoundTripsEmailAddress()
    {
        EmailAddress email  = EmailAddress.of("jane.doe@example.com");
        Session      target = new Session();

        Session
            .getRegistry()
            .register(String.class,EmailAddress.class,EmailAddress::toString)
            .register(EmailAddress.class,String.class,EmailAddress::of);
        target.setValue("email",email);

        assertEquals(Optional.of(email),target.getValue(EmailAddress.class,"email"));
    }

    @Test
    public void
    testSetValueOfEmailAddressStoresFormattedString()
    {
        EmailAddress email  = EmailAddress.of("jane.doe@example.com");
        Session      target = new Session();

        Session.getRegistry().register(String.class,EmailAddress.class,EmailAddress::toString);
        target.setValue("email",email);

        assertEquals("jane.doe@example.com",target.getValues().get("email"));
    }

    @Test
    public void
    testSetAndGetValueRoundTripsPhoneNumber()
    {
        PhoneNumber phone  = PhoneNumber.of("+1 (555) 123-4567");
        Session     target = new Session();

        Session
            .getRegistry()
            .register(String.class,PhoneNumber.class,PhoneNumber::toString)
            .register(PhoneNumber.class,String.class,PhoneNumber::of);
        target.setValue("phone",phone);

        assertEquals(Optional.of(phone),target.getValue(PhoneNumber.class,"phone"));
    }

    @Test
    public void
    testSetValueOfPhoneNumberStoresFormattedString()
    {
        PhoneNumber phone  = PhoneNumber.of("+1 (555) 123-4567");
        Session     target = new Session();

        Session.getRegistry().register(String.class,PhoneNumber.class,PhoneNumber::toString);
        target.setValue("phone",phone);

        assertEquals("+1 (555) 123-4567",target.getValues().get("phone"));
    }

    @Test
    public void
    testSetAndGetValueRoundTripsPostalCode()
    {
        PostalCode zip    = PostalCode.of("62704");
        Session    target = new Session();

        Session
            .getRegistry()
            .register(String.class,PostalCode.class,PostalCode::toString)
            .register(PostalCode.class,String.class,PostalCode::of);
        target.setValue("zip",zip);

        assertEquals(Optional.of(zip),target.getValue(PostalCode.class,"zip"));
    }

    @Test
    public void
    testSetAndGetValueRoundTripsPersonName()
    {
        IMapper<String,PersonName> toText =
            p ->
                String.join(
                    "|",
                    p.getTitle().orElse(""),
                    p.getFirstName(),
                    p.getMiddleName().orElse(""),
                    p.getLastName(),
                    p.getSuffix().orElse(""));
        IMapper<PersonName,String> fromText =
            s ->
            {
                String[] parts = s.split("\\|",-1);

                return
                    new PersonName(
                        parts[0].isEmpty() ? null : parts[0],
                        parts[1],
                        parts[2].isEmpty() ? null : parts[2],
                        parts[3],
                        parts[4].isEmpty() ? null : parts[4]);
            };
        PersonName name   = PersonName.of("Dr.","Jane","Q.","Doe","Jr.");
        Session    target = new Session();

        Session
            .getRegistry()
            .register(String.class,PersonName.class,toText)
            .register(PersonName.class,String.class,fromText);
        target.setValue("name",name);

        assertEquals(Optional.of(name),target.getValue(PersonName.class,"name"));
    }

    @Test
    public void
    testSetAndGetValueRoundTripsPostalAddress()
    {
        IMapper<String,PostalAddress> toText =
            a ->
                String.join(
                    "|",
                    a.getAddress(),
                    a.getStreet(),
                    a.getCity(),
                    a.getState(),
                    a.getCountryCode(),
                    a.getPostalCode());
        IMapper<PostalAddress,String> fromText =
            s ->
            {
                String[] parts = s.split("\\|",-1);

                return
                    new PostalAddress(
                        parts[0],
                        parts[1],
                        parts[2],
                        parts[3],
                        parts[4],
                        parts[5]);
            };
        PostalAddress address =
            new PostalAddress("123","Main St","Springfield","IL","US","62704");
        Session       target  = new Session();

        Session
            .getRegistry()
            .register(String.class,PostalAddress.class,toText)
            .register(PostalAddress.class,String.class,fromText);
        target.setValue("address",address);

        assertEquals(
            Optional.of(address),
            target.getValue(PostalAddress.class,"address"));
    }

    @Test
    public void
    testSetAndGetValueRoundTripsGeoLocation()
    {
        IMapper<String,GeoLocation> toText =
            g -> g.getLatitude() + "," + g.getLongitude();
        IMapper<GeoLocation,String> fromText =
            s ->
            {
                String[] parts = s.split(",");

                return
                    GeoLocation.of(
                        Double.parseDouble(parts[0]),
                        Double.parseDouble(parts[1]));
            };
        GeoLocation location = GeoLocation.of(39.7817,-89.6501);
        Session     target   = new Session();

        Session
            .getRegistry()
            .register(String.class,GeoLocation.class,toText)
            .register(GeoLocation.class,String.class,fromText);
        target.setValue("home",location);

        assertEquals(
            Optional.of(location),
            target.getValue(GeoLocation.class,"home"));
    }

    @Test
    public void
    testDestroyFiresSessionDestroyed()
    {
        Session           target   = new Session();
        CapturingObserver observer = new CapturingObserver();

        target.attach(observer);
        target.destroy();

        assertEquals(1,observer.events.size());
        assertTrue(observer.events.get(0) instanceof SessionDestroyed);
        assertEquals("SessionDestroyed",observer.events.get(0).getName());
    }

    @Test
    public void
    testNotifyCreatedFiresSessionCreated()
    {
        Session           target   = new Session();
        CapturingObserver observer = new CapturingObserver();

        target.attach(observer);
        target.notifyCreated();

        assertEquals(1,observer.events.size());
        assertTrue(observer.events.get(0) instanceof SessionCreated);
        assertEquals("SessionCreated",observer.events.get(0).getName());
    }

    @Test
    public void
    testNotifyUpdatedFiresSessionUpdated()
    {
        Session           target   = new Session();
        CapturingObserver observer = new CapturingObserver();

        target.attach(observer);
        target.notifyUpdated();

        assertEquals(1,observer.events.size());
        assertTrue(observer.events.get(0) instanceof SessionUpdated);
        assertEquals("SessionUpdated",observer.events.get(0).getName());
    }

    @Test
    public void
    testNotifyDestroyedFiresSessionDestroyed()
    {
        Session           target   = new Session();
        CapturingObserver observer = new CapturingObserver();

        target.attach(observer);
        target.notifyDestroyed();

        assertEquals(1,observer.events.size());
        assertTrue(observer.events.get(0) instanceof SessionDestroyed);
        assertEquals("SessionDestroyed",observer.events.get(0).getName());
    }

    @Test
    public void
    testEventSourceReportsSessionAsSource()
    {
        Session           target   = new Session();
        CapturingObserver observer = new CapturingObserver();

        target.attach(observer);
        target.notifyCreated();

        assertSame(target,observer.events.get(0).getSource());
    }
}

//////////////////////////////////////////////////////////////////////////////
