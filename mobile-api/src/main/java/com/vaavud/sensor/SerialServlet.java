package com.vaavud.sensor;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import com.vaavud.sensor.frequency.FrequencySensor;
import com.vaavud.server.api.ProtocolException;
import com.vaavud.server.api.util.ServiceUtil;

/**
 * Servlet implementation class SerialServlet
 */
@WebServlet("/SerialServlet")
public class SerialServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private Tester tester;   

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
	    
	    if (request.getParameter("json") != null) {
	        ServiceUtil.writeResponse(response, "{'status':1}", ServiceUtil.JSON_MIME_TYPE); 
	    } else {
	        tester = getTester();

//	      tester.toogleOnOff();
	        request.setAttribute("active", tester.active);
	        
	        request.getRequestDispatcher("/analysis/test.jsp").forward(request, response);
	    }
	    
	}

	
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	    
	    
	    
	    if (request.getParameter("action") == null && request.getParameter("status") == null) {
	        ServiceUtil.sendProtocolErrorResponse(response, new ProtocolException("No 'action' or 'status' parameter defined"));
	        return;
	    }
	    
	    tester = getTester();
        
	    if (request.getParameter("status") != null) {
	        if ("freq".equals(request.getParameter("status"))) {
	            ServiceUtil.writeResponse(response, tester.lastFreqEvent(), ServiceUtil.JSON_MIME_TYPE); 
	            return;
	        }
	        else {
	            ServiceUtil.sendProtocolErrorResponse(response, new ProtocolException("wrong 'status' arguments"));
	            return;
	        }
	    }
	    
	    
        if ("start".equals(request.getParameter("action"))) {
            tester.start();
            ServiceUtil.writeResponse(response, "{\"status\":1,\"action\":\"start\"}", ServiceUtil.JSON_MIME_TYPE); 
            return;
        } else if ("clear".equals(request.getParameter("action"))) {
            tester.clear();
            ServiceUtil.writeResponse(response, "{\"status\":1,\"action\":\"clear\"}", ServiceUtil.JSON_MIME_TYPE);
        }
        else {
            Long msId = tester.stop();
            
            Map<String, Object> jsonMap = new HashMap<String, Object>();
            jsonMap.put("status", 1);
            jsonMap.put("action", "stop");
            jsonMap.put("msId", msId);
            JSONObject jsMap = (JSONObject) JSONSerializer.toJSON( jsonMap );
            ServiceUtil.writeResponse(response,jsMap.toString(), ServiceUtil.JSON_MIME_TYPE);
            return;
        }
	    
	}
	
	private Tester getTester() {
        Context initCtx;
        Tester tester = null;
        try {
            initCtx = new InitialContext();
            Context envCtx = (Context) initCtx.lookup("java:comp/env");
            tester = (Tester) envCtx.lookup("bean/TesterFactory");
        } catch (NamingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return tester;
	    
    }
	
}
