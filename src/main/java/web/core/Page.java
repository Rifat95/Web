package web.core;

import java.io.IOException;
import java.util.ArrayList;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import web.util.Util;

public final class Page {
	private String title;
	private Object response;
	private String renderMode;
	private ArrayList<Message> messages;

	@SuppressWarnings("unchecked")
	public Page() {
		App app = App.getInstance();
		renderMode = app.getRequest().get("rm", "full");
		messages = (ArrayList<Message>) app.getSession().get("messages", new ArrayList<>());
	}

	public void setTitle(String s) {
		title = s;
	}

	public void setView(View v) {
		response = v;
	}

	public void setJson(JSONObject jo) {
		response = jo;
		renderMode = "json"; // Override render mode because json can't be displayed as html
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
		response = location;
		renderMode = "redirection";
	}

	/**
	 * Must be called at the end of core.Servlet.process() only.
	 */
	void send() {
		HttpServletResponse servletResponse = App.getInstance().getResponse();
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
				output = new JSONObject(v.getData()).toString();
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
