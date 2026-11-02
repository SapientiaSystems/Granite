//////////////////////////////////////////////////////////////////////////////
// MappingModule.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import granite.session.server.domain.Session;
import granite.session.service.requestreply.SessionData;
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
        mapper
            .emptyTypeMap(Session.class,SessionData.class)
            .addMapping(Session::getSessionId,SessionData::setSessionId)
            .addMapping(Session::getUserId,SessionData::setUserId)
            .addMapping(Session::getCreated,SessionData::setCreated)
            .addMapping(Session::getExpires,SessionData::setExpires)
            .addMapping(Session::getDeviceId,SessionData::setDeviceId)
            .addMapping(Session::getValues,SessionData::setValues)
            .addMapping(Session::getAuthorities,SessionData::setAuthorities)
            .addMapping(Session::getContinuityId,SessionData::setContinuityId)
            .addMapping(Session::getClosedReason,SessionData::setClosedReason)
            .addMapping(Session::getReplacedBy,SessionData::setReplacedBy)
            .addMapping(Session::getCopiedFrom,SessionData::setCopiedFrom)
            .addMapping(Session::getLastActivity,SessionData::setLastActivity)
            .addMapping(Session::getLoginMethod,SessionData::setLoginMethod);
    }
}

//////////////////////////////////////////////////////////////////////////////
