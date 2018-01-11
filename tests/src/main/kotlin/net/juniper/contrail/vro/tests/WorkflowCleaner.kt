/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import java.lang.Exception
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

fun disableCertificateValidation() {
    // Create a trust manager that does not validate certificate chains
    val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
        override fun getAcceptedIssuers(): Array<X509Certificate?> = arrayOfNulls(0)
        override fun checkClientTrusted(certs: Array<X509Certificate>, authType: String) = Unit
        override fun checkServerTrusted(certs: Array<X509Certificate>, authType: String) = Unit
    })
    // Ignore differences between given hostname and certificate hostname
    val hv = HostnameVerifier { _, _ -> true }
    // Install the all-trusting trust manager
    try {
        val sc = SSLContext.getInstance("SSL")
        sc.init(null, trustAllCerts, SecureRandom())
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.socketFactory)
        HttpsURLConnection.setDefaultHostnameVerifier(hv)
    } catch (e: Exception) {
    }
}

fun authorizationHttpHeader(): HttpEntity<Any> {
    val httpHeaders = HttpHeaders()
    httpHeaders.add("Authorization", "Basic YWRtaW5pc3RyYXRvckB2c3BoZXJlLmxvY2FsOlZNd2FyZTEh")
    return HttpEntity(httpHeaders)
}

fun deleteContrailActionsCategory(orchestratorUrl: String): ResponseEntity<String>? {
    val contrailId = getContrailActionsCategoryId(orchestratorUrl) ?: return null
    println("Contrail action category ID: $contrailId")
    return deleteCategory(orchestratorUrl, contrailId)
}

fun deleteContrailWorkflowsCategory(orchestratorUrl: String): ResponseEntity<String>? {
    val contrailId = getContrailWorkflowsCategoryId(orchestratorUrl) ?: return null
    println("Contrail workflows category ID: $contrailId")
    return deleteCategory(orchestratorUrl, contrailId)
}

fun deleteCategory(orchestratorUrl: String, categoryId: String): ResponseEntity<String> {
    val url = "$orchestratorUrl/vco/api/categories/$categoryId?deleteNonEmptyContent=true"
    val headers = authorizationHttpHeader()
    return RestTemplate().exchange<String>(url, HttpMethod.DELETE, headers, String::class.java)
}

fun listCategories(orchestratorUrl: String): ResponseEntity<String> {
    val url = "$orchestratorUrl/vco/api/categories"
    val headers = authorizationHttpHeader()
    return RestTemplate().exchange<String>(url, HttpMethod.GET, headers, String::class.java)
}

fun getContrailActionsCategoryId(orchestratorUrl: String): String? =
    getMatchingCategoryId(orchestratorUrl) {
        isContrailActionsCategory()
    }

fun getContrailWorkflowsCategoryId(orchestratorUrl: String): String? =
    getMatchingCategoryId(orchestratorUrl) {
        isContrailWorkflowsCategory()
    }

fun getMatchingCategoryId(orchestratorUrl: String, categoryFilter: JsonObject.() -> Boolean): String? {
    val categoriesString = listCategories(orchestratorUrl).body
    val parser = JsonParser()
    val json = parser.parse(categoriesString)
    return json.asJsonObject.get("link").asJsonArray.map { it.asJsonObject }.find {
        it.categoryFilter()
    }?.getCategoryId()
}

fun JsonObject.isContrailWorkflowsCategory(): Boolean =
    categoryName.equals("Contrail")

fun JsonObject.isContrailActionsCategory(): Boolean =
    categoryName.equals("net.juniper.contrail")

val JsonObject.categoryName get(): String? {
    return get("attributes").asJsonArray
        .find { it.asJsonObject.get("name").asString == "name" }
        ?.asJsonObject?.get("value")?.asString
}

fun JsonObject.getCategoryId(): String? {
    return get("attributes").asJsonArray
        .find { it.asJsonObject.get("name").asString == "id" }
        ?.asJsonObject?.get("value")?.asString
}

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        throw IllegalArgumentException("Orchestrator hostname must be passes as the first argument.")
    }
    disableCertificateValidation()
    val url = "https://${args[0]}:8281"
    println(deleteContrailWorkflowsCategory(url))
    println(deleteContrailActionsCategory(url))
}