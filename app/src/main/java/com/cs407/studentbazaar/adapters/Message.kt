package com.cs407.studentbazaar.adapters

class Message{
    var messageId: String? = null
    var message: String? = null
    var senderId: String? = null
    constructor(){}

    constructor(messageId: String?, message: String?, senderId: String?){
        this.messageId = messageId
        this.message = message
        this.senderId = senderId
    }
}
