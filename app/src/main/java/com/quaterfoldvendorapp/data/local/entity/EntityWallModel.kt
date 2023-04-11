package com.quaterfoldvendorapp.data.local.entity

import io.realm.RealmModel
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import io.realm.annotations.Required

@RealmClass
open class EntityWallModel : RealmModel {

    @PrimaryKey
    var _id: String = ""

    @Required
    var imageArray: String = ""
}