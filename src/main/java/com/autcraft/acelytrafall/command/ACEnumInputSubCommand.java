package com.autcraft.acelytrafall.command;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

public abstract class ACEnumInputSubCommand<T extends Enum<T>> extends ACInputSubCommand {

	@Override
	protected List<String> GetInputOptions() {
		List<String> inputOptions = new ArrayList<String>();
		
		@SuppressWarnings("unchecked")
		Class<T> clazz = (Class<T>) ((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[0];
		for(T enumConstant : clazz.getEnumConstants()) {
			inputOptions.add(enumConstant.toString());
		}
		
		return inputOptions;
	}
}
