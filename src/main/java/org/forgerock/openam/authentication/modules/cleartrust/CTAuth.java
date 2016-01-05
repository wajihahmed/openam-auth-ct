/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2005 Sun Microsystems Inc. All Rights Reserved
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * https://opensso.dev.java.net/public/CDDLv1.0.html or
 * opensso/legal/CDDLv1.0.txt
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at opensso/legal/CDDLv1.0.txt.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * $Id: CTAuth.java,v 1.0 2015/13/31 05:41:55 wahmed Exp $
 *
 */

/**
 * Portions Copyrighted [2015] [ForgeRock AS]
 */

package org.forgerock.openam.authentication.modules.cleartrust;

import java.io.*;
import java.util.*;
import java.util.Map;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Enumeration;
import java.security.Principal;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.LoginException;

import com.sun.identity.authentication.spi.AMLoginModule;
import com.sun.identity.authentication.spi.AuthLoginException;
import com.sun.identity.authentication.util.ISAuthConstants;
import com.sun.identity.shared.datastruct.CollectionHelper;
import com.sun.identity.shared.debug.Debug;

import sirrus.runtime.*;


/**
 * Custom authentication module for validating ClearTrust user session
 * to enable SSO from ClearTrust to OpenAM.
 */

public class CTAuth extends AMLoginModule {

    private static final String COOKIE_NAME = "CTCookieName";
    //private static final String SERVER_IP = "DispatchServerIPAddress";
    private static final String TRUST_REMOTE_USER_HEADER_ONLY = "TrustRemoteUserHeaderOnly";
    private static final String HOSTNAME = "DispatchServer";
    private static final String SERVER_PORT = "DispatchServerPort";
    private static final String REMOTE_USER_HEADER_NAME = "RemoteUserHeaderName";
    private static final String AUTHLEVEL = "iplanet-am-auth-ctauth-auth-level";


    private String ctCookieName = null;
    //private String DispatchServerIP = null;
    private String dispatchServer = null;
    private int dispatchServerPort = 5608;
    private String remoteUserHeader = "REMOTE_USER";
    private boolean trustRemoteUserHeaderOnly = false;
    private String authLevel = null;

    private RuntimeAPI runtimeAPI = null;
    private String userId = null;
    private Principal userPrincipal = null;
    private static Debug debug = Debug.getInstance("amAuthCT");

    public CTAuth() {
       super();
       if (debug.messageEnabled()) {
             debug.message("CTAuth calling super()");
       }
    }

    /**
     * Initialize the authentication module with it's configuration
     */

    public void init(Subject subject, Map sharedState, Map options) {
        if (debug.messageEnabled()) {
             debug.message("CTAuth initialization" + options);
        }

        ctCookieName = CollectionHelper.getMapAttr(options, COOKIE_NAME, "CTSESSION");

        //DispatchServerIP = CollectionHelper.getMapAttr(options, SERVER_IP);

        dispatchServer = CollectionHelper.getMapAttr(options, HOSTNAME);

        dispatchServerPort = Integer.parseInt(CollectionHelper.getMapAttr(options, SERVER_PORT, "5608"));

        remoteUserHeader = CollectionHelper.getMapAttr(options,
                           REMOTE_USER_HEADER_NAME, "REMOTE_USER");

        trustRemoteUserHeaderOnly = Boolean.valueOf(CollectionHelper.getMapAttr(
                   options, TRUST_REMOTE_USER_HEADER_ONLY, "false")).booleanValue();

        authLevel = CollectionHelper.getMapAttr(options, AUTHLEVEL);

        if (authLevel != null) {
            try {
                setAuthLevel(Integer.parseInt(authLevel));
            } catch (Exception e) {
                debug.error("{}: Unable to set auth level {}", "CTAuth", authLevel);
            }
        }


    }

    /**
     * This method process the login procedure for this authentication
     * module. If the administrator checks to just validate the HTTP headers
     * set by the agent then no further validation of the session will be done.
     */

