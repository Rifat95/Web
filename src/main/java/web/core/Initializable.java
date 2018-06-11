package web.core;

import javax.servlet.ServletContext;
import web.util.ForbiddenException;
import web.util.NotFoundException;

public interface Initializable {
  public void onStart(ServletContext context);

  public void onRequestStart(App app);

  public void onRequestFinish(App app);

  public void handleException(NotFoundException e, App app);

  public void handleException(ForbiddenException e, App app, Route route);

  public void handleException(Exception e, App app);
}
