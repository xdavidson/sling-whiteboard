/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.apache.sling.auth.saml2.sync;

import java.util.Map;
import java.util.Set;

public class Saml2User {
    private String id;
    private String username;
    private Map userProperties;
    private Set groupMembership;

    public String getUsername() {
        return username;
    }

    public String getId() {
        return id;
    }

    public Map getUserProperties() {
        return userProperties;
    }

    public Set getGroupMembership() {
        return groupMembership;
    }


    public void setUsername(String username) {
        this.username = username;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUserProperties(Map userProperties) {
        this.userProperties = userProperties;
    }

    public void setGroupMembership(Set groupMembership) {
        this.groupMembership = groupMembership;
    }
}
