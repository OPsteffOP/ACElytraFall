package com.autcraft.acelytrafall.async;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import com.autcraft.acelytrafall.ACElytraFall;

public class AsyncTaskManager {
	
	public static abstract class AsyncTask {
		
		private String key = null;
		
		public abstract void Execute();
		public void OnComplete() {}
		
		public AsyncTask(String key) {
			this.key = key;
		}
		
		private void Schedule() {
			AsyncTask _this = this;
			Bukkit.getScheduler().runTaskAsynchronously(JavaPlugin.getPlugin(ACElytraFall.class), new Runnable() {

				@Override
				public void run() {
					Execute();
					AsyncTaskManager.GetInstance().OnTaskComplete(_this);
				}
			});
		}
	}
	
	private static AsyncTaskManager instance = null;
	
	private Map<String, LinkedList<AsyncTask>> tasks = new HashMap<String, LinkedList<AsyncTask>>();
	
	public static AsyncTaskManager GetInstance() {
		if(instance == null) {
			instance = new AsyncTaskManager();
		}
		
		return instance;
	}
	
	public boolean IsTaskPending(String key) {
		return tasks.get(key) != null && !tasks.get(key).isEmpty();
	}
	
	public void ScheduleAsyncTask(AsyncTask task) {
		if(tasks.containsKey(task.key)) {
			// Task with same key already executing/pending, schedule behind it
			tasks.get(task.key).addLast(task);
		} else {
			// No tasks with same key executing/pending, immediately schedule
			LinkedList<AsyncTask> list = new LinkedList<AsyncTask>();
			list.addLast(task);
			tasks.put(task.key, list);
			task.Schedule();
		}
	}
	
	private void OnTaskComplete(AsyncTask task) {
		Bukkit.getScheduler().runTask(JavaPlugin.getPlugin(ACElytraFall.class), new Runnable() {

			@Override
			public void run() {
				task.OnComplete();
				
				LinkedList<AsyncTask> list = tasks.get(task.key);
				if(!list.isEmpty() && list.peekFirst().key.equals(task.key)) {
					list.pop();
				} else {
					Bukkit.getLogger().log(Level.WARNING, "%s - completed task isn't the first element in the queue");
				}
				
				if(!list.isEmpty()) {
					list.peekFirst().Schedule();
				} else {
					tasks.remove(task.key);
				}
			}
		});
	}
}
