package com.image.edit.virtual

interface OnEditImageObj : OnEditImageBase {

    /**
     * [obj1]
     * [obj2]
     * [obj3]
     * [obj4]
     * [obj5]
     * [obj6]
     */
    fun <T> findObj(obj: Any?): T? {
        @Suppress("UNCHECKED_CAST")
        return obj as T?
    }

    /**
     * 附带obj1
     */
    val obj1: Any?
        get() = Unit

    fun <T> findObj1(): T? = findObj<T>(obj1)

    /**
     * 附带obj2
     */
    val obj2: Any?
        get() = Unit

    fun <T> findObj2(): T? = findObj<T>(obj2)

    /**
     * 附带obj3
     */
    val obj3: Any?
        get() = Unit

    fun <T> findObj3(): T? = findObj<T>(obj3)

    /**
     * 附带obj4
     */
    val obj4: Any?
        get() = Unit

    fun <T> findObj4(): T? = findObj<T>(obj4)

    /**
     * 附带obj5
     */
    val obj5: Any?
        get() = Unit

    fun <T> findObj5(): T? = findObj<T>(obj5)

    /**
     * 附带obj6
     */
    val obj6: Any?
        get() = Unit

    fun <T> findObj6(): T? = findObj<T>(obj6)

}