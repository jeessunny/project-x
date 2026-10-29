package com.projectx.studyhelper.data.updater

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GithubReleaseDto(
    @SerialName("tag_name")
    val tagName: String,
    @SerialName("name")
    val name: String? = null,
    @SerialName("body")
    val body: String? = null,
    @SerialName("prerelease")
    val prerelease: Boolean = false,
    @SerialName("draft")
    val draft: Boolean = false,
    @SerialName("assets")
    val assets: List<GithubAssetDto> = emptyList()
)

@Serializable
data class GithubAssetDto(
    @SerialName("name")
    val name: String,
    @SerialName("browser_download_url")
    val browserDownloadUrl: String,
    @SerialName("size")
    val size: Long = 0L,
    @SerialName("content_type")
    val contentType: String? = null
)
