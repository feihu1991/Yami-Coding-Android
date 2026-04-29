package com.btelo.coding.domain.model

enum class GitFileStatus(val label: String) {
    MODIFIED("M"),
    ADDED("A"),
    DELETED("D"),
    RENAMED("R"),
    UNTRACKED("?")
}

data class GitFileChange(
    val path: String,
    val status: GitFileStatus,
    val additions: Int = 0,
    val deletions: Int = 0
)

data class GitCommit(
    val hash: String,
    val message: String,
    val author: String,
    val timestamp: Long,
    val filesChanged: Int = 0
)

data class GitStash(
    val index: Int,
    val message: String,
    val branch: String,
    val timestamp: Long
)

data class GitDiffFile(
    val path: String,
    val oldContent: String,
    val newContent: String
)
