#include "StdAfx.h"
#include "RDBase.h"
#include "PDFEnv.h"
#include "PDFDoc.h"
#include "PDFSecurity.h"
#include "PDFGRender.h"
#include "PDFOutline.h"
#include "PDFAnnot.h"
#include "PDFFont.h"
#include "RDHWriting.h"
#include "BMDatabase.h"
#include "PDFGRenderReflow.h"
#include "PDFContentStream.h"
#include "PDFSign.h"
#include "RDGSerial.h"
#include <jni.h>
#include <android/bitmap.h>
//#include <android/log.h>
#include "time.h"

#define FUNC_NAME(a) Java_ ## com_radaee_pdf_ ## a
const char g_ver[] =
{
	'2' ^ 0xAA,
	'0' ^ 0xAA,
	'1' ^ 0xAA,
	'4' ^ 0xAA,
	'0' ^ 0xAA,
	'7' ^ 0xAA,
	0
};

const char g_package[] =
{
	'c' ^ 0xAA,
	'o' ^ 0xAA,
	'm' ^ 0xAA,
	'.' ^ 0xAA,
	'r' ^ 0xAA,
	'a' ^ 0xAA,
	'd' ^ 0xAA,
	'a' ^ 0xAA,
	'e' ^ 0xAA,
	'e' ^ 0xAA,
	'.' ^ 0xAA,
	'r' ^ 0xAA,
	'e' ^ 0xAA,
	'a' ^ 0xAA,
	'd' ^ 0xAA,
	'e' ^ 0xAA,
	'r' ^ 0xAA,
	0
};

const char g_oem[] =
{
	'O' ^ 0xAA,
	'E' ^ 0xAA,
	'M' ^ 0xAA,
	'_' ^ 0xAA,
	'D' ^ 0xAA,
	'E' ^ 0xAA,
	'M' ^ 0xAA,
	'O' ^ 0xAA,
	0
};

void convert_str( const char *src, char *dst )
{
	while( *src )
	{
		*dst++ = src[0] ^ 0xAA;
		src++;
	}
	*dst = 0;
}

/*
#ifndef __dso_handle
extern "C"
{
	void *__dso_handle = NULL;
}
#endif
*/
extern RDI32 g_active_mode;
extern RDBOOL g_active_demo;
static RDI32 g_show_annots = 1;

void cvt_js_to_cs(JNIEnv* env, jstring jstr, PDF_String &str)
{
	str.init();
	if( jstr == NULL ) return;
	const char *tmp = env->GetStringUTFChars(jstr, NULL);
	str.init( tmp, ansi_len(tmp) );
}
jstring getPackage( JNIEnv* env, jobject context )
{
	jclass ctClass = env->GetObjectClass( context );
	jclass cls_cls = env->GetObjectClass( ctClass );
	jclass ctParent = NULL;
	if( !ctClass ) return NULL;
	const char *str_name;
	jmethodID mGetName = env->GetMethodID( cls_cls, "getName", "()Ljava/lang/String;" );
	jmethodID mGetSuper = env->GetMethodID( cls_cls, "getSuperclass", "()Ljava/lang/Class;" );
	jstring name = (jstring)env->CallObjectMethod(ctClass, mGetName);
	str_name = env->GetStringUTFChars( name, NULL );
	while( ansi_cmp(str_name, "android.content.ContextWrapper" ) != 0 )
	{
		jclass super = (jclass)env->CallObjectMethod(ctClass, mGetSuper);
		if( !super ) return NULL;
		env->DeleteLocalRef( ctClass );
		env->DeleteLocalRef( name );
		env->DeleteLocalRef( cls_cls );

		ctClass = super;
		cls_cls = env->GetObjectClass( super );
		mGetName = env->GetMethodID( cls_cls, "getName", "()Ljava/lang/String;" );
		mGetSuper = env->GetMethodID( cls_cls, "getSuperclass", "()Ljava/lang/Class;" );
		name = (jstring)env->CallObjectMethod(ctClass, mGetName);
		str_name = env->GetStringUTFChars( name, NULL );
	}
	jmethodID mGetPackageName = env->GetMethodID( ctClass, "getPackageName", "()Ljava/lang/String;" );
	if( !mGetPackageName ) return NULL;
	jstring ret = (jstring)env->CallNonvirtualObjectMethod(context, ctClass, mGetPackageName);
	env->DeleteLocalRef( ctClass );
	env->DeleteLocalRef( name );
	env->DeleteLocalRef( cls_cls );
	return ret;
}


class CPDFFontDelegateJNI:public CPDFFontDelegate
{
public:
	CPDFFontDelegateJNI()
	{
		m_vm = NULL;
		m_obj = NULL;
	}
	virtual ~CPDFFontDelegateJNI()
	{
		Uninit();
	}
	void Init( JNIEnv* env, jobject obj )
	{
		if( m_obj ) Uninit();
		env->GetJavaVM( &m_vm );
		m_obj = env->NewGlobalRef( obj );
	}
	void Uninit()
	{
		if( m_obj )
		{
			JNIEnv *env;
			m_vm->GetEnv( (void **)&env, JNI_VERSION_1_2 );
			env->DeleteGlobalRef( m_obj );
			m_obj = NULL;
			m_vm = NULL;
		}
	}
	virtual void get_ext_font(const char *collection, const char *fname, RDI32 flag, char *path, RDI32 &flags )
	{
		if( !m_obj || !fname )
		{
			path[0] = 0;
			return;
		}
		JNIEnv *env;
		m_vm->GetEnv( (void **)&env, JNI_VERSION_1_2 );
		jclass cls = env->GetObjectClass(m_obj);
		//string GetExtFont(string, string, int, int[])
		jmethodID id_get = env->GetMethodID( cls, "GetExtFont", "(Ljava/lang/String;Ljava/lang/String;I[I)Ljava/lang/String;" );
		jstring jcol = env->NewStringUTF(collection);
		jstring jname = env->NewStringUTF(fname);
		jintArray jflags = env->NewIntArray(1);
		jstring ret = (jstring)env->CallObjectMethod( m_obj, id_get, jcol, jname, flag, jflags );
		env->GetIntArrayRegion( jflags, 0, 1, &flags );
		PDF_String path1;
		cvt_js_to_cs( env, ret, path1 );
		if( path1.m_val )
			ansi_cpy( path, path1.m_val );
		else
			path[0] = 0;
		path1.free();
		if( ret ) env->DeleteLocalRef( ret );
		env->DeleteLocalRef( jcol );
		env->DeleteLocalRef( jname );
		env->DeleteLocalRef( jflags );
	}
private:
	JavaVM* m_vm;
	jobject m_obj;
};

extern "C" JNIEXPORT jstring FUNC_NAME(Global_getVersion)( JNIEnv* env, jobject thiz )
{
	char dst[32];
	convert_str( g_ver, dst );
	return env->NewStringUTF( dst );
}


//pdf envirment
extern "C" JNIEXPORT jboolean FUNC_NAME(Global_activePremium)( JNIEnv* env, jobject thiz, jobject context, jstring company, jstring mail, jstring serial )
{
	jstring name = getPackage( env, context );
	if( !name || !company || !mail || !serial ) return JNI_FALSE;
	const char *sname = env->GetStringUTFChars( name, NULL );
	const char *scompany = env->GetStringUTFChars( company, NULL );
	const char *smail = env->GetStringUTFChars( mail, NULL );
	const char *sserial = env->GetStringUTFChars( serial, NULL );
	if( !sname || !scompany || !smail || !sserial ) return JNI_FALSE;
	char path[512];
	ansi_cpy( path, sname );
	RDI32 len = ansi_len( path );
	path[len] = '[';
	len++;
	ansi_cpy( path + len, "advanced ver" );
	len += 12;
	path[len] = ']';
	path[len + 1] = 0;
	if( !GCmpSerialEx( path, scompany, smail, sserial ) )
	{
		return JNI_FALSE;
	}
	g_active_mode = 3;
	g_active_demo = !ansi_cmp( sname, "com.radaee.reader" );
	return JNI_TRUE;
}

extern "C" JNIEXPORT jboolean FUNC_NAME(Global_activeProfessional)( JNIEnv* env, jobject thiz, jobject context, jstring company, jstring mail, jstring serial )
{
	jstring name = getPackage( env, context );
	if( !name || !company || !mail || !serial ) return JNI_FALSE;
	const char *sname = env->GetStringUTFChars( name, NULL );
	const char *scompany = env->GetStringUTFChars( company, NULL );
	const char *smail = env->GetStringUTFChars( mail, NULL );
	const char *sserial = env->GetStringUTFChars( serial, NULL );
	if( !sname || !scompany || !smail || !sserial ) return JNI_FALSE;
	if( !GCmpSerialEx( sname, scompany, smail, sserial ) )
	{
		return JNI_FALSE;
	}
	g_active_mode = 2;
	g_active_demo = !ansi_cmp( sname, "com.radaee.reader" );
	return JNI_TRUE;
}

extern "C" JNIEXPORT jboolean FUNC_NAME(Global_activeStandard)( JNIEnv* env, jobject thiz, jobject context, jstring company, jstring mail, jstring serial )
{
	jstring name = getPackage( env, context );
	if( !name || !company || !mail || !serial ) return JNI_FALSE;
	const char *sname = env->GetStringUTFChars( name, NULL );
	const char *scompany = env->GetStringUTFChars( company, NULL );
	const char *smail = env->GetStringUTFChars( mail, NULL );
	const char *sserial = env->GetStringUTFChars( serial, NULL );
	if( !sname || !scompany || !smail || !sserial ) return JNI_FALSE;
	char path[512];
	ansi_cpy( path, sname );
	RDI32 len = ansi_len( path );
	path[len] = '[';
	len++;
	ansi_cpy( path + len, "view only" );
	len += 9;
	path[len] = ']';
	path[len + 1] = 0;
	if( !GCmpSerialEx( path, scompany, smail, sserial ) )
	{
		return JNI_FALSE;
	}
	g_active_mode = 1;
	g_active_demo = !ansi_cmp( sname, "com.radaee.reader" );
	return JNI_TRUE;
}

//pdf envirment
extern "C" JNIEXPORT jboolean FUNC_NAME(Global_activePremiumForVer)( JNIEnv* env, jobject thiz, jobject context, jstring company, jstring mail, jstring serial )
{
	jstring name = getPackage( env, context );
	if( !name || !company || !mail || !serial ) return JNI_FALSE;
	const char *sname = env->GetStringUTFChars( name, NULL );
	const char *scompany = env->GetStringUTFChars( company, NULL );
	const char *smail = env->GetStringUTFChars( mail, NULL );
	const char *sserial = env->GetStringUTFChars( serial, NULL );
	if( !sname || !scompany || !smail || !sserial ) return JNI_FALSE;
	char path[512];
	ansi_cpy( path, sname );
	RDI32 len = ansi_len( path );
	path[len] = '[';
	len++;
	ansi_cpy( path + len, "advanced ver " );
	len += 13;
	char ver[32];
	convert_str(g_ver, ver);
	ansi_cpy( path + len, ver );
	len += ansi_len( ver );
	path[len] = ']';
	path[len + 1] = 0;
	if( !GCmpSerialEx( path, scompany, smail, sserial ) )
	{
		return JNI_FALSE;
	}
	g_active_mode = 3;
	convert_str(g_package, ver);
	g_active_demo = !ansi_cmp( sname, ver );
	if( !g_active_demo )
	{
		convert_str(g_oem, ver);
		g_active_demo = ansi_str( scompany, ver ) != NULL;
	}
	return JNI_TRUE;
}

extern "C" JNIEXPORT jboolean FUNC_NAME(Global_activeProfessionalForVer)( JNIEnv* env, jobject thiz, jobject context, jstring company, jstring mail, jstring serial )
{
	jstring name = getPackage( env, context );
	if( !name || !company || !mail || !serial ) return JNI_FALSE;
	const char *sname = env->GetStringUTFChars( name, NULL );
	const char *scompany = env->GetStringUTFChars( company, NULL );
	const char *smail = env->GetStringUTFChars( mail, NULL );
	const char *sserial = env->GetStringUTFChars( serial, NULL );
	if( !sname || !scompany || !smail || !sserial ) return JNI_FALSE;
	char path[512];
	ansi_cpy( path, sname );
	RDI32 len = ansi_len( path );
	path[len] = '[';
	len++;
	ansi_cpy( path + len, "proifessional " );
	len += 13;
	char ver[32];
	convert_str(g_ver, ver);
	ansi_cpy( path + len, ver );
	len += ansi_len( ver );
	path[len] = ']';
	path[len + 1] = 0;
	if( !GCmpSerialEx( path, scompany, smail, sserial ) )
	{
		return JNI_FALSE;
	}
	g_active_mode = 2;
	convert_str(g_package, ver);
	g_active_demo = !ansi_cmp( sname, ver );
	if( !g_active_demo )
	{
		convert_str(g_oem, ver);
		g_active_demo = ansi_str( scompany, ver ) != NULL;
	}
	return JNI_TRUE;
}

extern "C" JNIEXPORT jboolean FUNC_NAME(Global_activeStandardForVer)( JNIEnv* env, jobject thiz, jobject context, jstring company, jstring mail, jstring serial )
{
	jstring name = getPackage( env, context );
	if( !name || !company || !mail || !serial ) return JNI_FALSE;
	const char *sname = env->GetStringUTFChars( name, NULL );
	const char *scompany = env->GetStringUTFChars( company, NULL );
	const char *smail = env->GetStringUTFChars( mail, NULL );
	const char *sserial = env->GetStringUTFChars( serial, NULL );
	if( !sname || !scompany || !smail || !sserial ) return JNI_FALSE;
	char path[512];
	ansi_cpy( path, sname );
	RDI32 len = ansi_len( path );
	path[len] = '[';
	len++;
	ansi_cpy( path + len, "view only " );
	len += 10;
	char ver[32];
	convert_str(g_ver, ver);
	ansi_cpy( path + len, ver );
	len += ansi_len( ver );
	path[len] = ']';
	path[len + 1] = 0;
	if( !GCmpSerialEx( path, scompany, smail, sserial ) )
	{
		return JNI_FALSE;
	}
	g_active_mode = 1;
	convert_str(g_package, ver);
	g_active_demo = !ansi_cmp( sname, ver );
	if( !g_active_demo )
	{
		convert_str(g_oem, ver);
		g_active_demo = ansi_str( scompany, ver ) != NULL;
	}
	return JNI_TRUE;
}

extern "C" JNIEXPORT jboolean FUNC_NAME(Global_keyGen)( JNIEnv* env, jobject thiz, jstring id, jstring save_file )
{
	PDF_String sid;
	PDF_String sfile;
	cvt_js_to_cs( env, id, sid );
	if( sid.m_len <= 0 ) return JNI_FALSE;
	cvt_js_to_cs( env, save_file, sfile );
	if( sfile.m_len <= 0 ) return JNI_FALSE;
	remove( sfile.m_val );
	CRDFile dst;
	if( dst.RDFOpen( sfile.m_val ) != RDFILE_ERR_OK )
	{
		sid.free();
		sfile.free();
		return JNI_FALSE;
	}
	RDU32 crc32 = GDigestCRC( (const RDU8 *)sid.m_val, sid.m_len, NULL, 0 );
	char ser[8];
	GSerial( crc32, ser );
	dst.RDFWrite( ser, 6 );
	dst.RDFClose();
	sid.free();
	sfile.free();
	return JNI_TRUE;
}

unsigned int GUnserial( const char *pSerial );
extern "C" JNIEXPORT jboolean FUNC_NAME(Global_keyCheck)( JNIEnv* env, jobject thiz, jstring id, jstring load_file )
{
	PDF_String sid;
	PDF_String sfile;
	cvt_js_to_cs( env, id, sid );
	if( sid.m_len <= 0 ) return JNI_FALSE;
	cvt_js_to_cs( env, load_file, sfile );
	if( sfile.m_len <= 0 ) return JNI_FALSE;
	CRDFile dst;
	if( dst.RDFOpen( sfile.m_val, RDFILE_READ ) != RDFILE_ERR_OK )
	{
		sid.free();
		sfile.free();
		return JNI_FALSE;
	}
	char ser[8];
	dst.RDFRead( ser, 6 );
	dst.RDFClose();
	RDU32 check = GUnserial( ser );
	RDU32 crc32 = GDigestCRC( (const RDU8 *)sid.m_val, sid.m_len, NULL, 0 )	% 0x81BF1000;
	sid.free();
	sfile.free();
	return (check == crc32);
}

extern "C" JNIEXPORT void FUNC_NAME(Global_hideAnnots)( JNIEnv* env, jobject thiz, jboolean hide )
{
	g_show_annots = !hide;
}

CPDFEnviroment g_env;
void load_std_font( RDI32 index, const char *path );
void unload_std_font( RDI32 index );

extern "C" JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved)
{
	JNIEnv* env = NULL;
	if( vm->GetEnv((void**)&env, JNI_VERSION_1_2) != JNI_OK)
	{
		return -1;
	}
	return JNI_VERSION_1_2;
}

extern "C" JNIEXPORT void FUNC_NAME(Global_loadStdFont)( JNIEnv* env, jobject thiz, jint index, jstring path )
{
	PDF_String cpath;
	cvt_js_to_cs( env, path, cpath );
	load_std_font( index, cpath.m_val );
	cpath.free();
}

extern "C" JNIEXPORT void FUNC_NAME(Global_unloadStdFont)( JNIEnv* env, jobject thiz, jint index )
{
	unload_std_font( index );
}

extern "C" JNIEXPORT void FUNC_NAME(Global_setCMapsPath)( JNIEnv* env, jobject thiz, jstring cmaps, jstring umaps )
{
	PDF_String cpath;
	PDF_String upath;
	cvt_js_to_cs( env, cmaps, cpath );
	cvt_js_to_cs( env, umaps, upath );
	g_env.env_set_cmaps_path( cpath.m_val, upath.m_val );
	cpath.free();
	upath.free();
}

extern "C" JNIEXPORT jboolean FUNC_NAME(Global_setCMYKICCPath)(JNIEnv* env, jobject thiz, jstring path)
{
	PDF_String cpath;
	cvt_js_to_cs(env, path, cpath);
	RDBOOL ret = g_env.env_set_cmyk_rgb(cpath.m_val);
	cpath.free();
	return ret;
}

extern "C" JNIEXPORT void JNICALL FUNC_NAME(Global_fontfileListStart)( JNIEnv* env, jobject thiz )
{
	g_env.env_add_font_file_start();
}

extern "C" JNIEXPORT void JNICALL FUNC_NAME(Global_fontfileListAdd)( JNIEnv* env, jobject thiz, jstring font_file )
{
	PDF_String cpath;
	cvt_js_to_cs( env, font_file, cpath );
	g_env.env_add_font_file( cpath.m_val );
	cpath.free();
}

extern "C" JNIEXPORT void JNICALL FUNC_NAME(Global_fontfileListEnd)( JNIEnv* env, jobject thiz )
{
	g_env.env_add_font_file_end();
}

extern "C" JNIEXPORT jboolean JNICALL FUNC_NAME(Global_fontfileMapping)( JNIEnv* env, jobject thiz, jstring map_name, jstring name )
{
	PDF_String map_str;
	PDF_String str;
	cvt_js_to_cs( env, map_name, map_str );
	cvt_js_to_cs( env, name, str );
	RDBOOL ret = g_env.env_map_face( map_str.m_val, str.m_val );
	map_str.free();
	str.free();
	return ret;
}

extern "C" JNIEXPORT jboolean JNICALL FUNC_NAME(Global_setAnnotFont)( JNIEnv* env, jobject thiz, jstring font_name )
{
	PDF_String sname;
	cvt_js_to_cs( env, font_name, sname );
	RDBOOL ret = g_env.env_set_annot_font( sname.m_val );
	sname.free();
	return ret;
}

extern "C" JNIEXPORT jboolean JNICALL FUNC_NAME(Global_setDefaultFont)( JNIEnv* env, jobject thiz, jstring collection, jstring font_name, jboolean fixed )
{
	PDF_String scol;
	PDF_String sname;
	cvt_js_to_cs( env, collection, scol );
	cvt_js_to_cs( env, font_name, sname );
	RDBOOL ret = g_env.env_set_def_font( scol.m_val, sname.m_val, fixed );
	scol.free();
	sname.free();
	return ret;
}

extern "C" JNIEXPORT jint JNICALL FUNC_NAME(Global_getFaceCount)( JNIEnv* env, jobject thiz )
{
	return g_env.env_get_face_count();
}

extern "C" JNIEXPORT jstring JNICALL FUNC_NAME(Global_getFaceName)( JNIEnv* env, jobject thiz, jint index )
{
	const char *tmp = g_env.env_get_face_name(index);
	if( tmp )
		return env->NewStringUTF(tmp);
	else
		return NULL;
}


extern "C" JNIEXPORT void JNICALL FUNC_NAME(Global_toDIBPoint)(JNIEnv* env, jobject thiz, jlong matrix, jfloatArray ppoint, jfloatArray dpoint)
{
	if( !matrix || !ppoint || !dpoint ) return;
	jfloat *uarr = env->GetFloatArrayElements( ppoint, JNI_FALSE );
	jfloat *darr = env->GetFloatArrayElements( dpoint, JNI_FALSE );
	RDMATRIX *mat = (RDMATRIX *)matrix;
	RDFIX x = uarr[0];
	RDFIX y = uarr[1];
	mat->transform_point( x, y );
	darr[0] = x.to_f32();
	darr[1] = y.to_f32();
	env->ReleaseFloatArrayElements(dpoint, darr, 0);
	env->ReleaseFloatArrayElements(ppoint, uarr, 0);
}

extern "C" JNIEXPORT void JNICALL FUNC_NAME(Global_toPDFPoint)(JNIEnv* env, jobject thiz, jlong matrix, jfloatArray dpoint, jfloatArray ppoint)
{
	if( !matrix || !ppoint || !dpoint ) return;
	jfloat *uarr = env->GetFloatArrayElements( ppoint, JNI_FALSE );
	jfloat *darr = env->GetFloatArrayElements( dpoint, JNI_FALSE );
	RDMATRIX *mat = (RDMATRIX *)matrix;
	RDFIX x = darr[0];
	RDFIX y = darr[1];
	RDMATRIX imat = *mat;
	imat.do_invert();
	imat.transform_point( x, y );
	uarr[0] = x.to_f32();
	uarr[1] = y.to_f32();
	env->ReleaseFloatArrayElements(dpoint, darr, 0);
	env->ReleaseFloatArrayElements(ppoint, uarr, 0);
}

extern "C" JNIEXPORT void JNICALL FUNC_NAME(Global_toDIBRect)(JNIEnv* env, jobject thiz, jlong matrix, jfloatArray prect, jfloatArray drect)
{
	if( !matrix || !prect || !drect ) return;
	jfloat *uarr = env->GetFloatArrayElements( prect, JNI_FALSE );
	jfloat *darr = env->GetFloatArrayElements( drect, JNI_FALSE );
	RDMATRIX *mat = (RDMATRIX *)matrix;
	RDRECTF rect;
	rect.left = uarr[0];
	rect.top = uarr[1];
	rect.right = uarr[2];
	rect.bottom = uarr[3];
	mat->get_bound( rect );
	darr[0] = rect.left.to_f32();
	darr[1] = rect.top.to_f32();
	darr[2] = rect.right.to_f32();
	darr[3] = rect.bottom.to_f32();
	env->ReleaseFloatArrayElements(drect, darr, 0);
	env->ReleaseFloatArrayElements(prect, uarr, 0);
}

extern "C" JNIEXPORT void JNICALL FUNC_NAME(Global_toPDFRect)(JNIEnv* env, jobject thiz, jlong matrix, jfloatArray drect, jfloatArray prect)
{
	if( !matrix || !prect || !drect ) return;
	jfloat *uarr = env->GetFloatArrayElements( prect, JNI_FALSE );
	jfloat *darr = env->GetFloatArrayElements( drect, JNI_FALSE );
	RDMATRIX *mat = (RDMATRIX *)matrix;
	RDRECTF rect;
	rect.left = darr[0];
	rect.top = darr[1];
	rect.right = darr[2];
	rect.bottom = darr[3];
	RDMATRIX imat = *mat;
	imat.do_invert();
	imat.get_bound( rect );



	uarr[0] = rect.left.to_f32();
	uarr[1] = rect.top.to_f32();
	uarr[2] = rect.right.to_f32();
	uarr[3] = rect.bottom.to_f32();
	env->ReleaseFloatArrayElements(drect, darr, 0);
	env->ReleaseFloatArrayElements(prect, uarr, 0);
}

struct PDF_CACHE
{
	RDI32 w;
	RDI32 h;
	RDI32 size;
	RDU8 dat[4];
};

#ifdef USE_ARM_ASM
extern RDBOOL g_support_neon;
extern "C" void vcvt_rgba_565(RDU16 *dst, RDU8 *src, RDI32 len);
extern "C" void vcvt_565_rgba(RDU8 *dst, RDU16 *src, RDI32 len);
extern "C" void vcvt_rgba_4444(RDU16 *dst, RDU8 *src, RDI32 len);
extern "C" void vcvt_4444_rgba(RDU8 *dst, RDU16 *src, RDI32 len);
#endif

