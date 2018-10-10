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

fun authorizationHttpHeader(): HttpEntity<Any> {
    val httpHeaders = HttpHeaders()
    httpHeaders.add("Authorization", "Basic YWRtaW5pc3RyYXRvckB2c3BoZXJlLmxvY2FsOkFiY2QxMjM0IQ==")
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
        throw IllegalArgumentException("Orchestrator hostname must be passed as the first argument.")
    }
    disableCertificateValidation()
    val url = "https://${args[0]}:8281"
    println(deleteContrailWorkflowsCategory(url))
    println(deleteContrailActionsCategory(url))
}