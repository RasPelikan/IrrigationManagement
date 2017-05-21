package com.pelikanit.im.admin;

public class IrrigationManagementHttpHandler extends CGIHttpHandler {

	public static final String PATH = "/im";
	
	private static final String RESOURCE_PACKAGE = "com/pelikanit";
	
	@Override
	protected String getResourcePackage() {
		
		return RESOURCE_PACKAGE;
		
	}

}
