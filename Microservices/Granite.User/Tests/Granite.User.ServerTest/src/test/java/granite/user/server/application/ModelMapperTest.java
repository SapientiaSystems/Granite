//////////////////////////////////////////////////////////////////////////////
// ModelMapperTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.server.application;

import granite.user.server.domain.User;
import granite.user.service.requestreply.UserData;
import org.junit.jupiter.api.*;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("CommitStage")
public
class ModelMapperTest
{
    private static ApplicationContext  context;
    private ModelMapper                mapper;

    @BeforeAll
    public static void
    initialize()
    {
        context =
            new AnnotationConfigApplicationContext(
                TestConfiguration.class);
    }

    @BeforeEach
    public void
    setUp()
    {
        mapper = context.getBean(ModelMapper.class);
    }

    @AfterEach
    public void
    tearDown()
    {
        mapper = null;
    }

    @Test
    public void
    testUserDataToUserMapping()
    {
        UserData data =
            new UserData()
                .setUserId(1L)
                .setUserName("alice@example.com");
        User entity = mapper.map(data,User.class);

        assertEquals(data.getUserId(),entity.getPrimaryId());
        assertEquals(data.getUserName(),entity.getUserName());
    }

    @Test
    public void
    testUserToUserDataMapping()
    {
        User entity =
            new User()
                .setPrimaryId(1L)
                .setUserName("alice@example.com");

        UserData data = mapper.map(entity,UserData.class);

        assertEquals(entity.getPrimaryId(),data.getUserId());
        assertEquals(entity.getUserName(),data.getUserName());
    }

    @Test
    public void
    testUserToUserDataValuesMapping()
    {
        User entity =
            new User()
                .setPrimaryId(1L)
                .setUserName("alice@example.com")
                .setValue("preferredTheme","dark");

        UserData data = mapper.map(entity,UserData.class);

        assertEquals("dark",data.getValues().get("preferredTheme"));
    }

}

//////////////////////////////////////////////////////////////////////////////
