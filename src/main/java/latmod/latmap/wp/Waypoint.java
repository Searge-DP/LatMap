package latmod.latmap.wp;

import com.google.gson.*;

import latmod.lib.*;

public class Waypoint
{
	public String name;
	//public String customIcon;
	public boolean enabled = true;
	public WaypointType type = WaypointType.BEACON;
	public int posX, posY, posZ, dim, color;
	public int listID = -1;
	public long created = System.currentTimeMillis();
	public boolean deathpoint = false;
	
	public void setPos(double x, double y, double z)
	{
		posX = MathHelperLM.floor(x);
		posY = MathHelperLM.floor(y);
		posZ = MathHelperLM.floor(z);
	}
	
	public String toString()
	{ return LMJsonUtils.toJson(this); }
	
	public int hashCode()
	{ return Long.hashCode(created); }
	
	public Waypoint clone()
	{
		Waypoint w = new Waypoint();
		w.name = name;
		w.enabled = enabled;
		w.type = type;
		w.setPos(posX, posY, posZ);
		w.dim = dim;
		w.color = color;
		w.created = created;
		w.deathpoint = deathpoint;
		return w;
	}
	
	public static class Serializer implements JsonSerializer<Waypoint>, JsonDeserializer<Waypoint>
	{
		public JsonElement serialize(Waypoint src, java.lang.reflect.Type typeOfSrc, JsonSerializationContext context)
		{
			if(src == null) return null;
			JsonObject o = new JsonObject();
			o.add("Name", new JsonPrimitive(src.name));
			o.add("On", new JsonPrimitive(src.enabled ? 1 : 0));
			o.add("Type", new JsonPrimitive(src.type.ID));
			o.add("X", new JsonPrimitive(src.posX));
			o.add("Y", new JsonPrimitive(src.posY));
			o.add("Z", new JsonPrimitive(src.posZ));
			o.add("Dim", new JsonPrimitive(src.dim));
			o.add("Col", new JsonPrimitive(LMColorUtils.getHex(src.color)));
			o.add("Date", new JsonPrimitive(src.created));
			if(src.deathpoint) o.add("deathpoint", new JsonPrimitive(true));
			return o;
		}
		
		public Waypoint deserialize(JsonElement json, java.lang.reflect.Type typeOfT, JsonDeserializationContext context) throws JsonParseException
		{
			if(json.isJsonNull()) return null;
			JsonObject o = json.getAsJsonObject();
			Waypoint w = new Waypoint();
			w.name = o.get("Name").getAsString();
			w.enabled = o.get("On").getAsInt() == 1;
			w.type = WaypointType.get(o.get("Type").getAsString());
			w.posX = o.get("X").getAsInt();
			w.posY = o.get("Y").getAsInt();
			w.posZ = o.get("Z").getAsInt();
			w.dim = o.get("Dim").getAsInt();
			w.color = Integer.decode(o.get("Col").getAsString());
			w.created = o.get("Date").getAsLong();
			w.deathpoint = o.has("deathpoint");
			return w;
		}
	}
}