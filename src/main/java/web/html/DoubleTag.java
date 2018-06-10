package web.html;

public class DoubleTag extends HtmlTag {
  protected String content;

  public DoubleTag(String name) {
    this(name, "");
  }

  public DoubleTag(String name, String content) {
    super(name);
    this.content = content;
  }

  public final void insert(String content) {
    this.content += content;
  }

  public final void insert(String content, String wrapperTag) {
    this.content += "<" + wrapperTag + ">" + content + "</" + wrapperTag + ">";
  }

  @Override
  public String toString() {
    return prefix + "<" + name + attributes + ">" + content + "</" + name + ">" + suffix;
  }
}
