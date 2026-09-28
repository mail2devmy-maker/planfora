package com.mail2dev.planfora.data.local

import androidx.room.TypeConverter
import com.mail2dev.planfora.data.local.entity.CustomFieldType
import com.mail2dev.planfora.data.local.entity.FieldTargetType

class RoomTypeConverters {
    @TypeConverter
    fun fromFieldTargetType(value: FieldTargetType): String = value.name

    @TypeConverter
    fun toFieldTargetType(value: String): FieldTargetType = FieldTargetType.valueOf(value)

    @TypeConverter
    fun fromCustomFieldType(value: CustomFieldType): String = value.name

    @TypeConverter
    fun toCustomFieldType(value: String): CustomFieldType = CustomFieldType.valueOf(value)
}
