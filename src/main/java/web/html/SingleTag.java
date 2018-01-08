package web.html;

public class SingleTag extends HtmlTag {
	public SingleTag(String name) {
		super(name);
	}

	@Override
	public String toString() {
		return prefix + "<" + name + attributes + "/>" + suffix;
	}
}
