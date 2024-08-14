package com.gclewis

import com.google.gson.Gson
import io.javalin.Javalin
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.SQLException

fun main() {
    Javalin.create()
        .get("/{searchTerm}") { ctx ->
            val json = getData(ctx.pathParam("searchTerm"))
            if (json != null) {
                ctx.result(json)
            }
        }
        .start(3000)
}

private fun getData(searchTerm: String): String? {
    var conn: Connection? = null
    var json: String? = null

    try {
        val url = "jdbc:sqlite:chinook.db"
        conn = DriverManager.getConnection(url)
        println("Connection to SQLite has been established.")

        val sql = "SELECT name FROM tracks WHERE name LIKE '%${searchTerm}%';"

        DriverManager.getConnection(url).use {
            it.createStatement().use { stmt ->
                json = convertResultSetToJsonString(stmt.executeQuery(sql))
            }
        }
    } catch (e: SQLException) {
        println(e.message)
    } finally {
        try {
            conn?.close()
            println("Connection to SQLite has been closed.")
        } catch (ex: SQLException) {
            println(ex.message)
        }
    }

    return json
}

data class ResultData(val name: String)

private fun convertResultSetToJsonString(resultSet: ResultSet): String {
    val dataList = mutableListOf<ResultData>()
    while (resultSet.next()) {
        val data = ResultData(
            resultSet.getString("name")
        )
        dataList.add(data)
    }
    val gson = Gson()
    return gson.toJson(dataList)
}