class CPDFBmp
{
public:
	CPDFBmp(JNIEnv* env, jobject bitmap)
	{
		m_bitmap = NULL;
		m_dat = NULL;
		m_env = env;
		AndroidBitmapInfo info;
		if (AndroidBitmap_getInfo(env, bitmap, &info) < 0)
		{
			//__android_log_write(ANDROID_LOG_INFO, "BITMAP", "FAILED to get info.");
			return;
		}
		//char tmp[256];
		//ansi_format(tmp, "Width%d  Height:%d  Stride:%d  Format:%d  Flags:%d", info.width, info.height, info.stride, info.format, info.flags );
		//__android_log_write(ANDROID_LOG_INFO, "BITMAP", tmp);
		if (info.height <= 0 || info.width <= 0)
		{
			//__android_log_write(ANDROID_LOG_INFO, "BITMAP", "FAILED to get info.");
			return;
		}
		m_format = 0;
		switch (info.format)
		{
		case ANDROID_BITMAP_FORMAT_RGB_565:
			m_format = 1;
			break;
		case ANDROID_BITMAP_FORMAT_RGBA_4444:
			m_format = 2;
			break;
		case ANDROID_BITMAP_FORMAT_A_8:
			m_dat = NULL;
			return;
		}
		if (!m_format && info.stride < (info.width << 2))
			return;
		if (m_format && info.stride < (info.width << 1))
			return;
		void *pixs;
		int ret = AndroidBitmap_lockPixels(env, bitmap, &pixs);
		//ansi_format(tmp, "LockPixels Failed:%d---%X", ret, (RDU32)(*pixs));
		//__android_log_write(ANDROID_LOG_INFO, "BITMAP", tmp);
		if (ret >= 0)
		{
			m_w = info.width;
			m_h = info.height;
			m_stride = info.stride;
			m_dat = (RDU8 *)pixs;
			m_bitmap = bitmap;
			m_env = env;
		}
	}
	~CPDFBmp()
	{
		free(m_env);
	}
	inline RDBOOL valid(){ return m_dat != NULL; }
	inline void free(JNIEnv* env)
	{
		if (m_dat && m_bitmap)
		{
			AndroidBitmap_unlockPixels(env, m_bitmap);
			m_dat = NULL;
			m_bitmap = NULL;
			m_env = NULL;
		}
	}
	inline void draw(RDI32 x, RDI32 y, PDF_CACHE *src)
	{
		if (!src) return;
		switch (m_format)
		{
		case 1:
			draw565(x, y, src);
			break;
		case 2:
			draw4444(x, y, src);
			break;
		default:
			draw32(x, y, src);
			break;
		}
	}
	inline void draw(RDI32 x, RDI32 y, CRDBmp32 *src)
	{
		if (!src) return;
		switch (m_format)
		{
		case 1:
			draw565(x, y, src);
			break;
		case 2:
			draw4444(x, y, src);
			break;
		default:
			draw32(x, y, src);
			break;
		}
	}
	inline void draw(RDI32 x, RDI32 y, RDI32 w, RDI32 h, PDF_CACHE *src)
	{
		if (!src) return;
		switch (m_format)
		{
		case 1:
		{
			CRDBmp32 *tmp = create32_565();
			if (tmp)
			{
				draw32(x, y, w, h, src, tmp);
				draw565(x, y, tmp);
				delete tmp;
			}
		}
			break;
		case 2:
		{
			CRDBmp32 *tmp = create32_4444();
			if (tmp)
			{
				draw32(x, y, w, h, src, tmp);
				draw4444(x, y, tmp);
				delete tmp;
			}
		}
			break;
		default:
		{
			CRDBmp32Ref dst(m_dat, m_w, m_h, m_stride);
			draw32(x, y, w, h, src, &dst);
		}
			break;
		}
	}
	inline void draw_rect(RDU32 color, RDI32 x, RDI32 y, RDI32 w, RDI32 h, RDI32 mode)
	{
		switch (m_format)
		{
		case 1:
			draw_rect565(color, x, y, w, h, mode);
			break;
		case 2:
			draw_rect4444(color, x, y, w, h, mode);
			break;
		default:
			draw_rect32(color, x, y, w, h, mode);
			break;
		}
	}
	inline void invert()
	{
		switch (m_format)
		{
		case 1:
		{
			RDU32 total = m_h * m_stride;
			rdxor_const_ints((RDU32 *)m_dat, 0xFFFFFFFF, (m_h * m_stride) >> 2);
			if (total & 3)
				*(RDU16 *)(m_dat + total - 2) ^= 0xFFFF;
		}
			break;
		case 2:
		{
			RDU32 total = m_h * m_stride;
			rdxor_const_ints((RDU32 *)m_dat, 0xFFF0FFF0, (m_h * m_stride) >> 2);
			if (total & 3)
				*(RDU16 *)(m_dat + total - 2) ^= 0xFFF0;
		}
			break;
		default:
			rdxor_const_ints((RDU32 *)m_dat, 0xFFFFFF, (m_h * m_stride) >> 2);
			break;
		}
	}
	inline RDI32 get_w(){ return m_w; }
	inline RDI32 get_h(){ return m_h; }
	inline RDI32 get_stride(){ return m_stride; }
	inline RDBOOL is_rgba32(){ return m_format == 0; }
	inline RDU8 *get_dat(){ return m_dat; }
	inline CRDBmp32 *create32()
	{
		switch (m_format)
		{
		case 1:
			return create32_565();
			break;
		case 2:
			return create32_4444();
			break;
		default:
			return new CRDBmp32Ref(m_dat, m_w, m_h, m_stride);
			break;
		}
	}
protected:
	inline CRDBmp32 *create32_4444()
	{
		CRDBmp32 *bmp = new CRDBmp32(m_w, m_h);
		if (!bmp) return NULL;
		if (!bmp->is_valid())
		{
			delete bmp;
			return NULL;
		}
		RDU8 *bits_dst = bmp->get_bits();
		RDU8 *bits_src = m_dat;
		RDI32 stride_dst = bmp->get_stride();
		RDI32 stride_src = m_stride;
		RDI32 h = m_h;
		while (h > 0)
		{
			RDU8 *dst = bits_dst;
			RDU16 *src = (RDU16 *)bits_src;
			RDU8 *dst_end = dst + stride_dst;
#ifdef USE_ARM_ASM
			if (g_support_neon)
			{
				vcvt_4444_rgba(dst, src, m_w);
				dst += (m_w &(~7)) << 2;
				src += m_w &(~7);
			}
#endif
			while (dst < dst_end)
			{
				dst[0] = (src[0] & 0xf000) >> 8;
				dst[1] = (src[0] & 0x0f00) >> 4;
				dst[2] = src[0] & 0x00f0;
				dst[3] = (src[0] & 0x000f) << 4;
				dst += 4;
				src++;
			}
			bits_dst += stride_dst;
			bits_src += stride_src;
			h--;
		}
		return bmp;
	}
	inline CRDBmp32 *create32_565()
	{
		CRDBmp32 *bmp = new CRDBmp32(m_w, m_h);
		if (!bmp) return NULL;
		if (!bmp->is_valid())
		{
			delete bmp;
			return NULL;
		}
		RDU8 *bits_dst = bmp->get_bits();
		RDU8 *bits_src = m_dat;
		RDI32 stride_dst = bmp->get_stride();
		RDI32 stride_src = m_stride;
		RDI32 h = m_h;
		while (h > 0)
		{
			RDU8 *dst = bits_dst;
			RDU16 *src = (RDU16 *)bits_src;
			RDU8 *dst_end = dst + stride_dst;
#ifdef USE_ARM_ASM
			if (g_support_neon)
			{
				vcvt_565_rgba(dst, src, m_w);
				dst += (m_w &(~7)) << 2;
				src += m_w &(~7);
			}
#endif
			while (dst < dst_end)
			{
				dst[0] = (src[0] & 0xf800) >> 8;
				dst[1] = (src[0] & 0x07e0) >> 3;
				dst[2] = (src[0] & 0x1f) << 3;
				dst[3] = 255;
				dst += 4;
				src++;
			}
			bits_dst += stride_dst;
			bits_src += stride_src;
			h--;
		}
		return bmp;
	}
	inline void draw32(RDI32 x, RDI32 y, RDI32 w, RDI32 h, PDF_CACHE *dib, CRDBmp32 *dst)
	{
		if (!dib == 0) return;
		CRDBmp32Ref src(((PDF_CACHE *)dib)->dat, ((PDF_CACHE *)dib)->w, ((PDF_CACHE *)dib)->h, ((PDF_CACHE *)dib)->w << 2);
		if (x == 0 && y == 0 && w == ((PDF_CACHE *)dib)->w && h == ((PDF_CACHE *)dib)->h)
		{
			RDRECT rect;
			rect.left = -x;
			rect.top = -y;
			rect.right = w - x;
			rect.bottom = h - y;
			dst->copy_rect(&src, rect);
		}
		else
		{
			RDPOINT org;
			org.x = x;
			org.y = y;
			if (w * 3 < ((PDF_CACHE *)dib)->w || h * 3 < ((PDF_CACHE *)dib)->h)
				src.bmp_scale_best_to(RDFIX(w) / ((PDF_CACHE *)dib)->w, RDFIX(h) / ((PDF_CACHE *)dib)->h, org, dst);
			else
				src.bmp_scale_normal_to(RDFIX(w) / ((PDF_CACHE *)dib)->w, RDFIX(h) / ((PDF_CACHE *)dib)->h, org, dst);
		}
	}
	void draw32(RDI32 x, RDI32 y, PDF_CACHE *dib)
	{
		RDI32 num_rows;
		RDI32 num_cols;
		RDI32 width_src = dib->w;
		RDI32 height_src = dib->h;
		RDI32 width_dst = m_w;
		RDI32 height_dst = m_h;
		if (x >= width_dst || y >= height_dst || x + width_src <= 0 || y + height_src <= 0)
			return;
		RDU8 *bits_dst = m_dat;
		RDU8 *bits_src = dib->dat;
		RDI32 stride_dst = m_stride;
		RDI32 stride_src = width_src << 2;
		if (x > 0)
		{
			bits_dst += (x << 2);
			num_cols = width_dst - x;
			if (num_cols > width_src) num_cols = width_src;
		}
		else
		{
			bits_src -= (x << 2);
			num_cols = width_src + x;
			if (num_cols > width_dst) num_cols = width_dst;
		}
		if (y > 0)
		{
			bits_dst += y * stride_dst;
			num_rows = height_dst - y;
			if (num_rows > height_src) num_rows = height_src;
		}
		else
		{
			bits_src -= y * stride_src;
			num_rows = height_src + y;
			if (num_rows > height_dst) num_rows = height_dst;
		}
		while (num_rows > 0)
		{
			rdcpy_ints((RDU32 *)bits_dst, (const RDU32 *)bits_src, num_cols);
			bits_src += stride_src;
			bits_dst += stride_dst;
			num_rows--;
		}
	}
	void draw4444(RDI32 x, RDI32 y, PDF_CACHE *dib)
	{
		RDI32 num_rows;
		RDI32 num_cols;
		RDI32 width_src = dib->w;
		RDI32 height_src = dib->h;
		RDI32 width_dst = m_w;
		RDI32 height_dst = m_h;
		if (x >= width_dst || y >= height_dst || x + width_src <= 0 || y + height_src <= 0)
			return;
		RDU8 *bits_dst = m_dat;
		RDU8 *bits_src = dib->dat;
		RDI32 stride_dst = m_stride;
		RDI32 stride_src = width_src << 2;
		if (x > 0)
		{
			bits_dst += (x << 2);
			num_cols = width_dst - x;
			if (num_cols > width_src) num_cols = width_src;
		}
		else
		{
			bits_src -= (x << 2);
			num_cols = width_src + x;
			if (num_cols > width_dst) num_cols = width_dst;
		}
		if (y > 0)
		{
			bits_dst += y * stride_dst;
			num_rows = height_dst - y;
			if (num_rows > height_src) num_rows = height_src;
		}
		else
		{
			bits_src -= y * stride_src;
			num_rows = height_src + y;
			if (num_rows > height_dst) num_rows = height_dst;
		}
		while (num_rows > 0)
		{
			RDU8 *src = bits_src;
			RDU8 *src_end = src + (num_cols << 2);
			RDU16 *dst = (RDU16 *)bits_dst;
#ifdef USE_ARM_ASM
			if (g_support_neon)
			{
				vcvt_rgba_4444(dst, src, num_cols);
				dst += num_cols &(~7);
				src += (num_cols &(~7)) << 2;
			}
#endif
			while (src < src_end)
			{
				*dst++ = ((src[0] & 0xf0) << 8) | ((src[1] & 0xf0) << 4) | (src[2] & 0xf0) | ((src[3] & 0xf0) >> 4);
				src += 4;
			}
			bits_src += stride_src;
			bits_dst += stride_dst;
			num_rows--;
		}
	}
	void draw565(RDI32 x, RDI32 y, PDF_CACHE *dib)
	{
		RDI32 num_rows;
		RDI32 num_cols;
		RDI32 width_src = dib->w;
		RDI32 height_src = dib->h;
		RDI32 width_dst = m_w;
		RDI32 height_dst = m_h;
		if (x >= width_dst || y >= height_dst || x + width_src <= 0 || y + height_src <= 0)
			return;
		RDU8 *bits_dst = m_dat;
		RDU8 *bits_src = dib->dat;
		RDI32 stride_dst = m_stride;
		RDI32 stride_src = width_src << 2;
		if (x > 0)
		{
			bits_dst += (x << 2);
			num_cols = width_dst - x;
			if (num_cols > width_src) num_cols = width_src;
		}
		else
		{
			bits_src -= (x << 2);
			num_cols = width_src + x;
			if (num_cols > width_dst) num_cols = width_dst;
		}
		if (y > 0)
		{
			bits_dst += y * stride_dst;
			num_rows = height_dst - y;
			if (num_rows > height_src) num_rows = height_src;
		}
		else
		{
			bits_src -= y * stride_src;
			num_rows = height_src + y;
			if (num_rows > height_dst) num_rows = height_dst;
		}
		while (num_rows > 0)
		{
			RDU8 *src = bits_src;
			RDU8 *src_end = src + (num_cols << 2);
			RDU16 *dst = (RDU16 *)bits_dst;
#ifdef USE_ARM_ASM
			if (g_support_neon)
			{
				vcvt_rgba_565(dst, src, num_cols);
				dst += num_cols &(~7);
				src += (num_cols &(~7))<<2;
			}
#endif
			while (src < src_end)
			{
				*dst++ = ((src[0] >> 3) << 11) | ((src[1] >> 2) << 5) | (src[2] >> 3);
				src += 4;
			}
			bits_src += stride_src;
			bits_dst += stride_dst;
			num_rows--;
		}
	}
	void draw32(RDI32 x, RDI32 y, CRDBmp32 *dib)
	{
		RDI32 num_rows;
		RDI32 num_cols;
		RDI32 width_src = dib->get_width();
		RDI32 height_src = dib->get_height();
		RDI32 width_dst = m_w;
		RDI32 height_dst = m_h;
		if (x >= width_dst || y >= height_dst || x + width_src <= 0 || y + height_src <= 0)
			return;
		RDU8 *bits_dst = m_dat;
		RDU8 *bits_src = dib->get_bits();
		RDI32 stride_dst = m_stride;
		RDI32 stride_src = width_src << 2;
		if (x > 0)
		{
			bits_dst += (x << 2);
			num_cols = width_dst - x;
			if (num_cols > width_src) num_cols = width_src;
		}
		else
		{
			bits_src -= (x << 2);
			num_cols = width_src + x;
			if (num_cols > width_dst) num_cols = width_dst;
		}
		if (y > 0)
		{
			bits_dst += y * stride_dst;
			num_rows = height_dst - y;
			if (num_rows > height_src) num_rows = height_src;
		}
		else
		{
			bits_src -= y * stride_src;
			num_rows = height_src + y;
			if (num_rows > height_dst) num_rows = height_dst;
		}
		while (num_rows > 0)
		{
			rdcpy_ints((RDU32 *)bits_dst, (const RDU32 *)bits_src, num_cols);
			bits_src += stride_src;
			bits_dst += stride_dst;
			num_rows--;
		}
	}
	void draw4444(RDI32 x, RDI32 y, CRDBmp32 *dib)
	{
		RDI32 num_rows;
		RDI32 num_cols;
		RDI32 width_src = dib->get_width();
		RDI32 height_src = dib->get_height();
		RDI32 width_dst = m_w;
		RDI32 height_dst = m_h;
		if (x >= width_dst || y >= height_dst || x + width_src <= 0 || y + height_src <= 0)
			return;
		RDU8 *bits_dst = m_dat;
		RDU8 *bits_src = dib->get_bits();
		RDI32 stride_dst = m_stride;
		RDI32 stride_src = width_src << 2;
		if (x > 0)
		{
			bits_dst += (x << 2);
			num_cols = width_dst - x;
			if (num_cols > width_src) num_cols = width_src;
		}
		else
		{
			bits_src -= (x << 2);
			num_cols = width_src + x;
			if (num_cols > width_dst) num_cols = width_dst;
		}
		if (y > 0)
		{
			bits_dst += y * stride_dst;
			num_rows = height_dst - y;
			if (num_rows > height_src) num_rows = height_src;
		}
		else
		{
			bits_src -= y * stride_src;
			num_rows = height_src + y;
			if (num_rows > height_dst) num_rows = height_dst;
		}
		while (num_rows > 0)
		{
			RDU8 *src = bits_src;
			RDU8 *src_end = src + (num_cols << 2);
			RDU16 *dst = (RDU16 *)bits_dst;
#ifdef USE_ARM_ASM
			if (g_support_neon)
			{
				vcvt_rgba_4444(dst, src, num_cols);
				dst += num_cols &(~7);
				src += (num_cols &(~7)) << 2;
			}
#endif
			while (src < src_end)
			{
				*dst++ = ((src[0] & 0xf0) << 8) | ((src[1] & 0xf0) << 4) | (src[2] & 0xf0) | ((src[3] & 0xf0) >> 4);
				src += 4;
			}
			bits_src += stride_src;
			bits_dst += stride_dst;
			num_rows--;
		}
	}
	void draw565(RDI32 x, RDI32 y, CRDBmp32 *dib)
	{
		RDI32 num_rows;
		RDI32 num_cols;
		RDI32 width_src = dib->get_width();
		RDI32 height_src = dib->get_height();
		RDI32 width_dst = m_w;
		RDI32 height_dst = m_h;
		if (x >= width_dst || y >= height_dst || x + width_src <= 0 || y + height_src <= 0)
			return;
		RDU8 *bits_dst = m_dat;
		RDU8 *bits_src = dib->get_bits();
		RDI32 stride_dst = m_stride;
		RDI32 stride_src = width_src << 2;
		if (x > 0)
		{
			bits_dst += (x << 2);
			num_cols = width_dst - x;
			if (num_cols > width_src) num_cols = width_src;
		}
		else
		{
			bits_src -= (x << 2);
			num_cols = width_src + x;
			if (num_cols > width_dst) num_cols = width_dst;
		}
		if (y > 0)
		{
			bits_dst += y * stride_dst;
			num_rows = height_dst - y;
			if (num_rows > height_src) num_rows = height_src;
		}
		else
		{
			bits_src -= y * stride_src;
			num_rows = height_src + y;
			if (num_rows > height_dst) num_rows = height_dst;
		}
		while (num_rows > 0)
		{
			RDU8 *src = bits_src;
			RDU8 *src_end = src + (num_cols << 2);
			RDU16 *dst = (RDU16 *)bits_dst;
#ifdef USE_ARM_ASM
			if (g_support_neon)
			{
				vcvt_rgba_565(dst, src, num_cols);
				dst += num_cols &(~7);
				src += (num_cols &(~7)) << 2;
			}
#endif
			while (src < src_end)
			{
				*dst++ = ((src[0] >> 3) << 11) | ((src[1] >> 2) << 5) | (src[2] >> 3);
				src += 4;
			}
			bits_src += stride_src;
			bits_dst += stride_dst;
			num_rows--;
		}
	}
	void draw_rect32(RDU32 cval, RDI32 x, RDI32 y, RDI32 width, RDI32 height, RDI32 mode)
	{
		RDI32 num_rows;
		RDI32 num_cols;
		RDI32 width_dst = m_w;
		RDI32 height_dst = m_h;
		if (x >= width_dst || y >= height_dst || x + width <= 0 || y + height <= 0 || width <= 0 || height <= 0)
			return;
		RDU8 *bits_dst = m_dat;
		RDI32 stride_dst = m_stride;
		if (x > 0)
		{
			bits_dst += (x << 2);
			num_cols = width_dst - x;
			if (num_cols > width) num_cols = width;
		}
		else
		{
			num_cols = width + x;
			if (num_cols > width_dst) num_cols = width_dst;
		}
		if (y > 0)
		{
			bits_dst += y * stride_dst;
			num_rows = height_dst - y;
			if (num_rows > height) num_rows = height;
		}
		else
		{
			num_rows = height + y;
			if (num_rows > height_dst) num_rows = height_dst;
		}
		RDU32 alpha = ((RDU8 *)&cval)[3];
		if (alpha > 252 || mode == 1)
		{
			RDI32 clr;
			((RDU8 *)&clr)[0] = ((RDU8 *)&cval)[2];
			((RDU8 *)&clr)[1] = ((RDU8 *)&cval)[1];
			((RDU8 *)&clr)[2] = ((RDU8 *)&cval)[0];
			((RDU8 *)&clr)[3] = ((RDU8 *)&cval)[3];
			while (num_rows > 0)
			{
				RDU8 *cur_dst = bits_dst;
				RDU8 *end_dst = bits_dst + ((num_cols - 7) << 2);
				while (cur_dst < end_dst)
				{
					((RDU32 *)cur_dst)[0] = clr;
					((RDU32 *)cur_dst)[1] = clr;
					((RDU32 *)cur_dst)[2] = clr;
					((RDU32 *)cur_dst)[3] = clr;
					((RDU32 *)cur_dst)[4] = clr;
					((RDU32 *)cur_dst)[5] = clr;
					((RDU32 *)cur_dst)[6] = clr;
					((RDU32 *)cur_dst)[7] = clr;
					cur_dst += 32;
				}
				end_dst += 28;
				while (cur_dst < end_dst)
				{
					((RDU32 *)cur_dst)[0] = clr;
					cur_dst += 4;
				}
				bits_dst += stride_dst;
				num_rows--;
			}
		}
		else if (alpha > 2)
		{
			RDU32 r = ((RDU8 *)&cval)[2];
			RDU32 g = ((RDU8 *)&cval)[1];
			RDU32 b = ((RDU8 *)&cval)[0];
			while (num_rows > 0)
			{
				RDU8 *cur_dst = bits_dst;
				RDU8 *end_dst = bits_dst + (num_cols << 2);
				while (cur_dst < end_dst)
				{
					cur_dst[0] = (r * alpha + cur_dst[0] * (256 - alpha)) >> 8;
					cur_dst[1] = (g * alpha + cur_dst[1] * (256 - alpha)) >> 8;
					cur_dst[2] = (b * alpha + cur_dst[2] * (256 - alpha)) >> 8;
					cur_dst[3] = 255;
					cur_dst += 4;
				}
				bits_dst += stride_dst;
				num_rows--;
			}
		}
	}
	void draw_rect4444(RDU32 cval, RDI32 x, RDI32 y, RDI32 width, RDI32 height, RDI32 mode)
	{
		RDI32 num_rows;
		RDI32 num_cols;
		RDI32 width_dst = m_w;
		RDI32 height_dst = m_h;
		if (x >= width_dst || y >= height_dst || x + width <= 0 || y + height <= 0 || width <= 0 || height <= 0)
			return;
		RDU8 *bits_dst = m_dat;
		RDI32 stride_dst = m_stride;
		if (x > 0)
		{
			bits_dst += (x << 2);
			num_cols = width_dst - x;
			if (num_cols > width) num_cols = width;
		}
		else
		{
			num_cols = width + x;
			if (num_cols > width_dst) num_cols = width_dst;
		}
		if (y > 0)
		{
			bits_dst += y * stride_dst;
			num_rows = height_dst - y;
			if (num_rows > height) num_rows = height;
		}
		else
		{
			num_rows = height + y;
			if (num_rows > height_dst) num_rows = height_dst;
		}
		RDU32 alpha = ((RDU8 *)&cval)[3];
		if (alpha > 252 || mode == 1)
		{
			RDU32 clr = (((cval >> 20) & 15) << 12) | (((cval >> 12) & 15) << 8) | (((cval >> 4) & 15) << 4) | ((cval >> 28) & 15);
			while (num_rows > 0)
			{
				RDU8 *cur_dst = bits_dst;
				RDU8 *end_dst = bits_dst + ((num_cols - 7) << 1);
				while (cur_dst < end_dst)
				{
					((RDU16 *)cur_dst)[0] = clr;
					((RDU16 *)cur_dst)[1] = clr;
					((RDU16 *)cur_dst)[2] = clr;
					((RDU16 *)cur_dst)[3] = clr;
					((RDU16 *)cur_dst)[4] = clr;
					((RDU16 *)cur_dst)[5] = clr;
					((RDU16 *)cur_dst)[6] = clr;
					((RDU16 *)cur_dst)[7] = clr;
					cur_dst += 16;
				}
				end_dst += 14;
				while (cur_dst < end_dst)
				{
					((RDU16 *)cur_dst)[0] = clr;
					cur_dst += 2;
				}
				bits_dst += stride_dst;
				num_rows--;
			}
		}
		else if (alpha > 2)
		{
			RDU32 r = ((RDU8 *)&cval)[2];
			RDU32 g = ((RDU8 *)&cval)[1];
			RDU32 b = ((RDU8 *)&cval)[0];
			while (num_rows > 0)
			{
				RDU16 *cur_dst = (RDU16 *)bits_dst;
				RDU16 *end_dst = cur_dst + num_cols;
				while (cur_dst < end_dst)
				{
					RDU32 tr = (r * alpha + (((cur_dst[0] >> 12) & 15) << 4) * (256 - alpha)) >> 12;
					RDU32 tg = (g * alpha + (((cur_dst[0] >> 8) & 15) << 4) * (256 - alpha)) >> 12;
					RDU32 tb = (b * alpha + (((cur_dst[0] >> 4) & 15) << 4) * (256 - alpha)) >> 12;
					*cur_dst++ = (tr<<12)|(tg<<8)|(tb<<4)|0xF;
				}
				bits_dst += stride_dst;
				num_rows--;
			}
		}
	}
	void draw_rect565(RDU32 cval, RDI32 x, RDI32 y, RDI32 width, RDI32 height, RDI32 mode)
	{
		RDI32 num_rows;
		RDI32 num_cols;
		RDI32 width_dst = m_w;
		RDI32 height_dst = m_h;
		if (x >= width_dst || y >= height_dst || x + width <= 0 || y + height <= 0 || width <= 0 || height <= 0)
			return;
		RDU8 *bits_dst = m_dat;
		RDI32 stride_dst = m_stride;
		if (x > 0)
		{
			bits_dst += (x << 2);
			num_cols = width_dst - x;
			if (num_cols > width) num_cols = width;
		}
		else
		{
			num_cols = width + x;
			if (num_cols > width_dst) num_cols = width_dst;
		}
		if (y > 0)
		{
			bits_dst += y * stride_dst;
			num_rows = height_dst - y;
			if (num_rows > height) num_rows = height;
		}
		else
		{
			num_rows = height + y;
			if (num_rows > height_dst) num_rows = height_dst;
		}
		RDU32 alpha = ((RDU8 *)&cval)[3];
		if (alpha > 252 || mode == 1)
		{
			RDU32 clr = ((cval >> 19) << 11) | (((cval >> 10) & 63) << 5) | ((cval >> 3) & 31);
			while (num_rows > 0)
			{
				RDU8 *cur_dst = bits_dst;
				RDU8 *end_dst = bits_dst + ((num_cols - 7) << 1);
				while (cur_dst < end_dst)
				{
					((RDU16 *)cur_dst)[0] = clr;
					((RDU16 *)cur_dst)[1] = clr;
					((RDU16 *)cur_dst)[2] = clr;
					((RDU16 *)cur_dst)[3] = clr;
					((RDU16 *)cur_dst)[4] = clr;
					((RDU16 *)cur_dst)[5] = clr;
					((RDU16 *)cur_dst)[6] = clr;
					((RDU16 *)cur_dst)[7] = clr;
					cur_dst += 16;
				}
				end_dst += 14;
				while (cur_dst < end_dst)
				{
					((RDU16 *)cur_dst)[0] = clr;
					cur_dst += 2;
				}
				bits_dst += stride_dst;
				num_rows--;
			}
		}
		else if (alpha > 2)
		{
			RDU32 r = ((RDU8 *)&cval)[2];
			RDU32 g = ((RDU8 *)&cval)[1];
			RDU32 b = ((RDU8 *)&cval)[0];
			while (num_rows > 0)
			{
				RDU16 *cur_dst = (RDU16 *)bits_dst;
				RDU16 *end_dst = cur_dst + num_cols;
				while (cur_dst < end_dst)
				{
					RDU32 tr = (r * alpha + ((cur_dst[0] & 0xf800) >> 8) * (256 - alpha)) >> 8;
					RDU32 tg = (g * alpha + ((cur_dst[0] & 0x07e0) >> 3) * (256 - alpha)) >> 8;
					RDU32 tb = (b * alpha + ((cur_dst[0] & 31) << 3) * (256 - alpha)) >> 8;
					*cur_dst++ = ((tr&0xf8) << 8) | ((tg&0xfc) << 3) | (tb>>3);
				}
				bits_dst += stride_dst;
				num_rows--;
			}
		}
	}
	RDI32 m_w;
	RDI32 m_h;
	RDI32 m_stride;
	RDI32 m_format;
	RDU8 *m_dat;
	jobject m_bitmap;
	JNIEnv *m_env;
};

extern "C" JNIEXPORT jlong JNICALL FUNC_NAME(DIB_get)(JNIEnv* env, jobject thiz, jlong dib, jint width, jint height)
{
	if (dib && ((PDF_CACHE *)dib)->size >= width * height * 4)
	{
		((PDF_CACHE *)dib)->w = width;
		((PDF_CACHE *)dib)->h = height;
		return dib;
	}
	PDF_CACHE *cache = (PDF_CACHE *)RDRealloc((void *)dib, sizeof(PDF_CACHE) + width * height * 4);
	if (cache)
	{
		cache->w = width;
		cache->h = height;
		cache->size = width * height * 4;
	}
	else
		RDFree((void *)dib);
	return (jlong)cache;
}

#include "GLES2/gl2.h"
extern "C" JNIEXPORT jint JNICALL FUNC_NAME(DIB_glGenTexture)(JNIEnv* env, jobject thiz, jlong dib, jboolean linear)
{
	if (!dib) return -1;
	PDF_CACHE *cache = (PDF_CACHE *)dib;
	GLuint texts;
	glGenTextures(1, &texts);
	glBindTexture(GL_TEXTURE_2D, texts);
	glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, cache->w, cache->h, 0, GL_RGBA, GL_UNSIGNED_BYTE, cache->dat);
	if (linear)
	{
		glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
	}
	else
	{
		glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
	}
	glBindTexture(GL_TEXTURE_2D, 0);
	return texts;
}

extern "C" JNIEXPORT void JNICALL FUNC_NAME(DIB_drawToDIB)(JNIEnv* env, jobject thiz, jlong dib, jlong dst, jint x, jint y)
{
	if (dib == 0 || dst == 0) return;
	PDF_CACHE *bmp = (PDF_CACHE *)dst;
	RDI32 num_rows;
	RDI32 num_cols;
	RDI32 width_src = ((PDF_CACHE *)dib)->w;
	RDI32 height_src = ((PDF_CACHE *)dib)->h;
	RDI32 width_dst = bmp->w;
	RDI32 height_dst = bmp->h;
	if (x >= width_dst || y >= height_dst || x + width_src <= 0 || y + height_src <= 0)
		return;
	RDU8 *bits_dst = bmp->dat;
	RDU8 *bits_src = ((PDF_CACHE *)dib)->dat;
	RDI32 stride_dst = bmp->w << 2;
	RDI32 stride_src = width_src << 2;
	if (x > 0)
	{
		bits_dst += x * 4;
		num_cols = width_dst - x;
		if (num_cols > width_src) num_cols = width_src;
	}
	else
	{
		bits_src -= x * 4;
		num_cols = width_src + x;
		if (num_cols > width_dst) num_cols = width_dst;
	}
	if (y > 0)
	{
		bits_dst += y * stride_dst;
		num_rows = height_dst - y;
		if (num_rows > height_src) num_rows = height_src;
	}
	else
	{
		bits_src -= y * stride_src;
		num_rows = height_src + y;
		if (num_rows > height_dst) num_rows = height_dst;
	}
	while (num_rows > 0)
	{
		rdcpy_ints((RDU32 *)bits_dst, (const RDU32 *)bits_src, num_cols);
		bits_src += stride_src;
		bits_dst += stride_dst;
		num_rows--;
	}
}
#include <stdint.h>
extern "C" JNIEXPORT void JNICALL FUNC_NAME(DIB_drawToBmp)(JNIEnv* env, jobject thiz, jlong dib, jlong bitmap, jint x, jint y)
{
	if (dib == 0 || bitmap == 0) return;
	((CPDFBmp *)bitmap)->draw(x, y, (PDF_CACHE *)dib);
}

extern "C" JNIEXPORT void JNICALL FUNC_NAME(DIB_drawToBmp2)(JNIEnv* env, jobject thiz, jlong dib, jlong bitmap, jint x, jint y, jint w, jint h)
{
	if (dib == 0 || bitmap == 0) return;
	((CPDFBmp *)bitmap)->draw(x, y, w, h, (PDF_CACHE *)dib);
}

