package com.duanyy.media.glutil;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.Image;
import android.opengl.GLES20;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;


public class CommonUtil {
//	public static Bitmap getBitmapByPath(final Context context, String path) {
//		final BitmapFactory.Options options = new BitmapFactory.Options();
//		options.inScaled = false; // No pre-scaling
//		options.inPreferredConfig = Config.ARGB_8888;
//
//		Bitmap bitmap = null;
//		InputStream is = null;
//		try {
//			is = context.getAssets().open(path);
//			bitmap = BitmapFactory.decodeStream(is, null, options);
//			is.close();
//		}
//		catch (Exception e)
//		{
//		}
//		return bitmap;
//	}

	public static void gpuLoge(String tag, String info) {
		if (false) {
			Log.e(tag, info);
		}
	}

	public static void rtcLoge(String tag, String info) {
		if (false) {
			Log.e(tag, info);
		}
	}

	@SuppressLint("NewApi")
	public static float getTotalMemory(Context context) {
		if(android.os.Build.VERSION.SDK_INT >=android.os.Build.VERSION_CODES.JELLY_BEAN)
		{
			ActivityManager actManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
			MemoryInfo memInfo = new MemoryInfo();
			actManager.getMemoryInfo(memInfo);
			return  (memInfo.totalMem/(1024.f*1024.f*1024.f));
			
		}else{
			String str1 = "/proc/meminfo";
			String str2;
			String[] arrayOfString;
			float initial_memory = 0;
			try {
				FileReader localFileReader = new FileReader(str1);
				BufferedReader localBufferedReader = new BufferedReader(localFileReader, 8192);
				str2 = localBufferedReader.readLine();//meminfo
				arrayOfString = str2.split("\\s+");
				for (String num : arrayOfString) {
					Log.i(str2, num + "\t");
				}
				//total Memory
				initial_memory = Integer.valueOf(arrayOfString[1]).intValue() / (1024.f*1024.f);
				localBufferedReader.close();
				return initial_memory;
			} 
			catch (IOException e)
			{       
				return -1;
			}
		}
	  }

