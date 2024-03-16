package com.autcraft.acelytrafall.configuration;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.logging.Level;

import org.apache.commons.lang3.ClassUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import com.autcraft.acelytrafall.configuration.ConfigField.FieldConversion;
import com.google.common.base.CaseFormat;

public class ACConfig extends YamlConfiguration {
	
	private Map<Class<?>, List<Field>> knownTypes = new HashMap<Class<?>, List<Field>>();
	
	public static ACConfig loadConfiguration(File file) {
		ACConfig config = new ACConfig();
		
		try {
			config.load(file);
		} catch(Exception e) {
			Bukkit.getLogger().log(Level.SEVERE, "Cannot load " + file, e);
		}
		
		return config;
    }
	
	@SuppressWarnings("unchecked")
	@Override
	public Object get(String path) {
		Object sectionObj = super.get(path);
		if(sectionObj == null) {
			return null;
		}
		
		if(!(sectionObj instanceof ConfigurationSection)) {
			return super.get(path);
		}
		
		ConfigurationSection section = (ConfigurationSection)sectionObj;
		Set<String> keys = section.getKeys(false);
		
		if(!keys.contains("!AC_TYPE")) {
			return super.get(path);
		}
		keys.remove("!AC_TYPE");
		
		List<Field> fields = null;
		Object object = null;
		
		try {
			Class<?> clazz = Class.forName(section.getString("!AC_TYPE"));
			fields = GetConfigFields(clazz);
			object = clazz.getConstructor().newInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(object == null) {
			return null;
		}
		
		for(String key : keys) {
			if(object instanceof List<?> list) {
				char separator = getRoot().options().pathSeparator();
				Object value = get(path + separator + key);
				
				int keyInt = Integer.valueOf(key);
				if(list.size() == keyInt) {
					((List<Object>)list).add(keyInt, value);
				} else if(list.size() < keyInt) {
					for(int i = list.size(); i < keyInt; ++i) {
						((List<Object>)list).add(i, null);
					}
					((List<Object>)list).add(keyInt, value);
				} else if(list.size() > keyInt) {
					((List<Object>)list).set(keyInt, value);
				}
				continue;
			}
			
			boolean doesFieldExist = false;
			for(Field field : fields) {
				if(GetFieldName(field).equals(key)) {
					char separator = getRoot().options().pathSeparator();
					Object value = get(path + separator + key);
					
					FieldConversion conversion = GetFieldConversion(field);
					value = conversion.ConvertToReadValue(field, value);
					
					try {
						field.set(object, value);
					} catch (Exception e) {
						e.printStackTrace();
					}
					
					doesFieldExist = true;
					break;
				}
			}
			
			if(!doesFieldExist) {
				Bukkit.getLogger().log(Level.WARNING, String.format("Field attached to '%s' doesn't exist in object '%s'.", 
						key, object.getClass().getSimpleName()));
			}
		}
		
		return object;
	}
	
	@Override
	public void set(String path, Object obj) {
		HandleCustomFields(path, obj, super::set, this::set);
	}
	
	@Override
	public void addDefault(String path, Object obj) {
		HandleCustomFields(path, obj, super::addDefault, this::addDefault);
	}
	
	private void HandleCustomFields(String path, Object obj, BiConsumer<String, Object> noConfigFieldsCallable, BiConsumer<String, Object> configFieldCallable) {
		if(obj instanceof List<?> list && !list.isEmpty()) {
			HandleCustomFieldsList(path, list, noConfigFieldsCallable, configFieldCallable);
			return;
		}
		
		List<Field> configFields = GetConfigFields(obj.getClass());
		
		if(configFields.isEmpty()) {
			noConfigFieldsCallable.accept(path, obj);
			return;
		}
		
		for(Field field : configFields) {
			String name = GetFieldName(field);
			Object value = null;
			try {
				value = field.get(obj);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			
			if(value == null) {
				continue;
			}
			
			FieldConversion conversion = GetFieldConversion(field);
			value = conversion.ConvertToWriteValue(field, value);
			
	        char separator = getRoot().options().pathSeparator();
	        
	        String typePath = path + separator + "!AC_TYPE";
	        configFieldCallable.accept(typePath, obj.getClass().getName());
	        
	        String fieldPath = path + separator + name;
	        configFieldCallable.accept(fieldPath, value);
		}
	}
	
	private void HandleCustomFieldsList(String path, List<?> list, BiConsumer<String, Object> noConfigFieldsCallable, BiConsumer<String, Object> configFieldCallable) {
		char separator = getRoot().options().pathSeparator();
		
        for(int i = 0; i < list.size(); ++i) {
        	Object element = list.get(i);
        	
        	String typePath = path + separator + "!AC_TYPE";
	        configFieldCallable.accept(typePath, list.getClass().getName());
        	
        	String listElementPath = path + separator + i;
        	HandleCustomFields(listElementPath, element, noConfigFieldsCallable, configFieldCallable);
        }
	}
	
	private String GetFieldName(Field field) {
		ConfigField configField = field.getAnnotation(ConfigField.class);
		
		String name = configField.name();
		if(name.isEmpty()) {
			name = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, field.getName());
		}
		
		return name;
	}
	
	private FieldConversion GetFieldConversion(Field field) {
		ConfigField configField = field.getAnnotation(ConfigField.class);
		return configField.conversion();
	}
	
	private List<Field> GetConfigFields(Class<?> clazz) {
		if(knownTypes.containsKey(clazz)) {
			return knownTypes.get(clazz);
		}
		
		
		List<Field> configFields = new ArrayList<Field>();
		
		for(Field field : clazz.getDeclaredFields()) {
			if(!field.trySetAccessible()) {
				continue;
			}
			
			ConfigField configField = field.getAnnotation(ConfigField.class);
			if(configField != null) {
				configFields.add(field);
			}
		}
		
		Class<?> parent = clazz.getSuperclass();
		if(parent != null) {
			configFields.addAll(GetConfigFields(parent));
		}
		
		knownTypes.put(clazz, configFields);
		return configFields;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T CreateConfigClone(T obj) {
		if(obj == null) {
			return null;
		}
		
		try {
			Class<?> objType = obj.getClass();
			if(ClassUtils.isPrimitiveWrapper(objType) || obj instanceof Enum || obj instanceof UUID) {
				// Primitive wrapper (like Integer, Boolean, ...) or Enum or UUID
				return obj;
			} else if(obj instanceof String) {
				// String class
				return (T)objType.getConstructor(objType).newInstance(obj);
			} else if(obj instanceof ArrayList<?>) {
				// ArrayList class
				List<?> objList = (List<?>)obj;
				List<Object> clone = new ArrayList<Object>(objList.size());
				for(Object element : objList) {
					clone.add(CreateConfigClone(element));
				}
				
				return (T)clone;
			} else if(obj instanceof Location) {
				return (T)((Location)obj).clone();
			} else {
				// Unspecified class, will add all @ConfigField fields
				Object clone = objType.getConstructor().newInstance();
				for(Field field : objType.getDeclaredFields()) {
					if(!field.isAnnotationPresent(ConfigField.class)) {
						continue;
					}
					
					field.setAccessible(true);
					field.set(clone, CreateConfigClone(field.get(obj)));
				}
				
				return (T)clone;
			}
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