    public int process(Callback[] callbacks, int state)
                 throws AuthLoginException {

        HttpServletRequest request = getHttpServletRequest();
        ServerDescriptor dispatcher = null;

        if(trustRemoteUserHeaderOnly) {
           Enumeration headers = request.getHeaderNames();
           while(headers.hasMoreElements()) {
               String headerName = (String)headers.nextElement();
               if(headerName.equals(remoteUserHeader)) {
                  userId = request.getHeader(headerName);
               }
           }
           if(userId == null) {
              throw new AuthLoginException("No remote user header found");
           }
           return ISAuthConstants.LOGIN_SUCCEED;
        }

        Cookie[] cookies = request.getCookies();
        boolean cookieFound = false;
        boolean apiStatus = false;

        if (cookies != null) {
          for (int i=0; i < cookies.length; i++) {
                  Cookie cookie = cookies[i];

            if(cookie.getName().equals(ctCookieName)) {
                cookieFound = true;
                String value = cookie.getValue();

                if (debug.messageEnabled()) {
                   debug.message("CT Session cookie value: " + value);
                }

                //value = java.net.URLEncoder.encode(value);

                // URLDecode the token, if necessary
                if (value!= null && value.matches(".*%.*")) {
                try {
                  value = java.net.URLDecoder.decode(value, "UTF-8");
                  } catch (UnsupportedEncodingException ex) {
                    ex.printStackTrace();
                  }
                }

                if (debug.messageEnabled()) {
                   debug.message("cookie value afer replacing: " + value);
                }

                ClearTrust apiClient = new ClearTrust();
                apiStatus = apiClient.connect(value);

                // Disconnect from ClearTrust
                apiClient.disconnect();
            }
          }
        }

        if (apiStatus && cookieFound) {
          if (debug.messageEnabled()) {
                debug.message("{}: Returning Success.", "CTAuth");
          }
          return ISAuthConstants.LOGIN_SUCCEED;
        }
        else {
          if (debug.messageEnabled()) {
                debug.message("{}: Returning Fail.", "CTAuth");
          }
          throw new AuthLoginException("CTAuth: Authentication Failed");
        }

    }

/**
* This method is inspired from RuntimeExample.java from the CT SDK Documentation
*/

  public class ClearTrust  {

        private RuntimeAPI runtimeAPI = null;

        private boolean connect(String value) throws AuthLoginException
        {
        ServerDescriptor dispatcher = null;

        if (debug.messageEnabled()) {
                  debug.message("Cookie value: " + value);
        }

        dispatcher = new ServerDescriptor(dispatchServer,
                                          dispatchServerPort,
                                          ServerDescriptor.SSL_ANON);

        try{
            runtimeAPI = APIFactory.createFromServerDispatcher(dispatcher);
            // Validate and touch the session
            String nowToken = runtimeAPI.validateToken(value);

            userId = runtimeAPI.getTokenValue(nowToken, TokenKeys.SC_USER_ID);

            if (debug.messageEnabled()) {
                  debug.message("userId: " + userId);
            }
        }
        catch( Exception e ){

          if (e instanceof TokenException) {
              debug.error("Existing Token is invalid or cannot be updated");
          }

          if (e instanceof RuntimeAPIException) {
              debug.error("Error connecting to dispatch server " +
                        dispatchServer + ":" + dispatchServerPort);
          }

          if (e instanceof SecurityException) {
              debug.error("Adequate credentials were not supplied to the RuntimeAPI");
          }

          e.printStackTrace();

          throw new AuthLoginException(e.getMessage());
        }

        return true;
      }

      private void disconnect()
      {
        if (runtimeAPI != null)
            runtimeAPI.close();
      }

  }

    /**
     * Returns the authenticated principal. This is consumed by the authentication
     * framework to set the principal
     */

    public java.security.Principal getPrincipal() {
        if (userPrincipal != null) {
            return userPrincipal;
        } else if (userId != null) {
            userPrincipal = new CTPrincipal(userId);
            return userPrincipal;
        } else {
            return null;
        }
    }
}
