package org.talend.esb.locator.proxy.client;

/**
 * Please modify this class to meet your needs
 * This class is not complete
 */

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.ws.wsaddressing.W3CEndpointReference;

import org.talend.esb.locator.proxy.service.LocatorProxyService;
import org.talend.esb.locator.proxy.service.LocatorProxyServiceImpl;

/**
 * This class was generated by Apache CXF 2.4.1 2011-07-28T12:18:44.296+03:00
 * Generated source version: 2.4.1
 * 
 */
public final class LocatorProxyService_Localhost_Client {

	private static final QName SERVICE_NAME = new QName(
			"http://service.proxy.locator.esb.talend.org/",
			"LocatorProxyServiceImpl");

	private LocatorProxyService_Localhost_Client() {
	}

	public static void main(String args[]) throws java.lang.Exception {
		URL wsdlURL = LocatorProxyServiceImpl.WSDL_LOCATION;
		if (args.length > 0 && args[0] != null && !"".equals(args[0])) {
			File wsdlFile = new File(args[0]);
			try {
				if (wsdlFile.exists()) {
					wsdlURL = wsdlFile.toURI().toURL();
				} else {
					wsdlURL = new URL(args[0]);
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}

		LocatorProxyServiceImpl ss = new LocatorProxyServiceImpl(wsdlURL,
				SERVICE_NAME);
		LocatorProxyService port = ss.getLocalhost();

		{
			System.out.println("Invoking registerEndpoint...");
			javax.xml.namespace.QName _registerEndpoint_serviceName = new javax.xml.namespace.QName(
					"http://services.talend.org/TestService", "TestServiceProvider");
			java.lang.String _registerEndpoint_endpointURL = "http://Service";
			org.talend.esb.locator.proxy.service.types.RegisterEndpointRequestType input = new org.talend.esb.locator.proxy.service.types.RegisterEndpointRequestType();
			input.setServiceName(_registerEndpoint_serviceName);
			input.setEndpointURL(_registerEndpoint_endpointURL);
			port.registerEndpoint(input);

		}
		{
			System.out.println("Invoking lookupEndpoint...");
			javax.xml.namespace.QName _lookupEndpoint_serviceName = new javax.xml.namespace.QName(
					"http://services.talend.org/TestService", "TestServiceProvider");
			org.talend.esb.locator.proxy.service.types.LookupRequestType input = new org.talend.esb.locator.proxy.service.types.LookupRequestType();
			input.setServiceName(_lookupEndpoint_serviceName);
			javax.xml.ws.wsaddressing.W3CEndpointReference _lookupEndpoint__return = port
					.lookupEndpoint(input);
			System.out.println("lookupEndpoint.result="
					+ _lookupEndpoint__return);

		}
		{
			System.out.println("Invoking lookupEndpoints...");
			javax.xml.namespace.QName _lookupEndpoints_serviceName = new javax.xml.namespace.QName(
					"http://services.talend.org/TestService", "TestServiceProvider");
			org.talend.esb.locator.proxy.service.types.LookupRequestType input = new org.talend.esb.locator.proxy.service.types.LookupRequestType();
			input.setServiceName(_lookupEndpoints_serviceName);
			org.talend.esb.locator.proxy.service.types.EndpointReferenceListType _lookupEndpoints__return = port
					.lookupEndpoints(input);
			List<W3CEndpointReference> endpointRefList = _lookupEndpoints__return
					.getReturn();
			System.out.println("lookupEndpoints.result="
					+ _lookupEndpoints__return);
			for (int i = 0; i < endpointRefList.size(); i++) {
				W3CEndpointReference endpoint = endpointRefList.get(i);
				System.out.println(" #" + i + " lookupEndpoint.result="
						+ endpoint);
			}

		}
		{
			System.out.println("Invoking unregisterEnpoint...");
			javax.xml.namespace.QName _unregisterEnpoint_serviceName = new javax.xml.namespace.QName(
					"http://services.talend.org/TestService", "TestServiceProvider");
			java.lang.String _unregisterEnpoint_endpointURL = "http://Service";
			org.talend.esb.locator.proxy.service.types.UnregisterEndpointRequestType input = new org.talend.esb.locator.proxy.service.types.UnregisterEndpointRequestType();
			input.setServiceName(_unregisterEnpoint_serviceName);
			input.setEndpointURL(_unregisterEnpoint_endpointURL);

			port.unregisterEnpoint(input);
			// System.out.println("unregisterEnpoint.result=" +
			// _unregisterEnpoint__return);

		}

		System.exit(0);
	}

}
