package com.plcoding.spotifycloneyt.other


// 많이 사용되는 event class (wrapper class)
// 목적 : 한 번만 소비될 수 있는 이벤트를 표현하도록 설계.
// 이벤트와 관련된 작업이 한 번만 트리거 되고 다시 가져오려고 하면 null 반환
// peak는 hasBeenHandled를 변경하지 않고 읽기 전용 뷰를 제공한다

open class Event<out T>(private val data: T) {

    var hasBeenHandled = false
        private set

    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            data
        }
    }

    fun peekContent() = data
}