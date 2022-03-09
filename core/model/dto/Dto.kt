package com.dev.podo.core.model.dto

interface Dto<T> {
    fun asEntity(): T
}

fun <T> List<Dto<T>>.asEntity(): List<T> {
    val arrayList = arrayListOf<T>()
    forEach {
        arrayList.add(it.asEntity())
    }
    return arrayList.toList()
}

fun <T> List<Dto<T>>.asEntityArray(): ArrayList<T> {
    val arrayList = arrayListOf<T>()
    forEach {
        arrayList.add(it.asEntity())
    }
    return arrayList
}