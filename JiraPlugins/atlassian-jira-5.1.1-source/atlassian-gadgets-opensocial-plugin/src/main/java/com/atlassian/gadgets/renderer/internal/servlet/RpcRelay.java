package com.atlassian.gadgets.renderer.internal.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The fallback Shindig RPC mechanism for all browsers is IFRC (Inter Fame Relay Communication).  When this mechanism
 * is used an invisible iframe is created inside the gadget using the URL to this servlet, which should be on the same
 * domain as the container.  This invisible iframe is able to talk to it's parents parent, which is the container,
 * providing a way for the gadget to pass a messages to the container.
 */
public class RpcRelay extends HttpServlet
{
    private static final String SCRIPT =
        "<script>\n"
      + "var u = location.href, h = u.substr(u.indexOf('#') + 1).split('&'), t, r;\n"
      + "try {\n"
      + "t = h[0] === '..' ? parent.parent : parent.frames[h[0]];\n"
      + "r = t.gadgets.rpc.receive;\n"
      + "} catch (e) {\n"
      + "}\n"
      + "r && r(h);\n"
      + "</script>";

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        response.setContentType("text/html");
        response.getWriter().write(SCRIPT);
    }
}
