package com.duanyy.media.glutil;

import android.opengl.GLES20;

import java.util.ArrayList;


public class GlProgram {

	private String mVShader;
	private String mFShader;
	private ArrayList<String> mattributes = new ArrayList<String>();
	private int mProgram = 0;
	public GlProgram(String vShaderString, String fShaderString){
		mVShader = vShaderString;
		mFShader = fShaderString;
		mProgram = GlUtil.createProgram( mVShader, mFShader );
//		GlUtil.checkGlError("GlProgram");
		
	}
	
	public void addAttributes(String attributeName){
		
		for( int i = 0; i < mattributes.size(); i++){
			if(mattributes.get(i).equalsIgnoreCase(attributeName)){
				return;
			}
		}
		
		mattributes.add(attributeName);
		
		int idx = -1;
		for(int i = 0; i < mattributes.size(); i++){
			if(mattributes.get(i).equalsIgnoreCase(attributeName)){
				idx = i;
			}
		}
		
		GLES20.glBindAttribLocation(mProgram, idx, attributeName);

		
	}
	public int attributeIndex(String attributeName){
//		for(int i = 0; i < mattributes.size(); i++){
//			if(mattributes.get(i).equalsIgnoreCase(attributeName)){
//				return i;
//			}
//		}
//		if(BuildConfig.DEBUG){
//			Log.e("GlProgram", "invalid attributeName");
//		}
//		
//		return -1;
		
		return GLES20.glGetAttribLocation(mProgram, attributeName);
		
	}
	public int uniformIndex(String uniformName ){
		
		return GLES20.glGetUniformLocation(mProgram, uniformName);
	}
	
	public void use(){
		GLES20.glUseProgram(mProgram);
//		Log.e("glprogram", "program id = " + mProgram);
	}
	
	public void release(boolean doEglRelease){
		GLES20.glDeleteProgram(mProgram);
		mProgram = 0;
	}
	
	
}
