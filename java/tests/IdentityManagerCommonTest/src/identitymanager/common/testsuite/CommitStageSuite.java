// ##########################################################################
// # File Name: CommitStageSuite.java
// #
// # Copyright: 2011, Sapientia Systems, LLC. All Rights Reserved.
// #
// # License:   This file is part of the StrataEntity Framework.
// #
// #            The StrataSwtInteractor Framework is free software: you 
// #            can redistribute it and/or modify it under the terms of 
// #            the GNU Lesser General Public License as published by
// #            the Free Software Foundation, either version 3 of the 
// #            License, or (at your option) any later version.
// #
// #            The StrataSwtInteractor Framework is distributed in the 
// #            hope that it will be useful, but WITHOUT ANY WARRANTY; 
// #            without even the implied warranty of MERCHANTABILITY or 
// #            FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser 
// #            General Public License for more details.
// #
// #            You should have received a copy of the GNU Lesser 
// #            General Public License along with the StrataSwtInteractor
// #            Framework. If not, see http://www.gnu.org/licenses/.
// ##########################################################################

package identitymanager.common.testsuite;

import identitymanager.common.utility.IdentifierGeneratorTest;
import identitymanager.common.utility.TimestampGeneratorTest;
import identitymanager.common.utility.UtilityModuleTest;
import identitymanager.common.value.EmailAddressTest;
import identitymanager.common.value.PersonNameTest;
import identitymanager.common.value.PhoneNumberTest;
import identitymanager.common.value.PostalAddressTest;
import identitymanager.common.value.SecurityQuestionTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * 
 * @author 		
 *     Sapientia Systems 
 * @conventions	
 *     <a href="{@docRoot}/NamingConventions.html">Naming Conventions</a>
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    IdentifierGeneratorTest.class,
    TimestampGeneratorTest.class,
    UtilityModuleTest.class,
    PersonNameTest.class,
    EmailAddressTest.class,
    PhoneNumberTest.class,
    PostalAddressTest.class,
    SecurityQuestionTest.class})
public 
class CommitStageSuite {}

// ##########################################################################
