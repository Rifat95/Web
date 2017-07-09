package web.core;

import java.util.ArrayList;

public final class Page {
	public static final String FULL = "full";
	public static final String VIEW = "view";
	public static final String JSON = "json";

	private App app;
	private String title;
	private View view;
	private String renderMode;
	private ArrayList<Message> messages;

	@SuppressWarnings("unchecked")
	public Page() {
		app = App.getInstance();
		messages = (ArrayList<Message>) app.getSessAttr("messages", new ArrayList<>(5));
	}

	public void setTitle(String s) {
		title = s;
	}

	public void setView(View v) {
		view = v;
	}

	public void setRenderMode(String rm) {
		renderMode = rm;
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

	public void send() {
		//...
	}
}
