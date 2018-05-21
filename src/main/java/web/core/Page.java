package web.core;

import java.io.IOException;
import java.util.ArrayList;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;

public final class Page {
	private Object response;
	private String redirection;
	private ArrayList<Message> messages;

	@SuppressWarnings("unchecked")
	public Page() {
		messages = (ArrayList<Message>) App.getInstance().getSession().get("messages", new ArrayList<>());
	}

	public void setResponse(View v) {
		response = v;
	}

	public void setResponse(JSONObject jo) {
		response = jo;
	}

	public void addMessage(String type, String content) {
		addMessage(new Message(type, content));
	}

	public void addMessage(Message m) {
		messages.add(m);
	}

	/**
	 * This method ensure that user will be redirected after the end of execution.
	 * To immediatly stop code execution and redirect the user, use RedirectionException.
	 *
	 * @param location the absolute URI or URL
	 */
	public void setRedirection(String location) {
		redirection = location;
	}

	/**
	 * Must be called at the end of core.Servlet.process() only.
	 */
	void send() {
		HttpServletResponse servletResponse = App.getInstance().getResponse();
		String contentType = "text/html;charset=UTF-8";
		String output = "";

		if (response instanceof View) {
			output = response.toString();
		} else if (response instanceof JSONObject) {
			contentType = "application/json;charset=UTF-8";
			output = response.toString();
		}

		try {
			if (redirection != null) {
				servletResponse.sendRedirect(redirection);
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
