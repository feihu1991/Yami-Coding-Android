package com.btelo.coding.domain.model

data class SkillTag(
    val id: String,
    val name: String,
    val type: SkillTagType,
    val path: String? = null
)

enum class SkillTagType {
    PATH, FEATURE
}
