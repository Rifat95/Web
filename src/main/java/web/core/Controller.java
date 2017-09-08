package web.core;

public abstract class Controller {
	protected App app;
	protected Page page;
	protected Request req;
	protected Translator t;

	public Controller() {
		app = App.getInstance();
		page = app.getPage();
		req = app.getRequest();
		t = app.getT();
	}
}
