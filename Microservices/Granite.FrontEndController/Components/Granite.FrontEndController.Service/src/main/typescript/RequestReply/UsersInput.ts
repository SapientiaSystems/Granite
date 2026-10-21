//////////////////////////////////////////////////////////////////////////////
// Granite.FrontEndController.Service
//////////////////////////////////////////////////////////////////////////////

import type { FrontEndInput }                    from "strata.frontendcontroller.service/RequestReply";
import type { CreateUserData, UpdateUserData }   from "granite.user.service/RequestReply";

export
interface UsersInput
    extends FrontEndInput
{
    usersToCreate:   Array<CreateUserData>;
    usersToUpdate:   Array<UpdateUserData>;
    userIdsToDelete: Array<number>;
}

//////////////////////////////////////////////////////////////////////////////
