#include <stdlib.h>
#include "tv_yewai_live_jni_ImageUtilEngine.h"
#include "string.h"
#include "jni.h"
#include  "dlfcn.h"
#include <fcntl.h>

int r_v_table[256],g_v_table[256],g_u_table[256],b_u_table[256],y_table[256];
int r_yv_table[256][256],b_yu_table[256][256];
int inited = 0;

void initTable()
{
	if (inited == 0)
	{
		inited = 1;
		int m = 0,n=0;
		for (; m < 256; m++)
		{
			r_v_table[m] = 1634 * (m - 128);
			g_v_table[m] = 833 * (m - 128);
			g_u_table[m] = 400 * (m - 128);
			b_u_table[m] = 2066 * (m - 128);
			y_table[m] = 1192 * (m - 16);
		}
		int temp = 0;
		for (m = 0; m < 256; m++)
			for (n = 0; n < 256; n++)
			{
				temp = 1192 * (m - 16) + 1634 * (n - 128);
				if (temp < 0) temp = 0; else if (temp > 262143) temp = 262143;
				r_yv_table[m][n] = temp;

				temp = 1192 * (m - 16) + 2066 * (n - 128);
				if (temp < 0) temp = 0; else if (temp > 262143) temp = 262143;
				b_yu_table[m][n] = temp;
			}
	}
}

jintArray Java_tv_yewai_live_jni_ImageUtilEngine_decodeYUV420SP(JNIEnv * env,
		jobject thiz, jbyteArray buf, jint width, jint height) {
	jbyte * yuv420sp = (*env)->GetByteArrayElements(env, buf, 0);

	int frameSize = width * height;
	jint rgb[frameSize]; // 新图像像素值

	initTable();

	int i = 0, j = 0,yp = 0;
	int uvp = 0, u = 0, v = 0;
	for (j = 0, yp = 0; j < height; j++)
	{
		uvp = frameSize + (j >> 1) * width;
		u = 0;
		v = 0;
		for (i = 0; i < width; i++, yp++)
		{
			int y = (0xff & ((int) yuv420sp[yp]));
			if (y < 0)
				y = 0;
			if ((i & 1) == 0)
			{
				v = (0xff & yuv420sp[uvp++]);
				u = (0xff & yuv420sp[uvp++]);
			}

//			int y1192 = 1192 * y;
//			int r = (y1192 + 1634 * v);
//			int g = (y1192 - 833 * v - 400 * u);
//			int b = (y1192 + 2066 * u);
			int y1192 = y_table[y];
			int r = r_yv_table[y][v];//(y1192 + r_v_table[v]);
			int g = (y1192 - g_v_table[v] - g_u_table[u]);
			int b = b_yu_table[y][u];//(y1192 + b_u_table[u]);

			//if (r < 0) r = 0; else if (r > 262143) r = 262143;
			if (g < 0) g = 0; else if (g > 262143) g = 262143;
			//if (b < 0) b = 0; else if (b > 262143) b = 262143;
//			r = (r >> 31) ? 0 : (r & 0x3ffff);
//			g = (g >> 31) ? 0 : (g & 0x3ffff);
//			b = (b >> 31) ? 0 : (b & 0x3ffff);

			rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
		}
	}

	jintArray result = (*env)->NewIntArray(env, frameSize);
	(*env)->SetIntArrayRegion(env, result, 0, frameSize, rgb);
	(*env)->ReleaseByteArrayElements(env, buf, yuv420sp, 0);
	return result;
}

//void * filehandle = NULL;
//jstring (*getResult)(JNIEnv *, jobject, jstring, jstring) =NULL;
//
//JNIEXPORT jstring JNICALL Java_tv_yewai_live_jni_ImageUtilEngine_call(JNIEnv *env,jobject  context ,jstring param1,jstring param2){
//
//	jstring result = (*env)->NewStringUTF("","UTF8");
//	filehandle = dlopen("liblc.so", RTLD_LAZY);
//	if (filehandle) {
//		getResult = (jstring (*)(JNIEnv *, jobject, jstring, jstring)) dlsym(filehandle,"Java_tv_douyu_misc_util_CMessage_jni_authcodeEncode");
//		if (getResult)
//			result = getResult(env, context, param1,param2);
//		dlclose(filehandle);
//		filehandle = NULL;
//	}
//
//	return result;
//}
