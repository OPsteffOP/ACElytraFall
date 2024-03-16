package com.autcraft.acelytrafall.configuration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ConfigField {
	
	public String name() default "";
	public FieldConversion conversion() default FieldConversion.NONE;
	
	public static enum FieldConversion {
		NONE,
		UUID_TO_STRING,
		ENUM_TO_STRING,
		MAP_TO_STRUCT;
		
		@SuppressWarnings("unchecked")
		public Object ConvertToWriteValue(Field field, Object value) {
			if(this == FieldConversion.UUID_TO_STRING) {
				return ((UUID)value).toString();
			} else if(this == FieldConversion.ENUM_TO_STRING) {
				return value.toString();
			} else if(this == FieldConversion.MAP_TO_STRUCT) {
				Map<Object, Object> map = (Map<Object, Object>)value;
				List<MapPair> list = new ArrayList<MapPair>();
				
				for(Map.Entry<Object, Object> entry : map.entrySet()) {
					MapPair pair = new MapPair();
					pair.key = entry.getKey();
					pair.value = entry.getValue();
					list.add(pair);
				}
				
				return list;
			}
			
			return value;
		}
		
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public Object ConvertToReadValue(Field field, Object value) {
			if(this == FieldConversion.UUID_TO_STRING) {
				return UUID.fromString((String)value);
			} else if(this == FieldConversion.ENUM_TO_STRING) {
				return Enum.valueOf((Class<Enum>)field.getType(), (String)value);
			} else if(this == FieldConversion.MAP_TO_STRUCT) {
				List<MapPair> list = (List<MapPair>)value;
				Map<Object, Object> map = new HashMap<Object, Object>();
				
				for(MapPair pair : list) {
					map.put(pair.key, pair.value);
				}
				
				return map;
			}
			
			return value;
		}
		
		public static class MapPair {
			@ConfigField public Object key = null;
			@ConfigField public Object value = null;
		}
	}
}
