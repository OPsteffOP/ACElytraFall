package com.autcraft.acelytrafall.command;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class ACInputSubCommand extends ACSubCommand {
	
	protected abstract List<String> GetInputOptions();
	
	protected boolean ShouldValidateInputOnCommand() {
		return true;
	}
	
	@Override
	protected String GetSubCommandName() {
		return "[input]";
	}
	
	public boolean ValidateInput(String input) {
		List<String> rawAllowedInputs = GetInputOptions();
		if(rawAllowedInputs == null) {
			return true;
		}
		
		Set<String> allowedInputs = new HashSet<String>();
		for(String rawAllowedInput : rawAllowedInputs) {
			allowedInputs.add(rawAllowedInput.toLowerCase());
		}
		
		return allowedInputs.contains(input.toLowerCase());
	}
}
