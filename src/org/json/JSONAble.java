package org.json;

public interface JSONAble {

    public JSONObject toJSON();

    public void fromJSON(JSONObject json);
}