	public static void testScreenShoot(int width, int height, String fileName) {

		int screenshotSize = width * height;
		ByteBuffer bb = ByteBuffer.allocateDirect(screenshotSize * 4);
		bb.order(ByteOrder.nativeOrder());
		GLES20.glReadPixels(0, 0, width, height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, bb);
		int pixelsBuffer[] = new int[screenshotSize];
		bb.asIntBuffer().get(pixelsBuffer);
		bb = null;
		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
		bitmap.setPixels(pixelsBuffer, screenshotSize-width, -width, 0, 0, width, height);
		pixelsBuffer = null;

		short sBuffer[] = new short[screenshotSize];
		ShortBuffer sb = ShortBuffer.wrap(sBuffer);
		bitmap.copyPixelsToBuffer(sb);

		//Making created bitmap (from OpenGL points) compatible with Android bitmap
		for (int i = 0; i < screenshotSize; ++i) {
			short v = sBuffer[i];
			sBuffer[i] = (short) (((v&0x1f) << 11) | (v&0x7e0) | ((v&0xf800) >> 11));
		}
		sb.rewind();
		bitmap.copyPixelsFromBuffer(sb);
//            lastScreenshot = bitmap;

		{
			FileOutputStream out = null;
			try {
				File sdCard = Environment.getExternalStorageDirectory();
				out = new FileOutputStream(sdCard.getAbsolutePath() + "/" + fileName);
				bitmap.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
				// PNG is a lossless format, the compression factor (100) is ignored
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					if (out != null) {
						out.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static boolean captureGLTexture(int width, int height, String fileName) {
		boolean result = false;
		FileOutputStream out = null;
		try {
			File dstFile = new File(fileName);
			if (dstFile.exists())
				dstFile.delete();
			int screenshotSize = width * height;
			ByteBuffer bb = ByteBuffer.allocateDirect(screenshotSize * 4);
			bb.order(ByteOrder.nativeOrder());
			GLES20.glReadPixels(0, 0, width, height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, bb);
			int pixelsBuffer[] = new int[screenshotSize];
			bb.asIntBuffer().get(pixelsBuffer);
			bb = null;
			Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
			bitmap.setPixels(pixelsBuffer, screenshotSize-width, -width, 0, 0, width, height);
			pixelsBuffer = null;

			short sBuffer[] = new short[screenshotSize];
			ShortBuffer sb = ShortBuffer.wrap(sBuffer);
			bitmap.copyPixelsToBuffer(sb);

			//Making created bitmap (from OpenGL points) compatible with Android bitmap
			for (int i = 0; i < screenshotSize; ++i) {
				short v = sBuffer[i];
				sBuffer[i] = (short) (((v&0x1f) << 11) | (v&0x7e0) | ((v&0xf800) >> 11));
			}
			sb.rewind();
			bitmap.copyPixelsFromBuffer(sb);

			out = new FileOutputStream(dstFile);
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance

			result = true;
		} catch (Exception e) {
			e.printStackTrace();
			result = false;
		} finally {
			try {
				if (out != null) {
					out.close();
				}
			} catch (IOException e) {
				result = false;
				e.printStackTrace();
			}
		}
		return result;
	}


	public static boolean saveJPEGWithNV21Data(byte[] data, int width, int height, int rotateDegree, String filePath) {
		if (data == null)
			return false;
		YuvImage im = new YuvImage(data, ImageFormat.NV21, width, height, null);
		Rect r = new Rect(0, 0, width, height);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		if (im.compressToJpeg(r, 100, baos)) {
			byte[] jdata = baos.toByteArray();
			BitmapFactory.Options bitmapFactoryOptions = new BitmapFactory.Options();
			bitmapFactoryOptions.inPreferredConfig = Bitmap.Config.RGB_565;
			Bitmap bmp = BitmapFactory.decodeByteArray(jdata, 0, jdata.length, bitmapFactoryOptions);
			if (bmp != null) {
				Matrix m = new Matrix();
				m.setRotate(rotateDegree);

				Bitmap rotatedBmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), m, true);

				FileOutputStream out = null;
				try {
					File sdCard = new File(filePath);
					if (sdCard.exists())
						sdCard.delete();
					out = new FileOutputStream(sdCard.getAbsolutePath());
					rotatedBmp.compress(Bitmap.CompressFormat.JPEG, 80, out); // bmp is your Bitmap instance
					// PNG is a lossless format, the compression factor (100) is ignored
				} catch (Exception e) {
					e.printStackTrace();
					return false;
				} finally {
					try {
						if (out != null) {
							out.close();
						}
					} catch (IOException e) {
						e.printStackTrace();
						return false;
					}
				}
				bmp.recycle();
			}

		}
		return true;
	}

	public static boolean saveJPGWithRGBAData(int[] data, int width, int height, int rotateDegree, String filePath) {
		if (data == null)
			return false;
		// You are using RGBA that's why Config is ARGB.8888
		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

		// vector is your int[] of ARGB
		bitmap.copyPixelsFromBuffer(IntBuffer.wrap(data));

		Matrix matrix = new Matrix();
		matrix.postRotate(rotateDegree);
		Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap , 0, 0, bitmap .getWidth(), bitmap.getHeight(), matrix, true);


		{
			FileOutputStream out = null;
			try {
				File sdCard = new File(filePath);
				if (sdCard.exists())
					sdCard.delete();
				out = new FileOutputStream(sdCard.getAbsolutePath());
				rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out); // bmp is your Bitmap instance
				// PNG is a lossless format, the compression factor (100) is ignored
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			} finally {
				try {
					if (out != null) {
						out.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
					return false;
				}
			}
		}
		return true;
	}

	public static Bitmap byteBufferToBitmap(ByteBuffer byteBuffer, int width, int height) {
		int pixelsBuffer[] = new int[width * height];
		byteBuffer.asIntBuffer().get(pixelsBuffer);

		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
		bitmap.setPixels(pixelsBuffer, width*height-width, -width, 0, 0, width, height);

		short sBuffer[] = new short[width * height];
		ShortBuffer sb = ShortBuffer.wrap(sBuffer);
		bitmap.copyPixelsToBuffer(sb);

		//Making created bitmap (from OpenGL points) compatible with Android bitmap
		for (int i = 0; i < width * height; ++i) {
			short v = sBuffer[i];
			sBuffer[i] = (short) (((v&0x1f) << 11) | (v&0x7e0) | ((v&0xf800) >> 11));
		}
		sb.rewind();
		bitmap.copyPixelsFromBuffer(sb);
		return bitmap;
	}

	public static Bitmap frameBufferToBitmap8888(int width, int height) {
		int screenshotSize = width * height;
		ByteBuffer bb = ByteBuffer.allocateDirect(screenshotSize * 4);
		bb.order(ByteOrder.nativeOrder());
		GLES20.glReadPixels(0, 0, width, height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, bb);
		int pixelsBuffer[] = new int[screenshotSize];
		bb.asIntBuffer().get(pixelsBuffer);
		bb = null;

		for (int i = 0; i < screenshotSize; ++i) {
			// The alpha and green channels' positions are preserved while the red and blue are swapped
			pixelsBuffer[i] = ((pixelsBuffer[i] & 0xff00ff00)) | ((pixelsBuffer[i] & 0x000000ff) << 16) | ((pixelsBuffer[i] & 0x00ff0000) >> 16);
		}

		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		bitmap.setPixels(pixelsBuffer, screenshotSize-width, -width, 0, 0, width, height);
		return bitmap;
	}

	public static Bitmap screenCaptureBitmap(int surfaceWidth, int surfaceHeight) {

//		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

		ByteBuffer byteBuffer = ByteBuffer.allocate(surfaceWidth * surfaceHeight * 4);
		byteBuffer.order(ByteOrder.nativeOrder());

		GLES20.glReadPixels(0, 0, surfaceWidth, surfaceHeight, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, byteBuffer);

		int pixelsBuffer[] = new int[surfaceWidth * surfaceHeight];
		byteBuffer.asIntBuffer().get(pixelsBuffer);

		Bitmap bitmap = Bitmap.createBitmap(surfaceWidth, surfaceHeight, Bitmap.Config.RGB_565);
		bitmap.setPixels(pixelsBuffer, surfaceWidth*surfaceHeight-surfaceWidth, -surfaceWidth, 0, 0, surfaceWidth, surfaceHeight);

		short sBuffer[] = new short[surfaceWidth * surfaceHeight];
		ShortBuffer sb = ShortBuffer.wrap(sBuffer);
		bitmap.copyPixelsToBuffer(sb);

		//Making created bitmap (from OpenGL points) compatible with Android bitmap
		for (int i = 0; i < surfaceWidth * surfaceHeight; ++i) {
			short v = sBuffer[i];
			sBuffer[i] = (short) (((v&0x1f) << 11) | (v&0x7e0) | ((v&0xf800) >> 11));
		}
		sb.rewind();
		bitmap.copyPixelsFromBuffer(sb);

		return bitmap;
	}

	public static Bitmap decodeSampledBitmapFromByteData(byte[] queryIs,
                                                         byte[] is, int reqWidth, int reqHeight) {

		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeByteArray(queryIs, 0, queryIs.length, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth,
				reqHeight);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeByteArray(is, 0, is.length, options);
	}


	public static Bitmap getBitmapByPath(Context context, final String filePath){
		if (filePath == null)
			return null;
		String fileName = filePath;
		if( fileName.contains("assets") ) {
			fileName = fileName.substring("assets/".length());
			AssetManager assetManager = context.getAssets();
			try {
				InputStream stream = assetManager.open(fileName);
				Bitmap bitmap = BitmapFactory.decodeStream(stream);
				if(bitmap != null){
					return bitmap;
				}
			}catch (Throwable e){
				e.printStackTrace();
			}
		}else{
			Bitmap bitmap = BitmapFactory.decodeFile(filePath);
			if(bitmap != null){
				return bitmap;
			}
		}
		return null;
	}

	public static int calculateInSampleSize(BitmapFactory.Options options,
											int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		/* delete by dll,2015-07-22
		 * if (height > reqHeight || width > reqWidth) {
			if (width > height) {
				inSampleSize = Math.round((float) height / (float) reqHeight);
			} else {
				inSampleSize = Math.round((float) width / (float) reqWidth);
			}
		}*/

		/*************add begin*********************/
		/* add by dll 2015-07-22
		 * Note: A power of two value is calculated because the decoder uses a final value
		 * by rounding down to the nearest power of two, as per the inSampleSize documentation.
		 */
		if (height > reqHeight || width > reqWidth) {

			final int halfHeight = height / 2;
			final int halfWidth = width / 2;

			// Calculate the largest inSampleSize value that is a power of 2 and keeps both
			// height and width larger than the requested height and width.
			while ((halfHeight / inSampleSize) > reqHeight
					|| (halfWidth / inSampleSize) > reqWidth) {
				inSampleSize *= 2;
			}

		}
		/****************add end*********************/
		return inSampleSize;
	}

	public static byte[] getBitmapBytes(Bitmap srcBmp) {
		if (srcBmp == null)
			return null;
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		srcBmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
		byte[] byteArray = stream.toByteArray();
		return byteArray;
	}

	public static byte[] getBitmapNV21(Bitmap srcBmp, int inputWidth, int inputHeight) {

		int [] argb = new int[inputWidth * inputHeight];

		srcBmp.getPixels(argb, 0, inputWidth, 0, 0, inputWidth, inputHeight);

		byte [] yuv = new byte[inputWidth*inputHeight*3/2];
		encodeYUV420SP(yuv, argb, inputWidth, inputHeight);

//		srcBmp.recycle();

		return yuv;
	}

	public static void encodeYUV420SP(byte[] yuv420sp, int[] argb, int width, int height) {
		final int frameSize = width * height;

		int yIndex = 0;
		int uvIndex = frameSize;

		int a, R, G, B, Y, U, V;
		int index = 0;
		for (int j = 0; j < height; j++) {
			for (int i = 0; i < width; i++) {

				a = (argb[index] & 0xff000000) >> 24; // a is not used obviously
				R = (argb[index] & 0xff0000) >> 16;
				G = (argb[index] & 0xff00) >> 8;
				B = (argb[index] & 0xff) >> 0;

				// well known RGB to YUV algorithm
				Y = ( (  66 * R + 129 * G +  25 * B + 128) >> 8) +  16;
				V = ( ( -38 * R -  74 * G + 112 * B + 128) >> 8) + 128; // Previously U
				U = ( ( 112 * R -  94 * G -  18 * B + 128) >> 8) + 128; // Previously V

				yuv420sp[yIndex++] = (byte) ((Y < 0) ? 0 : ((Y > 255) ? 255 : Y));
				if (j % 2 == 0 && index % 2 == 0) {
					yuv420sp[uvIndex++] = (byte)((V<0) ? 0 : ((V > 255) ? 255 : V));
					yuv420sp[uvIndex++] = (byte)((U<0) ? 0 : ((U > 255) ? 255 : U));
				}

				index ++;
			}
		}
	}

	public static boolean isSupportGLES30(Context context) {
		if (context == null)
			return false;
		final ActivityManager activityManager =
				(ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		final ConfigurationInfo configurationInfo =
				activityManager.getDeviceConfigurationInfo();
		final boolean supportsEs3 = configurationInfo.reqGlEsVersion >= 0x30000;
		return supportsEs3;
	}


	private static byte[] YUV_420_888toNV21(Image image) {
		if (android.os.Build.VERSION.SDK_INT < 19)
			return null;
		byte[] nv21;
		ByteBuffer yBuffer = image.getPlanes()[0].getBuffer();
		ByteBuffer uBuffer = image.getPlanes()[1].getBuffer();
		ByteBuffer vBuffer = image.getPlanes()[2].getBuffer();

		int ySize = yBuffer.remaining();
		int uSize = uBuffer.remaining();
		int vSize = vBuffer.remaining();

		nv21 = new byte[ySize + uSize + vSize];

		//U and V are swapped
		yBuffer.get(nv21, 0, ySize);
		vBuffer.get(nv21, ySize, vSize);
		uBuffer.get(nv21, ySize + vSize, uSize);

		return nv21;
	}

	public static byte[] YUV_420_888toNV21(ByteBuffer[] i420frame) {
		if (i420frame == null)
			return null;
		if (i420frame.length != 3)
			return null;
		byte[] nv21;
		ByteBuffer yBuffer = i420frame[0];
		ByteBuffer uBuffer = i420frame[1];
		ByteBuffer vBuffer = i420frame[2];

		int ySize = yBuffer.remaining();
		int uSize = uBuffer.remaining();
		int vSize = vBuffer.remaining();
		Log.e("yuvdata", "ySize = " +ySize+", vsize=" +vSize +", usize="+uSize);
		nv21 = new byte[ySize + uSize + vSize];

		byte[] uBytes = new byte[uSize];
		byte[] vBytes = new byte[vSize];
		uBuffer.get(uBytes, 0, uSize);
		vBuffer.get(vBytes, 0, vSize);

		//U and V are swapped
		yBuffer.get(nv21, 0, ySize);
		for (int i=0; i<uSize+vSize; i++) {
			nv21[i+ySize] = (i/2==0) ? vBytes[i/2] : uBytes[i/2];
		}
//		vBuffer.get(nv21, ySize, vSize);
//		uBuffer.get(nv21, ySize + vSize, uSize);

		return nv21;
	}

	public static byte[] NV21toJPEG(byte[] nv21, int width, int height) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		YuvImage yuv = new YuvImage(nv21, ImageFormat.NV21, width, height, null);
		yuv.compressToJpeg(new Rect(0, 0, width, height), 100, out);
		return out.toByteArray();
	}

	/**
	 *
	 * @param fileName
	 * @param data  jpeg data
	 *
     */
	public static boolean writeJPEG(String fileName, byte[] data) {
		try {
			File sdCard = new File(fileName);
			if (sdCard.exists())
				sdCard.delete();
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(fileName));
			bos.write(data);
			bos.flush();
			bos.close();
//            Log.e(TAG, "" + data.length + " bytes have been written to " + filesDir + fileName + ".jpg");
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}