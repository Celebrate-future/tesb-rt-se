/*
 * #%L
 * Service Activity Monitoring :: Server
 * %%
 * Copyright (C) 2011 Talend Inc.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.talend.esb.sam.server.ui;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * The JSON API servlet serves simple requests
 * 
 * @author zubairov
 *
 */
@SuppressWarnings("serial")
public class APIServlet extends HttpServlet {
	
	private Logger log = LoggerFactory.getLogger(APIServlet.class);
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		resp.setContentType("application/json");
		try {
			WebApplicationContext ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(this.getServletContext());
			long start = req.getParameter("start") == null ? 1 : Long.parseLong(req.getParameter("start"));
			long limit = req.getParameter("limit") == null ? 10 : Long.parseLong(req.getParameter("limit"));
			Map<String, String> attrs = new HashMap<String, String>();
			@SuppressWarnings("rawtypes")
			Enumeration parameterNames = req.getParameterNames();
			while(parameterNames.hasMoreElements()) {
				String name = (String) parameterNames.nextElement();
				String value = (String) req.getParameter(name);
				if (value != null) {
					attrs.put(name, value);
				}
			}
			UIProvider provider = (UIProvider) ctx.getBean("uiProvider");
			CriteriaAdapter adapter = new CriteriaAdapter(start, limit, attrs);
			JsonObject result = provider.getEvents(start, adapter);
			resp.getWriter().println(result);
		} catch (Exception e) {
			log.error("Exception processing request", e);
			resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			resp.getWriter().println(toJSON(e));
		}
	}

	/**
	 * Converts {@link Exception} to {@link JsonObject}
	 */
	private JsonObject toJSON(Exception e) {
		JsonObject result = new JsonObject();
		result.add("message", new JsonPrimitive(String.valueOf(e.getMessage())));
		return result;
	}

}