extern "C" JNIEXPORT void JNICALL FUNC_NAME(DIB_drawRect)(JNIEnv* env, jobject thiz, jlong dib, jint color, jint x, jint y, jint width, jint height, jint mode)
{
	if (dib == 0) return;
	PDF_CACHE *bmp = (PDF_CACHE *)dib;
	RDI32 num_rows;
	RDI32 num_cols;
	RDI32 width_dst = bmp->w;
	RDI32 height_dst = bmp->h;
	if (x >= width_dst || y >= height_dst || x + width <= 0 || y + height <= 0 || width <= 0 || height <= 0)
		return;
	RDU8 *bits_dst = bmp->dat;
	RDI32 stride_dst = bmp->w << 2;
	if (x > 0)
	{
		bits_dst += x * 4;
		num_cols = width_dst - x;
		if (num_cols > width) num_cols = width;
	}
	else
	{
		num_cols = width + x;
		if (num_cols > width_dst) num_cols = width_dst;
	}
	if (y > 0)
	{
		bits_dst += y * stride_dst;
		num_rows = height_dst - y;
		if (num_rows > height) num_rows = height;
	}
	else
	{
		num_rows = height + y;
		if (num_rows > height_dst) num_rows = height_dst;
	}
	RDU32 alpha = ((RDU8 *)&color)[3];
	if (alpha > 252 || mode == 1)
	{
		RDI32 clr;
		((RDU8 *)&clr)[0] = ((RDU8 *)&color)[2];
		((RDU8 *)&clr)[1] = ((RDU8 *)&color)[1];
		((RDU8 *)&clr)[2] = ((RDU8 *)&color)[0];
		((RDU8 *)&clr)[3] = ((RDU8 *)&color)[3];
		while (num_rows > 0)
		{
			RDU8 *cur_dst = bits_dst;
			RDU8 *end_dst = bits_dst + ((num_cols - 7) << 2);
			while (cur_dst < end_dst)
			{
				((RDU32 *)cur_dst)[0] = clr;
				((RDU32 *)cur_dst)[1] = clr;
				((RDU32 *)cur_dst)[2] = clr;
				((RDU32 *)cur_dst)[3] = clr;
				((RDU32 *)cur_dst)[4] = clr;
				((RDU32 *)cur_dst)[5] = clr;
				((RDU32 *)cur_dst)[6] = clr;
				((RDU32 *)cur_dst)[7] = clr;
				cur_dst += 32;
			}
			end_dst += 28;
			while (cur_dst < end_dst)
			{
				((RDU32 *)cur_dst)[0] = clr;
				cur_dst += 4;
			}
			bits_dst += stride_dst;
			num_rows--;
		}
	}
	else if (alpha > 2)
	{
		RDU32 r = ((RDU8 *)&color)[2];
		RDU32 g = ((RDU8 *)&color)[1];
		RDU32 b = ((RDU8 *)&color)[0];
		while (num_rows > 0)
		{
			RDU8 *cur_dst = bits_dst;
			RDU8 *end_dst = bits_dst + (num_cols << 2);
			while (cur_dst < end_dst)
			{
				cur_dst[0] = (r * alpha + cur_dst[0] * (256 - alpha)) >> 8;
				cur_dst[1] = (g * alpha + cur_dst[1] * (256 - alpha)) >> 8;
				cur_dst[2] = (b * alpha + cur_dst[2] * (256 - alpha)) >> 8;
				cur_dst[3] = 255;
				cur_dst += 4;
			}
			bits_dst += stride_dst;
			num_rows--;
		}
	}
}

extern "C" JNIEXPORT void JNICALL FUNC_NAME(DIB_free)(JNIEnv* env, jobject thiz, jlong dib)
{
	RDFree((void *)dib);
}

extern "C" JNIEXPORT jlong JNICALL FUNC_NAME(BMP_get)(JNIEnv* env, jobject thiz, jobject bitmap)
{
	if( bitmap == NULL ) return 0;
	CPDFBmp *bmp = new CPDFBmp(env, bitmap);
	if (!bmp->valid())
	{
		delete bmp;
		return 0;
	}
	else
		return (jlong)bmp;
}

extern "C" JNIEXPORT void JNICALL FUNC_NAME(BMP_free)(JNIEnv* env, jobject thiz, jobject bitmap, jlong dib)
{
	if (dib)
	{
		((CPDFBmp *)dib)->free(env);
		delete ((CPDFBmp *)dib);
	}
}

extern "C" JNIEXPORT void JNICALL FUNC_NAME(BMP_drawToDIB)(JNIEnv* env, jobject thiz, jlong src, jlong dst, jint x, jint y)
{
	if( src == 0 || dst == 0 ) return;
	PDF_CACHE *tmp = (PDF_CACHE *)dst;
	CPDFBmp *dib = (CPDFBmp *)src;
	RDI32 num_rows;
	RDI32 num_cols;
	RDI32 width_src = dib->get_w();
	RDI32 height_src = dib->get_h();
	RDI32 width_dst = tmp->w;
	RDI32 height_dst = tmp->h;
	if (x >= width_dst || y >= height_dst || x + width_src <= 0 || y + height_src <= 0)
	{
		return;
	}
	CRDBmp32 *bmp = dib->create32();
	RDU8 *bits_dst = tmp->dat;
	RDU8 *bits_src = bmp->get_bits();
	RDI32 stride_dst = tmp->w << 2;
	RDI32 stride_src = width_src<<2;
	if( x > 0 )
	{
		bits_dst += x * 4;
		num_cols = width_dst - x;
		if( num_cols > width_src ) num_cols = width_src;
	}
	else
	{
		bits_src -= x * 4;
		num_cols = width_src + x;
		if( num_cols > width_dst ) num_cols = width_dst;
	}
	if( y > 0 )
	{
		bits_dst += y * stride_dst;
		num_rows = height_dst - y;
		if( num_rows > height_src ) num_rows = height_src;
	}
	else
	{
		bits_src -= y * stride_src;
		num_rows = height_src + y;
		if( num_rows > height_dst ) num_rows = height_dst;
	}
	while( num_rows > 0 )
	{
		register RDU8 *cur_src = bits_src;
		register RDU8 *cur_dst = bits_dst;
		register RDU8 *end_src = bits_src + (num_cols<<2);
		while( cur_src < end_src )
		{
			*(RDU32 *)cur_dst = *(RDU32 *)cur_src;
			cur_src += 4;
			cur_dst += 4;
		}
		bits_src += stride_src;
		bits_dst += stride_dst;
		num_rows--;
	}
	delete bmp;
}

extern "C" JNIEXPORT void JNICALL FUNC_NAME(BMP_drawRect)(JNIEnv* env, jobject thiz, jlong bitmap, jint color, jint x, jint y, jint width, jint height, jint mode)
{
	if (bitmap == 0) return;
	((CPDFBmp *)bitmap)->draw_rect(color, x, y, width, height, mode);
}

extern "C" JNIEXPORT void JNICALL FUNC_NAME(BMP_invert)(JNIEnv* env, jobject thiz, jlong bitmap)
{
	if( bitmap == 0 ) return;
	((CPDFBmp *)bitmap)->invert();
}

void drawPage1( CRDBmp32 *dst, CRDBmp32 *cur_page, CRDBmp32 *next_page, const RDPOINTF &point );
void drawPage2( CRDBmp32 *dst, CRDBmp32 *cur_page, CRDBmp32 *next_page, const RDPOINTF &point );
extern "C" JNIEXPORT void JNICALL FUNC_NAME(Global_drawScroll)(JNIEnv* env, jobject thiz, jobject bitmap, jlong dib1, jlong dib2, jint x, jint y, jint style)
{
	if( bitmap == NULL || dib1 == 0 || dib2 == 0 ) return;
	CPDFBmp tmp(env, bitmap);
	if (!tmp.valid()) return;
	CRDBmp32Ref src1(((PDF_CACHE *)dib1)->dat, ((PDF_CACHE *)dib1)->w, ((PDF_CACHE *)dib1)->h, ((PDF_CACHE *)dib1)->w * 4);
	CRDBmp32Ref src2(((PDF_CACHE *)dib2)->dat, ((PDF_CACHE *)dib2)->w, ((PDF_CACHE *)dib2)->h, ((PDF_CACHE *)dib2)->w * 4);
	RDPOINTF pt;
	pt.x = x;
	pt.y = y;
	if (tmp.is_rgba32())
	{
		CRDBmp32Ref dst((RDU8 *)tmp.get_dat(), tmp.get_w(), tmp.get_h(), tmp.get_stride());
		if (style == 1 || style == -1)
			drawPage1(&dst, &src1, &src2, pt);
		else
			drawPage2(&dst, &src1, &src2, pt);
	}
	else
	{
		CRDBmp32 dst(tmp.get_w(), tmp.get_h(), tmp.get_stride());
		if (style == 1 || style == -1)
			drawPage1(&dst, &src1, &src2, pt);
		else
			drawPage2(&dst, &src1, &src2, pt);
		tmp.draw(0, 0, &dst);
	}
	if(style < 0)
		tmp.invert();
}

extern "C" JNIEXPORT void FUNC_NAME(Global_setAnnotTransparency)(JNIEnv* env, jobject thiz, jint color)
{
	*(RDU32 *)&CPDFAnnot::ms_color_act = color;
}

extern "C" JNIEXPORT jlong JNICALL FUNC_NAME(Matrix_create)(JNIEnv* env, jobject thiz, jfloat xx, jfloat yx, jfloat xy, jfloat yy, jfloat x0, jfloat y0)
{
	RDMATRIX *mat = new RDMATRIX;
	mat->xx = xx;
	mat->yx = yx;
	mat->xy = xy;
	mat->yy = yy;
	mat->x0 = x0;
	mat->y0 = y0;
	return (jlong)mat;
}

extern "C" JNIEXPORT jlong JNICALL FUNC_NAME(Matrix_createScale)(JNIEnv* env, jobject thiz, jfloat scalex, jfloat scaley, jfloat x0, jfloat y0)
{
	RDMATRIX *mat = new RDMATRIX;
	mat->xx = scalex;
	mat->yx = 0;
	mat->xy = 0;
	mat->yy = scaley;
	mat->x0 = x0;
	mat->y0 = y0;
	return (jlong)mat;
}

extern "C" JNIEXPORT void JNICALL FUNC_NAME(Matrix_invert)(JNIEnv* env, jobject thiz, jlong matrix)
{
	RDMATRIX *mat = (RDMATRIX *)matrix;
	if( mat ) mat->do_invert();
}

extern "C" JNIEXPORT void JNICALL FUNC_NAME(Matrix_transformPath)(JNIEnv* env, jobject thiz, jlong matrix, jlong path)
{
	RDMATRIX *mat = (RDMATRIX *)matrix;
	CRDPath *path1 = (CRDPath *)path;
	if( mat && path1 ) path1->path_transform( *mat );
}

extern "C" JNIEXPORT void JNICALL FUNC_NAME(Matrix_transformInk)(JNIEnv* env, jobject thiz, jlong matrix, jlong ink)
{
	RDMATRIX *mat = (RDMATRIX *)matrix;
	CRDInk *ink1 = (CRDInk *)ink;
	if( mat && ink1 ) ink1->Transform( *mat );
}

extern "C" JNIEXPORT void JNICALL FUNC_NAME(Matrix_transformRect)(JNIEnv* env, jobject thiz, jlong matrix, jfloatArray rect)
{
	RDMATRIX *mat = (RDMATRIX *)matrix;
	if( mat && rect )
	{
		jfloat *arr = (jfloat *)env->GetFloatArrayElements(rect, JNI_FALSE);
		RDRECTF bound;
		bound.left = arr[0];
		bound.top = arr[1];
		bound.right = arr[2];
		bound.bottom = arr[3];
		mat->get_bound( bound );
		arr[0] = bound.left.to_f32();
		arr[1] = bound.top.to_f32();
		arr[2] = bound.right.to_f32();
		arr[3] = bound.bottom.to_f32();
		env->ReleaseFloatArrayElements(rect, arr, 0);
	}
}

extern "C" JNIEXPORT void JNICALL FUNC_NAME(Matrix_transformPoint)(JNIEnv* env, jobject thiz, jlong matrix, jfloatArray point)
{
	RDMATRIX *mat = (RDMATRIX *)matrix;
	if( mat && point )
	{
		jfloat *arr = (jfloat *)env->GetFloatArrayElements(point, JNI_FALSE);
		RDPOINTF pt;
		pt.x = arr[0];
		pt.y = arr[1];
		mat->transform_point( pt );
		arr[0] = pt.x.to_f32();
		arr[1] = pt.y.to_f32();
		env->ReleaseFloatArrayElements(point, arr, 0);
	}
}

extern "C" JNIEXPORT void JNICALL FUNC_NAME(Matrix_destroy)(JNIEnv* env, jobject thiz, jlong matrix)
{
	RDMATRIX *mat = (RDMATRIX *)matrix;
	delete mat;
}


struct PDF_DOC_INNER
{
	CPDFDoc doc;
	CRDStream *str;
	jbyteArray barr;
	RDU8 *data;
	CPDFSecHandStd *sec;
	RDBOOL editable;
	CPDFFontDelegateJNI fdel;
};
/*
public interface PDFStream
{
	public boolean writeable();
	public int get_size();
	public int read( byte[] data );
	public int write( byte[] data );
	public void seek( int pos );
	public int tell();
}
boolean        Z  
byte           B  
char           C  
short          S  
int            I  
long           J  
float          F  
double         D  
void           V  
*/

class CRDStreamJNI:public CRDStream
{
public:
	CRDStreamJNI( JNIEnv* env, jobject str ):CRDStream()
	{
		env->GetJavaVM( &m_vm );
		m_str = env->NewGlobalRef( str );
	}
	RDBOOL writeable()
	{
		JNIEnv *env;
		m_vm->GetEnv( (void **)&env, JNI_VERSION_1_2 );
		jclass cls = env->GetObjectClass(m_str);
		jmethodID id_writeable = env->GetMethodID( cls, "writeable", "()Z" );
		RDBOOL ret = env->CallBooleanMethod( m_str, id_writeable );
		env->DeleteLocalRef( cls );
		return ret;
	}
	virtual ~CRDStreamJNI()
	{
		Close();
		JNIEnv *env;
		m_vm->GetEnv( (void **)&env, JNI_VERSION_1_2 );
		env->DeleteGlobalRef( m_str );
	}
	virtual RDU64 GetLen() const
	{
		JNIEnv *env;
		m_vm->GetEnv( (void **)&env, JNI_VERSION_1_2 );
		jclass cls = env->GetObjectClass(m_str);
		jmethodID id_get_size = env->GetMethodID( cls, "get_size", "()I" );
		RDI32 ret = env->CallIntMethod( m_str, id_get_size ) - m_start;
		env->DeleteLocalRef( cls );
		return ret;
	}
	virtual RDU64 GetPos() const
	{
		JNIEnv *env;
		m_vm->GetEnv( (void **)&env, JNI_VERSION_1_2 );
		jclass cls = env->GetObjectClass(m_str);
		jmethodID id_tell = env->GetMethodID( cls, "tell", "()I" );
		RDI32 ret =  env->CallIntMethod( m_str, id_tell ) - m_start;
		env->DeleteLocalRef( cls );
		return ret;
	}
	virtual RDBOOL SetPos( STR_SEEK pos, RDI64 off )
	{
		JNIEnv *env;
		m_vm->GetEnv( (void **)&env, JNI_VERSION_1_2 );
		jclass cls = env->GetObjectClass(m_str);
		jmethodID id_seek = env->GetMethodID( cls, "seek", "(I)V" );
		switch( pos )
		{
		case CRDStream::RDSEEK_BEG:
			env->CallVoidMethod( m_str, id_seek, (jint)(off + m_start) );
			break;
		case CRDStream::RDSEEK_CUR:
			env->CallVoidMethod( m_str, id_seek, (jint)(GetPos() + off) );
			break;
		case CRDStream::RDSEEK_END:
			env->CallVoidMethod( m_str, id_seek, (jint)(GetLen() + off) );
			break;
		}
		env->DeleteLocalRef( cls );
		return true;
	}
	virtual RDU32 Read( void *pBuf, RDU32 dwBuf )
	{
		if( dwBuf == 0 ) return 0;
		JNIEnv *env;
		m_vm->GetEnv( (void **)&env, JNI_VERSION_1_2 );
		jclass cls = env->GetObjectClass(m_str);
		jmethodID id_read = env->GetMethodID( cls, "read", "([B)I" );
		jbyteArray barr = env->NewByteArray( dwBuf );
		jint ret = env->CallIntMethod( m_str, id_read, barr );
		env->GetByteArrayRegion( barr, 0, ret, (jbyte *)pBuf );
		env->DeleteLocalRef( barr );
		env->DeleteLocalRef( cls );
		return ret;
	}
	virtual RDU32 Write( const void *pBuf, RDU32 dwBuf )
	{
		if( dwBuf == 0 ) return 0;
		JNIEnv *env;
		m_vm->GetEnv( (void **)&env, JNI_VERSION_1_2 );
		jclass cls = env->GetObjectClass(m_str);
		jmethodID id_write = env->GetMethodID( cls, "write", "([B)I" );
		jbyteArray barr = env->NewByteArray( dwBuf );
		env->SetByteArrayRegion( barr, 0, dwBuf, (const jbyte *)pBuf );
		jint ret = env->CallIntMethod( m_str, id_write, barr );
		env->DeleteLocalRef( barr );
		env->DeleteLocalRef( cls );
		return ret;
	}
	virtual void Close(){m_start = 0;}
	virtual void Flush(){}
protected:
	JavaVM* m_vm;
	jobject m_str;
};

extern "C" JNIEXPORT jlong JNICALL FUNC_NAME(Document_openStream)(JNIEnv* env, jobject thiz, jobject stream, jstring password)
{
	if( !stream ) return -10;
	PDF_String psw;
	cvt_js_to_cs( env, password, psw );
	PDF_DOC_INNER *doc = new PDF_DOC_INNER();
	if( !doc ) return 0;

	doc->barr = NULL;
	doc->data = NULL;
	doc->str = new CRDStreamJNI( env, stream );
	doc->editable = ((CRDStreamJNI *)doc->str)->writeable();

	doc->sec = new CPDFSecHandStd( &psw );
	RDI32 ret = doc->doc.DocOpen( doc->str, doc->sec );
	if( ERR_OK != ret )
	{
		if( ret == ERR_ENCRYPT )
		{
			const char *filter = doc->doc.DocGetRequiredDecryptor();
			if( filter && ansi_cmp( filter, "Standard" ) == 0 ) ret = -1;
			else ret = -2;
		}
		else ret = -3;
		delete doc->str;
		delete doc;
		psw.free();
		return ret;
	}
	psw.free();
	if( ERR_OK != doc->doc.DocLoadPageTree( &g_env ) )
	{
		delete doc->str;
		delete doc;
		return -3;
	}
	return (jlong)doc;
}

extern "C" JNIEXPORT jlong JNICALL FUNC_NAME(Document_openMem)(JNIEnv* env, jobject thiz, jbyteArray data, jstring password)
{
	if( !data ) return -10;
	PDF_String psw;
	cvt_js_to_cs( env, password, psw );
	PDF_DOC_INNER *doc = new PDF_DOC_INNER();
	if( !doc ) return 0;

	doc->barr = (jbyteArray)env->NewGlobalRef( data );
	doc->data = (RDU8 *)env->GetByteArrayElements( doc->barr, NULL );
	doc->str = new CRDStreamMemFix( doc->data, env->GetArrayLength(doc->barr) );
	doc->editable = false;

	doc->sec = new CPDFSecHandStd( &psw );
	RDI32 ret = doc->doc.DocOpen( doc->str, doc->sec );
	if( ERR_OK != ret )
	{
		if( ret == ERR_ENCRYPT )
		{
			const char *filter = doc->doc.DocGetRequiredDecryptor();
			if( filter && ansi_cmp( filter, "Standard" ) == 0 ) ret = -1;
			else ret = -2;
		}
		else ret = -3;
		delete doc->str;
		env->ReleaseByteArrayElements( doc->barr, (jbyte *)doc->data, 0 );
		env->DeleteGlobalRef( doc->barr );
		delete doc;
		psw.free();
		return ret;
	}
	psw.free();
	if( ERR_OK != doc->doc.DocLoadPageTree( &g_env ) )
	{
		delete doc->str;
		env->ReleaseByteArrayElements( doc->barr, (jbyte *)doc->data, 0 );
		env->DeleteGlobalRef( doc->barr );
		delete doc;
		return -3;
	}
	return (jlong)doc;
}

extern "C" JNIEXPORT jlong JNICALL FUNC_NAME(Document_open)(JNIEnv* env, jobject thiz, jstring path, jstring password)
{
	if( !path ) return -10;
	PDF_String cpath;
	PDF_String psw;
	cvt_js_to_cs( env, path, cpath );
	cvt_js_to_cs( env, password, psw );

	PDF_DOC_INNER *doc = new PDF_DOC_INNER();
	doc->data = NULL;
	doc->barr = NULL;
	if( !doc ) return 0;
	CRDStreamFile *str_file = new CRDStreamFile();
	doc->editable = true;
	doc->str = str_file;
	if( str_file->RDFOpen( cpath.m_val, RDFILE_READ|RDFILE_WRITE ) != RDFILE_ERR_OK )
	{
		doc->editable = false;
		if( str_file->RDFOpen( cpath.m_val, RDFILE_READ ) != RDFILE_ERR_OK )
		{
			delete doc;
			cpath.free();
			psw.free();
			return -10;
		}
	}
	doc->sec = new CPDFSecHandStd( &psw );
	RDI32 ret = doc->doc.DocOpen( str_file, doc->sec );
	if( ERR_OK != ret )
	{
		if( ret == ERR_ENCRYPT )
		{
			const char *filter = doc->doc.DocGetRequiredDecryptor();
			if( filter && ansi_cmp( filter, "Standard" ) == 0 ) ret = -1;
			else ret = -2;
		}
		else ret = -3;
		delete doc;
		cpath.free();
		psw.free();
		return ret;
	}
	cpath.free();
	psw.free();
	if( ERR_OK != doc->doc.DocLoadPageTree( &g_env ) )
	{
		delete doc;


		return -3;
	}
	return (jlong)doc;
}


extern "C" JNIEXPORT jlong JNICALL FUNC_NAME(Document_createForStream)(JNIEnv* env, jobject thiz, jobject stream)
{
	if( !stream || g_active_mode < 3 ) return -10;
	PDF_DOC_INNER *doc = new PDF_DOC_INNER();
	if( !doc ) return 0;

	doc->barr = NULL;
	doc->data = NULL;
	doc->str = new CRDStreamJNI( env, stream );
	doc->editable = ((CRDStreamJNI *)doc->str)->writeable();
	if( !doc->editable )
	{
		delete doc->str;
		delete doc;
		return -10;
	}

	doc->sec = new CPDFSecHandStd( NULL );
	RDI32 ret = doc->doc.DocCreate( doc->str );
	if( ERR_OK != ret )
	{
		if( ret == ERR_ENCRYPT )
		{
			const char *filter = doc->doc.DocGetRequiredDecryptor();
			if( filter && ansi_cmp( filter, "Standard" ) == 0 ) ret = -1;
			else ret = -2;
		}
		else ret = -3;
		delete doc->str;
		delete doc;
		return ret;
	}
	if( ERR_OK != doc->doc.DocLoadPageTree( &g_env ) )
	{
		delete doc->str;
		delete doc;
		return -3;
	}
	return (jlong)doc;
}

extern "C" JNIEXPORT jlong JNICALL FUNC_NAME(Document_create)(JNIEnv* env, jobject thiz, jstring path)
{
	if( !path || g_active_mode < 3 ) return -10;
	PDF_String cpath;
	cvt_js_to_cs( env, path, cpath );

	PDF_DOC_INNER *doc = new PDF_DOC_INNER();
	doc->data = NULL;
	doc->barr = NULL;
	if( !doc ) return 0;
	CRDStreamFile *str_file = new CRDStreamFile();
	doc->editable = true;
	doc->str = str_file;
	if( str_file->RDFOpen( cpath.m_val ) != RDFILE_ERR_OK )
	{
		delete doc;
		cpath.free();
		return -10;
	}
	doc->sec = new CPDFSecHandStd( NULL );
	RDI32 ret = doc->doc.DocCreate( str_file );
	if( ERR_OK != ret )
	{
		if( ret == ERR_ENCRYPT )
		{
			const char *filter = doc->doc.DocGetRequiredDecryptor();
			if( filter && ansi_cmp( filter, "Standard" ) == 0 ) ret = -1;
			else ret = -2;
		}
		else ret = -3;
		delete doc;
		cpath.free();
		return ret;
	}
	cpath.free();
	if( ERR_OK != doc->doc.DocLoadPageTree( &g_env ) )
	{
		delete doc;
		return -3;
	}
	return (jlong)doc;
}

extern "C" JNIEXPORT jboolean JNICALL FUNC_NAME(Document_setCache)(JNIEnv* env, jobject thiz, jlong doc, jstring path)
{
	if( !doc || g_active_mode < 2 ) return JNI_FALSE;
	PDF_DOC_INNER *idoc = (PDF_DOC_INNER *)doc;
	return idoc->doc.DocCreateCache( env->GetStringUTFChars(path, NULL) );
}


extern "C" JNIEXPORT void JNICALL FUNC_NAME(Document_setFontDel)(JNIEnv* env, jobject thiz, jlong doc, jobject del)
{
	if( !doc || g_active_mode < 2 ) return;
	PDF_DOC_INNER *idoc = (PDF_DOC_INNER *)doc;
	if( !del ) 
		idoc->doc.DocSetFontDel( NULL );
	else
	{
		idoc->fdel.Init( env, del );
		idoc->doc.DocSetFontDel( &idoc->fdel );
	}
}

extern "C" JNIEXPORT jint JNICALL FUNC_NAME(Document_getPermission)(JNIEnv* env, jobject thiz, jlong doc)
{
	if( !doc || g_active_mode < 2 ) return JNI_FALSE;
	PDF_DOC_INNER *idoc = (PDF_DOC_INNER *)doc;
	return idoc->sec->get_permission();
}

extern "C" JNIEXPORT jint JNICALL FUNC_NAME(Document_getPerm)(JNIEnv* env, jobject thiz, jlong doc)
{
	if( !doc || g_active_mode < 2 ) return JNI_FALSE;
	PDF_DOC_INNER *idoc = (PDF_DOC_INNER *)doc;
	return idoc->doc.DocGetPerm();
}

extern "C" JNIEXPORT jstring JNICALL FUNC_NAME(Document_exportForm)( JNIEnv* env, jobject thiz, jlong doc )
{
	if( !doc || g_active_mode < 3 ) return NULL;
	PDF_DOC_INNER *idoc = (PDF_DOC_INNER *)doc;
	char *tmp = idoc->doc.DocExportForm();
	if( !tmp ) return NULL;
	jstring ret = env->NewStringUTF( tmp );
	RDFree( tmp );
	return ret;
}

extern "C" JNIEXPORT jboolean JNICALL FUNC_NAME(Document_canSave)( JNIEnv* env, jobject thiz, jlong doc )
{
	if( !doc || g_active_mode < 2 ) return JNI_FALSE;
	PDF_DOC_INNER *idoc = (PDF_DOC_INNER *)doc;
	return idoc->editable;
}

extern "C" JNIEXPORT jstring JNICALL FUNC_NAME(Document_getOutlineTitle)(JNIEnv* env, jobject thiz, jlong doc, jlong outlinenode)
{
	if( !doc || !outlinenode ) return NULL;
	wchar_t title0[512];
	char title[512];
	CPDFOutlineItem* node = (CPDFOutlineItem *)outlinenode;
	node->get_title( title0, 511 );
	cvt_utol( title0, title, 510 );
	return env->NewStringUTF(title);
}

extern "C" JNIEXPORT jboolean JNICALL FUNC_NAME(Document_setOutlineTitle)(JNIEnv* env, jobject thiz, jlong doc, jlong outlinenode, jstring title)
{
	if( !doc || !outlinenode || g_active_mode < 3 ) return JNI_FALSE;
	wchar_t title0[512];
	PDF_String label;
	label.init();
	cvt_js_to_cs( env, title, label );
	PDF_DOC_INNER *idoc = (PDF_DOC_INNER*)doc;
	CPDFOutlineItem* node = (CPDFOutlineItem *)outlinenode;
	cvt_text_to_ucs( &label, title0, 511 );
	return idoc->doc.DocSetOutlineTitle( node, title0 );
}

extern "C" JNIEXPORT jint JNICALL FUNC_NAME(Document_getOutlineDest)(JNIEnv* env, jobject thiz, jlong doc, jlong outlinenode)
{
	if( !doc || !outlinenode ) return -1;
	PDF_DOC_INNER *idoc = (PDF_DOC_INNER*)doc;
	CPDFOutlineItem* node = (CPDFOutlineItem *)outlinenode;
	RDFIX y;
	return idoc->doc.DocGetOutlineDest( node, y );
}

