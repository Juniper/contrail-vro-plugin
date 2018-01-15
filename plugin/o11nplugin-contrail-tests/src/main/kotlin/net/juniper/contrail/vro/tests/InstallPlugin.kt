/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests

import org.springframework.core.io.FileSystemResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate

object InstallPlugin {
    @JvmStatic fun main(args: Array<String>) {
        val path = args[0]
        val hostIp = args[1]
        val url = "https://$hostIp:8281/vco/api/plugins/"
        disableCertificateValidation()
        val parts = LinkedMultiValueMap<String, Any>()
        val r = FileSystemResource(path)
        parts.put("file", listOf(r))
        parts.put("format", listOf("dar"))
        parts.put("overwrite", listOf("true"))
        val httpHeaders = HttpHeaders()
        httpHeaders.add("Authorization", "Basic YWRtaW5pc3RyYXRvckB2c3BoZXJlLmxvY2FsOlZNd2FyZTEh")
        val stuff = HttpEntity<MultiValueMap<*, *>>(parts, httpHeaders)
        RestTemplate().postForLocation(url, stuff)
    }
}
