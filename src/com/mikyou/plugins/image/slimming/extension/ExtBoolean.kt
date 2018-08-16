package com.mikyou.plugins.image.slimming.extension

sealed class BooleanExt<out T>//data是只读的，所以是协变

object Otherwise : BooleanExt<Nothing>()//Nothing是所有类的子类，协变的类继承关系和泛型参数类型继承关系一致

class TransferData<T>(val data: T) : BooleanExt<T>()

inline fun <T> Boolean.yes(block: () -> T): BooleanExt<T> = when {
    this -> TransferData(block.invoke())
    else -> Otherwise
}

inline fun <T> Boolean.no(block: () -> T): BooleanExt<T> = when {
    this -> Otherwise
    else -> TransferData(block.invoke())
}

inline fun <T> BooleanExt<T>.otherwise(block: () -> T): T = when (this) {
    is Otherwise ->
        block()
    is TransferData ->
        this.data
}