extern "C" JNIEXPORT jstring JNICALL FUNC_NAME(Document_getOutlineURI)(JNIEnv* env, jobject thiz, jlong doc, jlong outlinenode)
{
	if( !doc || !outlinenode ) return NULL;
	PDF_DOC_INNER *idoc = (PDF_DOC_INNER*)doc;
	CPDFOutlineItem* node = (CPDFOutlineItem *)outlinenode;
	wchar_t wtxt[512];
	if( idoc->doc.DocGetOutlineUri( node, wtxt, 511 ) )
	{
		char txt[1024];
		cvt_utol( wtxt, txt, 1023 );
		return env->NewStringUTF( txt );
	}
	else
		return NULL;
}

extern "C" JNIEXPORT jstring JNICALL FUNC_NAME(Document_getOutlineFileLink)(JNIEnv* env, jobject thiz, jlong doc, jlong outlinenode)
{
	if( !doc || !outlinenode ) return NULL;
	PDF_DOC_INNER *idoc = (PDF_DOC_INNER*)doc;
	CPDFOutlineItem* node = (CPDFOutlineItem *)outlinenode;
	wchar_t wtxt[512];
	if( idoc->doc.DocGetOutlineFileLink( node, wtxt, 511 ) )
	{
		char txt[1024];
		cvt_utol( wtxt, txt, 1023 );
		return env->NewStringUTF( txt );
	}
	else
		return NULL;
}

extern "C" JNIEXPORT jlong JNICALL FUNC_NAME(Document_getOutlineChild)(JNIEnv* env, jobject thiz, jlong doc, jlong outlinenode)
{
	if( !doc ) return 0;
	if( outlinenode )
	{
		CPDFOutlineItem *node = (CPDFOutlineItem *)outlinenode;
		return (jlong)node->get_child();
	}
	else
	{



		PDF_DOC_INNER *idoc = (PDF_DOC_INNER *)doc;
		CPDFOutlineItem* result = idoc->doc.DocGetRootOutline();
		return (jlong)result;
	}
}

extern "C" JNIEXPORT jlong JNICALL FUNC_NAME(Document_getOutlineNext)(JNIEnv* env, jobject thiz, jlong doc, jlong outlinenode)
{
	if( !doc ) return 0;
	if( outlinenode )
	{
		CPDFOutlineItem *node = (CPDFOutlineItem *)outlinenode;
		return (jlong)node->get_next();
	}
	else
	{
		PDF_DOC_INNER *idoc = (PDF_DOC_INNER *)doc;
		CPDFOutlineItem* result = idoc->doc.DocGetRootOutline();
		return (jlong)result;
	}
}

extern "C" JNIEXPORT jboolean JNICALL FUNC_NAME(Document_addOutlineNext)(JNIEnv* env, jobject thiz, jlong doc, jlong outlinenode, jstring label, jint pageno, jfloat top)
{
	if( !doc || g_active_mode < 3 ) return JNI_FALSE;
	PDF_DOC_INNER *idoc = (PDF_DOC_INNER *)doc;
	if( !idoc->editable ) return JNI_FALSE;
	wchar_t txt[256];
	cvt_ltou( env->GetStringUTFChars(label, NULL), txt, 255 );
	if( !outlinenode )
		return idoc->doc.DocNewRootOutline( txt, pageno, top );
	else
		return idoc->doc.DocAddNextOutline( (CPDFOutlineItem *)outlinenode, txt, pageno, top );
}

extern "C" JNIEXPORT jboolean JNICALL FUNC_NAME(Document_addOutlineChild)(JNIEnv* env, jobject thiz, jlong doc, jlong outlinenode, jstring label, jint pageno, jfloat top)
{
	if( !doc || g_active_mode < 3 ) return JNI_FALSE;
	PDF_DOC_INNER *idoc = (PDF_DOC_INNER *)doc;
	if( !idoc->editable ) return JNI_FALSE;
	wchar_t txt[256];
	cvt_ltou( env->GetStringUTFChars(label, NULL), txt, 255 );
	if( !outlinenode )
		return idoc->doc.DocNewRootOutline( txt, pageno, top );
	else
		return idoc->doc.DocAddFirstOutline( (CPDFOutlineItem *)outlinenode, txt, pageno, top );
}

extern "C" JNIEXPORT jboolean JNICALL FUNC_NAME(Document_removeOutline)(JNIEnv* env, jobject thiz, jlong doc, jlong outlinenode)
{
	if( !doc || g_active_mode < 3 ) return JNI_FALSE;
	PDF_DOC_INNER *idoc = (PDF_DOC_INNER *)doc;
	if( !idoc->editable ) return JNI_FALSE;
	return idoc->doc.DocRemoveOutline( (CPDFOutlineItem *)outlinenode );
}

extern "C" JNIEXPORT jboolean JNICALL FUNC_NAME(Document_setMeta)( JNIEnv* env, jobject thiz, jlong doc, jstring tag, jstring val )
{
	if( !doc || g_active_mode < 3 ) return JNI_FALSE;
	PDF_DOC_INNER *idoc = (PDF_DOC_INNER *)doc;
	if( !idoc->editable ) return JNI_FALSE;
	PDF_String ctag;
	PDF_String cval;
	cvt_js_to_cs( env, tag, ctag );
	cvt_js_to_cs( env, val, cval );
	wchar_t wsVal[1024];
	cvt_ltou( cval.m_val, wsVal, 1020 );
	RDBOOL ret = idoc->doc.DocSetMetaData( ctag.m_val, wsVal );
	ctag.free();
	cval.free();
	return ret;
}

extern "C" JNIEXPORT jstring JNICALL FUNC_NAME(Document_getMeta)( JNIEnv* env, jobject thiz, jlong doc, jstring tag )
{
	if( !doc ) return NULL;
	PDF_DOC_INNER *idoc = (PDF_DOC_INNER *)doc;
	PDF_String ctag;
	cvt_js_to_cs( env, tag, ctag );
	wchar_t wsVal[512];
	char sVal[512];
	if( ansi_cmp( ctag.m_val, "ver" ) )
	{
		idoc->doc.DocGetMetaData( ctag.m_val, wsVal, 500 );
		cvt_utol( wsVal, sVal, 500 );
	}
	else
		idoc->doc.DocGetVer( sVal );
	ctag.free();
	return env->NewStringUTF(sVal);
}

extern "C" JNIEXPORT jbyteArray JNICALL FUNC_NAME(Document_getID)(JNIEnv* env, jobject thiz, jlong doc, jint index)
{
	if (!doc) return NULL;
	PDF_DOC_INNER *idoc = (PDF_DOC_INNER *)doc;
	if (index < 0 || index > 1) return NULL;
	const PDF_String *str = idoc->doc.DocGetID(index);
	if (!str || str->m_len != 16) return NULL;
	jbyteArray arr = env->NewByteArray(16);
	jbyte *data = env->GetByteArrayElements(arr, NULL);
	mem_cpy(data, str->m_val, 16);
	env->ReleaseByteArrayElements(arr, data, 0);
	return arr;
}

extern "C" JNIEXPORT jfloat JNICALL FUNC_NAME(Document_getPageWidth)( JNIEnv* env, jobject thiz, jlong doc, jint pageno )
{
	if( !doc ) return 0;
	PDF_DOC_INNER *idoc = (PDF_DOC_INNER *)doc;
	return (float)idoc->doc.DocGetPageWidth( pageno )/100;
}

extern "C" JNIEXPORT jfloat JNICALL FUNC_NAME(Document_getPageHeight)( JNIEnv* env, jobject thiz, jlong doc, jint pageno )
{
	if( !doc ) return 0;
	PDF_DOC_INNER *idoc = (PDF_DOC_INNER *)doc;
	return (float)idoc->doc.DocGetPageHeight( pageno )/100;
}

extern "C" JNIEXPORT jboolean JNICALL FUNC_NAME(Document_changePageRect)( JNIEnv* env, jobject thiz, jlong doc, jint pageno, jfloat dl, jfloat dt, jfloat dr, jfloat db )
{
	if( !doc || g_active_mode < 3 ) return JNI_FALSE;
	PDF_DOC_INNER *idoc = (PDF_DOC_INNER *)doc;
	return idoc->doc.DocModifyPageRect( pageno, dl, dt, dr, db );
}

extern "C" JNIEXPORT jboolean JNICALL FUNC_NAME(Document_setPageRotate)( JNIEnv* env, jobject thiz, jlong doc, jint pageno, jint degree )
{
	if( !doc || g_active_mode < 3 ) return JNI_FALSE;
	PDF_DOC_INNER *idoc = (PDF_DOC_INNER *)doc;
	return idoc->doc.DocSetPageRotate( pageno, degree );
}

extern "C" JNIEXPORT jint JNICALL FUNC_NAME(Document_getPageCount)( JNIEnv* env, jobject thiz, jlong doc )
{
	if( !doc ) return 0;
	PDF_DOC_INNER *idoc = (PDF_DOC_INNER *)doc;
	return (jint)idoc->doc.DocGetPageCount();
}

extern "C" JNIEXPORT jlong JNICALL FUNC_NAME(Document_newFontCID)(JNIEnv* env, jobject thiz, jlong doc, jstring name, jint style)
{
	if( !doc || !name ) return 0;
	PDF_DOC_INNER *idoc = (PDF_DOC_INNER *)doc;
	if( !idoc->editable ) return 0;
	return (jlong)idoc->doc.DocNewFontCID(env->GetStringUTFChars(name, NULL), style);
}

extern "C" JNIEXPORT jfloat JNICALL FUNC_NAME(Document_getFontAscent)(JNIEnv* env, jobject thiz, jlong doc, jlong font)
{
	if( !doc || !font ) return 0;
	PDF_DOC_INNER *idoc = (PDF_DOC_INNER *)doc;
	return idoc->doc.DocGetFontAscent( (PDF_FONT)font ).to_f32();
}

extern "C" JNIEXPORT jfloat JNICALL FUNC_NAME(Document_getFontDescent)(JNIEnv* env, jobject thiz, jlong doc, jlong font)
{
	if( !doc || !font ) return 0;
	PDF_DOC_INNER *idoc = (PDF_DOC_INNER *)doc;
	return idoc->doc.DocGetFontDescent( (PDF_FONT)font ).to_f32();
}

extern "C" JNIEXPORT jlong JNICALL FUNC_NAME(Document_newGState)(JNIEnv* env, jobject thiz, jlong doc)
{
	if( !doc ) return 0;
	PDF_DOC_INNER *idoc = (PDF_DOC_INNER *)doc;
	if( !idoc->editable ) return 0;
	return idoc->doc.DocNewGState();
}

extern "C" JNIEXPORT jboolean JNICALL FUNC_NAME(Document_setGStateStrokeAlpha)(JNIEnv* env, jobject thiz, jlong doc, jlong state, jint alpha)
{
	if( !doc || !state ) return JNI_FALSE;
	PDF_DOC_INNER *idoc = (PDF_DOC_INNER *)doc;
	idoc->doc.DocSetGStateStrokeAlpha(state, alpha);
	return JNI_TRUE;
}

extern "C" JNIEXPORT jboolean JNICALL FUNC_NAME(Document_setGStateFillAlpha)(JNIEnv* env, jobject thiz, jlong doc, jlong state, jint alpha)
{
	if( !doc || !state ) return JNI_FALSE;
	PDF_DOC_INNER *idoc = (PDF_DOC_INNER *)doc;
	idoc->doc.DocSetGStateFillAlpha(state, alpha);
	return JNI_TRUE;
}


typedef struct _PDF_PAGE_INNER
{
	PDF_DOC_INNER *idoc;
	PDF_PAGE page;
	RDI32 index;
	CPDFGRenderText text;
	CPDFGRenderReflow reflow;
	RDBOOL load_content;
	RDBOOL objs_get;
	CPDFPageSession session;
}PDF_PAGE_INNER;

extern "C" JNIEXPORT jlong JNICALL FUNC_NAME(Document_newPage)(JNIEnv* env, jobject thiz, jlong doc, jint pageno, jfloat w, jfloat h)
{
	if( !doc ) return 0;
	PDF_DOC_INNER *idoc = (PDF_DOC_INNER *)doc;
	if( !idoc->editable ) return 0;
	PDF_PAGE pg = idoc->doc.DocNewPage( pageno, w, h );
	PDF_PAGE_INNER *page = new PDF_PAGE_INNER;
	if( page )
	{
		page->idoc = idoc;
		page->page = pg;
		page->index = pageno;
		page->objs_get = false;
		page->load_content = false;
	}
	return (jlong)page;
}

extern "C" JNIEXPORT jlong JNICALL FUNC_NAME(Document_importStart)(JNIEnv* env, jobject thiz, jlong doc, jlong doc_src)
{
	if( !doc || !doc_src ) return 0;
	PDF_DOC_INNER *idoc = (PDF_DOC_INNER *)doc;
	PDF_DOC_INNER *idoc_src = (PDF_DOC_INNER *)doc_src;
	if( !idoc->editable || g_active_mode < 3 ) return 0;
	return (jlong)idoc->doc.DocImportStart(&idoc_src->doc);
}

extern "C" JNIEXPORT jboolean JNICALL FUNC_NAME(Document_importPage)(JNIEnv* env, jobject thiz, jlong doc, jlong ctx, jint srcno, jint dstno)
{
	if( !doc || !ctx ) return JNI_FALSE;
	PDF_DOC_INNER *idoc = (PDF_DOC_INNER *)doc;
	if( !idoc->editable || g_active_mode < 3 ) return JNI_FALSE;
	return idoc->doc.DocImportPage( (CPDFMergeContext *)ctx, srcno, dstno );
}

extern "C" JNIEXPORT void JNICALL FUNC_NAME(Document_importEnd)(JNIEnv* env, jobject thiz, jlong doc, jlong ctx)
{
	if( !doc ) return;
	PDF_DOC_INNER *idoc = (PDF_DOC_INNER *)doc;
	idoc->doc.DocImportEnd( (CPDFMergeContext *)ctx );
}

extern "C" JNIEXPORT jboolean JNICALL FUNC_NAME(Document_movePage)( JNIEnv* env, jobject thiz, jlong doc, jint pageno1, jint pageno2 )
{
	if( !doc ) return JNI_FALSE;
	PDF_DOC_INNER *idoc = (PDF_DOC_INNER *)doc;
	if( !idoc->editable || g_active_mode < 3 ) return 0;
	return idoc->doc.DocMovePage( pageno1, pageno2 );
}

extern "C" JNIEXPORT jboolean JNICALL FUNC_NAME(Document_removePage)( JNIEnv* env, jobject thiz, jlong doc, jint pageno )
{
	if( !doc ) return JNI_FALSE;
	PDF_DOC_INNER *idoc = (PDF_DOC_INNER *)doc;
	if( !idoc->editable || g_active_mode < 3 ) return 0;
	return idoc->doc.DocRemovePage( pageno );
}

extern "C" JNIEXPORT jlong JNICALL FUNC_NAME(Document_newImage)(JNIEnv* env, jobject thiz, jlong doc, jobject bmp, jboolean has_alpha)
{
	if( !doc || !bmp ) return 0;
	PDF_DOC_INNER *idoc = (PDF_DOC_INNER *)doc;
	if( !idoc->editable ) return 0;
	CPDFBmp tmp(env, bmp);
	if (!tmp.valid()) return 0;
	CRDBmp32 *src = tmp.create32();
	PDF_IMAGE img = idoc->doc.DocNewImage( src, has_alpha, false );
	delete src;
	return img;
}

extern "C" JNIEXPORT jlong JNICALL FUNC_NAME(Document_newImageJPX)(JNIEnv* env, jobject thiz, jlong doc, jstring path)
{
	if( !doc || !path ) return 0;
	PDF_DOC_INNER *idoc = (PDF_DOC_INNER *)doc;
	if( !idoc->editable ) return 0;
	return idoc->doc.DocNewImageJPX(env->GetStringUTFChars(path, NULL));
}

extern "C" JNIEXPORT jlong JNICALL FUNC_NAME(Document_newImageJPEG)(JNIEnv* env, jobject thiz, jlong doc, jstring path)
{
	if( !doc || !path ) return 0;
	PDF_DOC_INNER *idoc = (PDF_DOC_INNER *)doc;
	if( !idoc->editable ) return 0;
	return idoc->doc.DocNewImageJPEG(env->GetStringUTFChars(path, NULL));
}

extern "C" JNIEXPORT jboolean JNICALL FUNC_NAME(Document_save)( JNIEnv* env, jobject thiz, jlong doc )
{
	if( !doc || g_active_mode < 2 ) return JNI_FALSE;
	PDF_DOC_INNER *idoc = (PDF_DOC_INNER *)doc;
	if( idoc->editable )
	{
		CPDFEncryptStd *enc = NULL;
		if( idoc->doc.DocIsEncrypted() )
			enc = idoc->sec->CreateEncrypt();
		RDBOOL ret = idoc->doc.DocSave(enc);
		if( enc ) delete enc;
		return ret;
	}
	else return false;
}

extern "C" JNIEXPORT jboolean JNICALL FUNC_NAME(Document_saveAs)( JNIEnv* env, jobject thiz, jlong doc, jstring dst, jboolean rem_sec )
{
	if( !doc || g_active_mode < 2 ) return JNI_FALSE;
	PDF_DOC_INNER *idoc = (PDF_DOC_INNER *)doc;
	PDF_String path;
	cvt_js_to_cs( env, dst, path );
	CRDStreamFile file;
	if( file.RDFOpen( path.m_val ) != RDFILE_ERR_OK )
	{
		path.free();
		return JNI_FALSE;
	}
	if( rem_sec || !idoc->doc.DocIsEncrypted() || !idoc->sec )
		idoc->doc.DocSaveAs( &file );
	else
	{
		CPDFEncrypt *enc = idoc->sec->CreateEncrypt();
		idoc->doc.DocSaveAs( &file, enc );
		delete enc;
	}
	file.RDFClose();
	path.free();
	return JNI_TRUE;
}

extern "C" JNIEXPORT jboolean JNICALL FUNC_NAME(Document_encryptAs)(JNIEnv* env, jobject thiz, jlong doc, jstring dst, jstring upswd, jstring opswd, jint perm, jint method, jbyteArray id)
{
	if (!doc || g_active_mode < 3 || !id || env->GetArrayLength(id) != 32) return JNI_FALSE;
	PDF_DOC_INNER *idoc = (PDF_DOC_INNER *)doc;
	PDF_String path;
	cvt_js_to_cs(env, dst, path);
	CRDStreamFile file;
	if (file.RDFOpen(path.m_val) != RDFILE_ERR_OK)
	{
		path.free();
		return JNI_FALSE;
	}
	PDF_String up;
	PDF_String op;
	PDF_String fid0;
	PDF_String fid1;
	jbyte *data = env->GetByteArrayElements(id, NULL);
	fid0.init((const char *)data, 16);
	fid1.init((const char *)data + 16, 16);
	env->ReleaseByteArrayElements(id, data, 0);
	cvt_js_to_cs(env, upswd, up);
	cvt_js_to_cs(env, opswd, op);
	perm &= 0xFFFFFFFC;
	perm |= 0xFFFFF0C0;
	CPDFEncryptStd *enc = new CPDFEncryptStd(up, op, (PDF_CRYPT_METHOD)method, perm, fid0, -1);
	up.free();
	op.free();
	fid0.free();
	fid1.free();
	idoc->doc.DocSetID(fid0, fid1);
	idoc->doc.DocSaveAs(&file, enc);
	delete enc;

	file.RDFClose();
	path.free();
	return JNI_TRUE;
}

extern "C" JNIEXPORT jboolean JNICALL FUNC_NAME(Document_isEncrypted)( JNIEnv* env, jobject thiz, jlong doc )
{
	if( !doc ) return JNI_FALSE;
	PDF_DOC_INNER *idoc = (PDF_DOC_INNER *)doc;
	return idoc->doc.DocIsEncrypted();
}

extern "C" JNIEXPORT jbyteArray JNICALL FUNC_NAME(Document_getSignContents)( JNIEnv* env, jobject thiz, jlong doc )
{
	if( !doc || g_active_mode < 2 ) return NULL;
	PDF_DOC_INNER *idoc = (PDF_DOC_INNER *)doc;
	CPDFSign *sign = idoc->doc.DocGetSign();
	if( !sign ) return NULL;
	const PDF_String *contents = sign->get_sign_data();
	jbyteArray arr = env->NewByteArray(contents->m_len);
	jbyte *data = env->GetByteArrayElements( arr, NULL );
	mem_cpy( data, contents->m_val, contents->m_len );
	env->ReleaseByteArrayElements( arr, data, 0 );
	return arr;
}

extern "C" JNIEXPORT jstring JNICALL FUNC_NAME(Document_getSignFilter)( JNIEnv* env, jobject thiz, jlong doc )
{
	if( !doc || g_active_mode < 2 ) return NULL;
	PDF_DOC_INNER *idoc = (PDF_DOC_INNER *)doc;
	CPDFSign *sign = idoc->doc.DocGetSign();
	if( !sign ) return NULL;
	return env->NewStringUTF(sign->get_filter());
}

extern "C" JNIEXPORT jstring JNICALL FUNC_NAME(Document_getSignSubFilter)( JNIEnv* env, jobject thiz, jlong doc )
{
	if( !doc || g_active_mode < 2 ) return NULL;
	PDF_DOC_INNER *idoc = (PDF_DOC_INNER *)doc;
	CPDFSign *sign = idoc->doc.DocGetSign();
	if( !sign ) return NULL;
	return env->NewStringUTF(sign->get_sub_filter());
}

extern "C" JNIEXPORT jintArray JNICALL FUNC_NAME(Document_getSignByteRange)( JNIEnv* env, jobject thiz, jlong doc )
{
	if( !doc || g_active_mode < 2 ) return NULL;
	PDF_DOC_INNER *idoc = (PDF_DOC_INNER *)doc;
	CPDFSign *sign = idoc->doc.DocGetSign();
	if( !sign ) return NULL;
	const PDF_STREAM_REC *ranges = sign->get_ranges();
	RDI32 ranges_cnt = sign->get_ranges_cnt();
	const PDF_STREAM_REC *ranges_end = ranges + ranges_cnt;
	jintArray arr = env->NewIntArray(ranges_cnt * 2);
	jint *data = env->GetIntArrayElements( arr, NULL );
	jint *cur = data;
	while( ranges < ranges_end )
	{
		cur[0] = ranges->offset;
		cur[1] = ranges->len;
		cur += 2;
		ranges++;
	}
	env->ReleaseIntArrayElements( arr, data, 0 );
	return arr;
}

extern "C" JNIEXPORT jint JNICALL FUNC_NAME(Document_checkSignByteRange)( JNIEnv* env, jobject thiz, jlong doc )
{
	if( !doc || g_active_mode < 2 ) return -1;
	PDF_DOC_INNER *idoc = (PDF_DOC_INNER *)doc;
	CPDFSign *sign = idoc->doc.DocGetSign();
	return idoc->doc.DocCheckSign( sign );
}

extern "C" JNIEXPORT void JNICALL FUNC_NAME(Document_close)( JNIEnv* env, jobject thiz, jlong doc )
{
	if( !doc ) return;
	PDF_DOC_INNER *idoc = (PDF_DOC_INNER *)doc;
	if( idoc )
	{
		idoc->doc.DocClose();
		delete idoc->str;
		if( idoc->barr )
		{
			env->ReleaseByteArrayElements( idoc->barr, (jbyte *)idoc->data, 0 );
			env->DeleteGlobalRef( idoc->barr );
		}
		delete idoc->sec;
		delete idoc;
	}
}

extern "C" JNIEXPORT jlong JNICALL FUNC_NAME(Document_getPage)(JNIEnv* env, jobject thiz, jlong doc, jint pageno)
{
	if( !doc || pageno < 0 ) return 0;
	PDF_DOC_INNER *idoc = (PDF_DOC_INNER *)doc;
	PDF_PAGE_INNER *page = new PDF_PAGE_INNER;
	if( page )
	{
		page->idoc = idoc;
		page->page = idoc->doc.DocGetPage( pageno );
		page->index = pageno;
		page->objs_get = false;
		page->load_content = false;
	}
	return (jlong)page;
}


extern "C" JNIEXPORT jlong JNICALL FUNC_NAME(PageContent_create)(JNIEnv* env, jobject thiz)
{
	return (jlong)(new CPDFContentStream);
}

extern "C" JNIEXPORT void JNICALL FUNC_NAME(PageContent_gsSave)(JNIEnv* env, jobject thiz, jlong content)
{
	if( !content ) return;
	((CPDFContentStream *)content)->write_gs_save();
}

extern "C" JNIEXPORT void JNICALL FUNC_NAME(PageContent_gsRestore)(JNIEnv* env, jobject thiz, jlong content)
{
	if( !content ) return;
	((CPDFContentStream *)content)->write_gs_restore();
}

extern "C" JNIEXPORT void JNICALL FUNC_NAME(PageContent_gsSet)(JNIEnv* env, jobject thiz, jlong content, jlong gs)
{
	if( !content ) return;
	((CPDFContentStream *)content)->write_gs( (PDFRES_GSTATE *)gs );
}

extern "C" JNIEXPORT void JNICALL FUNC_NAME(PageContent_gsSetMatrix)(JNIEnv* env, jobject thiz, jlong content, jlong mat)
{
	if( !content ) return;
	((CPDFContentStream *)content)->write_gs_mat( *(RDMATRIX *)mat );
}

extern "C" JNIEXPORT void JNICALL FUNC_NAME(PageContent_textBegin)(JNIEnv* env, jobject thiz, jlong content)
{
	if( !content ) return;
	((CPDFContentStream *)content)->write_text_begin();
}

extern "C" JNIEXPORT void JNICALL FUNC_NAME(PageContent_textEnd)(JNIEnv* env, jobject thiz, jlong content)
{
	if( !content ) return;
	((CPDFContentStream *)content)->write_text_end();
}

extern "C" JNIEXPORT void JNICALL FUNC_NAME(PageContent_drawImage)(JNIEnv* env, jobject thiz, jlong content, jlong img)
{
	if( !content || !img ) return;
	((CPDFContentStream *)content)->write_image((PDFRES_IMAGE *)img);
}

extern "C" JNIEXPORT void JNICALL FUNC_NAME(PageContent_drawText)(JNIEnv* env, jobject thiz, jlong content, jstring text)
{
	if( !content || !text ) return;
	const char *utf = env->GetStringUTFChars( text, NULL );
	RDI32 len = ansi_len(utf) + 1;
	wchar_t *wtxt = (wchar_t *)RDAlloc( len * 4 + 4 );

	cvt_ltou( utf, wtxt, len );
	((CPDFContentStream *)content)->write_text( wtxt );

	RDFree( wtxt );
}

extern "C" JNIEXPORT jfloatArray JNICALL FUNC_NAME(PageContent_textGetSize)(JNIEnv* env, jobject thiz, jlong content, jlong font, jstring text, jfloat width, jfloat height, jfloat char_space, jfloat word_space)
{
	if( !content || !text ) return NULL;
	const char *utf = env->GetStringUTFChars( text, NULL );
	RDI32 len = ansi_len(utf);
	wchar_t *wtxt = (wchar_t *)RDAlloc( len * 4 + 4 );

	cvt_ltou( utf, wtxt, len );
	RDSIZEF sz = ((CPDFContentStream *)content)->get_text_size( (PDFRES_FONT *)font, wtxt, width, height, char_space, word_space );
	jfloat tmp[2];
	tmp[0] = sz.cx.to_f32();
	tmp[1] = sz.cy.to_f32();
	jfloatArray arr = env->NewFloatArray(2);
	env->SetFloatArrayRegion( arr, 0, 2, tmp );
	RDFree( wtxt );
	return arr;
}

extern "C" JNIEXPORT void JNICALL FUNC_NAME(PageContent_strokePath)(JNIEnv* env, jobject thiz, jlong content, jlong path)
{
	if( !content || !path ) return;
	((CPDFContentStream *)content)->write_stroke( *(CRDPath *)path );
}

extern "C" JNIEXPORT void JNICALL FUNC_NAME(PageContent_fillPath)(JNIEnv* env, jobject thiz, jlong content, jlong path, jboolean winding)
{
	if( !content || !path ) return;
	((CPDFContentStream *)content)->write_fill( *(CRDPath *)path, winding );
}

extern "C" JNIEXPORT void JNICALL FUNC_NAME(PageContent_clipPath)(JNIEnv* env, jobject thiz, jlong content, jlong path, jboolean winding)
{
	if( !content || !path ) return;
	((CPDFContentStream *)content)->write_clip( *(CRDPath *)path, winding );
}

extern "C" JNIEXPORT void JNICALL FUNC_NAME(PageContent_setFillColor)(JNIEnv* env, jobject thiz, jlong content, jint color)
{
	if( !content ) return;
	((CPDFContentStream *)content)->write_fill_color( *(RDRGBA *)&color );
}

extern "C" JNIEXPORT jfloatArray JNICALL FUNC_NAME(Page_getCropBox)(JNIEnv* env, jobject thiz, jlong page)
{
	if( !page || g_active_mode < 1 ) return NULL;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	RDRECTF rect;
	ipage->idoc->doc.Page_GetCropBox( ipage->page, rect );
	jfloatArray arr = env->NewFloatArray( 4 );
	jfloat *data = env->GetFloatArrayElements( arr, NULL );
	data[0] = rect.left.to_f32();
	data[1] = rect.top.to_f32();
	data[2] = rect.right.to_f32();
	data[3] = rect.bottom.to_f32();
	env->ReleaseFloatArrayElements( arr, data, 0 );
	return arr;
}

