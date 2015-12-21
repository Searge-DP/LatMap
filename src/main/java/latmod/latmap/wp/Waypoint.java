package latmod.latmap.wp;

import com.google.gson.*;

import latmod.lib.*;

public class Waypoint
{
	public final long created;
	public String name;
	//public String customIcon;
	public boolean enabled = true;
	public WaypointType type = WaypointType.BEACON;
	public int posX, posY, posZ, dim, color;
	public boolean deathpoint = false;
	
	public Waypoint(long l)
	{ created = l; }
	
	public Waypoint()
	{ this(System.currentTimeMillis()); }
	
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
	
	public boolean equals(Object o)
	{ return o != null && (o == this || ((Waypoint)o).created == created); }
	
	public Waypoint clone(long l)
	{
		Waypoint w = new Waypoint(l);
		w.name = name;
		w.enabled = enabled;
		w.type = type;
		w.setPos(posX, posY, posZ);
		w.dim = dim;
		w.color = color;
		w.deathpoint = deathpoint;
		return w;
	}
	
	public static class Serializer implements JsonSerializer<Waypoint>, JsonDeserializer<Waypoint>
	{
		public JsonElement serialize(Waypoint src, java.lang.reflect.Type typeOfSrc, JsonSerializationContext context)
		{
			if(src == null) return null;
			JsonObject o = new JsonObject();
			o.add("name", new JsonPrimitive(src.name));
			if(!src.enabled) o.add("disabled", new JsonPrimitive(true));
			if(src.type.isMarker()) o.add("marker", new JsonPrimitive(true));
			
			JsonArray posA = new JsonArray();
			posA.add(new JsonPrimitive(src.posX));
			posA.add(new JsonPrimitive(src.posY));
			posA.add(new JsonPrimitive(src.posZ));
			o.add("pos", posA);
			
			o.add("dim", new JsonPrimitive(src.dim));
			o.add("col", new JsonPrimitive(LMColorUtils.getHex(src.color)));
			o.add("date", new JsonPrimitive(src.created));
			if(src.deathpoint) o.add("deathpoint", new JsonPrimitive(true));
			return o;
		}
		
		public Waypoint deserialize(JsonElement json, java.lang.reflect.Type typeOfT, JsonDeserializationContext context) throws JsonParseException
		{
			if(json.isJsonNull()) return null;
			JsonObject o = json.getAsJsonObject();
			Waypoint w = new Waypoint(o.get("date").getAsLong());
			w.name = o.get("name").getAsString();
			w.enabled = o.has("disabled") ? !o.get("disabled").getAsBoolean() : true;
			w.type = WaypointType.get(o.has("marker") ? o.get("marker").getAsBoolean() : false);
			
			JsonArray posA = o.get("pos").getAsJsonArray();
			w.posX = posA.get(0).getAsInt();
			w.posY = posA.get(1).getAsInt();
			w.posZ = posA.get(2).getAsInt();
			w.dim = o.get("dim").getAsInt();
			w.color = Integer.decode(o.get("col").getAsString());
			w.deathpoint = o.has("death") ? o.get("death").getAsBoolean() : false;
			return w;
		}
	}
}