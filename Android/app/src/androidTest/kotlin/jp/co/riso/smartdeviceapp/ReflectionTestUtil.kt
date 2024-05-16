package jp.co.riso.smartdeviceapp

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
            val field = if (obj is Class<*>) obj.getDeclaredField(fieldName)
                else obj.javaClass.getDeclaredField(fieldName)
            field.isAccessible = true
            field.set(if (obj is Class<*>) null else obj, fieldValue)
        }

        fun callMethod(obj: Any, methodName: String, vararg params: Param): Any? {
            val parameterClasses = arrayOfNulls<Class<*>>(params.size)
            for (i in params.indices) {
                parameterClasses[i] = params[i].clazz
            }
            val method = obj.javaClass.getDeclaredMethod(methodName, *parameterClasses)
            method.isAccessible = true
            val parameterValues = arrayOfNulls<Any>(params.size)
            for (i in params.indices) {
                parameterValues[i] = params[i].value
            }
            return method.invoke(obj, *parameterValues)
        }
    }
}