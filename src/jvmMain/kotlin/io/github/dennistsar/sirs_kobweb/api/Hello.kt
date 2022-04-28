package io.github.dennistsar.sirs_kobweb.api

import com.varabyte.kobweb.api.Api
import com.varabyte.kobweb.api.ApiContext
import com.varabyte.kobweb.api.data.getValue
import com.varabyte.kobweb.api.http.readBodyText
import com.varabyte.kobweb.api.http.setBodyText

@Api
fun hello(ctx: ApiContext) {
    ctx.res.setBodyText(ctx.req.readBodyText()?:"mo")
}