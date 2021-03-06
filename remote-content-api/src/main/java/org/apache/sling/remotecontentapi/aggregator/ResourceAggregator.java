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
 */

package org.apache.sling.remotecontentapi.aggregator;

import java.util.Map;

import javax.json.Json;
import javax.json.JsonObjectBuilder;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.remotecontentapi.rcaservlet.P;

public class ResourceAggregator {
    public static final String SLING_RESOURCE_TYPE = "sling:resourceType";
    
    public static interface Context {
        /** Return the full URL to access the given path */
        String getUrlForPath(String path, boolean includeApiSelectorsAndExtension);

        /** Return r's path relative to the Resource being rendered */
        String getRelativePath(Resource r);
    }

    private final Context context;
    private final Resource resource;

    private void addProperties(JsonObjectBuilder json, Resource r, ValueMap vm) {
        if(vm != null) {
            // Add an _id to all components which have a resource type
            if(vm.containsKey(SLING_RESOURCE_TYPE)) {
                json.add("_id", r.getPath());
            }

            for(Map.Entry<String, Object> entry : vm.entrySet()) {
                processProperty(json, entry.getKey(), entry.getValue());
            }
        }
    }

    private void addChild(JsonObjectBuilder parent, JsonObjectBuilder childJson, Resource r, ValueMap vm) {
        if(context.getRelativePath(r).isEmpty()) {
            // at content root, do not add intermediate element
            parent.addAll(childJson);
        } else {
            if("jcr:content".equals(r.getName())) {
                parent.add("_composite", childJson);
            } else {
                parent.add(r.getName(), childJson);
            }
        }
    }

    /** True if r is a "document" like a website
     *  page, that shouldn't be recursed into when
     *  visiting content.
     */
    private static boolean isDocument(Resource r) {
        // TODO shouldn't be hardcoded, of course...but we'll need
        // to differentiate between our first-class content objects
        // and their content.
        return isNodeType(r.adaptTo(ValueMap.class), "cq:Page");
    }

    private void addTo(JsonObjectBuilder parentJson, Resource r) {
        final ValueMap vm = r.adaptTo(ValueMap.class);

        if(isNodeType(vm, "nt:file")) {
            // nt:file nodes: emit just a link
            parentJson.add("file", Json.createObjectBuilder()
                .add("url", context.getUrlForPath(r.getPath(), false))
            );
        } else {
            // general node: add a child JSON node with its name, properties and non-document child nodes
            final JsonObjectBuilder childJson = Json.createObjectBuilder();
            addProperties(childJson, r, vm);
            for(Resource child : r.getChildren()) {
                if(!isDocument(child)) {
                    addTo(childJson, child);
                }
            }
            addChild(parentJson, childJson, r, vm);
        }
    }

    private static boolean isNodeType(ValueMap vm, String nodeType) {
        return vm == null ? false : nodeType.equals(vm.get("jcr:primaryType", String.class));
    }

    private void processProperty(JsonObjectBuilder json, String key, Object value) {
        if(value != null) {
            final String newName = processPropertyName(key);
            if(newName != null) {
                P.addValue(json, newName, value);
            }
        }
    }

    private String processPropertyName(String propertyName) {
        if(!propertyName.contains(":")) {
            return propertyName;
        } else if(propertyName.equals("jcr:title")) {
            return "_title";
        } else if(propertyName.equals("jcr:description")) {
            return "_description";
        } else if(propertyName.equals(SLING_RESOURCE_TYPE)) {
            return "_componentType";
        } else {
            return null;
        }
    }

    public ResourceAggregator(Resource r, Context ctx) {
        context = ctx;
        resource = r;
    }

    public void addTo(JsonObjectBuilder content) {
        addTo(content, resource);
    }
}