//////////////////////////////////////////////////////////////////////////////
// MappingModule.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.server.application;

import granite.user.server.domain.User;
import granite.user.service.requestreply.CreateUserData;
import granite.user.service.requestreply.UserData;
import org.modelmapper.ModelMapper;
import org.modelmapper.Module;

public
class MappingModule
    implements Module
{
    @Override
    public void
    setupModule(ModelMapper mapper)
    {
        mapper.createTypeMap(CreateUserData.class,User.class);

        mapper
            .createTypeMap(UserData.class,User.class)
            .addMapping(UserData::getUserId,User::setPrimaryId);

        mapper
            .emptyTypeMap(User.class,UserData.class)
            .addMapping(User::getPrimaryId,UserData::setUserId)
            .addMapping(User::getUserName,UserData::setUserName)
            .addMapping(User::getExternalIdentity,UserData::setExternalIdentity)
            .addMapping(User::getAuthorities,UserData::setAuthorities)
            .addMapping(User::getValues,UserData::setValues);
    }
}

//////////////////////////////////////////////////////////////////////////////
