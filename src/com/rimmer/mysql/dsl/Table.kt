package com.rimmer.mysql.dsl

import com.rimmer.mysql.protocol.decoder.*
import io.netty.buffer.ByteBuf
import org.joda.time.DateTime
import java.util.*

interface FieldSet {
    val fields: List<TypedExpression<*>>
    val source: ColumnSet
}

interface ColumnSet: FieldSet {
    val columns: List<Column<*>>
    override val fields: List<TypedExpression<*>> get() = columns
    override val source: ColumnSet get() = this

    fun format(builder: QueryBuilder)

    fun slice(vararg columns: TypedExpression<*>): FieldSet = Slice(this, columns.toList())
    fun slice(columns: List<TypedExpression<*>>): FieldSet = Slice(this, columns)
}

class Slice(override val source: ColumnSet, override val fields: List<TypedExpression<*>>): FieldSet

open class Table(name: String? = null): ColumnSet {
    val tableName = name ?: javaClass.simpleName.removeSuffix("Table")
    val quotedName = "`$tableName`"

    override val columns = ArrayList<Column<*>>()

    fun integer(name: String): Column<Int> {
        val answer = Column(this, name, intType)
        columns.add(answer)
        return answer
    }

    fun short(name: String): Column<Short> {
        val answer = Column(this, name, shortType)
        columns.add(answer)
        return answer
    }

    fun char(name: String): Column<Char> {
        val answer = Column(this, name, charType)
        columns.add(answer)
        return answer
    }

    fun float(name: String): Column<Float> {
        val answer = Column(this, name, floatType)
        columns.add(answer)
        return answer
    }

    fun double(name: String): Column<Double> {
        val answer = Column(this, name, doubleType)
        columns.add(answer)
        return answer
    }

    fun long(name: String): Column<Long> {
        val answer = Column(this, name, longType)
        columns.add(answer)
        return answer
    }

    fun date(name: String): Column<DateTime> {
        val answer = Column(this, name, dateTimeType)
        columns.add(answer)
        return answer
    }

    fun bool(name: String): Column<Boolean> {
        val answer = Column(this, name, booleanType)
        columns.add(answer)
        return answer
    }

    fun blob(name: String): Column<ByteArray> {
        val answer = Column(this, name, ByteArray::class.java)
        columns.add(answer)
        return answer
    }

    fun text(name: String): Column<String> {
        val answer = Column(this, name, stringType)
        columns.add(answer)
        return answer
    }

    fun binary(name: String): Column<ByteBuf> {
        val answer = Column(this, name, ByteBuf::class.java)
        columns.add(answer)
        return answer
    }

    inline fun <reified T: Enum<T>> enum(name: String): Column<T> {
        val answer = Column(this, name, T::class.java)
        columns.add(answer)
        return answer
    }

    fun <T:Any> Column<T>.nullable(): Column<T?> {
        val newColumn = Column(table, name, type as Class<T?>, true)
        return replaceColumn(this, newColumn)
    }

    override fun toString() = tableName

    override fun equals(other: Any?): Boolean {
        if(other !is Table) return false
        return other.tableName == tableName
    }

    override fun format(builder: QueryBuilder) { builder.append(quotedName) }
    override val fields: List<TypedExpression<*>> get() = columns

    private fun<T: Column<*>> replaceColumn(oldColumn: Column<*>, newColumn: T) : T {
        columns.remove(oldColumn)
        columns.add(newColumn)
        return newColumn
    }
}
