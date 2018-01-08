package web.html;

public class DoubleTag extends HtmlTag {
	protected String content;

	public DoubleTag(String name) {
		this(name, "");
	}

	public DoubleTag(String name, HtmlTag tag) {
		this(name, tag.toString());
	}

	public DoubleTag(String name, String content) {
		super(name);
		this.content = content;
	}

	public final void insert(HtmlTag tag) {
		insert(tag.toString());
	}

	public final void insert(String content) {
		this.content += content;
	}

	@Override
	public String toString() {
		return prefix + "<" + name + attributes + ">" + content + "</" + name + ">" + suffix;
	}
}
