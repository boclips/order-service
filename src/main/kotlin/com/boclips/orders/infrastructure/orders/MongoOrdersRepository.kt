package com.boclips.orders.infrastructure.orders

import com.boclips.orders.domain.exceptions.OrderItemNotFoundException
import com.boclips.orders.domain.exceptions.OrderNotFoundException
import com.boclips.orders.domain.model.Order
import com.boclips.orders.domain.model.OrderFilter
import com.boclips.orders.domain.model.OrderId
import com.boclips.orders.domain.model.OrderUpdateCommand
import com.boclips.orders.domain.model.OrdersRepository
import com.boclips.orders.domain.model.Price
import com.boclips.orders.infrastructure.orders.converters.OrderDocumentConverter
import com.boclips.orders.infrastructure.orders.converters.OrderItemDocumentConverter
import com.mongodb.MongoClient
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.UpdateOneModel
import mu.KLogging
import org.bson.conversions.Bson
import org.bson.types.ObjectId
import org.litote.kmongo.SetTo
import org.litote.kmongo.`in`
import org.litote.kmongo.combine
import org.litote.kmongo.deleteMany
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection
import org.litote.kmongo.orderBy
import org.litote.kmongo.set
import java.time.Instant

const val databaseName = "order-service-db"

class MongoOrdersRepository(private val mongoClient: MongoClient) : OrdersRepository {

    companion object : KLogging() {
        const val collectionName = "orders"
    }

    override fun save(order: Order) =
        collection()
            .insertOne(
                OrderDocumentConverter.toOrderDocument(order)
            ).let { this.findOne(order.id) } ?: throw IllegalStateException("Could not save order")

    override fun deleteAll() {
        collection().deleteMany()
    }

    override fun findAll(): List<Order> =
        collection()
            .find()
            .sort(orderBy(OrderDocument::createdAt, ascending = false))
            .map(OrderDocumentConverter::toOrder)
            .toList()

    override fun findOne(id: OrderId): Order? =
        id.value
            .takeIf { ObjectId.isValid(it) }
            ?.let { collection().findOne(OrderDocument::id eq ObjectId(it)) }
            ?.let(OrderDocumentConverter::toOrder)

    override fun findOneByLegacyId(legacyOrderId: String): Order? {
        return collection().findOne(OrderDocument::legacyOrderId eq legacyOrderId)?.let(
            OrderDocumentConverter::toOrder
        )
    }

    override fun update(orderUpdateCommand: OrderUpdateCommand): Order {
        if (!ObjectId.isValid(orderUpdateCommand.orderId.value)) {
            throw OrderNotFoundException(orderUpdateCommand.orderId)
        }

        convertUpdateToBson(orderUpdateCommand)?.let { updateBson ->
            collection().updateOne(
                OrderDocument::id eq ObjectId(orderUpdateCommand.orderId.value),
                combine(
                    updateBson,
                    set(OrderDocument::updatedAt, Instant.now())
                )
            )
        }


        return findOne(orderUpdateCommand.orderId) ?: throw OrderNotFoundException(
            orderUpdateCommand.orderId
        )
    }

    override fun bulkUpdate(orderUpdateCommands: List<OrderUpdateCommand>) {
        val updateDocs = orderUpdateCommands.mapNotNull {
            convertUpdateToBson(it)?.let { updateBson ->
                UpdateOneModel<OrderDocument>(
                    OrderDocument::id eq ObjectId(it.orderId.value),
                    combine(
                        updateBson,
                        set(OrderDocument::updatedAt, Instant.now())
                    )
                )
            }
        }

        val result = collection().bulkWrite(updateDocs)
        logger.info("Updated videos: modified: ${result.modifiedCount}, deleted: ${result.deletedCount}, inserted: ${result.insertedCount}")
    }

    override fun streamAll(filter: OrderFilter, consumer: (orders: Sequence<Order>) -> Unit) {
        val filterBson = when (filter) {
            is OrderFilter.HasStatus -> OrderDocument::status `in` filter.status.map { it.name }
        }

        val sequence = Sequence {
            collection()
                .find(filterBson)
                .noCursorTimeout(true)
                .iterator()
        }
            .mapNotNull(OrderDocumentConverter::toOrder)

        consumer(sequence)
    }

    private fun convertUpdateToBson(orderUpdateCommand: OrderUpdateCommand): Bson? {
        return when (orderUpdateCommand) {
            is OrderUpdateCommand.ReplaceStatus ->
                set(OrderDocument::status, orderUpdateCommand.orderStatus.toString())
            is OrderUpdateCommand.UpdateOrderCurrency ->
                set(
                    SetTo(OrderDocument::currency, orderUpdateCommand.currency),
                    SetTo(OrderDocument::fxRateToGbp, orderUpdateCommand.fxRateToGbp)
                )
            is OrderUpdateCommand.OrderItemUpdateCommand -> findOne(orderUpdateCommand.orderId)?.let {
                convertItemsUpdateToBson(retrievedOrder = it, updateCommand = orderUpdateCommand)
            }

        }
    }

    private fun convertItemsUpdateToBson(
        retrievedOrder: Order,
        updateCommand: OrderUpdateCommand.OrderItemUpdateCommand
    ): Bson {
        if (retrievedOrder.items.firstOrNull { it.id == updateCommand.orderItemsId } == null) {
            throw OrderItemNotFoundException(orderId = retrievedOrder.id, orderItemId = updateCommand.orderItemsId)
        }

        val updatedItems = retrievedOrder.items.map { orderItem ->
            if (orderItem.id == updateCommand.orderItemsId) {
                when (updateCommand) {
                    is OrderUpdateCommand.OrderItemUpdateCommand.UpdateOrderItemPrice -> orderItem.copy(
                        price = Price(
                            amount = updateCommand.amount,
                            currency = orderItem.price.currency
                        )
                    )
                    is OrderUpdateCommand.OrderItemUpdateCommand.UpdateOrderItemLicense -> orderItem.copy(license = updateCommand.orderItemLicense)
                    is OrderUpdateCommand.OrderItemUpdateCommand.ReplaceVideo -> orderItem.copy(video = updateCommand.video)
                    is OrderUpdateCommand.OrderItemUpdateCommand.UpdateCaptionStatus -> orderItem.copy(
                        video = orderItem.video.copy(
                            captionStatus = updateCommand.captionStatus
                        )
                    )
                }
            } else {
                orderItem
            }
        }

        return set(OrderDocument::items, updatedItems.map { OrderItemDocumentConverter.toOrderItemDocument(it) })
    }

    private fun collection(): MongoCollection<OrderDocument> =
        mongoClient.getDatabase(databaseName).getCollection<OrderDocument>(collectionName)
}
