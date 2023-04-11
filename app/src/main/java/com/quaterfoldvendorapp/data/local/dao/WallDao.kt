package com.quaterfoldvendorapp.data.local.dao

import com.quaterfoldvendorapp.data.local.entity.EntityWallModel
import io.realm.Realm
import io.realm.kotlin.deleteFromRealm

object WallDao {

    fun insertWallImages(
        id: String,
        imageArray: String
    ) {
        val realm = Realm.getDefaultInstance()
        try {
            realm.executeTransaction {
                val wall = it.createObject(EntityWallModel::class.java, id)
                wall.imageArray = imageArray
                it.insertOrUpdate(wall)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (realm?.isClosed == false) {
                realm.close()
            }
        }
    }

    fun getAll(): List<EntityWallModel>? {
        val realm = Realm.getDefaultInstance()
        try {
            val data = mutableListOf<EntityWallModel>()
            realm.where(EntityWallModel::class.java)
                .findAll()
                ?.mapNotNull {
                    it?.let {
                        EntityWallModel().apply {
                            _id = it._id
                            imageArray = it.imageArray
                        }
                    }
                }
            return data
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (realm?.isClosed == false) {
                realm.close()
            }
        }
        return null
    }

    fun deleteWallImages(
        id: String
    ) {
        val realm = Realm.getDefaultInstance()
        try {
            realm.where(EntityWallModel::class.java)
                .containsValue("_id", id)
                .findFirst()
                ?.deleteFromRealm()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (realm?.isClosed == false) {
                realm.close()
            }
        }
    }

}