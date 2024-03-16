package com.autcraft.acelytrafall.async;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import com.autcraft.acelytrafall.ACElytraFall;

public class TemporaryCacheMap<T, U> {
	
	private class ListElement {
		T key;
		float secondsLeft;
		long addTime;
	}
	
	private class MapElement {
		U value;
		long addTime;
	}
	
	private final float defaultKeepDurationSeconds;
	
	private List<ListElement> internalList = new ArrayList<ListElement>(); // provides list speed to iterate each tick
	private Map<T, MapElement> internalMap = new HashMap<T, MapElement>(); // provides map speed for searching by key
	
	private long lastTickTime = System.currentTimeMillis();
	
	public TemporaryCacheMap(float defaultKeepDurationSeconds) {
		this.defaultKeepDurationSeconds = defaultKeepDurationSeconds;
		StartRemoveInactiveElementsTask();
	}
	
	public boolean Contains(T key) {
		return internalMap.containsKey(key);
	}
	
	public void AddTemporary(T key, U value) {
		AddTemporary(key, value, defaultKeepDurationSeconds);
	}
	
	public void AddTemporary(T key, U value, float keepDurationSeconds) {
		// Allowing elements to be overwritten if they already exist.
		// In the map, the element will get overwritten - thus the previous element getting removed from the map.
		// In the list, the previous element will stay but will get removed once inactive,
		// checks addTime to avoid removing wrong elements.
		
		long currentTime = System.currentTimeMillis();
		
		ListElement listElement = new ListElement();
		listElement.key = key;
		listElement.secondsLeft = keepDurationSeconds;
		listElement.addTime = currentTime;
		
		MapElement mapElement = new MapElement();
		mapElement.value = value;
		mapElement.addTime = currentTime;
		
		internalList.add(listElement);
		internalMap.put(key, mapElement);
	}
	
	public U RemoveTemporary(T key) {
		// Purposefully don't remove the element from internalList as it'd require a slow operation.
		// Instead letting the tick iteration removes it once it becomes inactive,
		// whilst also checking if we've added an element with the same key in the meantime,
		// and not removing from the map if that's the case - to avoid removing elements that shouldn't be removed.
		
		MapElement element = internalMap.remove(key);
		if(element == null) {
			return null;
		}
		
		return element.value;
	}
	
	private void StartRemoveInactiveElementsTask() {
		Bukkit.getScheduler().runTaskTimer(JavaPlugin.getPlugin(ACElytraFall.class), new Runnable() {
			
			@Override
			public void run() {
				long currentTime = System.currentTimeMillis();
				long elapsedTime = currentTime - lastTickTime;
				float elapsedTimeSeconds = elapsedTime / 1000.f;
				
				ListIterator<ListElement> iterator = internalList.listIterator();
				while(iterator.hasNext()) {
					ListElement listElement = iterator.next();
					
					listElement.secondsLeft -= elapsedTimeSeconds;
					if(listElement.secondsLeft <= 0) {
						iterator.remove();
						
						MapElement mapElement = internalMap.get(listElement.key);
						if(mapElement != null && mapElement.addTime == listElement.addTime) {
							internalMap.remove(listElement.key);
						}
					}
				}
				
				lastTickTime = currentTime;
			}
		}, 0L, 1L);
	}
}