extern "C" JNIEXPORT jfloatArray JNICALL FUNC_NAME(Page_getMediaBox)(JNIEnv* env, jobject thiz, jlong page)
{
	if( !page || g_active_mode < 1 ) return NULL;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	RDRECTF rect;
	ipage->idoc->doc.Page_GetMediaBox( ipage->page, rect );
	jfloatArray arr = env->NewFloatArray( 4 );
	jfloat *data = env->GetFloatArrayElements( arr, NULL );
	data[0] = rect.left.to_f32();
	data[1] = rect.top.to_f32();
	data[2] = rect.right.to_f32();
	data[3] = rect.bottom.to_f32();
	env->ReleaseFloatArrayElements( arr, data, 0 );
	return arr;
}

extern "C" JNIEXPORT jboolean JNICALL FUNC_NAME(Page_setAnnotIcon)( JNIEnv* env, jobject thiz, jlong page, jlong annot, jint icon )
{
	if( !page || !annot || g_active_mode < 2 ) return JNI_FALSE;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	if( !ipage->idoc->editable ) return JNI_FALSE;
	CPDFAnnot *obj_annot = (CPDFAnnot *)annot;
	return ipage->idoc->doc.Annot_SetIcon( obj_annot, icon );
}

extern "C" JNIEXPORT jboolean JNICALL FUNC_NAME(Page_setAnnotIcon2)(JNIEnv* env, jobject thiz, jlong page, jlong annot, jstring name, jlong content)
{
	if( !page || !annot || g_active_mode < 2 ) return JNI_FALSE;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	if( !ipage->idoc->editable ) return JNI_FALSE;
	CPDFAnnot *obj_annot = (CPDFAnnot *)annot;
	return ipage->idoc->doc.Annot_SetIcon( obj_annot, env->GetStringUTFChars(name, NULL), (CPDFContentStream *)content );
}

extern "C" JNIEXPORT jint JNICALL FUNC_NAME(Page_getAnnotIcon)( JNIEnv* env, jobject thiz, jlong page, jlong annot )
{
	if( !page || !annot || g_active_mode < 2 ) return -1;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	CPDFAnnot *obj_annot = (CPDFAnnot *)annot;
	return ipage->idoc->doc.Annot_GetIcon( obj_annot );
}

extern "C" JNIEXPORT jfloatArray JNICALL FUNC_NAME(Page_getAnnotMarkupRects)( JNIEnv* env, jobject thiz, jlong page, jlong annot )
{
	if( !page || !annot || g_active_mode < 2 ) return NULL;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	CPDFAnnot *obj_annot = (CPDFAnnot *)annot;
	RDI32 cnt = ipage->idoc->doc.Page_GetAnnotMarkupRectsCnt( ipage->page, obj_annot );
	if( cnt <= 0 ) return NULL;
	RDRECTF *rects = (RDRECTF *)RDAlloc( sizeof( RDRECTF ) * cnt );
	RDRECTF *rects_cur = rects;
	RDRECTF *rects_end = rects + cnt;
	ipage->idoc->doc.Page_GetAnnotMarkupRects( ipage->page, obj_annot, rects, cnt );
	jfloatArray arr = env->NewFloatArray( cnt<<2 );
	jfloat *vals = env->GetFloatArrayElements( arr, NULL );
	jfloat *vals_cur = vals;

	while( rects_cur < rects_end )
	{
		vals_cur[0] = rects_cur->left.to_f32();
		vals_cur[1] = rects_cur->top.to_f32();
		vals_cur[2] = rects_cur->right.to_f32();
		vals_cur[3] = rects_cur->bottom.to_f32();
		rects_cur++;
		vals_cur += 4;
	}

	env->ReleaseFloatArrayElements( arr, vals, 0 );
	RDFree(rects);
	return arr;
}

extern "C" JNIEXPORT void JNICALL FUNC_NAME(PageContent_setStrokeColor)(JNIEnv* env, jobject thiz, jlong content, jint color)
{
	if( !content ) return;
	((CPDFContentStream *)content)->write_stroke_color( *(RDRGBA *)&color );
}

extern "C" JNIEXPORT void JNICALL FUNC_NAME(PageContent_setStrokeCap)(JNIEnv* env, jobject thiz, jlong content, jint cap)
{
	if( !content ) return;
	((CPDFContentStream *)content)->write_stroke_cap( (RDLINE_CAP)cap );
}

extern "C" JNIEXPORT void JNICALL FUNC_NAME(PageContent_setStrokeJoin)(JNIEnv* env, jobject thiz, jlong content, jint join)
{
	if( !content ) return;
	((CPDFContentStream *)content)->write_stroke_join( (RDLINE_JOIN)join );
}

extern "C" JNIEXPORT void JNICALL FUNC_NAME(PageContent_setStrokeWidth)(JNIEnv* env, jobject thiz, jlong content, jfloat w)
{
	if( !content ) return;
	((CPDFContentStream *)content)->write_stroke_width( w );
}

extern "C" JNIEXPORT void JNICALL FUNC_NAME(PageContent_setStrokeMiter)(JNIEnv* env, jobject thiz, jlong content, jfloat miter)
{
	if( !content ) return;
	((CPDFContentStream *)content)->write_stroke_miter( miter );
}

extern "C" JNIEXPORT void JNICALL FUNC_NAME(PageContent_textSetCharSpace)(JNIEnv* env, jobject thiz, jlong content, jfloat space)
{
	if( !content ) return;
	((CPDFContentStream *)content)->write_text_char_space( space );
}

extern "C" JNIEXPORT void JNICALL FUNC_NAME(PageContent_textSetWordSpace)(JNIEnv* env, jobject thiz, jlong content, jfloat space)
{
	if( !content ) return;
	((CPDFContentStream *)content)->write_text_word_space( space );
}

extern "C" JNIEXPORT void JNICALL FUNC_NAME(PageContent_textSetLeading)(JNIEnv* env, jobject thiz, jlong content, jfloat leading)
{
	if( !content ) return;
	((CPDFContentStream *)content)->write_text_leading( leading );
}

extern "C" JNIEXPORT void JNICALL FUNC_NAME(PageContent_textSetRise)(JNIEnv* env, jobject thiz, jlong content, jfloat rise)
{
	if( !content ) return;
	((CPDFContentStream *)content)->write_text_rise( rise );
}

extern "C" JNIEXPORT void JNICALL FUNC_NAME(PageContent_textSetHScale)(JNIEnv* env, jobject thiz, jlong content, jint scale)
{
	if( !content ) return;
	((CPDFContentStream *)content)->write_text_scale( scale );
}

extern "C" JNIEXPORT void JNICALL FUNC_NAME(PageContent_textNextLine)(JNIEnv* env, jobject thiz, jlong content)
{
	if( !content ) return;
	((CPDFContentStream *)content)->write_text_next_line();
}

extern "C" JNIEXPORT void JNICALL FUNC_NAME(PageContent_textMove)(JNIEnv* env, jobject thiz, jlong content, jfloat x, jfloat y)
{
	if( !content ) return;
	((CPDFContentStream *)content)->write_text_pos( x, y );
}

extern "C" JNIEXPORT void JNICALL FUNC_NAME(PageContent_textSetFont)(JNIEnv* env, jobject thiz, jlong content, jlong font, jfloat size)
{
	if( !content ) return;
	((CPDFContentStream *)content)->write_text_font( (PDFRES_FONT *)font, size );
}

extern "C" JNIEXPORT void JNICALL FUNC_NAME(PageContent_destroy)(JNIEnv* env, jobject thiz, jlong content)
{
	if( !content ) return;
	delete ((CPDFContentStream *)content);
}

extern "C" JNIEXPORT void JNICALL FUNC_NAME(PageContent_textSetRenderMode)(JNIEnv* env, jobject thiz, jlong content, jint mode)
{
	if( !content ) return;
	((CPDFContentStream *)content)->write_text_render(mode);
}

extern "C" JNIEXPORT jlong JNICALL FUNC_NAME(Page_addResFont)(JNIEnv* env, jobject thiz, jlong page, jlong font)
{
	if( !page || g_active_mode < 3 ) return 0;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	if( !ipage->idoc->editable ) return 0;
	if( !ipage->load_content )
		ipage->load_content = ipage->idoc->doc.Page_LoadContent( ipage->page, ipage->session );
	return (jlong)ipage->idoc->doc.Page_AddResFont(ipage->page, (PDF_FONT)font);
}

extern "C" JNIEXPORT jlong JNICALL FUNC_NAME(Page_addResImage)(JNIEnv* env, jobject thiz, jlong page, jlong image)
{
	if( !page || g_active_mode < 3 ) return 0;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	if( !ipage->idoc->editable ) return 0;
	if( !ipage->load_content )
		ipage->load_content = ipage->idoc->doc.Page_LoadContent( ipage->page, ipage->session );
	return (jlong)ipage->idoc->doc.Page_AddResImage(ipage->page, image);
}

extern "C" JNIEXPORT jlong JNICALL FUNC_NAME(Page_addResGState)(JNIEnv* env, jobject thiz, jlong page, jlong gstate)
{
	if( !page || g_active_mode < 3 ) return 0;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	if( !ipage->idoc->editable ) return 0;
	if( !ipage->load_content )
		ipage->load_content = ipage->idoc->doc.Page_LoadContent( ipage->page, ipage->session );
	return (jlong)ipage->idoc->doc.Page_AddResGState(ipage->page, gstate);
}

extern "C" JNIEXPORT jboolean JNICALL FUNC_NAME(Page_addContent)(JNIEnv* env, jobject thiz, jlong page, jlong content, jboolean flush)
{
	if( !page || !content || g_active_mode < 3 ) return JNI_FALSE;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	if( !ipage->idoc->editable ) return JNI_FALSE;
	if( !ipage->load_content )
		ipage->load_content = ipage->idoc->doc.Page_LoadContent( ipage->page, ipage->session );
	return ipage->idoc->doc.Page_AddContent( ipage->page, (CPDFContentStream *)content, flush );
}

extern "C" JNIEXPORT void JNICALL FUNC_NAME(Page_close)( JNIEnv* env, jobject thiz, jlong page )
{
	if( !page ) return;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	if( ipage->load_content )
		ipage->idoc->doc.Page_UnloadContent( ipage->page );
	ipage->idoc->doc.Page_Close( ipage->page );
	delete ipage;
}

extern "C" JNIEXPORT jfloat JNICALL FUNC_NAME(Page_reflowStart)( JNIEnv* env, jobject thiz, jlong page, jfloat width, jfloat ratio, jboolean reflow_images )
{
	if( !page || g_active_mode < 2 ) return 0;
	RDMATRIX mat;
	mat.init_scale( ratio, ratio );
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	ipage->reflow.reflow_images( reflow_images );
	ipage->session.reset();
	if( !ipage->load_content )
		ipage->load_content = ipage->idoc->doc.Page_LoadContent( ipage->page, ipage->session );
	ipage->idoc->doc.Page_Render( ipage->page, &ipage->reflow, mat, true, ipage->session );
	RDFIX height;
	ipage->reflow.reflow_start( width, height );
	return height.to_f32();
}

extern "C" JNIEXPORT jboolean JNICALL FUNC_NAME(Page_reflow)( JNIEnv* env, jobject thiz, jlong page, jlong dib, jfloat orgx, jfloat orgy )
{
	if( !page || !dib || g_active_mode < 2 ) return JNI_FALSE;
	register RDU32 *cur = (RDU32 *)((PDF_CACHE *)dib)->dat;
	register RDU32 *end = cur + ((PDF_CACHE *)dib)->w * ((PDF_CACHE *)dib)->h;
	while( cur < end )
		*cur++ = 0xFFFFFFFF;
	CRDBmp32Ref bmp( ((PDF_CACHE *)dib)->dat, ((PDF_CACHE *)dib)->w, ((PDF_CACHE *)dib)->h, ((PDF_CACHE *)dib)->w * 4 );
	CRDSurfaceRGBA surface( &bmp );
	RDPOINTF pt;
	pt.x = orgx;
	pt.y = orgy;
	((PDF_PAGE_INNER *)page)->reflow.reflow_render( &surface, pt );
	return JNI_TRUE;
}

extern "C" JNIEXPORT jint JNICALL FUNC_NAME(Page_reflowGetParaCount)( JNIEnv* env, jobject thiz, jlong page )
{
	if( !page || g_active_mode < 2 ) return 0;
	return ((PDF_PAGE_INNER *)page)->reflow.get_paras_count();
}

extern "C" JNIEXPORT jint JNICALL FUNC_NAME(Page_reflowGetCharCount)( JNIEnv* env, jobject thiz, jlong page, jint iparagraph )
{
	if( !page || g_active_mode < 2 ) return 0;
	return ((PDF_PAGE_INNER *)page)->reflow.get_char_count(iparagraph);
}

extern "C" JNIEXPORT jfloat JNICALL FUNC_NAME(Page_reflowGetCharWidth)( JNIEnv* env, jobject thiz, jlong page, jint iparagraph, jint ichar )
{
	if( !page || g_active_mode < 2 ) return 0;
	RDFIX width;
	((PDF_PAGE_INNER *)page)->reflow.get_char_width(iparagraph, ichar, width);
	return width.to_f32();
}

extern "C" JNIEXPORT jfloat JNICALL FUNC_NAME(Page_reflowGetCharHeight)( JNIEnv* env, jobject thiz, jlong page, jint iparagraph, jint ichar )
{
	if( !page || g_active_mode < 2 ) return 0;
	RDFIX height;
	((PDF_PAGE_INNER *)page)->reflow.get_char_height(iparagraph, ichar, height);
	return height.to_f32();
}

extern "C" JNIEXPORT jint JNICALL FUNC_NAME(Page_reflowGetCharColor)( JNIEnv* env, jobject thiz, jlong page, jint iparagraph, jint ichar )
{
	if( !page || g_active_mode < 2 ) return 0;
	return ((PDF_PAGE_INNER *)page)->reflow.get_char_color(iparagraph, ichar);
}

extern "C" JNIEXPORT jint JNICALL FUNC_NAME(Page_reflowGetCharUnicode)( JNIEnv* env, jobject thiz, jlong page, jint iparagraph, jint ichar )
{
	if( !page || g_active_mode < 2 ) return 0;
	return ((PDF_PAGE_INNER *)page)->reflow.get_char_unicode(iparagraph, ichar);
}

extern "C" JNIEXPORT jstring JNICALL FUNC_NAME(Page_reflowGetCharFont)( JNIEnv* env, jobject thiz, jlong page, jint iparagraph, jint ichar )
{
	if( !page || g_active_mode < 2 ) return NULL;
	return env->NewStringUTF( ((PDF_PAGE_INNER *)page)->reflow.get_char_font(iparagraph, ichar) );
}

extern "C" JNIEXPORT void JNICALL FUNC_NAME(Page_reflowGetCharRect)( JNIEnv* env, jobject thiz, jlong page, jint iparagraph, jint ichar, jfloatArray rect )
{
	if( !page || g_active_mode < 2 ) return;
	jfloat *arr = env->GetFloatArrayElements( rect, NULL );
	RDRECTF bound;
	((PDF_PAGE_INNER *)page)->reflow.get_char_rect(iparagraph, ichar, bound);
	arr[0] = bound.left.to_f32();
	arr[1] = bound.top.to_f32();
	arr[2] = bound.right.to_f32();
	arr[3] = bound.bottom.to_f32();
	env->ReleaseFloatArrayElements( rect, arr, 0 );
}

extern "C" JNIEXPORT jstring JNICALL FUNC_NAME(Page_reflowGetText)( JNIEnv* env, jobject thiz, jlong page, jint iparagraph1, jint ichar1, jint iparagraph2, jint ichar2 )
{
	if( !page || g_active_mode < 2 ) return NULL;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	wchar_t *wtxt = (wchar_t *)RDAlloc( sizeof(wchar_t) * 256 );
	RDI32 wcnt = 0;
	RDI32 wmax = 255;
	RDI32 ie;
	while( iparagraph1 <= iparagraph2 )
	{
		if( iparagraph1 == iparagraph2 )
			ie = ichar2;
		else
			ie = ipage->reflow.get_char_count( iparagraph1 ) - 1;
		while( ichar1 <= ie )
		{
			if( wcnt >= wmax )
			{
				wmax += 256;
				wtxt = (wchar_t *)RDRealloc( wtxt, sizeof(wchar_t) * (wmax + 1) );
			}
			wtxt[wcnt] = ipage->reflow.get_char_unicode( iparagraph1, ichar1 );
			wcnt++;
			ichar1++;

		}
		if( wcnt >= wmax - 1 )
		{
			wmax += 256;
			wtxt = (wchar_t *)RDRealloc( wtxt, sizeof(wchar_t) * (wmax + 1) );
		}
		wtxt[wcnt] = '\r';
		wtxt[wcnt + 1] = '\n';
		wcnt += 2;
		ichar1 = 0;
		iparagraph1++;
	}
	wtxt[wcnt] = 0;
	char *txt = (char *)RDAlloc( wcnt * 4 + 4 );
	cvt_utol( wtxt, txt, wcnt * 4 + 3 );
	RDFree( wtxt );
	jstring str = env->NewStringUTF( txt );
	RDFree( txt );
	return str;
}

extern "C" JNIEXPORT jboolean JNICALL FUNC_NAME(Page_reflowToBmp)( JNIEnv* env, jobject thiz, jlong page, jobject bitmap, jfloat orgx, jfloat orgy )
{
	if( !page || !bitmap || g_active_mode < 2 ) return JNI_FALSE;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	CPDFBmp tmp(env, bitmap);
	if (!tmp.valid()) return JNI_FALSE;
	RDPOINTF pt;
	pt.x = orgx;
	pt.y = orgy;
	if (tmp.is_rgba32())
	{
		CRDBmp32Ref bmp(tmp.get_dat(), tmp.get_w(), tmp.get_h(), tmp.get_stride());
		CRDSurfaceRGBA surface(&bmp);
		((PDF_PAGE_INNER *)page)->reflow.reflow_render(&surface, pt);
	}
	else
	{
		CRDBmp32 *bmp = tmp.create32();
		CRDSurfaceRGBA surface(bmp);
		((PDF_PAGE_INNER *)page)->reflow.reflow_render(&surface, pt);
		tmp.draw(0, 0, bmp);
		delete bmp;
	}
	return JNI_TRUE;
}

extern "C" JNIEXPORT void JNICALL FUNC_NAME(Page_renderPrepare)( JNIEnv* env, jobject thiz, jlong page, jlong dib )
{
	if( dib )
	{
		CRDBmp32Ref bmp( (RDU8 *)((PDF_CACHE *)dib)->dat, ((PDF_CACHE *)dib)->w, ((PDF_CACHE *)dib)->h, ((PDF_CACHE *)dib)->w<<2 );
		bmp.reset( 0xFFFFFFFF );
	}
	if( page )
		((PDF_PAGE_INNER *)page)->session.reset();
}

