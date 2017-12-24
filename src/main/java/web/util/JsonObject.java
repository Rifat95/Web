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

	public String getStr(String var) {
		return (String) jo.get(var);
	}

	public int getInt(String var) {
		return (int) jo.get(var);
	}

	public boolean getBool(String var) {
		return (boolean) jo.get(var);
	}

	@Override
	public String toString() {
		return jo.toJSONString();
	}
}
