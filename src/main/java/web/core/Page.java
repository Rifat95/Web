package web.core;

import java.util.ArrayList;
import javax.servlet.http.HttpServletResponse;
import org.json.simple.JSONObject;
import web.util.JsonObject;

public final class Page {
	public static final String FULL = "full";
	public static final String VIEW = "view";
	public static final String JSON = "json";

	private App app;
	private String title;
	private Object response;
	private String renderMode;
	private ArrayList<Message> messages;

	@SuppressWarnings("unchecked")
	public Page() {
		app = App.getInstance();
		renderMode = app.getRequest().get("rm", FULL);
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
		renderMode = JSON; // Override render mode because json can't be displayed as html
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

		switch (renderMode) {
		case FULL:
			String msgOutput = "";
			if (!messages.isEmpty()) {
				msgOutput = new View("messages", View.CORE)
					.add("messages", messages)
					.toString();
				messages.clear();
			}

			output = new View("body", View.CORE)
				.add("title", title)
				.add("messages", msgOutput)
				.add("content", response.toString())
				.toString();
			break;
		case VIEW:
			output = response.toString();
			break;
		case JSON:
			contentType = "application/json;charset=UTF-8";

			if (response instanceof View) {
				View v = (View) response;
				output = new JsonObject(new JSONObject(v.getData())).toString();
			} else {
				output = response.toString();
			}
			break;
		}

		try {
			servletResponse.setCharacterEncoding("UTF-8");
			servletResponse.setContentType(contentType);
			servletResponse.getWriter().write(output);
		} catch (Exception e) {
			// Ignore
		}
	}
}
