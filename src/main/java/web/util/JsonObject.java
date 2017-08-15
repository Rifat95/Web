package web.util;

import org.json.simple.JSONObject;

// JSONObject wrapper class
public final class JsonObject {
	private JSONObject jo;

	public JsonObject() {
		this(new JSONObject());
	}

	public JsonObject(JSONObject jo) {
		this.jo = jo;
	}

	@SuppressWarnings("unchecked")
	public JsonObject put(String var, Object value) {
		jo.put(var, value);
		return this;
	}

	public String get(String var) {
		return (String) jo.get(var);
	}

	@Override
	public String toString() {
		return jo.toJSONString();
	}
}
