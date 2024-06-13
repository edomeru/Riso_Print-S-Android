package jp.co.riso.smartdeviceapp

import java.lang.reflect.Method

/**
 * Utility class for Java Reflection
 * Access private fields and members for testing
 */
class ReflectionTestUtil {
    class Param(var clazz: Class<*>, var value: Any?)

    companion object {
        fun getField(obj: Any, fieldName: String): Any? {
            val field = obj.javaClass.getDeclaredField(fieldName)
            field.isAccessible = true
            return field.get(obj)
        }

        fun setField(obj: Any, fieldName: String, fieldValue: Any?) {
            var clazz: Class<*>? = if (obj is Class<*>) obj else obj.javaClass
            while (clazz != null) {
                try {
                    val field = clazz.getDeclaredField(fieldName)
                    field.isAccessible = true
                    field.set(if (obj is Class<*>) null else obj, fieldValue)
                    break
                } catch (e: NoSuchFieldException) {
                    clazz = clazz.superclass
                }
            }
        }

        fun callMethod(clazz: Class<*>, obj: Any, methodName: String, vararg params: Param): Any? {
            val parameterClasses = getParameterClasses(*params)
            val method = clazz.getDeclaredMethod(methodName, *parameterClasses)
            return invokeMethod(obj, method, *params)
        }

        fun callMethod(obj: Any, methodName: String, vararg params: Param): Any? {
            val parameterClasses = getParameterClasses(*params)
            val method = obj.javaClass.getDeclaredMethod(methodName, *parameterClasses)
            return invokeMethod(obj, method, *params)
        }

        private fun invokeMethod(obj: Any, method: Method, vararg params: Param): Any? {
            method.isAccessible = true
            val parameterValues = arrayOfNulls<Any>(params.size)
            for (i in params.indices) {
                parameterValues[i] = params[i].value
            }
            return method.invoke(obj, *parameterValues)
        }

        private fun getParameterClasses(vararg params: Param): Array<Class<*>?> {
            val parameterClasses = arrayOfNulls<Class<*>>(params.size)
            for (i in params.indices) {
                parameterClasses[i] = params[i].clazz
            }
            return parameterClasses
        }
    }
}