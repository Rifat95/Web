package web.core;

import javax.servlet.ServletContext;
import web.exception.ForbiddenException;
import web.exception.NotFoundException;

public interface Initializable {
  public void onAppStart(ServletContext context);

  public void onRequestStart(App app);

  public void onRequestFinish(App app);

  public void handleException(NotFoundException e, App app);

  public void handleException(ForbiddenException e, App app, Route route);

  public void handleException(Exception e, App app);
}