extern "C" JNIEXPORT jboolean JNICALL FUNC_NAME(Page_render)( JNIEnv* env, jobject thiz, jlong page, jlong dib, jlong matrix, jint quality )
{
	if( page && dib && matrix )
	{
		PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
		RDMATRIX *mat = (RDMATRIX *)matrix;
		if( ipage->session.is_cancelling() )
			return JNI_FALSE;

		CRDBmp32Ref bmp( ((PDF_CACHE *)dib)->dat, ((PDF_CACHE *)dib)->w, ((PDF_CACHE *)dib)->h, ((PDF_CACHE *)dib)->w * 4 );
		CRDSurfaceRGBA surface( &bmp );
		CPDFGRenderRGBA render;
		render.set_quality( quality );
		render.set_surface( &surface );
		if( !ipage->load_content )
		{
			ipage->load_content = ipage->idoc->doc.Page_LoadContent( ipage->page, ipage->session );
		}
		ipage->idoc->doc.Page_Render( ipage->page, &render, *mat, g_show_annots, ipage->session );
		render.set_surface( NULL );
		return JNI_TRUE;
	}
	else
		return JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL FUNC_NAME(Page_renderToBmp)( JNIEnv* env, jobject thiz, jlong page, jobject bitmap, jlong matrix, jint quality )
{
	if( !page || !bitmap || !matrix ) return JNI_FALSE;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	RDMATRIX *mat = (RDMATRIX *)matrix;
	ipage->session.reset();
	CPDFGRenderRGBA render;
	render.set_quality(quality);
	if (!ipage->load_content)
		ipage->load_content = ipage->idoc->doc.Page_LoadContent(ipage->page, ipage->session);
	if (ipage->session.is_cancelling())
		return JNI_FALSE;

	CPDFBmp tmp(env, bitmap);
	if (!tmp.valid()) return JNI_FALSE;
	if (tmp.is_rgba32())
	{
		CRDBmp32Ref bmp(tmp.get_dat(), tmp.get_w(), tmp.get_h(), tmp.get_stride());
		CRDSurfaceRGBA surface(&bmp);
		render.set_surface(&surface);
		ipage->idoc->doc.Page_Render(ipage->page, &render, *mat, g_show_annots, ipage->session);
	}
	else
	{
		CRDBmp32 *bmp = tmp.create32();
		if (bmp)
		{
			CRDSurfaceRGBA surface(bmp);
			render.set_surface(&surface);
			ipage->idoc->doc.Page_Render(ipage->page, &render, *mat, g_show_annots, ipage->session);
			tmp.draw(0, 0, bmp);
			delete bmp;
		}
	}
	render.set_surface(NULL);
	return JNI_TRUE;
}

extern "C" JNIEXPORT jboolean JNICALL FUNC_NAME(Page_renderToBuf)(JNIEnv* env, jobject thiz, jlong page, jintArray data, jint w, jint h, jlong matrix, jint quality)
{
	if (!page || !data || w <= 0 || h <= 0 || !matrix) return JNI_FALSE;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	RDMATRIX *mat = (RDMATRIX *)matrix;
	ipage->session.reset();
	if (ipage->session.is_cancelling())
		return JNI_FALSE;
	if (env->GetArrayLength(data) != w * h) return JNI_FALSE;
	void *pixs = env->GetPrimitiveArrayCritical(data, NULL);
	if (!pixs) return JNI_FALSE;
	CRDBmp32Ref bmp((RDU8 *)pixs, w, h, w << 2);
	CRDSurfaceRGBA surface(&bmp);
	CPDFGRenderRGBA render;
	render.set_quality(quality);
	render.set_surface(&surface);
	if (!ipage->load_content)
	{
		ipage->load_content = ipage->idoc->doc.Page_LoadContent(ipage->page, ipage->session);
	}
	ipage->idoc->doc.Page_Render(ipage->page, &render, *mat, g_show_annots, ipage->session);
	render.set_surface(NULL);
	env->ReleasePrimitiveArrayCritical(data, pixs, 0);
	return JNI_TRUE;
}

extern "C" JNIEXPORT jboolean JNICALL FUNC_NAME(Page_renderThumb)( JNIEnv* env, jobject thiz, jlong page, jobject bitmap )
{
	if( !page || !bitmap ) return JNI_FALSE;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	RDBOOL ret = false;
	RDRECT rect;

	CPDFBmp tmp(env, bitmap);
	if (!tmp.valid()) return JNI_FALSE;
	if (tmp.is_rgba32())
	{
		CRDBmp32Ref bmp(tmp.get_dat(), tmp.get_w(), tmp.get_h(), tmp.get_stride());
		if (ret = ipage->idoc->doc.Page_RenderThumb(ipage->page, &bmp, rect))
		{
			RDI32 x1 = rect.left;
			RDI32 w1 = rect.get_width();
			RDI32 y1 = rect.top;
			RDI32 h1 = rect.get_height();
			RDI32 stride = bmp.get_stride();
			RDU8 *row = bmp.get_bits() + y1 * stride + (x1 << 2);
			while (h1 > 0)
			{
				RDU8 *cur = row;
				RDU8 *end = cur + (w1 << 2);
				while (cur < end)
				{
					RDU8 ctmp = cur[0];
					cur[0] = cur[2];
					cur[2] = ctmp;
					cur += 4;
				}
				row += stride;
				h1--;
			}
		}
	}
	else
	{
		CRDBmp32 *bmp = tmp.create32();
		if (ret = ipage->idoc->doc.Page_RenderThumb(ipage->page, bmp, rect))
		{
			RDI32 x1 = rect.left;
			RDI32 w1 = rect.get_width();
			RDI32 y1 = rect.top;
			RDI32 h1 = rect.get_height();
			RDI32 stride = bmp->get_stride();
			RDU8 *row = bmp->get_bits() + y1 * stride + (x1 << 2);
			while (h1 > 0)
			{
				RDU8 *cur = row;
				RDU8 *end = cur + (w1 << 2);
				while (cur < end)
				{
					RDU8 ctmp = cur[0];
					cur[0] = cur[2];
					cur[2] = ctmp;
					cur += 4;
				}
				row += stride;
				h1--;
			}
			tmp.draw(0, 0, bmp);
		}
		delete bmp;
	}
	return ret;
}

extern "C" JNIEXPORT jboolean JNICALL FUNC_NAME(Page_renderThumbToDIB)(JNIEnv* env, jobject thiz, jlong page, jlong dib)
{
	if (!page || !dib) return JNI_FALSE;

	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	CRDBmp32Ref bmp(((PDF_CACHE *)dib)->dat, ((PDF_CACHE *)dib)->w, ((PDF_CACHE *)dib)->h, ((PDF_CACHE *)dib)->w<<2);
	RDRECT rect;
	RDBOOL ret = ipage->idoc->doc.Page_RenderThumb(ipage->page, &bmp, rect);
	if (ret)
	{
		RDI32 x1 = rect.left;
		RDI32 w1 = rect.get_width();
		RDI32 y1 = rect.top;
		RDI32 h1 = rect.get_height();
		RDI32 stride = bmp.get_stride();
		RDU8 *row = bmp.get_bits() + y1 * stride + (x1 << 2);
		while (h1 > 0)
		{
			RDU8 *cur = row;
			RDU8 *end = cur + (w1 << 2);
			while (cur < end)
			{
				RDU8 ctmp = cur[0];
				cur[0] = cur[2];
				cur[2] = ctmp;
				cur += 4;
			}
			row += stride;
			h1--;
		}
	}
	return ret;
}

extern "C" JNIEXPORT jboolean JNICALL FUNC_NAME(Page_renderThumbToBuf)(JNIEnv* env, jobject thiz, jlong page, jintArray data, jint w, jint h)
{
	if (!page || !data || w <= 0 || h <= 0) return JNI_FALSE;
	if (env->GetArrayLength(data) != w * h) return JNI_FALSE;
	void *pixs = env->GetPrimitiveArrayCritical(data, NULL);
	if (!pixs) return JNI_FALSE;

	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	CRDBmp32Ref bmp((RDU8 *)pixs, w, h, w<<2);
	RDRECT rect;
	RDBOOL ret = ipage->idoc->doc.Page_RenderThumb(ipage->page, &bmp, rect);
	if (ret)
	{
		RDI32 x1 = rect.left;
		RDI32 w1 = rect.get_width();
		RDI32 y1 = rect.top;
		RDI32 h1 = rect.get_height();
		RDI32 stride = bmp.get_stride();
		RDU8 *row = bmp.get_bits() + y1 * stride + (x1 << 2);
		while (h1 > 0)
		{
			RDU8 *cur = row;
			RDU8 *end = cur + (w1 << 2);
			while (cur < end)
			{
				RDU8 ctmp = cur[0];
				cur[0] = cur[2];
				cur[2] = ctmp;
				cur += 4;
			}
			row += stride;
			h1--;
		}
	}
	env->ReleasePrimitiveArrayCritical(data, pixs, 0);
	return ret;
}

extern "C" JNIEXPORT void JNICALL FUNC_NAME(Page_renderCancel)( JNIEnv* env, jobject thiz, jlong page )
{
	if( page )
		((PDF_PAGE_INNER *)page)->session.cancel();
}

extern "C" JNIEXPORT jboolean JNICALL FUNC_NAME(Page_renderIsFinished)( JNIEnv* env, jobject thiz, jlong page )
{
	if( page )
		return ((PDF_PAGE_INNER *)page)->session.is_finished();
	else
		return true;
}

extern "C" JNIEXPORT void JNICALL FUNC_NAME(Page_objsStart)( JNIEnv* env, jobject thiz, jlong page, jboolean rtol )
{
	if( !page || g_active_mode < 1 ) return;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	if( !ipage->objs_get )
	{
		RDSIZEF sz;
		ipage->idoc->doc.Page_GetSize( ipage->page, sz );
		RDRECTF bound;
		bound.left = sz.cx >> 2;
		bound.top = sz.cy >> 2;
		bound.right = sz.cx - bound.left;
		bound.bottom = sz.cy - bound.top;
		ipage->text.set_page_size( sz.cx, sz.cy );
		ipage->text.set_bound( bound );
		ipage->text.set_rtol( rtol );
		if( !ipage->load_content )
		{
			ipage->load_content = ipage->idoc->doc.Page_LoadContent( ipage->page, ipage->session );
		}
		RDMATRIX mat;
		mat.init_scale( 1, 1 );
		ipage->idoc->doc.Page_Render( ipage->page, &ipage->text, mat, true, ipage->session );
		if( ipage->session.is_finished() )
			ipage->objs_get = true;
	}
}

extern "C" JNIEXPORT jint JNICALL FUNC_NAME(Page_objsGetCharCount)( JNIEnv* env, jobject thiz, jlong page )
{
	if( !page || g_active_mode < 1 ) return 0;
	return ((PDF_PAGE_INNER *)page)->text.get_char_count();
}

extern "C" JNIEXPORT jint JNICALL FUNC_NAME(Page_objsGetCharIndex)( JNIEnv* env, jobject thiz, jlong page, jfloatArray pt )
{
	if( !page ) return -1;
	jfloat *arr = env->GetFloatArrayElements( pt, NULL );
	RDPOINT point;
	point.x = arr[0] * 100;
	point.y = arr[1] * 100;
	env->ReleaseFloatArrayElements( pt, arr, 0 );
	return ((PDF_PAGE_INNER *)page)->text.get_char_index( point );
}

extern "C" JNIEXPORT jstring JNICALL FUNC_NAME(Page_objsGetString)( JNIEnv* env, jobject thiz, jlong page, jint from, jint to )
{
	if( !page ) return NULL;
	if( to > ((PDF_PAGE_INNER *)page)->text.get_char_count() )
		to = ((PDF_PAGE_INNER *)page)->text.get_char_count();
	if( from < 0 ) from = 0;
	if( from >= to ) return NULL;
	struct _PDF_CHAR_INFO *the_char = (struct _PDF_CHAR_INFO *)(((PDF_PAGE_INNER *)page)->text.get_chars() + from);
	if( !the_char ) return NULL;
	wchar_t *ucs_val = (wchar_t *)RDAlloc( sizeof( wchar_t ) * (to - from + 2) );
	wchar_t *ucs_cur = ucs_val;
	wchar_t *ucs_end = ucs_val + to - from;
	while( ucs_cur < ucs_end )
	{
		ucs_cur[0] = the_char->unicode;
		ucs_cur++;
		the_char++;
	}
	ucs_cur[0] = 0;
	char *tmp = (char *)RDAlloc( 4 * (to - from) + 8 );
	cvt_utol( ucs_val, tmp, 4 * (to - from) + 4 );
	jstring ret = env->NewStringUTF(tmp);
	RDFree( tmp );
	RDFree( ucs_val );
	return ret;
}

extern "C" JNIEXPORT jint JNICALL FUNC_NAME(Page_objsAlignWord)( JNIEnv* env, jobject thiz, jlong page, jint from, jint dir )
{
	if( !page ) return from;
	RDI32 end = ((PDF_PAGE_INNER *)page)->text.get_char_count();
	if( from >= end || from <= 0  ) return from;
	register struct _PDF_CHAR_INFO *the_char = (struct _PDF_CHAR_INFO *)(((PDF_PAGE_INNER *)page)->text.get_chars() + from);
#define check(u) (is_letter(u) || (u >= 0xc0 && u < 0x250))
	if( !check( the_char->unicode ) ) return from;
	if( dir < 0 )
	{
		while( from >= 0 && check( the_char->unicode ) )
		{
			the_char--;
			from--;
		}
		from++;
	}
	else
	{
		while( from < end && check( the_char->unicode ) )
		{
			the_char++;
			from++;
		}
		from--;
	}
	return from;
}

extern "C" JNIEXPORT void JNICALL FUNC_NAME(Page_objsGetCharRect)( JNIEnv* env, jobject thiz, jlong page, jint index, jfloatArray vals )
{
	if( !page ) return;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	struct _PDF_CHAR_INFO *the_char = (struct _PDF_CHAR_INFO *)(ipage->text.get_chars() + index);
	jfloat *arr = (jfloat *)env->GetFloatArrayElements(vals, JNI_FALSE);
	if( index < 0 || index >= ipage->text.get_char_count() )
	{
		arr[0] = 0;
		arr[1] = 0;
		arr[2] = 0;
		arr[3] = 0;
	}
	else
	{
		arr[0] = the_char->rect.left / 100.0f;
		arr[1] = the_char->rect.top / 100.0f;
		arr[2] = the_char->rect.right / 100.0f;
		arr[3] = the_char->rect.bottom / 100.0f;
	}
	env->ReleaseFloatArrayElements(vals, arr, 0);
}

extern "C" JNIEXPORT jstring JNICALL FUNC_NAME(Page_objsGetCharFontName)( JNIEnv* env, jobject thiz, jlong page, jint index )
{
	if( !page ) return NULL;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	struct _PDF_CHAR_INFO *the_char = (struct _PDF_CHAR_INFO *)(ipage->text.get_chars() + index);
	if( the_char->font && the_char->font->get_font_name() )
		return env->NewStringUTF(the_char->font->get_font_name());
	else
		return NULL;
}

struct PDF_FINDER_INNER
{
	int *pos;
	int count;
	int max;
};

int jni_cmp_chars( const wchar_t* pattern, _PDF_CHAR_INFO *chars )
{
	while( pattern[0] && pattern[0] == chars[0].unicode )
	{
		pattern++;
		chars++;
	}
	if( pattern[0] == 0 ) return 0;
	else return pattern[0] - chars[0].unicode;
}

int jni_cmp_chars_i( const wchar_t* pattern, _PDF_CHAR_INFO *chars )
{
	while( pattern[0] &&
		( pattern[0] == chars[0].unicode ||
		(pattern[0] >= 'A' && pattern[0] <= 'Z' && pattern[0] + 32 == chars[0].unicode) ||
		(pattern[0] >= 'a' && pattern[0] <= 'z' && pattern[0] - 32 == chars[0].unicode) ) )
	{
		pattern++;
		chars++;
	}
	if( pattern[0] == 0 ) return 0;
	else return pattern[0] - chars[0].unicode;
}

void jni_add_char(PDF_FINDER_INNER *finder, int pos)
{
	if( finder->count >= finder->max )
	{
		finder->max += 8;
		if( !(finder->pos = (int *)RDRealloc( finder->pos, sizeof( int ) * finder->max )) )
		{
			finder->count = finder->max = 0;
			return;
		}
	}
	finder->pos[finder->count] = pos;
	finder->count++;
}

extern "C" JNIEXPORT jlong JNICALL FUNC_NAME(Page_findOpen)(JNIEnv* env, jobject thiz, jlong page, jstring str, jboolean match_case, jboolean whole_word)
{
	if( !page || !str ) return 0;
	PDF_FINDER_INNER *ifinder = new PDF_FINDER_INNER;
	PDF_String sPat;
	cvt_js_to_cs( env, str, sPat );
	wchar_t pattern[128];
	cvt_ltou( sPat.m_val, pattern, 128 );
	sPat.free();

	if( whole_word )
	{
		const wchar_t *str = pattern;
		while( str[0] )
		{
			if( str[0] > 0x250 || str[0] < ' ' )
			{
				whole_word = false;
				break;
			}
			str++;
		}
	}
	if( whole_word )
	{
		PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
		ifinder->pos = NULL;
		ifinder->count = 0;
		ifinder->max = 0;
		int word_len = ucs_len( pattern );
		int len = ipage->text.get_char_count();
		int pos = 0;
		_PDF_CHAR_INFO *chars = ipage->text.get_chars();
		if( match_case )
		{
			while( len >= word_len )
			{
				if( jni_cmp_chars( pattern, chars ) == 0 )
				{
					if( pos > 0 && is_blank(chars[-1].unicode) )
					{
						if( pos + word_len < len && is_blank(chars[word_len].unicode) )
							jni_add_char( ifinder, pos );
						else if( pos + word_len >= len )
							jni_add_char( ifinder, pos );
					}
					else if( pos + word_len < len && is_blank(chars[word_len].unicode) )
						jni_add_char( ifinder, pos );
				}
				chars++;
				len--;
				pos++;
			}
		}
		else
		{
			while( len >= word_len )
			{
				if( jni_cmp_chars_i( pattern, chars ) == 0 )
				{
					if( pos > 0 && is_blank(chars[-1].unicode) )
					{
						if( pos + word_len < len && is_blank(chars[word_len].unicode) )
							jni_add_char( ifinder, pos );
						else if( pos + word_len >= len )
							jni_add_char( ifinder, pos );
					}
					else if( pos + word_len < len && is_blank(chars[word_len].unicode) )
						jni_add_char( ifinder, pos );
				}
				chars++;
				len--;
				pos++;
			}
		}
	}
	else
	{
		PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
		ifinder->pos = NULL;
		ifinder->count = 0;
		ifinder->max = 0;
		int word_len = ucs_len( pattern );
		int len = ipage->text.get_char_count();
		int pos = 0;
		_PDF_CHAR_INFO *chars = ipage->text.get_chars();
		if( match_case )
		{
			while( len >= word_len )
			{
				if( jni_cmp_chars( pattern, chars ) == 0 )
					jni_add_char( ifinder, pos );
				chars++;
				len--;
				pos++;
			}
		}
		else
		{
			while( len >= word_len )
			{
				if( jni_cmp_chars_i( pattern, chars ) == 0 )
					jni_add_char( ifinder, pos );
				chars++;
				len--;
				pos++;
			}
		}
	}
	return (jlong)ifinder;
}

extern "C" JNIEXPORT jint JNICALL FUNC_NAME(Page_findGetCount)( JNIEnv* env, jobject thiz, jlong finder )
{
	if( !finder ) return -1;
	PDF_FINDER_INNER *ifinder = (PDF_FINDER_INNER *)finder;
	return ifinder->count;
}

extern "C" JNIEXPORT jint JNICALL FUNC_NAME(Page_findGetFirstChar)( JNIEnv* env, jobject thiz, jlong finder, jint index )
{
	PDF_FINDER_INNER *ifinder = (PDF_FINDER_INNER *)finder;
	if( !finder || !ifinder->pos || index < 0 || index >= ifinder->count ) return -1;
	return ifinder->pos[index];
}

extern "C" JNIEXPORT void JNICALL FUNC_NAME(Page_findClose)( JNIEnv* env, jobject thiz, jlong finder )
{
	if( finder )
	{
		PDF_FINDER_INNER *ifinder = (PDF_FINDER_INNER *)finder;
		RDFree( ifinder->pos );
		delete ifinder;
	}
}

extern "C" JNIEXPORT jint JNICALL FUNC_NAME(Page_getRotate)( JNIEnv* env, jobject thiz, jlong page )
{
	if( !page ) return 0;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	return ipage->idoc->doc.Page_GetRotate( ipage->page );
}

extern "C" JNIEXPORT jint JNICALL FUNC_NAME(Page_getAnnotCount)( JNIEnv* env, jobject thiz, jlong page )
{
	if( !page || g_active_mode < 2 ) return 0;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	return ipage->idoc->doc.Page_GetAnnotCount( ipage->page );
}

extern "C" JNIEXPORT jlong JNICALL FUNC_NAME(Page_getAnnot)(JNIEnv* env, jobject thiz, jlong page, jint index)
{
	if( !page || g_active_mode < 2 ) return 0;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	return (jlong)ipage->idoc->doc.Page_GetAnnot(ipage->page, index);
}

extern "C" JNIEXPORT jlong JNICALL FUNC_NAME(Page_getAnnotFromPoint)(JNIEnv* env, jobject thiz, jlong page, jfloat x, jfloat y)
{
	if( !page || g_active_mode < 2 ) return 0;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	return (jlong)ipage->idoc->doc.Page_GetAnnot(ipage->page, RDFIX(x), RDFIX(y));
}

extern "C" JNIEXPORT jlong JNICALL FUNC_NAME(Page_getAnnotByName)(JNIEnv* env, jobject thiz, jlong page, jstring name)
{
	if (!page || !name || g_active_mode < 2) return 0;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	PDF_String cname;
	cvt_js_to_cs(env, name, cname);
	jlong ret = (jlong)ipage->idoc->doc.Page_GetAnnot(ipage->page, &cname);
	cname.free();
	return ret;
}

extern "C" JNIEXPORT jint JNICALL FUNC_NAME(Page_getAnnotType)( JNIEnv* env, jobject thiz, jlong page, jlong annot )
{
	if( !page || !annot || g_active_mode < 2 ) return 0;
	CPDFAnnot *obj_annot = (CPDFAnnot *)annot;
	return obj_annot->get_type();
}

extern "C" JNIEXPORT jboolean JNICALL FUNC_NAME(Page_isAnnotLocked)( JNIEnv* env, jobject thiz, jlong page, jlong annot )
{
	if( !page || !annot || g_active_mode < 2 ) return JNI_TRUE;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	CPDFAnnot *obj_annot = (CPDFAnnot *)annot;
	return obj_annot->is_locked();
}

extern "C" JNIEXPORT jboolean JNICALL FUNC_NAME(Page_isAnnotLockedContent)( JNIEnv* env, jobject thiz, jlong page, jlong annot )
{
	if( !page || !annot || g_active_mode < 2 ) return JNI_TRUE;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	CPDFAnnot *obj_annot = (CPDFAnnot *)annot;
	return obj_annot->is_locked_contents();
}

extern "C" JNIEXPORT jboolean JNICALL FUNC_NAME(Page_isAnnotHide)( JNIEnv* env, jobject thiz, jlong page, jlong annot )
{
	if( !page || !annot || g_active_mode < 2 ) return false;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	return ((CPDFAnnot *)annot)->is_hide();
}

extern "C" JNIEXPORT void JNICALL FUNC_NAME(Page_setAnnotHide)( JNIEnv* env, jobject thiz, jlong page, jlong annot, jboolean hide )
{
	if( !page || !annot || g_active_mode < 2 ) return;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	CPDFAnnot *obj_annot = (CPDFAnnot *)annot;
	ipage->idoc->doc.Annot_SetHide( obj_annot, hide );
}

extern "C" JNIEXPORT void JNICALL FUNC_NAME(Page_setAnnotLock)( JNIEnv* env, jobject thiz, jlong page, jlong annot, jboolean lock )
{
	if( !page || !annot || g_active_mode < 2 ) return;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	CPDFAnnot *obj_annot = (CPDFAnnot *)annot;
	ipage->idoc->doc.Annot_SetLock( obj_annot, lock );
}

extern "C" JNIEXPORT jboolean JNICALL FUNC_NAME(Page_renderAnnotToBmp)( JNIEnv* env, jobject thiz, jlong page, jlong annot, jobject bitmap )
{
	if( !page || !annot || g_active_mode < 2 ) return JNI_FALSE;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	CPDFAnnot *obj_annot = (CPDFAnnot *)annot;
	CPDFBmp tmp(env, bitmap);
	if (!tmp.valid()) return JNI_FALSE;
	RDBOOL ret = false;
	if (tmp.is_rgba32())
	{
		CRDBmp32Ref bmp(tmp.get_dat(), tmp.get_w(), tmp.get_h(), tmp.get_stride());
		ret = ipage->idoc->doc.Page_RenderAnnot(ipage->page, obj_annot, &bmp, true);
	}
	else
	{
		CRDBmp32 *bmp = tmp.create32();
		ret = ipage->idoc->doc.Page_RenderAnnot(ipage->page, obj_annot, bmp, true);
		tmp.draw(0, 0, bmp);
		delete bmp;
	}
	return ret;
}

extern "C" JNIEXPORT void JNICALL FUNC_NAME(Page_getAnnotRect)( JNIEnv* env, jobject thiz, jlong page, jlong annot, jfloatArray rect )
{
	if( !page || !annot || !rect || g_active_mode < 2 ) return;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	CPDFAnnot *obj_annot = (CPDFAnnot *)annot;
	RDRECTF rc;
	ipage->idoc->doc.Page_GetAnnotRect( ipage->page, obj_annot, rc );
	
	jfloat *arr = env->GetFloatArrayElements( rect, JNI_FALSE );
	arr[0] = rc.left.to_f32();
	arr[1] = rc.top.to_f32();
	arr[2] = rc.right.to_f32();
	arr[3] = rc.bottom.to_f32();
	env->ReleaseFloatArrayElements(rect, arr, 0);
}

extern "C" JNIEXPORT jint JNICALL FUNC_NAME(Page_getAnnotFillColor)( JNIEnv* env, jobject thiz, jlong page, jlong annot )
{
	if( !page || !annot || g_active_mode < 2 ) return 0;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	CPDFAnnot *obj_annot = (CPDFAnnot *)annot;
	return ipage->idoc->doc.Annot_GetFillColor( obj_annot );
}

extern "C" JNIEXPORT jboolean JNICALL FUNC_NAME(Page_setAnnotFillColor)( JNIEnv* env, jobject thiz, jlong page, jlong annot, jint color )
{
	if( !page || !annot || g_active_mode < 2 ) return JNI_FALSE;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	if( !ipage->idoc->editable ) return JNI_FALSE;
	CPDFAnnot *obj_annot = (CPDFAnnot *)annot;
	return ipage->idoc->doc.Annot_SetFillColor( obj_annot, color );
}


extern "C" JNIEXPORT jint JNICALL FUNC_NAME(Page_getAnnotStrokeColor)( JNIEnv* env, jobject thiz, jlong page, jlong annot )
{
	if( !page || !annot || g_active_mode < 2 ) return 0;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	CPDFAnnot *obj_annot = (CPDFAnnot *)annot;
	return ipage->idoc->doc.Annot_GetStrokeColor( obj_annot );
}

extern "C" JNIEXPORT jboolean JNICALL FUNC_NAME(Page_setAnnotStrokeColor)( JNIEnv* env, jobject thiz, jlong page, jlong annot, jint color )
{
	if( !page || !annot || g_active_mode < 2 || (color&0xFF000000) == 0 ) return JNI_FALSE;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	if( !ipage->idoc->editable ) return JNI_FALSE;
	CPDFAnnot *obj_annot = (CPDFAnnot *)annot;
	return ipage->idoc->doc.Annot_SetStrokeColor( obj_annot, color );
}

extern "C" JNIEXPORT jfloat JNICALL FUNC_NAME(Page_getAnnotStrokeWidth)( JNIEnv* env, jobject thiz, jlong page, jlong annot )
{
	if( !page || !annot || g_active_mode < 2 ) return 0;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	CPDFAnnot *obj_annot = (CPDFAnnot *)annot;
	return ipage->idoc->doc.Annot_GetStrokeWidth( obj_annot ).to_f32();
}

extern "C" JNIEXPORT jboolean JNICALL FUNC_NAME(Page_setAnnotStrokeWidth)( JNIEnv* env, jobject thiz, jlong page, jlong annot, jfloat width )
{
	if( !page || !annot || g_active_mode < 2 || width <= 0 ) return JNI_FALSE;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	if( !ipage->idoc->editable ) return JNI_FALSE;
	CPDFAnnot *obj_annot = (CPDFAnnot *)annot;
	return ipage->idoc->doc.Annot_SetStrokeWidth( obj_annot, width );
}

extern "C" JNIEXPORT jlong JNICALL FUNC_NAME(Page_getAnnotInkPath)(JNIEnv* env, jobject thiz, jlong page, jlong annot)
{
	if( !page || !annot || g_active_mode < 2 ) return 0;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	CPDFAnnot *obj_annot = (CPDFAnnot *)annot;
	return (jlong)ipage->idoc->doc.Page_GetAnnotInkPath(ipage->page, obj_annot);
}

extern "C" JNIEXPORT jboolean JNICALL FUNC_NAME(Page_setAnnotInkPath)( JNIEnv* env, jobject thiz, jlong page, jlong annot, jlong path )
{
	if( !page || !annot || g_active_mode < 2 || !path ) return JNI_FALSE;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	if( !ipage->idoc->editable ) return JNI_FALSE;
	CPDFAnnot *obj_annot = (CPDFAnnot *)annot;
	return ipage->idoc->doc.Page_SetAnnotInkPath( ipage->page, obj_annot, (CRDPath *)path );
}

extern "C" JNIEXPORT jlong JNICALL FUNC_NAME(Page_getAnnotPolygonPath)(JNIEnv* env, jobject thiz, jlong page, jlong annot)
{
	if( !page || !annot || g_active_mode < 2 ) return 0;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	CPDFAnnot *obj_annot = (CPDFAnnot *)annot;
	return (jlong)ipage->idoc->doc.Page_GetAnnotPolygonPath(ipage->page, obj_annot);
}

extern "C" JNIEXPORT jboolean JNICALL FUNC_NAME(Page_setAnnotPolygonPath)( JNIEnv* env, jobject thiz, jlong page, jlong annot, jlong path )
{
	if( !page || !annot || g_active_mode < 2 || !path ) return JNI_FALSE;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	if( !ipage->idoc->editable ) return JNI_FALSE;
	CPDFAnnot *obj_annot = (CPDFAnnot *)annot;
	return ipage->idoc->doc.Page_SetAnnotPolygonPath( ipage->page, obj_annot, (CRDPath *)path );
}

extern "C" JNIEXPORT jlong JNICALL FUNC_NAME(Page_getAnnotPolylinePath)(JNIEnv* env, jobject thiz, jlong page, jlong annot)
{
	if( !page || !annot || g_active_mode < 2 ) return 0;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	CPDFAnnot *obj_annot = (CPDFAnnot *)annot;
	return (jlong)ipage->idoc->doc.Page_GetAnnotPolylinePath(ipage->page, obj_annot);
}

extern "C" JNIEXPORT jboolean JNICALL FUNC_NAME(Page_setAnnotPolylinePath)( JNIEnv* env, jobject thiz, jlong page, jlong annot, jlong path )
{
	if( !page || !annot || g_active_mode < 2 || !path ) return JNI_FALSE;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	if( !ipage->idoc->editable ) return JNI_FALSE;
	CPDFAnnot *obj_annot = (CPDFAnnot *)annot;
	return ipage->idoc->doc.Page_SetAnnotPolylinePath( ipage->page, obj_annot, (CRDPath *)path );
}

extern "C" JNIEXPORT void JNICALL FUNC_NAME(Page_setAnnotRect)( JNIEnv* env, jobject thiz, jlong page, jlong annot, jfloatArray rect )
{
	if( !page || !annot || !rect || g_active_mode < 2 ) return;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	if( !ipage->idoc->editable ) return;
	CPDFAnnot *obj_annot = (CPDFAnnot *)annot;
	RDRECTF rc;
	jfloat *arr = env->GetFloatArrayElements( rect, JNI_FALSE );
	rc.left = arr[0];
	rc.top = arr[1];
	rc.right = arr[2];
	rc.bottom = arr[3];
	ipage->idoc->doc.Page_SetAnnotRect( ipage->page, obj_annot, rc );
	env->ReleaseFloatArrayElements(rect, arr, 0);
}

extern "C" JNIEXPORT jstring JNICALL FUNC_NAME(Page_getAnnotName)(JNIEnv* env, jobject thiz, jlong page, jlong annot)
{
	if (!page || !annot || g_active_mode < 2) return NULL;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	CPDFAnnot *obj_annot = (CPDFAnnot *)annot;
	wchar_t *vals = (wchar_t *)RDAlloc(65536 * sizeof(wchar_t) + (65536 * 2));
	char *utf8 = (char *)(vals + 65536);
	if (ipage->idoc->doc.Annot_GetName(obj_annot, vals, 65535))
	{
		cvt_utol(vals, utf8, 65536 * 2 - 1);
		jstring ret = env->NewStringUTF(utf8);
		RDFree(vals);
		return ret;
	}
	else
	{
		RDFree(vals);
		return NULL;
	}
}

extern "C" JNIEXPORT jboolean JNICALL FUNC_NAME(Page_setAnnotName)(JNIEnv* env, jobject thiz, jlong page, jlong annot, jstring text)
{
	if (!page || !annot || g_active_mode < 2) return JNI_FALSE;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	if (!ipage->idoc->editable) return JNI_FALSE;
	CPDFAnnot *obj_annot = (CPDFAnnot *)annot;
	PDF_String str;
	str.init();
	cvt_js_to_cs(env, text, str);

	wchar_t *vals = (wchar_t *)RDAlloc(65536 * sizeof(wchar_t));
	if (str.m_len > 0) cvt_ltou(str.m_val, vals, 65535);
	else vals[0] = 0;
	str.free();
	RDBOOL ret = ipage->idoc->doc.Annot_SetName(obj_annot, vals);
	RDFree(vals);
	if (ret) return JNI_TRUE;
	else return JNI_FALSE;
}

extern "C" JNIEXPORT jint JNICALL FUNC_NAME(Page_getAnnotDest)( JNIEnv* env, jobject thiz, jlong page, jlong annot )
{
	if( !page || !annot || g_active_mode < 2 ) return -1;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	CPDFAnnot *obj_annot = (CPDFAnnot *)annot;
	RDFIX y;
	return ipage->idoc->doc.Annot_GetDest( ipage->index, obj_annot, y );
}

extern "C" JNIEXPORT jstring JNICALL FUNC_NAME(Page_getAnnotURI)( JNIEnv* env, jobject thiz, jlong page, jlong annot )
{
	if( !page || !annot || g_active_mode < 2 ) return NULL;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	CPDFAnnot *obj_annot = (CPDFAnnot *)annot;
	wchar_t vals[512];
	char utf8[1024];
	if( !ipage->idoc->doc.Annot_GetUri( obj_annot, vals, 511 ) ) return NULL;
	cvt_utol( vals, utf8, 1023 );
	return env->NewStringUTF( utf8 );
}

extern "C" JNIEXPORT jstring JNICALL FUNC_NAME(Page_getAnnotJS)(JNIEnv* env, jobject thiz, jlong page, jlong annot)
{
	if (!page || !annot || g_active_mode < 2) return NULL;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	CPDFAnnot *obj_annot = (CPDFAnnot *)annot;
	wchar_t *js = (wchar_t *)RDAlloc(8192 * sizeof(wchar_t));
	char *utf8 = (char *)(js + 4096);
	if (!ipage->idoc->doc.Annot_GetJS(obj_annot, js, 4095)) return NULL;
	cvt_utol(js, utf8, 8191);
	jstring ret = env->NewStringUTF(utf8);
	RDFree(js);
	return ret;
}

extern "C" JNIEXPORT jstring JNICALL FUNC_NAME(Page_getAnnotFileLink)( JNIEnv* env, jobject thiz, jlong page, jlong annot )
{
	if( !page || !annot || g_active_mode < 2 ) return NULL;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	CPDFAnnot *obj_annot = (CPDFAnnot *)annot;
	wchar_t vals[512];
	char utf8[1024];
	if( !ipage->idoc->doc.Annot_GetFileLink( obj_annot, vals, 511 ) ) return NULL;
	cvt_utol( vals, utf8, 1023 );
	return env->NewStringUTF( utf8 );
}

extern "C" JNIEXPORT jstring JNICALL FUNC_NAME(Page_getAnnotPopupSubject)( JNIEnv* env, jobject thiz, jlong page, jlong annot )
{
	if( !page || !annot || g_active_mode < 2 ) return NULL;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	CPDFAnnot *obj_annot = (CPDFAnnot *)annot;
	wchar_t vals[512];
	char utf8[1024];
	if( ipage->idoc->doc.Annot_GetSubject( obj_annot, vals, 511 ) )
	{
		cvt_utol( vals, utf8, 1023 );
		return env->NewStringUTF( utf8 );
	}
	else
		return NULL;
}

extern "C" JNIEXPORT jboolean JNICALL FUNC_NAME(Page_setAnnotPopupSubject)( JNIEnv* env, jobject thiz, jlong page, jlong annot, jstring text )
{
	if( !page || !annot || g_active_mode < 2 ) return JNI_FALSE;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	if( !ipage->idoc->editable ) return JNI_FALSE;
	CPDFAnnot *obj_annot = (CPDFAnnot *)annot;
	PDF_String str;
	str.init();
	cvt_js_to_cs( env, text, str );

	wchar_t vals[512];
	if( str.m_len > 0 ) cvt_ltou( str.m_val, vals, 511 );
	else vals[0] = 0;
	str.free();
	RDBOOL ret = ipage->idoc->doc.Annot_SetSubject( obj_annot, vals );
	if( ret ) return JNI_TRUE;
	else return JNI_FALSE;
}

extern "C" JNIEXPORT jstring JNICALL FUNC_NAME(Page_getAnnotPopupText)( JNIEnv* env, jobject thiz, jlong page, jlong annot )
{
	if( !page || !annot || g_active_mode < 2 ) return NULL;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	CPDFAnnot *obj_annot = (CPDFAnnot *)annot;
	wchar_t *vals = (wchar_t *)RDAlloc( 65536 * sizeof( wchar_t ) + (65536 * 2) );
	char *utf8 = (char *)(vals + 65536);
	if( ipage->idoc->doc.Annot_GetText( obj_annot, vals, 65535 ) )
	{
		cvt_utol( vals, utf8, 65536 * 2 - 1 );
		jstring ret = env->NewStringUTF( utf8 );
		RDFree( vals );
		return ret;
	}
	else
	{
		RDFree( vals );
		return NULL;
	}
}

extern "C" JNIEXPORT jboolean JNICALL FUNC_NAME(Page_setAnnotPopupText)( JNIEnv* env, jobject thiz, jlong page, jlong annot, jstring text )
{
	if( !page || !annot || g_active_mode < 2 ) return JNI_FALSE;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	if( !ipage->idoc->editable ) return JNI_FALSE;
	CPDFAnnot *obj_annot = (CPDFAnnot *)annot;
	PDF_String str;
	str.init();
	cvt_js_to_cs( env, text, str );

	wchar_t *vals = (wchar_t *)RDAlloc( 65536 * sizeof( wchar_t ) );
	if( str.m_len > 0 ) cvt_ltou( str.m_val, vals, 65535 );
	else vals[0] = 0;
	str.free();
	RDBOOL ret = ipage->idoc->doc.Annot_SetText( obj_annot, vals );
	RDFree( vals );
	if( ret ) return JNI_TRUE;
	else return JNI_FALSE;
}

extern "C" JNIEXPORT jint JNICALL FUNC_NAME(Page_getAnnotFieldType)( JNIEnv* env, jobject thiz, jlong page, jlong annot )
{
	if( !page || !annot || g_active_mode < 3 ) return 0;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	CPDFAnnot *obj_annot = (CPDFAnnot *)annot;
	return ipage->idoc->doc.Annot_GetFieldType( obj_annot );
}

extern "C" JNIEXPORT jstring JNICALL FUNC_NAME(Page_getAnnotFieldName)( JNIEnv* env, jobject thiz, jlong page, jlong annot )
{
	if( !page || !annot || g_active_mode < 3 ) return NULL;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	CPDFAnnot *obj_annot = (CPDFAnnot *)annot;
	wchar_t wname[512];
	RDI32 len = ipage->idoc->doc.Annot_GetFieldName( obj_annot, wname, 512 );
	if( len <= 0 ) return NULL;
	char name[1024];
	cvt_utol( wname, name, 1023 );
	return env->NewStringUTF( name );
}

extern "C" JNIEXPORT jstring JNICALL FUNC_NAME(Page_getAnnotFieldFullName)( JNIEnv* env, jobject thiz, jlong page, jlong annot )
{
	if( !page || !annot || g_active_mode < 3 ) return NULL;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	CPDFAnnot *obj_annot = (CPDFAnnot *)annot;
	wchar_t wname[512];
	RDI32 len = ipage->idoc->doc.Annot_GetFieldFullName( obj_annot, wname, 512 );
	if( len <= 0 ) return NULL;
	char name[1024];
	cvt_utol( wname, name, 1023 );
	return env->NewStringUTF( name );
}

extern "C" JNIEXPORT jstring JNICALL FUNC_NAME(Page_getAnnotFieldFullName2)( JNIEnv* env, jobject thiz, jlong page, jlong annot )
{
	if( !page || !annot || g_active_mode < 3 ) return NULL;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	CPDFAnnot *obj_annot = (CPDFAnnot *)annot;
	wchar_t wname[512];
	RDI32 len = ipage->idoc->doc.Annot_GetFieldFullName2( obj_annot, wname, 512 );
	if( len <= 0 ) return NULL;
	char name[1024];
	cvt_utol( wname, name, 1023 );
	return env->NewStringUTF( name );
}

extern "C" JNIEXPORT jint JNICALL FUNC_NAME(Page_getAnnotSignStatus)( JNIEnv* env, jobject thiz, jlong page, jlong annot )
{
	if( !page || !annot || g_active_mode < 3 ) return -1;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	CPDFAnnot *obj_annot = (CPDFAnnot *)annot;
	return ipage->idoc->doc.Annot_GetSignStatus( obj_annot );
}

extern "C" JNIEXPORT jint JNICALL FUNC_NAME(Page_getAnnotEditType)( JNIEnv* env, jobject thiz, jlong page, jlong annot )
{
	if( !page || !annot || g_active_mode < 3 ) return -1;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	CPDFAnnot *obj_annot = (CPDFAnnot *)annot;
	return ipage->idoc->doc.Annot_GetEditType( obj_annot );
}

extern "C" JNIEXPORT jint JNICALL FUNC_NAME(Page_getAnnotEditMaxlen)( JNIEnv* env, jobject thiz, jlong page, jlong annot )
{
	if( !page || !annot || g_active_mode < 3 ) return 0;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	CPDFAnnot *obj_annot = (CPDFAnnot *)annot;
	return ipage->idoc->doc.Annot_GetEditMaxlen( obj_annot );
}

extern "C" JNIEXPORT jboolean JNICALL FUNC_NAME(Page_getAnnotEditTextRect)( JNIEnv* env, jobject thiz, jlong page, jlong annot, jfloatArray rect )
{
	if( !page || !annot || g_active_mode < 3 ) return JNI_FALSE;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	CPDFAnnot *obj_annot = (CPDFAnnot *)annot;
	jfloat *arr = env->GetFloatArrayElements( rect, NULL );
	RDRECTF rc;
	ipage->idoc->doc.Page_GetAnnotEditRect( ipage->page, obj_annot, rc );
	arr[0] = rc.left.to_f32();
	arr[1] = rc.top.to_f32();
	arr[2] = rc.right.to_f32();
	arr[3] = rc.bottom.to_f32();
	env->ReleaseFloatArrayElements( rect, arr, 0 );
	return true;
}

extern "C" JNIEXPORT jfloat JNICALL FUNC_NAME(Page_getAnnotEditTextSize)( JNIEnv* env, jobject thiz, jlong page, jlong annot )
{
	if( !page || !annot || g_active_mode < 3 ) return 0;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	CPDFAnnot *obj_annot = (CPDFAnnot *)annot;
	return ipage->idoc->doc.Annot_GetEditTextSize( obj_annot ).to_f32();
}

extern "C" JNIEXPORT jstring JNICALL FUNC_NAME(Page_getAnnotEditTextFormat)( JNIEnv* env, jobject thiz, jlong page, jlong annot )
{
	if( !page || !annot || g_active_mode < 3 ) return NULL;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	CPDFAnnot *obj_annot = (CPDFAnnot *)annot;
	wchar_t *vals = (wchar_t *)RDAlloc( 4096 * sizeof( wchar_t ) + 8192 );
	char *utf8 = (char *)(vals + 4096);
	if( ipage->idoc->doc.Annot_GetFieldFormat( obj_annot, vals, 4095 ) )
	{
		cvt_utol( vals, utf8, 8191 );
		jstring ret = env->NewStringUTF( utf8 );
		RDFree( vals );
		return ret;
	}
	else
	{
		RDFree( vals );
		return NULL;
	}
}

extern "C" JNIEXPORT jstring JNICALL FUNC_NAME(Page_getAnnotFieldFormat)(JNIEnv* env, jobject thiz, jlong page, jlong annot)
{
	if (!page || !annot || g_active_mode < 3) return NULL;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	CPDFAnnot *obj_annot = (CPDFAnnot *)annot;
	wchar_t *vals = (wchar_t *)RDAlloc(4096 * sizeof(wchar_t) + 8192);
	char *utf8 = (char *)(vals + 4096);
	if (ipage->idoc->doc.Annot_GetFieldFormat(obj_annot, vals, 4095))
	{
		cvt_utol(vals, utf8, 8191);
		jstring ret = env->NewStringUTF(utf8);
		RDFree(vals);
		return ret;
	}
	else
	{
		RDFree(vals);
		return NULL;
	}
}

extern "C" JNIEXPORT jstring JNICALL FUNC_NAME(Page_getAnnotEditText)( JNIEnv* env, jobject thiz, jlong page, jlong annot )
{
	if( !page || !annot || g_active_mode < 3 ) return NULL;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	CPDFAnnot *obj_annot = (CPDFAnnot *)annot;
	wchar_t *vals = (wchar_t *)RDAlloc( 4096 * sizeof( wchar_t ) + 8192 );
	char *utf8 = (char *)(vals + 4096);
	if( ipage->idoc->doc.Annot_GetEditText( obj_annot, vals, 4095 ) )
	{
		cvt_utol( vals, utf8, 8191 );
		jstring ret = env->NewStringUTF( utf8 );
		RDFree( vals );
		return ret;
	}
	else
	{
		RDFree( vals );
		return NULL;
	}
}

extern "C" JNIEXPORT jboolean JNICALL FUNC_NAME(Page_setAnnotEditText)( JNIEnv* env, jobject thiz, jlong page, jlong annot, jstring text )
{
	if( !page || !annot || g_active_mode < 3 ) return JNI_FALSE;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	if( !ipage->idoc->editable ) return JNI_FALSE;
	CPDFAnnot *obj_annot = (CPDFAnnot *)annot;
	PDF_String str;
	str.init();
	cvt_js_to_cs( env, text, str );

	wchar_t vals[1024];
	if( str.m_len > 0 ) cvt_ltou( str.m_val, vals, 1023 );
	else vals[0] = 0;
	RDI32 max_len = ipage->idoc->doc.Annot_GetEditMaxlen( obj_annot );
	if( max_len > 0 && max_len < 1024 ) vals[max_len] = 0;
	str.free();
	RDBOOL ret = ipage->idoc->doc.Annot_SetEditText( obj_annot, vals );
	if( ret ) return JNI_TRUE;
	else return JNI_FALSE;
}


extern "C" JNIEXPORT jint JNICALL FUNC_NAME(Page_getAnnotEditTextColor)(JNIEnv* env, jobject thiz, jlong page, jlong annot)
{
	if (!page || !annot || g_active_mode < 3) return 0;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	CPDFAnnot *obj_annot = (CPDFAnnot *)annot;
	RDRGBA clr = ipage->idoc->doc.Annot_GetEditTextColor(obj_annot);
	return *(jint *)&clr;
}

extern "C" JNIEXPORT jboolean JNICALL FUNC_NAME(Page_setAnnotEditTextColor)(JNIEnv* env, jobject thiz, jlong page, jlong annot, jint color)
{
	if (!page || !annot || g_active_mode < 3) return JNI_FALSE;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	if (!ipage->idoc->editable) return JNI_FALSE;
	CPDFAnnot *obj_annot = (CPDFAnnot *)annot;
	RDBOOL ret = ipage->idoc->doc.Annot_SetEditTextColor(obj_annot, *(RDRGBA *)&color);
	if (ret) return JNI_TRUE;
	else return JNI_FALSE;
}

extern "C" JNIEXPORT jint JNICALL FUNC_NAME(Page_getAnnotComboItemCount)( JNIEnv* env, jobject thiz, jlong page, jlong annot )
{
	if( !page || !annot || g_active_mode < 3 ) return -1;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	CPDFAnnot *obj_annot = (CPDFAnnot *)annot;
	return ipage->idoc->doc.Annot_GetComboItemCount( obj_annot );
}

extern "C" JNIEXPORT jstring JNICALL FUNC_NAME(Page_getAnnotComboItem)( JNIEnv* env, jobject thiz, jlong page, jlong annot, jint item )
{
	if( !page || !annot || g_active_mode < 3 ) return NULL;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	CPDFAnnot *obj_annot = (CPDFAnnot *)annot;
	wchar_t vals[512];
	char utf8[1024];
	if( ipage->idoc->doc.Annot_GetComboItem( obj_annot, item, vals, 511 ) )
	{
		cvt_utol( vals, utf8, 1023 );
		return env->NewStringUTF( utf8 );
	}
	else
		return NULL;
}

extern "C" JNIEXPORT jint JNICALL FUNC_NAME(Page_getAnnotComboItemSel)( JNIEnv* env, jobject thiz, jlong page, jlong annot )
{
	if( !page || !annot || g_active_mode < 3 ) return -1;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	CPDFAnnot *obj_annot = (CPDFAnnot *)annot;
	return ipage->idoc->doc.Annot_GetComboSel( obj_annot );
}

extern "C" JNIEXPORT jboolean JNICALL FUNC_NAME(Page_setAnnotComboItem)( JNIEnv* env, jobject thiz, jlong page, jlong annot, jint item )
{
	if( !page || !annot || g_active_mode < 3 ) return JNI_FALSE;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	if( !ipage->idoc->editable ) return JNI_FALSE;
	CPDFAnnot *obj_annot = (CPDFAnnot *)annot;
	return ipage->idoc->doc.Annot_SetComboSel( obj_annot, item );
}



extern "C" JNIEXPORT jint JNICALL FUNC_NAME(Page_getAnnotListItemCount)( JNIEnv* env, jobject thiz, jlong page, jlong annot )
{
	if( !page || !annot || g_active_mode < 3 ) return -1;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	CPDFAnnot *obj_annot = (CPDFAnnot *)annot;
	return ipage->idoc->doc.Annot_GetListItemCount( obj_annot );
}

extern "C" JNIEXPORT jstring JNICALL FUNC_NAME(Page_getAnnotListItem)( JNIEnv* env, jobject thiz, jlong page, jlong annot, jint item )
{
	if( !page || !annot || g_active_mode < 3 ) return NULL;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	CPDFAnnot *obj_annot = (CPDFAnnot *)annot;
	wchar_t vals[512];
	char utf8[1024];
	if( ipage->idoc->doc.Annot_GetListItem( obj_annot, item, vals, 511 ) )
	{
		cvt_utol( vals, utf8, 1023 );
		return env->NewStringUTF( utf8 );
	}
	else
		return NULL;
}

extern "C" JNIEXPORT jintArray JNICALL FUNC_NAME(Page_getAnnotListSels)( JNIEnv* env, jobject thiz, jlong page, jlong annot )
{
	if( !page || !annot || g_active_mode < 3 ) return NULL;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	CPDFAnnot *obj_annot = (CPDFAnnot *)annot;
	const RDI32 *sels = ipage->idoc->doc.Annot_GetListSels(obj_annot);
	RDI32 sels_cnt = ipage->idoc->doc.Annot_GetListSelsCount(obj_annot);
	jintArray arr = env->NewIntArray( sels_cnt );
	jint *arr1 = env->GetIntArrayElements( arr, NULL );
	mem_cpy( arr1, sels, 4 * sels_cnt );
	env->ReleaseIntArrayElements( arr, arr1, 0 );
	return arr;
}

extern "C" JNIEXPORT jboolean JNICALL FUNC_NAME(Page_setAnnotListSels)( JNIEnv* env, jobject thiz, jlong page, jlong annot, jintArray items )
{
	if( !page || !annot || g_active_mode < 3 ) return JNI_FALSE;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	if( !ipage->idoc->editable ) return JNI_FALSE;
	CPDFAnnot *obj_annot = (CPDFAnnot *)annot;
	RDI32 arr1_cnt = env->GetArrayLength( items );
	jint *arr1 = env->GetIntArrayElements( items, NULL );
	RDBOOL ret = ipage->idoc->doc.Annot_SetListSels( obj_annot, arr1, arr1_cnt );
	env->ReleaseIntArrayElements( items, arr1, 0 );
	return ret;
}


extern "C" JNIEXPORT jint JNICALL FUNC_NAME(Page_getAnnotCheckStatus)( JNIEnv* env, jobject thiz, jlong page, jlong annot )
{
	if( !page || !annot || g_active_mode < 3 ) return -1;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	CPDFAnnot *obj_annot = (CPDFAnnot *)annot;
	RDBOOL v;
	RDI32 type = ipage->idoc->doc.Annot_GetCheckStatus( obj_annot, v );
	if( type == 0 ) return -1;
	if( type == 1 )
	{
		if( v ) return 1;
		else return 0;
	}
	if( type == 2 )
	{
		if( v ) return 3;
		else return 2;
	}
	return -1;
}

extern "C" JNIEXPORT jboolean JNICALL FUNC_NAME(Page_setAnnotCheckValue)( JNIEnv* env, jobject thiz, jlong page, jlong annot, jboolean check )
{
	if( !page || !annot || g_active_mode < 3 ) return JNI_FALSE;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	CPDFAnnot *obj_annot = (CPDFAnnot *)annot;
	return ipage->idoc->doc.Annot_SetCheck( obj_annot, check );
}

extern "C" JNIEXPORT jboolean JNICALL FUNC_NAME(Page_setAnnotRadio)( JNIEnv* env, jobject thiz, jlong page, jlong annot )
{
	if( !page || !annot || g_active_mode < 3 ) return JNI_FALSE;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	if( !ipage->idoc->editable ) return JNI_FALSE;
	CPDFAnnot *obj_annot = (CPDFAnnot *)annot;
	return ipage->idoc->doc.Annot_SetRadio( obj_annot );
}

extern "C" JNIEXPORT jboolean JNICALL FUNC_NAME(Page_getAnnotReset)( JNIEnv* env, jobject thiz, jlong page, jlong annot )
{
	if( !page || !annot || g_active_mode < 3 ) return false;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	CPDFAnnot *obj_annot = (CPDFAnnot *)annot;
	return ipage->idoc->doc.Annot_GetReset( obj_annot );
}

extern "C" JNIEXPORT jboolean JNICALL FUNC_NAME(Page_setAnnotReset)( JNIEnv* env, jobject thiz, jlong page, jlong annot )
{
	if( !page || !annot || g_active_mode < 3 ) return JNI_FALSE;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	if( !ipage->idoc->editable ) return JNI_FALSE;
	CPDFAnnot *obj_annot = (CPDFAnnot *)annot;
	return ipage->idoc->doc.Annot_SetReset( obj_annot );
}

extern "C" JNIEXPORT jstring JNICALL FUNC_NAME(Page_getAnnotSubmitTarget)( JNIEnv* env, jobject thiz, jlong page, jlong annot )
{
	if( !page || !annot || g_active_mode < 3 ) return NULL;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	CPDFAnnot *obj_annot = (CPDFAnnot *)annot;
	wchar_t vals[512];
	char utf8[1024];
	if( ipage->idoc->doc.Annot_GetSubmitTarget( obj_annot, vals, 511 ) )
	{
		cvt_utol( vals, utf8, 1023 );
		return env->NewStringUTF( utf8 );
	}
	else
		return NULL;
}

extern "C" JNIEXPORT jstring JNICALL FUNC_NAME(Page_getAnnotSubmitPara)( JNIEnv* env, jobject thiz, jlong page, jlong annot )
{
	if( !page || !annot || g_active_mode < 3 ) return NULL;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	CPDFAnnot *obj_annot = (CPDFAnnot *)annot;
	wchar_t vals[512];
	char utf8[1024];
	if( ipage->idoc->doc.Annot_GetSubmitPara( obj_annot, vals, 511 ) )
	{
		cvt_utol( vals, utf8, 1023 );
		return env->NewStringUTF( utf8 );
	}
	else
		return NULL;
}

extern "C" JNIEXPORT jstring JNICALL FUNC_NAME(Page_getAnnot3D)( JNIEnv* env, jobject thiz, jlong page, jlong annot )
{
	if( !page || !annot || g_active_mode < 2 ) return NULL;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	CPDFAnnot *obj_annot = (CPDFAnnot *)annot;
	char utf8[1024];
	if( !ipage->idoc->doc.Annot_Get3D( obj_annot, utf8, 1024 ) ) return NULL;
	return env->NewStringUTF( utf8 );
}

extern "C" JNIEXPORT jstring JNICALL FUNC_NAME(Page_getAnnotMovie)( JNIEnv* env, jobject thiz, jlong page, jlong annot )
{
	if( !page || !annot || g_active_mode < 2 ) return NULL;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	CPDFAnnot *obj_annot = (CPDFAnnot *)annot;
	char utf8[1024];
	if( !ipage->idoc->doc.Annot_GetMovie( obj_annot, utf8, 1024 ) ) return NULL;
	return env->NewStringUTF( utf8 );
}

extern "C" JNIEXPORT jstring JNICALL FUNC_NAME(Page_getAnnotSound)( JNIEnv* env, jobject thiz, jlong page, jlong annot )
{
	if( !page || !annot || g_active_mode < 2 ) return NULL;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	CPDFAnnot *obj_annot = (CPDFAnnot *)annot;
	char utf8[1024];
	if( !ipage->idoc->doc.Annot_GetSound( obj_annot, utf8, 1024 ) ) return NULL;
	return env->NewStringUTF( utf8 );
}

extern "C" JNIEXPORT jstring JNICALL FUNC_NAME(Page_getAnnotAttachment)( JNIEnv* env, jobject thiz, jlong page, jlong annot )
{
	if( !page || !annot || g_active_mode < 2 ) return NULL;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	CPDFAnnot *obj_annot = (CPDFAnnot *)annot;
	char utf8[1024];
	if( !ipage->idoc->doc.Annot_GetAttachment( obj_annot, utf8, 1024 ) ) return NULL;
	return env->NewStringUTF( utf8 );
}

extern "C" JNIEXPORT jboolean JNICALL FUNC_NAME(Page_getAnnot3DData)( JNIEnv* env, jobject thiz, jlong page, jlong annot, jstring path )
{
	if( !page || !annot || g_active_mode < 2 ) return JNI_FALSE;
	PDF_String str;
	cvt_js_to_cs( env, path, str );
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	CPDFAnnot *obj_annot = (CPDFAnnot *)annot;
	jboolean ret = ipage->idoc->doc.Annot_Get3DData( obj_annot, str.m_val );
	str.free();
	return ret;
}

extern "C" JNIEXPORT jboolean JNICALL FUNC_NAME(Page_getAnnotMovieData)( JNIEnv* env, jobject thiz, jlong page, jlong annot, jstring path )
{
	if( !page || !annot || g_active_mode < 2 ) return JNI_FALSE;
	PDF_String str;
	cvt_js_to_cs( env, path, str );
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	CPDFAnnot *obj_annot = (CPDFAnnot *)annot;
	jboolean ret = ipage->idoc->doc.Annot_GetMovieData( obj_annot, str.m_val );
	str.free();
	return ret;
}

extern "C" JNIEXPORT jboolean JNICALL FUNC_NAME(Page_getAnnotSoundData)( JNIEnv* env, jobject thiz, jlong page, jlong annot, jintArray paras, jstring path )
{
	if( !page || !annot || g_active_mode < 2 ) return JNI_FALSE;
	PDF_String str;
	cvt_js_to_cs( env, path, str );
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	CPDFAnnot *obj_annot = (CPDFAnnot *)annot;
	RDI32 *para = env->GetIntArrayElements( paras, JNI_FALSE );
	jboolean ret = ipage->idoc->doc.Annot_GetSoundData( obj_annot, para, str.m_val );
	env->ReleaseIntArrayElements( paras, para, 0 );
	str.free();
	return ret;
}

extern "C" JNIEXPORT jboolean JNICALL FUNC_NAME(Page_getAnnotAttachmentData)( JNIEnv* env, jobject thiz, jlong page, jlong annot, jstring path )
{
	if( !page || !annot || g_active_mode < 2 ) return JNI_FALSE;
	PDF_String str;
	cvt_js_to_cs( env, path, str );
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	CPDFAnnot *obj_annot = (CPDFAnnot *)annot;
	jboolean ret = ipage->idoc->doc.Annot_GetAttachmentData( obj_annot, str.m_val );
	str.free();
	return ret;
}

extern "C" JNIEXPORT jboolean JNICALL FUNC_NAME(Page_moveAnnot)( JNIEnv* env, jobject thiz, jlong page_src, jlong page_dst, jlong annot, jfloatArray rect )
{
	if( !page_src || !page_dst || !annot || g_active_mode < 2 ) return JNI_FALSE;
	PDF_PAGE_INNER *ipage_src = (PDF_PAGE_INNER *)page_src;
	PDF_PAGE_INNER *ipage_dst = (PDF_PAGE_INNER *)page_dst;
	if( ipage_src->idoc != ipage_dst->idoc ) return JNI_FALSE;
	CPDFAnnot *obj_annot = (CPDFAnnot *)annot;
	if( !ipage_src->idoc->editable ) return JNI_FALSE;
	RDRECTF rect1;
	jfloat *arr = env->GetFloatArrayElements( rect, NULL );
	rect1.left = arr[0];
	rect1.top = arr[1];
	rect1.right = arr[2];
	rect1.bottom = arr[3];
	env->ReleaseFloatArrayElements(rect, arr, 0);
	ipage_src->idoc->doc.Page_MoveAnnot( ipage_src->page, ipage_dst->page, obj_annot, rect1 );
	return JNI_TRUE;
}

extern "C" JNIEXPORT jboolean JNICALL FUNC_NAME(Page_copyAnnot)( JNIEnv* env, jobject thiz, jlong page, jlong annot, jfloatArray rect )
{
	if( !page || !annot || g_active_mode < 2 ) return JNI_FALSE;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	CPDFAnnot *obj_annot = (CPDFAnnot *)annot;
	if( !ipage->idoc->editable ) return JNI_FALSE;
	RDRECTF rect1;
	jfloat *arr = env->GetFloatArrayElements( rect, NULL );
	rect1.left = arr[0];
	rect1.top = arr[1];
	rect1.right = arr[2];
	rect1.bottom = arr[3];
	env->ReleaseFloatArrayElements(rect, arr, 0);
	return ipage->idoc->doc.Page_CopyAnnot( ipage->page, obj_annot, rect1 );
}

extern "C" JNIEXPORT jboolean JNICALL FUNC_NAME(Page_removeAnnot)( JNIEnv* env, jobject thiz, jlong page, jlong annot )
{
	if( !page || !annot || g_active_mode < 2 ) return JNI_FALSE;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	CPDFAnnot *obj_annot = (CPDFAnnot *)annot;
	if( !ipage->idoc->editable ) return JNI_FALSE;
	return ipage->idoc->doc.Page_RemoveAnnot( ipage->page, obj_annot );
}

extern "C" JNIEXPORT jlong JNICALL FUNC_NAME(Ink_create)(JNIEnv* env, jobject thiz, jfloat line_w, jint color, jint style)
{
	CRDInk *hw = new CRDInk(line_w, style);
	hw->SetColor( *(RDRGBA *)&color );
	return (jlong)hw;
}

extern "C" JNIEXPORT void JNICALL FUNC_NAME(Ink_destroy)( JNIEnv* env, jobject thiz, jlong hand )
{
	CRDInk *hw = (CRDInk *)hand;
	if( hw ) delete hw;
}

extern "C" JNIEXPORT void JNICALL FUNC_NAME(Ink_onDown)( JNIEnv* env, jobject thiz, jlong hand, jfloat x, jfloat y )
{
	CRDInk *hw = (CRDInk *)hand;
	if( hw ) hw->OnDown( x, y );
}

extern "C" JNIEXPORT void JNICALL FUNC_NAME(Ink_onMove)( JNIEnv* env, jobject thiz, jlong hand, jfloat x, jfloat y )
{
	CRDInk *hw = (CRDInk *)hand;
	if( hw ) hw->OnMove( x, y );
}

extern "C" JNIEXPORT void JNICALL FUNC_NAME(Ink_onUp)( JNIEnv* env, jobject thiz, jlong hand, jfloat x, jfloat y )
{
	CRDInk *hw = (CRDInk *)hand;
	if( hw ) hw->OnUp( x, y );
}

extern "C" JNIEXPORT jint JNICALL FUNC_NAME(Ink_getNodeCount)( JNIEnv* env, jobject thiz, jlong hand )
{
	CRDInk *hw = (CRDInk *)hand;
	if( hw ) return hw->GetNodeCnt();
	else return 0;
}

extern "C" JNIEXPORT jint JNICALL FUNC_NAME(Ink_getNode)( JNIEnv* env, jobject thiz, jlong hand, jint index, jfloatArray pt )
{
	CRDInk *hw = (CRDInk *)hand;
	if( hw )
	{
		RDFIX x;
		RDFIX y;
		RDI32 op = hw->GetNode( index, x, y );
		jfloat *arr = env->GetFloatArrayElements( pt, NULL );
		arr[0] = x.to_f32();
		arr[1] = y.to_f32();
		env->ReleaseFloatArrayElements( pt, arr, 0 );
		return op;
	}
	else
		return -1;
}

extern "C" JNIEXPORT jlong JNICALL FUNC_NAME(Path_create)(JNIEnv* env, jobject thiz)
{
	return (jlong)new CRDPath;
}

extern "C" JNIEXPORT void JNICALL FUNC_NAME(Path_moveTo)(JNIEnv* env, jobject thiz, jlong path, jfloat x, jfloat y)
{
	((CRDPath *)path)->path_move_to( x, y );
}

extern "C" JNIEXPORT void JNICALL FUNC_NAME(Path_lineTo)(JNIEnv* env, jobject thiz, jlong path, jfloat x, jfloat y)
{
	((CRDPath *)path)->path_line_to( x, y );
}

extern "C" JNIEXPORT void JNICALL FUNC_NAME(Path_curveTo)(JNIEnv* env, jobject thiz, jlong path, jfloat x1, jfloat y1, jfloat x2, jfloat y2, jfloat x3, jfloat y3 )
{
	((CRDPath *)path)->path_bezier3_to( x1, y1, x2, y2, x3, y3 );
}

extern "C" JNIEXPORT void JNICALL FUNC_NAME(Path_closePath)(JNIEnv* env, jobject thiz, jlong path)
{
	((CRDPath *)path)->path_close_sub();
}

extern "C" JNIEXPORT void JNICALL FUNC_NAME(Path_destroy)(JNIEnv* env, jobject thiz, jlong path)
{
	delete (CRDPath *)path;
}

extern "C" JNIEXPORT jint JNICALL FUNC_NAME(Path_getNodeCount)(JNIEnv* env, jobject thiz, jlong path )
{
	return ((CRDPath *)path)->path_get_nodes_cnt();
}

extern "C" JNIEXPORT jint JNICALL FUNC_NAME(Path_getNode)(JNIEnv* env, jobject thiz, jlong path, jint index, jfloatArray pt )
{
	const RDPATH_NODE *node = ((CRDPath *)path)->path_get_node(index);
	jfloat *arr = env->GetFloatArrayElements( pt, NULL );
	arr[0] = node->pt.x.to_f32();
	arr[1] = node->pt.y.to_f32();
	env->ReleaseFloatArrayElements(pt, arr, 0);
	return node->op;
}

extern "C" JNIEXPORT jlong JNICALL FUNC_NAME(HWriting_create)(JNIEnv* env, jobject thiz, jint w, jint h, jfloat min_w, jfloat max_w, jint clr_r, jint clr_g, jint clr_b)
{
	CRDBmp8 *bmp = new CRDBmp8( w, h, (w + 3)&(~3) );
	CRDHWriting *hw = new CRDHWriting( min_w * SCALE_BASE, max_w * SCALE_BASE, 50 * SCALE_BASE, bmp );
	hw->SetColor( clr_r, clr_g, clr_b );
	return (jlong)hw;
}

extern "C" JNIEXPORT void JNICALL FUNC_NAME(HWriting_onDown)( JNIEnv* env, jobject thiz, jlong hand, jfloat x, jfloat y )
{
	CRDHWriting *hw = (CRDHWriting *)hand;
	if( hw )
		hw->OnDown( x * SCALE_BASE, y * SCALE_BASE );
}

extern "C" JNIEXPORT void JNICALL FUNC_NAME(HWriting_onMove)( JNIEnv* env, jobject thiz, jlong hand, jfloat x, jfloat y )
{
	CRDHWriting *hw = (CRDHWriting *)hand;
	if( hw )
		hw->OnMove( x * SCALE_BASE, y * SCALE_BASE );
}

extern "C" JNIEXPORT void JNICALL FUNC_NAME(HWriting_onDraw)( JNIEnv* env, jobject thiz, jlong hand, jlong dib )
{
	if( !hand || !dib ) return;
	CRDHWriting *hw = (CRDHWriting *)hand;
	CPDFBmp *tmp = (CPDFBmp *)dib;
	CRDBmp8 *src = hw->GetBmp();
	RDPOINT org;
	org.x = 0;
	org.y = 0;
	if (tmp->is_rgba32())
	{
		CRDBmp32Ref bmp(tmp->get_dat(), tmp->get_w(), tmp->get_h(), tmp->get_stride());
		CRDSurfaceBGRA surface(&bmp);
		surface.fill(org, src, hw->GetColor());
	}
	else
	{
		CRDBmp32 *bmp = tmp->create32();
		CRDSurfaceBGRA surface(bmp);
		surface.fill(org, src, hw->GetColor());
		tmp->draw(0, 0, bmp);
		delete bmp;
	}
}

extern "C" JNIEXPORT void JNICALL FUNC_NAME(HWriting_onUp)( JNIEnv* env, jobject thiz, jlong hand, jfloat x, jfloat y )
{
	CRDHWriting *hw = (CRDHWriting *)hand;
	if( hw )
		hw->OnUp( x * SCALE_BASE, y * SCALE_BASE );
}

extern "C" JNIEXPORT void JNICALL FUNC_NAME(HWriting_destroy)( JNIEnv* env, jobject thiz, jlong hand )
{
	CRDHWriting *hw = (CRDHWriting *)hand;
	if( hw )
	{
		delete hw->GetBmp();
		delete hw;
	}
}

extern "C" JNIEXPORT jboolean JNICALL FUNC_NAME(Page_addAnnotGoto)( JNIEnv* env, jobject thiz, jlong page, jfloatArray rect, jint pageno, jfloat top )
{
	if( !page || !rect || g_active_mode < 2 ) return JNI_FALSE;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	if( !ipage->idoc->editable ) return JNI_FALSE;
	RDRECTF rc;
	jfloat *arr = env->GetFloatArrayElements( rect, JNI_FALSE );
	rc.left = arr[0];
	rc.top = arr[1];
	rc.right = arr[2];
	rc.bottom = arr[3];
	env->ReleaseFloatArrayElements(rect, arr, 0);
	ipage->idoc->doc.Page_AddGotoAnnot( ipage->page, rc, pageno, top );
	return JNI_TRUE;
}

extern "C" JNIEXPORT jboolean JNICALL FUNC_NAME(Page_addAnnotURI)( JNIEnv* env, jobject thiz, jlong page, jfloatArray rect, jstring uri )
{
	if( !page || !rect || !uri || g_active_mode < 2 ) return JNI_FALSE;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	if( !ipage->idoc->editable ) return JNI_FALSE;
	RDRECTF rc;
	jfloat *arr = env->GetFloatArrayElements( rect, JNI_FALSE );
	rc.left = arr[0];
	rc.top = arr[1];
	rc.right = arr[2];
	rc.bottom = arr[3];
	env->ReleaseFloatArrayElements(rect, arr, 0);
	PDF_String URI;
	cvt_js_to_cs( env, uri, URI );
	ipage->idoc->doc.Page_AddUriAnnot( ipage->page, rc, URI.m_val );
	URI.free();
	return JNI_TRUE;
}

extern "C" JNIEXPORT jboolean JNICALL FUNC_NAME(Page_addAnnotStamp)( JNIEnv* env, jobject thiz, jlong page, jfloatArray rect, jint icon )
{
	if( !page || !rect || g_active_mode < 2 ) return JNI_FALSE;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	if( !ipage->idoc->editable ) return JNI_FALSE;
	RDRECTF rc;
	jfloat *arr = env->GetFloatArrayElements( rect, JNI_FALSE );
	rc.left = arr[0];
	rc.top = arr[1];
	rc.right = arr[2];
	rc.bottom = arr[3];
	env->ReleaseFloatArrayElements(rect, arr, 0);
	ipage->idoc->doc.Page_AddStampAnnot( ipage->page, rc, icon );
	return JNI_TRUE;
}

extern "C" JNIEXPORT jboolean JNICALL FUNC_NAME(Page_addAnnotAttachment)( JNIEnv* env, jobject thiz, jlong page, jstring path, jint icon, jfloatArray rect )
{
	if( !page || !path || !rect || g_active_mode < 2 ) return JNI_FALSE;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	if( !ipage->idoc->editable ) return JNI_FALSE;
	RDRECTF rc;
	jfloat *arr = env->GetFloatArrayElements( rect, JNI_FALSE );
	rc.left = arr[0];
	rc.top = arr[1];
	rc.right = arr[2];
	rc.bottom = arr[3];
	env->ReleaseFloatArrayElements(rect, arr, 0);
	const char *tmp = env->GetStringUTFChars(path, NULL);
	return ipage->idoc->doc.Page_AddFileAnnot( ipage->page, tmp, rc, icon );
}

extern "C" JNIEXPORT jboolean JNICALL FUNC_NAME(Page_addAnnotBitmap)( JNIEnv* env, jobject thiz, jlong page, jobject bitmap, jboolean has_alpha, jfloatArray rect )
{
	if( !page || !bitmap || !rect || g_active_mode < 2 ) return JNI_FALSE;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	if( !ipage->idoc->editable ) return JNI_FALSE;
	CPDFBmp tmp(env, bitmap);
	if (!tmp.valid()) return JNI_FALSE;
	RDRECTF rc;
	jfloat *arr = env->GetFloatArrayElements( rect, JNI_FALSE );
	rc.left = arr[0];
	rc.top = arr[1];
	rc.right = arr[2];
	rc.bottom = arr[3];
	env->ReleaseFloatArrayElements(rect, arr, 0);
	CRDBmp32 *dst = tmp.create32();
	RDBOOL ret = ipage->idoc->doc.Page_AddBitmapAnnot( ipage->page, dst, rc, has_alpha, false );
	delete dst;
	return ret;
}

extern "C" JNIEXPORT jboolean JNICALL FUNC_NAME(Page_addAnnotInk)( JNIEnv* env, jobject thiz, jlong page, jlong matrix, jlong hand, jfloat orgx, jfloat orgy )
{
	if( !page || !hand || !matrix || g_active_mode < 2 ) return JNI_FALSE;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	CRDInk *hw = (CRDInk *)hand;
	if( !ipage->idoc->editable ) return JNI_FALSE;
	RDMATRIX mat = *(RDMATRIX *)matrix;
	mat.do_invert();

	CRDPath path;
	hw->GetPath(path);
	path.path_translate( orgx, orgy );
	path.path_transform( mat );
	ipage->idoc->doc.Page_AddInkAnnot( ipage->page, path, hw->GetColor(), hw->GetWidth() * mat.get_scale_x() );
	return JNI_TRUE;
}

extern "C" JNIEXPORT jboolean JNICALL FUNC_NAME(Page_addAnnotInk2)( JNIEnv* env, jobject thiz, jlong page, jlong hand )
{
	if( !page || !hand || g_active_mode < 2 ) return JNI_FALSE;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	CRDInk *hw = (CRDInk *)hand;
	if( !ipage->idoc->editable ) return JNI_FALSE;

	CRDPath path;
	hw->GetPath(path);
	ipage->idoc->doc.Page_AddInkAnnot( ipage->page, path, hw->GetColor(), hw->GetWidth() );
	return JNI_TRUE;
}

extern "C" JNIEXPORT jboolean JNICALL FUNC_NAME(Page_addAnnotPolygon)( JNIEnv* env, jobject thiz, jlong page, jlong hand, jint color, jint fill_color, jfloat width )
{
	if( !page || !hand || g_active_mode < 2 ) return JNI_FALSE;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	CRDPath *path = (CRDPath *)hand;
	if( !ipage->idoc->editable ) return JNI_FALSE;

	if( !path->path_is_valid() || !path->path_is_polygon() ) return JNI_FALSE;
	ipage->idoc->doc.Page_AddPolygonAnnot( ipage->page, *path, *(RDRGBA *)&color, *(RDRGBA *)&fill_color, width );
	return JNI_TRUE;
}

extern "C" JNIEXPORT jboolean JNICALL FUNC_NAME(Page_addAnnotPolyline)( JNIEnv* env, jobject thiz, jlong page, jlong hand, jint style1, jint style2, jint color, jint fill_color, jfloat width )
{
	if( !page || !hand || g_active_mode < 2 ) return JNI_FALSE;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	CRDPath *path = (CRDPath *)hand;
	if( !ipage->idoc->editable ) return JNI_FALSE;

	if( !path->path_is_valid() || !path->path_is_polyline() ) return JNI_FALSE;
	ipage->idoc->doc.Page_AddPolylineAnnot( ipage->page, *path, style1, style2, *(RDRGBA *)&color, *(RDRGBA *)&fill_color, width );
	return JNI_TRUE;
}

extern "C" JNIEXPORT jboolean JNICALL FUNC_NAME(Page_addAnnotHWriting)( JNIEnv* env, jobject thiz, jlong page, jlong matrix, jlong hand, jfloat orgx, jfloat orgy )
{
	if( !page || !hand || !matrix || g_active_mode < 2 ) return JNI_FALSE;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	CRDHWriting *hw = (CRDHWriting *)hand;
	if( !ipage->idoc->editable ) return JNI_FALSE;
	RDMATRIX mat = *(RDMATRIX *)matrix;
	mat.do_invert();
	if( hw->GetMinW() == hw->GetMaxW() )
	{
		CRDPath path;
		hw->GetStroke( path );
		path.path_translate( orgx, orgy );
		path.path_transform( mat );
		ipage->idoc->doc.Page_AddInkAnnot( ipage->page, path, hw->GetColor(), (RDFIX(hw->GetMinW() * 2) * mat.get_scale_x())>>SCALE_BITS );
	}
	else
	{
		RDRECT rect;
		hw->GetBound( rect );
		rect.right += SCALE_MASK;
		rect.bottom += SCALE_MASK;
		rect.left >>= SCALE_BITS;
		rect.top >>= SCALE_BITS;
		rect.right >>= SCALE_BITS;
		rect.bottom >>= SCALE_BITS;
		CRDBmp8 *bmp = hw->GetBmp();
		RDI32 w = rect.get_width();
		RDI32 h = rect.get_height();
		CRDBmp32 dst( w, h ,w<<2 );
		RDRGBA clr = hw->GetColor();
		dst.reset( *(RDU32 *)&clr );
		dst.mask_alpha( bmp, -rect.left, -rect.top );

		RDRECTF bound;
		bound.left = rect.left + orgx;
		bound.top = rect.top + orgy;

		bound.right = rect.right + orgx;
		bound.bottom = rect.bottom + orgy;
		mat.get_bound( bound );
		ipage->idoc->doc.Page_AddBitmapAnnot( ipage->page, &dst, bound, true, true );
	}
	return JNI_TRUE;
}

extern "C" JNIEXPORT jboolean JNICALL FUNC_NAME(Page_addAnnotEllipse)( JNIEnv* env, jobject thiz, jlong page, jlong matrix, jfloatArray rect, jfloat width, jint color, jint icolor )
{
	if( !page || !rect || !matrix || g_active_mode < 2 ) return JNI_FALSE;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	if( !ipage->idoc->editable ) return JNI_FALSE;
	RDMATRIX mat = *(RDMATRIX *)matrix;
	mat.do_invert();
	RDRECTF rc;
	jfloat *arr = env->GetFloatArrayElements( rect, JNI_FALSE );
	rc.left = arr[0];
	rc.top = arr[1];
	rc.right = arr[2];
	rc.bottom = arr[3];
	env->ReleaseFloatArrayElements(rect, arr, 0);

	mat.get_bound( rc );
	RDFIX w = width;
	w *= mat.get_scale_x();
	ipage->idoc->doc.Page_AddEllipseAnnot( ipage->page, rc, w, *(RDRGBA *)&color, *(RDRGBA *)&icolor );
	return JNI_TRUE;
}

extern "C" JNIEXPORT jboolean JNICALL FUNC_NAME(Page_addAnnotEllipse2)( JNIEnv* env, jobject thiz, jlong page, jfloatArray rect, jfloat width, jint color, jint icolor )
{
	if( !page || !rect || g_active_mode < 2 ) return JNI_FALSE;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	if( !ipage->idoc->editable ) return JNI_FALSE;
	RDRECTF rc;
	jfloat *arr = env->GetFloatArrayElements( rect, JNI_FALSE );
	rc.left = arr[0];
	rc.top = arr[1];
	rc.right = arr[2];
	rc.bottom = arr[3];
	env->ReleaseFloatArrayElements(rect, arr, 0);

	ipage->idoc->doc.Page_AddEllipseAnnot( ipage->page, rc, width, *(RDRGBA *)&color, *(RDRGBA *)&icolor );
	return JNI_TRUE;
}

extern "C" JNIEXPORT jboolean JNICALL FUNC_NAME(Page_addAnnotLine)( JNIEnv* env, jobject thiz, jlong page, jlong matrix, jfloatArray pt1, jfloatArray pt2, jint style1, jint style2, jfloat width, jint color, jint icolor )
{
	if( !page || !pt1 || !pt2 || !matrix || g_active_mode < 2 ) return JNI_FALSE;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	if( !ipage->idoc->editable ) return JNI_FALSE;
	RDMATRIX mat = *(RDMATRIX *)matrix;
	mat.do_invert();
	RDPOINTF p1;
	RDPOINTF p2;
	jfloat *arr1 = env->GetFloatArrayElements( pt1, JNI_FALSE );
	p1.x = arr1[0];
	p1.y = arr1[1];
	env->ReleaseFloatArrayElements(pt1, arr1, 0);
	jfloat *arr2 = env->GetFloatArrayElements( pt2, JNI_FALSE );
	p2.x = arr2[0];
	p2.y = arr2[1];
	env->ReleaseFloatArrayElements(pt2, arr2, 0);

	mat.transform_point( p1 );
	mat.transform_point( p2 );
	RDFIX w = width;
	w *= mat.get_scale_x();
	ipage->idoc->doc.Page_AddLineAnnot( ipage->page, p1, p2, style1, style2, w, *(RDRGBA *)&color, *(RDRGBA *)&icolor );
	return JNI_TRUE;
}

extern "C" JNIEXPORT jboolean JNICALL FUNC_NAME(Page_addAnnotLine2)( JNIEnv* env, jobject thiz, jlong page, jfloatArray pt1, jfloatArray pt2, jint style1, jint style2, jfloat width, jint color, jint icolor )
{
	if( !page || !pt1 || !pt2 || g_active_mode < 2 ) return JNI_FALSE;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	if( !ipage->idoc->editable ) return JNI_FALSE;
	RDPOINTF p1;
	RDPOINTF p2;
	jfloat *arr1 = env->GetFloatArrayElements( pt1, JNI_FALSE );
	p1.x = arr1[0];
	p1.y = arr1[1];
	env->ReleaseFloatArrayElements(pt1, arr1, 0);
	jfloat *arr2 = env->GetFloatArrayElements( pt2, JNI_FALSE );
	p2.x = arr2[0];
	p2.y = arr2[1];
	env->ReleaseFloatArrayElements(pt2, arr2, 0);

	ipage->idoc->doc.Page_AddLineAnnot( ipage->page, p1, p2, style1, style2, width, *(RDRGBA *)&color, *(RDRGBA *)&icolor );
	return JNI_TRUE;
}

extern "C" JNIEXPORT jboolean JNICALL FUNC_NAME(Page_addAnnotRect)( JNIEnv* env, jobject thiz, jlong page, jlong matrix, jfloatArray rect, jfloat width, jint color, jint icolor )
{
	if( !page || !rect || !matrix || g_active_mode < 2 ) return JNI_FALSE;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	if( !ipage->idoc->editable ) return JNI_FALSE;
	RDMATRIX mat = *(RDMATRIX *)matrix;
	mat.do_invert();
	RDRECTF rc;
	jfloat *arr = env->GetFloatArrayElements( rect, JNI_FALSE );
	rc.left = arr[0];
	rc.top = arr[1];
	rc.right = arr[2];
	rc.bottom = arr[3];
	env->ReleaseFloatArrayElements(rect, arr, 0);

	mat.get_bound( rc );
	RDFIX w = width;
	w *= mat.get_scale_x();
	ipage->idoc->doc.Page_AddRectAnnot( ipage->page, rc, w, *(RDRGBA *)&color, *(RDRGBA *)&icolor );
	return JNI_TRUE;
}

extern "C" JNIEXPORT jboolean JNICALL FUNC_NAME(Page_addAnnotRect2)( JNIEnv* env, jobject thiz, jlong page, jfloatArray rect, jfloat width, jint color, jint icolor )
{
	if( !page || !rect || g_active_mode < 2 ) return JNI_FALSE;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	if( !ipage->idoc->editable ) return JNI_FALSE;
	RDRECTF rc;
	jfloat *arr = env->GetFloatArrayElements( rect, JNI_FALSE );
	rc.left = arr[0];
	rc.top = arr[1];
	rc.right = arr[2];
	rc.bottom = arr[3];
	env->ReleaseFloatArrayElements(rect, arr, 0);

	ipage->idoc->doc.Page_AddRectAnnot( ipage->page, rc, width, *(RDRGBA *)&color, *(RDRGBA *)&icolor );
	return JNI_TRUE;
}

extern "C" JNIEXPORT jboolean JNICALL FUNC_NAME(Page_addAnnotEditbox)( JNIEnv* env, jobject thiz, jlong page, jlong matrix, jfloatArray rect, jint line_clr, jfloat line_w, jint fill_clr, jfloat tsize, jint text_clr )
{
	if( !page || !rect || !matrix || g_active_mode < 3 ) return JNI_FALSE;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	if( !ipage->idoc->editable ) return JNI_FALSE;
	RDMATRIX mat = *(RDMATRIX *)matrix;
	mat.do_invert();
	RDRECTF rc;
	jfloat *arr = env->GetFloatArrayElements( rect, JNI_FALSE );
	rc.left = arr[0];
	rc.top = arr[1];
	rc.right = arr[2];
	rc.bottom = arr[3];
	env->ReleaseFloatArrayElements(rect, arr, 0);

	mat.get_bound( rc );
	RDFIX ts = tsize;
	ts *= mat.get_scale_x();
	ipage->idoc->doc.Page_AddEditboxAnnot( ipage->page, rc, *(RDRGBA *)&line_clr, line_w, *(RDRGBA *)&fill_clr, *(RDRGBA *)&text_clr, ts );
	return JNI_TRUE;
}

extern "C" JNIEXPORT jboolean JNICALL FUNC_NAME(Page_addAnnotEditbox2)( JNIEnv* env, jobject thiz, jlong page, jfloatArray rect, jint line_clr, jfloat line_w, jint fill_clr, jfloat tsize, jint text_clr )
{
	if( !page || !rect || g_active_mode < 3 ) return JNI_FALSE;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	if( !ipage->idoc->editable ) return JNI_FALSE;
	RDRECTF rc;
	jfloat *arr = env->GetFloatArrayElements( rect, JNI_FALSE );
	rc.left = arr[0];
	rc.top = arr[1];
	rc.right = arr[2];
	rc.bottom = arr[3];
	env->ReleaseFloatArrayElements(rect, arr, 0);

	ipage->idoc->doc.Page_AddEditboxAnnot( ipage->page, rc, *(RDRGBA *)&line_clr, line_w, *(RDRGBA *)&fill_clr, *(RDRGBA *)&text_clr, tsize );
	return JNI_TRUE;
}

extern "C" JNIEXPORT jboolean JNICALL FUNC_NAME(Page_addAnnotText)( JNIEnv* env, jobject thiz, jlong page, jfloatArray pt )
{
	if( !page || !pt || g_active_mode < 2 ) return JNI_FALSE;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	if( !ipage->idoc->editable ) return JNI_FALSE;
	jfloat *arr = env->GetFloatArrayElements( pt, JNI_FALSE );
	RDPOINTF point;
	point.x = arr[0];
	point.y = arr[1];
	env->ReleaseFloatArrayElements(pt, arr, 0);

	ipage->idoc->doc.Page_AddTextAnnot( ipage->page, point );
	return JNI_TRUE;
}

extern "C" JNIEXPORT jboolean JNICALL FUNC_NAME(Page_addAnnotGlyph)( JNIEnv* env, jobject thiz, jlong page, jlong matrix, jlong path, jint color, jboolean winding )
{
	if( !page || !path || !matrix || g_active_mode < 2 ) return JNI_FALSE;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	if( !ipage->idoc->editable ) return JNI_FALSE;
	RDMATRIX mat = *(RDMATRIX *)matrix;
	mat.do_invert();
	CRDPath pdf_path;
	((CRDPath *)path)->path_clone_to( pdf_path );
	pdf_path.path_transform( mat );
	ipage->idoc->doc.Page_AddGlyphAnnot( ipage->page, pdf_path, *(RDRGBA *)&color, winding );
	return JNI_TRUE;
}

extern "C" JNIEXPORT jboolean JNICALL FUNC_NAME(Page_addAnnotMarkup2)( JNIEnv* env, jobject thiz, jlong page, jint ci1, jint ci2, jint color, jint type )
{
	if( !page || ci1 < 0 || ci2 < 0 ) return false;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	PDF_DOC_INNER *idoc = ipage->idoc;
	RDI32 cnt;
	if( ci1 > ci2 )
	{
		cnt = ci1;
		ci1 = ci2;
		ci2 = cnt;
	}
	cnt = ipage->text.get_char_count();
	if( ci1 >= cnt ) ci1 = cnt - 1;
	if( ci2 >= cnt ) ci2 = cnt - 1;
	_PDF_CHAR_INFO *char1 = ipage->text.get_chars() + ci1;
	_PDF_CHAR_INFO *char2 = ipage->text.get_chars() + ci2;
	RDRECTF rc_line;
	RDRECTF rc_char;
	RDRECTF *rects = NULL;
	RDI32 rects_cnt = 0;
	RDI32 rects_max = 0;
	rc_line.init_empty_bound();
	rc_char.init_empty_bound();
	while( char1 <= char2 )
	{
		if (char1->unicode == 0x20 || char1->unicode == 0xa0 || char1->unicode == 0x3000)
		{
			char1++;
			continue;
		}
		rc_char.left = RDFIX(char1->rect.left)/100;
		rc_char.top = RDFIX(char1->rect.top)/100;
		rc_char.right = rc_char.left + RDFIX(char1->rect.get_width())/100;
		rc_char.bottom = rc_char.top + RDFIX(char1->rect.get_height())/100;
		if( rc_line.is_empty() ) rc_line = rc_char;
		RDFIX top = rc_char.top;
		RDFIX bot = rc_char.bottom;
		if( top < rc_line.top ) top = rc_line.top;
		if( bot > rc_line.bottom ) bot = rc_line.bottom;
		bot -= top;
		if( (bot > rc_char.get_height() * 4/5 || bot > rc_line.get_height() * 4/5) &&
			(rc_char.left < rc_line.right + rc_line.get_height() * 3) && (rc_char.right > rc_line.left - rc_line.get_height() * 3))
			rc_line.merge_bound( rc_char );
		else
		{
			if( rects_cnt >= rects_max )
			{
				rects_max += 4;
				rects = (RDRECTF *)RDRealloc( rects, sizeof( RDRECTF ) * rects_max );
			}
			rects[rects_cnt] = rc_line;
			rects_cnt++;
			rc_line = rc_char;
		}

		char1++;
	}
	if( rects_cnt >= rects_max )
	{
		rects_max += 4;
		rects = (RDRECTF *)RDRealloc( rects, sizeof( RDRECTF ) * rects_max );
	}
	rects[rects_cnt] = rc_line;
	rects_cnt++;

	idoc->doc.Page_AddMarkupAnnot( ipage->page, rects, rects_cnt, *(RDRGBA *)&color, type );
	RDFree( rects );
	return true;
}

extern "C" JNIEXPORT jboolean JNICALL FUNC_NAME(Page_addAnnotMarkup)( JNIEnv* env, jobject thiz, jlong page, jlong matrix, jfloatArray rects, jint color, jint type )
{
	if( !page || !rects || !matrix || g_active_mode < 2 ) return JNI_FALSE;
	PDF_PAGE_INNER *ipage = (PDF_PAGE_INNER *)page;
	if( !ipage->idoc->editable ) return JNI_FALSE;
	RDMATRIX mat = *(RDMATRIX *)matrix;
	mat.do_invert();

	RDI32 cnt = env->GetArrayLength( rects );
	if( cnt <= 0 ) return JNI_FALSE;
	RDRECTF *rcs = (RDRECTF *)RDAlloc( sizeof( RDRECTF ) * cnt/4 );
	jfloat *arr = env->GetFloatArrayElements( rects, JNI_FALSE );
	jfloat *cur = arr;
	jfloat *end = arr + cnt - 3;
	RDRECTF *rc_cur= rcs;
	while( cur < end )
	{
		rc_cur->left = cur[0];
		rc_cur->top = cur[1];
		rc_cur->right = cur[2];
		rc_cur->bottom = cur[3];
		mat.get_bound( *rc_cur );
		cur += 4;
		rc_cur++;
	}
	env->ReleaseFloatArrayElements(rects, arr, 0);
	ipage->idoc->doc.Page_AddMarkupAnnot( ipage->page, rcs, cnt/4, *(RDRGBA *)&color, type );
	RDFree( rcs );
	return JNI_TRUE;
}


extern "C" JNIEXPORT jlong FUNC_NAME(BMDatabase_openAndCreate)(JNIEnv* env, jobject thiz, jstring db_path)
{
	PDF_String path;
	path.init();
	cvt_js_to_cs( env, db_path, path );

	CBMDatabase *db = new CBMDatabase;
	if( db->BMOpen( path.m_val ) )
	{
		if( db->BMCreate( path.m_val ) )
		{
			delete db;
			return 0;
		}
	}
	path.free();
	return (jlong)db;
}

extern "C" JNIEXPORT void FUNC_NAME(BMDatabase_close)(JNIEnv* env, jobject thiz, jlong db)
{
	if( !db ) return;
	((CBMDatabase *)db)->BMClose();
	delete ((CBMDatabase *)db);
}

extern "C" JNIEXPORT jlong FUNC_NAME(BMDatabase_recOpen)(JNIEnv* env, jobject thiz, jlong db, jstring look_path)
{
	if( !db ) return 0;
	PDF_String path;
	path.init();
	cvt_js_to_cs( env, look_path, path );
	CBMRecords * rec = ((CBMDatabase *)db)->BMGetRecords( path.m_val );
	path.free();
	return (jlong)rec;
}

extern "C" JNIEXPORT void FUNC_NAME(BMDatabase_recClose)(JNIEnv* env, jobject thiz, jlong rec)
{
	if( !rec ) return;
	delete ((CBMRecords *)rec);
}

extern "C" JNIEXPORT jint FUNC_NAME(BMDatabase_recGetCount)(JNIEnv* env, jobject thiz, jlong rec)
{
	if( !rec ) return 0;
	return ((CBMRecords *)rec)->Count();
}

extern "C" JNIEXPORT jstring FUNC_NAME(BMDatabase_recItemGetName)(JNIEnv* env, jobject thiz, jlong rec, jint index)
{
	if( !rec ) return NULL;
	const CBMRecord *item = ((CBMRecords *)rec)->Get( index );
	if( item )
		return env->NewStringUTF( item->m_name );
	else
		return NULL;
}

extern "C" JNIEXPORT jint FUNC_NAME(BMDatabase_recItemGetPage)(JNIEnv* env, jobject thiz, jlong rec, jint index)
{
	if( !rec ) return -1;
	const CBMRecord *item = ((CBMRecords *)rec)->Get( index );
	if( item )
		return item->m_page;
	else
		return -1;
}

extern "C" JNIEXPORT jboolean FUNC_NAME(BMDatabase_recItemRemove)(JNIEnv* env, jobject thiz, jlong rec, jint index)
{
	if( !rec ) return JNI_FALSE;
	if( ((CBMRecords *)rec)->Remove( index ) )
		return JNI_FALSE;
	else
		return JNI_TRUE;
}

extern "C" JNIEXPORT jboolean FUNC_NAME(BMDatabase_recItemInsert)(JNIEnv* env, jobject thiz, jlong rec, jstring name, jint pageno)
{
	if( !rec ) return JNI_FALSE;
	PDF_String iname;
	iname.init();
	cvt_js_to_cs( env, name, iname );
	RDI32 ret = ((CBMRecords *)rec)->Insert( iname.m_val, pageno );
	iname.free();
	if( ret ) return JNI_FALSE;
	else return JNI_TRUE;
}
