package web.core;

import java.io.IOException;
import java.util.ArrayList;
import javax.servlet.http.HttpServletResponse;
import org.json.simple.JSONObject;
import web.util.JsonObject;
import web.util.Util;

public final class Page {
	private App app;
	private String title;
	private Object response;
	private String renderMode;
	private ArrayList<Message> messages;

	@SuppressWarnings("unchecked")
	public Page() {
		app = App.getInstance();
		renderMode = app.getRequest().get("rm", "full");
		messages = (ArrayList<Message>) app.getSession().get("messages", new ArrayList<>());
	}

	public void setTitle(String s) {
		title = s;
	}

	public void setView(View v) {
		response = v;
	}

	public void setJson(JsonObject jo) {
		response = jo;
		renderMode = "json"; // Override render mode because json can't be displayed as html
	}

	public void setRedirection(String location) {
		response = location.startsWith("http") ? location : Util.uri(location);
		renderMode = "redirection";
	}

	public void addInfo(String msg) {
		messages.add(new Message(Message.INFO, msg));
	}

	public void addMessage(String msg) {
		messages.add(new Message(Message.SUCCESS, msg));
	}

	public void addWarning(String msg) {
		messages.add(new Message(Message.WARNING, msg));
	}

	/**
	 * Must be called at the end of core.Servlet.process() only.
	 */
	public void send() {
		HttpServletResponse servletResponse = app.getResponse();
		String contentType = "text/html;charset=UTF-8";
		String output = "";
		boolean redirect = false;

		switch (renderMode) {
		case "full":
			String msgOutput = "";
			if (!messages.isEmpty()) {
				msgOutput = new View("core/messages")
					.add("messages", messages)
					.toString();
				messages.clear();
			}

			output = new View("core/body")
				.add("title", title)
				.add("messages", msgOutput)
				.add("content", response.toString())
				.toString();
			break;
		case "view":
			output = response.toString();
			break;
		case "json":
			contentType = "application/json;charset=UTF-8";

			if (response instanceof View) {
				View v = (View) response;
				output = new JSONObject(v.getData()).toJSONString();
			} else {
				output = response.toString();
			}
			break;
		case "redirection":
			output = response.toString();
			redirect = true;
			break;
		}

		try {
			if (redirect) {
				servletResponse.sendRedirect(output);
			} else {
				servletResponse.setCharacterEncoding("UTF-8");
				servletResponse.setContentType(contentType);
				servletResponse.getWriter().write(output);
			}
		} catch (IOException e) {
			// Ignore
		}
	}
}
