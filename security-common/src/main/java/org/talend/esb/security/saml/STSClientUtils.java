/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.talend.esb.security.saml;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.cxf.Bus;
import org.apache.cxf.ws.security.SecurityConstants;
import org.apache.cxf.ws.security.trust.STSClient;

public class STSClientUtils {

    private static final String STS_WSDL_LOCATION = "sts.wsdl.location";
    private static final String STS_X509_WSDL_LOCATION = "sts.x509.wsdl.location";
    private static final String STS_NAMESPACE = "sts.namespace";
    private static final String STS_SERVICE_NAME = "sts.service.name";
    private static final String STS_ENDPOINT_NAME = "sts.endpoint.name";
    private static final String STS_X509_ENDPOINT_NAME = "sts.x509.endpoint.name";

    private STSClientUtils() {
    }

    public static STSClient createSTSClient(Bus bus, Map<String, String> stsProps) {
        STSClient stsClient = createClient(bus, stsProps);

        stsClient.setWsdlLocation(stsProps.get(STS_WSDL_LOCATION));
        stsClient.setEndpointQName(new QName(stsProps.get(STS_NAMESPACE), stsProps.get(STS_ENDPOINT_NAME)));
//        props.put(SecurityConstants.CALLBACK_HANDLER, new PasswordCallbackHandler(
//                stsProps.get(SecurityConstants.USERNAME), stsProps.get(SecurityConstants.PASSWORD)));

        return stsClient;
    }

    public static STSClient createSTSX509Client(Bus bus, Map<String, String> stsProps) {
        STSClient stsClient = createClient(bus, stsProps);

        stsClient.setWsdlLocation(stsProps.get(STS_X509_WSDL_LOCATION));
        stsClient.setEndpointQName(new QName(stsProps.get(STS_NAMESPACE), stsProps.get(STS_X509_ENDPOINT_NAME)));

        return stsClient;
    }

    private static STSClient createClient(Bus bus, Map<String, String> stsProps) {
        STSClient stsClient = new STSClient(bus);
        stsClient.setServiceQName(new QName(stsProps.get(STS_NAMESPACE), stsProps.get(STS_SERVICE_NAME)));

        Map<String, Object> props = new HashMap<String, Object>();
        for (Map.Entry<String, String> entry : stsProps.entrySet()) {
            if (SecurityConstants.ALL_PROPERTIES.contains(entry.getKey())) {
                props.put(entry.getKey(), processFileURI(entry.getValue()));
            }
        }
        stsClient.setProperties(props);

        return stsClient;
    }

    public static void applyAuthorization(final STSClient stsClient, String role) {
        if (null != role && role.length() > 0) {
            stsClient.setClaims(ClaimsBuilder.createClaimValue(role));
        } else {
            stsClient.setClaims(ClaimsBuilder.createClaimType());
        }
    }

    private static Object processFileURI(String fileURI) {
        if (null != fileURI && fileURI.startsWith("file:")) {
            try {
                return new URL(fileURI);
            } catch (MalformedURLException e) {
            }
        }
        return fileURI;
    }

}
