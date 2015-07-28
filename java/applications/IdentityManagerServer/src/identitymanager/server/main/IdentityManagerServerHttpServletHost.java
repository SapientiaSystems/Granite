// ##########################################################################
// # File Name:	IdentityManagerServerHttpServletHost.java
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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

/****************************************************************************
 * 
 * @author 		
 *     Sapientia Systems
 * @conventions	
 *     <a href="{@docRoot}/NamingConventions.html">Naming Conventions</a>
 */
public 
class IdentityManagerServerHttpServletHost 
    extends HttpServlet
{

    private static final long        serialVersionUID = 6283602574940782783L;
    
    private IdentityManagerServerApp itsApplication;

    /************************************************************************
     * Creates a new TicTacToeServerHttpServletHost. 
     *
     */
    public 
    IdentityManagerServerHttpServletHost()
    {
        itsApplication = null;
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public void 
    init() 
        throws ServletException
    {
        super.init();
        
        try
        {
            itsApplication = new IdentityManagerServerApp();
            itsApplication.start();
        }
        catch (Exception e)
        {
            throw new ServletException( e );
        }
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public void 
    destroy()
    {
        try
        {
            itsApplication.stop();
            super.destroy();
        }
        catch (Exception e)
        {
            e.printStackTrace( System.err );
        }
    }
}

// ##########################################################################
