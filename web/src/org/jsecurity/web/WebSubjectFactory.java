/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jsecurity.web;

import org.jsecurity.mgt.DefaultSubjectFactory;
import org.jsecurity.mgt.SecurityManager;
import org.jsecurity.mgt.SessionSubjectBinder;
import org.jsecurity.session.Session;
import org.jsecurity.subject.PrincipalCollection;
import org.jsecurity.subject.Subject;
import org.jsecurity.web.session.WebSessionManager;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.net.InetAddress;

/**
 * @author Les Hazlewood
 * @since 1.0
 */
public class WebSubjectFactory extends DefaultSubjectFactory {

    public static final String PRINCIPALS_SESSION_KEY = SessionSubjectBinder.PRINCIPALS_SESSION_KEY;
    public static final String AUTHENTICATED_SESSION_KEY = SessionSubjectBinder.AUTHENTICATED_SESSION_KEY;

    WebSessionManager webSessionManager;

    public WebSubjectFactory() {
    }

    public WebSubjectFactory(SecurityManager securityManager) {
        super(securityManager);
    }

    public WebSubjectFactory(SecurityManager securityManager, WebSessionManager webSessionManager) {
        super(securityManager);
        setWebSessionManager(webSessionManager);
    }

    public WebSessionManager getWebSessionManager() {
        return webSessionManager;
    }

    public void setWebSessionManager(WebSessionManager webSessionManager) {
        this.webSessionManager = webSessionManager;
    }

    protected PrincipalCollection getPrincipals(Session existing) {
        PrincipalCollection principals = null;
        if (existing != null) {
            principals = (PrincipalCollection) existing.getAttribute(PRINCIPALS_SESSION_KEY);
        }
//        if (principals == null) {
//            //check remember me:
//            principals = getRememberedIdentity();
//            if (principals != null && existing != null) {
//                existing.setAttribute(PRINCIPALS_SESSION_KEY, principals);
//            }
//        }
        return principals;
    }

    protected boolean isAuthenticated(Session session) {
        Boolean value = null;
        if (session != null) {
            value = (Boolean) session.getAttribute(AUTHENTICATED_SESSION_KEY);
        }
        return value != null && value;
    }

    protected Session getWebSession() {
        ServletRequest request = WebUtils.getRequiredServletRequest();
        ServletResponse response = WebUtils.getRequiredServletResponse();
        return getWebSessionManager().getSession(request, response);
    }

    @Override
    public Subject createSubject(PrincipalCollection principals, Session existing, boolean authenticated, InetAddress inetAddress) {
        Session session = existing;
        if (session == null) {
            session = getWebSession();
        }

        PrincipalCollection pc = principals;
        if (pc == null) {
            pc = getPrincipals(session);
        }

        boolean authc = authenticated;
        if (!authc) {
            //check session to be sure:
            authc = isAuthenticated(session);
        }

        InetAddress inet = inetAddress;
        if (inet == null) {
            inet = WebUtils.getInetAddress(WebUtils.getRequiredServletRequest());
        }

        return super.createSubject(pc, session, authc, inet);
    }
}