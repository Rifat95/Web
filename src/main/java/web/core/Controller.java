package web.core;

public abstract class Controller {
  protected App app;
  protected Request req;
  protected Session session;
  protected AppUser appUser;
  protected Page page;
  protected Translator t;

  public Controller() {
    app = App.getInstance();
    req = app.getRequest();
    session = app.getSession();
    appUser = app.getUser();
    page = app.getPage();
    t = app.getT();
  }
}
