package dto

import enums.AttachmentType

data class Attachment(
    val url: String,
    val description: String,
    val type: AttachmentType,
)