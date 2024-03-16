package com.autcraft.acelytrafall.configuration;

public interface ACConfigCloneable<T> {
	
	@SuppressWarnings("unchecked")
	public default T CreateConfigClone() {
		return (T)ACConfig.CreateConfigClone(this);
	}
}
