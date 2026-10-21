//////////////////////////////////////////////////////////////////////////////
// MappingModule.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.server.application;

import granite.user.server.domain.User;
import granite.user.service.requestreply.CreateUserData;
import granite.user.service.requestreply.UserData;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.Module;

public
class MappingModule
    implements Module
{
    private static final String PASSWORD_MASK = "********";

    @Override
    public void
    setupModule(ModelMapper mapper)
    {
        mapper
            .emptyTypeMap(CreateUserData.class,User.class)
            .addMapping(CreateUserData::getUserName,User::setUserName)
            .addMapping(CreateUserData::getPassword,User::setPassword)
            .addMapping(CreateUserData::getExternalIdentity,User::setExternalIdentity)
            .addMapping(CreateUserData::getAuthorities,User::setAuthorities);

        mapper
            .createTypeMap(UserData.class,User.class)
            .addMapping(UserData::getUserId,User::setPrimaryId);

        Converter<String,String> passwordMask =
            context ->
                context.getSource() == null || context.getSource().isBlank()
                    ? null
                    : PASSWORD_MASK;

        mapper
            .emptyTypeMap(User.class,UserData.class)
            .addMapping(User::getPrimaryId,UserData::setUserId)
            .addMapping(User::getUserName,UserData::setUserName)
            .addMapping(User::getExternalIdentity,UserData::setExternalIdentity)
            .addMappings(m -> m.using(passwordMask).map(User::getPassword,UserData::setPassword))
            .addMapping(User::getAuthorities,UserData::setAuthorities)
            .addMapping(User::getValues,UserData::setValues);
    }
}

//////////////////////////////////////////////////////////////////////////////
