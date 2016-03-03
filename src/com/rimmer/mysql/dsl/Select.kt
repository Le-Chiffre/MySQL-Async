package com.rimmer.mysql.dsl

import com.rimmer.mysql.protocol.Connection
import com.rimmer.mysql.protocol.QueryResult
import java.util.*

class Select(val set: FieldSet, val where: Op<Boolean>?): Expression(), Query {
    val groupedBy = ArrayList<Expression>()
    val orderBy = ArrayList<Pair<Expression, Boolean>>()

    var having: Op<Boolean>? = null
    var limit: Int? = null
    var offset: Int? = null
    var forUpdate: Boolean = false

    override fun format(builder: QueryBuilder) = with(builder) {
        append("SELECT ")

        set.fields.sepBy(string, ", ") {
            it.format(this)
        }

        append(" FROM ")
        set.source.format(this)

        if(where != null) {
            append(" WHERE ")
            where.format(this)
        }

        if(groupedBy.isNotEmpty()) {
            append(" GROUP BY ")
            groupedBy.sepBy(string, ", ") { it.format(this) }
        }

        if(having != null) {
            append(" HAVING ")
            having!!.format(builder)
        }

        if(orderBy.isNotEmpty()) {
            append(" ORDER BY ")
            orderBy.sepBy(builder.string, ", ") {
                it.first.format(this)
                append(if(it.second) " ASC" else " DESC")
            }
        }

        if(limit != null) {
            append(" LIMIT ")
            append(limit!!)

            if(offset != null) {
                append(" OFFSET ")
                append(offset!!)
            }
        }

        if(forUpdate) {
            append(" FOR UPDATE")
        }
    }

    override fun run(c: Connection, f: (QueryResult?, Throwable?) -> Unit) {
        val builder = QueryBuilder()
        format(builder)
        builder.run(c, f)
    }

    fun forUpdate(): Select {
        forUpdate = true
        return this
    }

    infix fun groupBy(column: Expression): Select {
        groupedBy.add(column)
        return this
    }

    fun groupBy(vararg columns: Expression): Select {
        groupedBy.addAll(columns)
        return this
    }

    infix fun orderBy(column: Expression) = orderBy(column, true)

    fun orderBy(column: Expression, ascending: Boolean): Select {
        orderBy.add(Pair(column, ascending))
        return this
    }

    fun orderBy(vararg column: Pair<Expression, Boolean>, ascending: Boolean): Select {
        orderBy.addAll(column)
        return this
    }

    infix fun limit(count: Int): Select {
        limit = count
        return this
    }

    infix fun offset(index: Int): Select {
        offset = index
        return this
    }
}