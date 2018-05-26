package web.core;

import web.util.ForbiddenException;
import web.util.NotFoundException;

public interface MainController {
	public void start();

	public void end();

	public void handleException(NotFoundException e);

	public void handleException(ForbiddenException e);

	public void handleException(Exception otherException);
}
