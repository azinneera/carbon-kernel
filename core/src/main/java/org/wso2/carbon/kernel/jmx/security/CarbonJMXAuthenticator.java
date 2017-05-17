/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.kernel.jmx.security;


import org.slf4j.Logger;
import org.wso2.carbon.kernel.Constants;
import org.wso2.carbon.security.caas.api.exception.CarbonSecurityServerException;
import org.wso2.carbon.security.caas.api.model.User;
import org.wso2.carbon.security.caas.api.util.CarbonSecurityUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.management.remote.JMXAuthenticator;
import javax.management.remote.JMXPrincipal;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;



/**
 * Implementation class for JMXAuthenticator.
 *
 * @since 5.1.0
 */
public class CarbonJMXAuthenticator implements JMXAuthenticator {

    private static Logger audit = Constants.AUDIT_LOG;

    private static final String JMX_MONITOR_ROLE = "monitorRole";
    private static final String JMX_CONTROL_ROLE = "controlRole";
    private static final String JMX_USER_READONLY_PERMISSION = "/permission/protected/server-admin/jmx/readonly";
    private static final String JMX_USER_READWRITE_PERMISSION = "/permission/protected/server-admin/jmx/readwrite";

    @Override
    public Subject authenticate(Object credentials) {
        if (credentials == null) {
            throw new SecurityException("Credentials required");
        }

        if (!(credentials instanceof String[])) {
            throw new SecurityException("Credentials should be String[]");
        }

        CallbackHandler callbackHandler = new CarbonJMXCallbackHandler(credentials);

        try {
            LoginContext loginContext = new LoginContext(Constants.LOGIN_MODULE_ENTRY, callbackHandler);
            loginContext.login();

            return new Subject(true, Collections.singleton(new JMXPrincipal(authorize(((String[]) credentials)[0]))),
                    Collections.EMPTY_SET, Collections.EMPTY_SET);
        } catch (LoginException e) {
            throw new SecurityException("Invalid credentials", e);
        }
    }

    public String authorize(String username) throws CarbonSecurityServerException {

        User user = CarbonSecurityUtils.getUser(username);
        String strPermission = user.getPermission();
        String roleName = null;
        List<String> permissionList = Arrays.asList(strPermission.split(","));

        if (permissionList.contains(JMX_USER_READWRITE_PERMISSION)) {
            roleName = JMX_CONTROL_ROLE;
        } else if (permissionList.contains(JMX_USER_READONLY_PERMISSION)) {
            roleName = JMX_MONITOR_ROLE;
        }

        if (roleName != null) {
            audit.info("User: " + username + " successfully authorized as " +
                    roleName + " to perform JMX operations.");

            return roleName;
        } else {
            throw new SecurityException("User: " + username + " not authorized to perform JMX operations.");
        }

    }

}
