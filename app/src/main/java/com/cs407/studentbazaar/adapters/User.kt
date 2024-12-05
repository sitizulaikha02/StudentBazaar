package com.cs407.studentbazaar.adapters

class User {
    var name: String? = null
    var email: String? = null
    var uid: String? = null

    constructor(){}

    constructor(name: String?, email: String?, uid: String?) {
        this.name = name
        this.email = email
        this.uid = uid
    }

}