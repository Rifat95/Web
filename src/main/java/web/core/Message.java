package web.core;

public final class Message {
  public static final String INFO = "info";
  public static final String SUCCESS = "success";
  public static final String WARNING = "warning";

  private String type;
  private String content;

  public Message(String type, String content) {
    this.type = type;
    this.content = content;
  }

  public String getType() {
    return type;
  }

  public String getContent() {
    return content;
  }
}
