package web.html;

public abstract class HtmlTag {
	protected String name;
	protected String attributes;
	protected String prefix;
	protected String suffix;

	public HtmlTag(String name) {
		this.name = name;
		attributes = "";
		prefix = "";
		suffix = "";
	}

	public final void addAttr(String name) {
		attributes += " " + name;
	}

	public final void addAttr(String name, String value) {
		attributes += " " + name + "=\"" + value + "\"";
	}

	public final void setPrefix(String content) {
		prefix = content;
	}

	public final void setSuffix(String content) {
		suffix = content;
	}

	@Override
	public abstract String toString();
}
