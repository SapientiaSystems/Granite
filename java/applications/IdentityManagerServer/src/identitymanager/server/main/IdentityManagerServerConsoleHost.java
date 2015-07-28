// ##########################################################################
// # File Name:	IdentityManagerServerConsoleHost.java
// #
// # Copyright:	2015, Sapientia Systems, LLC. All Rights Reserved.
// #
// # License:	This file is part of the IdentityManagerServer Framework.
// #
// #   			The IdentityManagerServer Framework is free software: you 
// #			can redistribute it and/or modify it under the terms of 
// #			the GNU Lesser General Public License as published by
// #    		the Free Software Foundation, either version 3 of the 
// #			License, or (at your option) any later version.
// #
// #    		The IdentityManagerServer Framework is distributed in the 
// #			hope that it will be useful, but WITHOUT ANY WARRANTY; 
// #			without even the implied warranty of MERCHANTABILITY or 
// #			FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser 
// #			General Public License for more details.
// #
// #    		You should have received a copy of the GNU Lesser 
// #			General Public License along with the IdentityManagerServer
// #			Framework. If not, see http://www.gnu.org/licenses/.
// ##########################################################################

package identitymanager.server.main;

import java.io.IOException;

/****************************************************************************
 * 
 * @author 		
 *     Sapientia Systems
 * @conventions	
 *     <a href="{@docRoot}/NamingConventions.html">Naming Conventions</a>
 */
public 
class IdentityManagerServerConsoleHost
{

    /************************************************************************
     * Creates a new IdentityManagerServerConsoleHost. 
     *
     */
    public 
    IdentityManagerServerConsoleHost() {}

    /************************************************************************
     *  
     *
     * @param args
     */
    public static void 
    main(String[] args)
    {
        IdentityManagerServerApp application = 
            new IdentityManagerServerApp();
        
        try
        {
            System.out.println( "debug A" );
            application.start();
            
            System.out.print( "Hit enter to exit application:" );
            
            try
            {
                System.in.read();
            }
            catch (IOException e)
            {
                throw new IllegalStateException(e);
            }

            System.out.println( "debug B" );
            application.stop();
            System.exit( 0 );
        }
        catch (Exception e)
        {
            e.printStackTrace( System.out );
            System.exit( -1 );
        }
    }
    
}

// ##########################################################################
