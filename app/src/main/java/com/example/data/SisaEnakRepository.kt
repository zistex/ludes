package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class SisaEnakRepository(
    private val database: SisaEnakDatabase,
    private val prefsRepo: UserPreferencesRepository
) {
    private val productDao = database.productDao()
    private val favoriteStoreDao = database.favoriteStoreDao()
    private val orderDao = database.orderDao()
    private val viewedHistoryDao = database.viewedHistoryDao()
    private val withdrawDao = database.withdrawDao()
    private val productReviewDao = database.productReviewDao()
    private val patunganDao = database.patunganDao()

    val products: Flow<List<ProductEntity>> = productDao.observeAllProducts()
    val liveProducts: Flow<List<ProductEntity>> = productDao.observeLiveProducts()
    val favoriteProducts: Flow<List<ProductEntity>> = productDao.observeFavoriteProducts()
    val favoriteStores: Flow<List<FavoriteStoreEntity>> = favoriteStoreDao.observeFavoriteStores()
    val orders: Flow<List<OrderEntity>> = orderDao.observeAllOrders()
    val viewedHistory: Flow<List<ViewedHistoryEntity>> = viewedHistoryDao.observeHistory()
    val withdrawRequests: Flow<List<WithdrawRequestEntity>> = withdrawDao.observeAllWithdraws()
    val productReviews: Flow<List<ProductReviewEntity>> = productReviewDao.observeAllReviews()
    val patunganGroups: Flow<List<PatunganGroupEntity>> = patunganDao.observeAllPatungan()
    val userPrefs: Flow<UserProfilePrefs> = prefsRepo.userPrefs

    suspend fun initialize() {
        SisaEnakDatabase.ensureSeeded(database)
    }

    suspend fun registerAndVerifyUser(name: String, email: String, password: String) {
        prefsRepo.registerAndVerifyUser(name, email, password)
    }

    suspend fun loginWithEmail(email: String, password: String): Pair<Boolean, String> {
        return prefsRepo.loginWithEmail(email, password)
    }

    suspend fun loginDemoSiti() {
        prefsRepo.loginDemoSiti()
    }

    suspend fun logout() {
        prefsRepo.logout()
    }

    suspend fun toggleProductFavorite(productId: String) {
        val product = productDao.getProductById(productId) ?: return
        val newFav = !product.isFavorite
        val delta = if (newFav) 1 else -1
        productDao.updateFavoriteWithCount(productId, newFav, delta)
    }

    suspend fun removeFavoriteStore(storeId: String) {
        favoriteStoreDao.setStoreFavorite(storeId, false)
    }

    suspend fun recordProductViewed(productId: String) {
        productDao.incrementViewCount(productId)
        viewedHistoryDao.upsert(
            ViewedHistoryEntity(
                productId = productId,
                viewedLabel = "Baru saja dilihat",
                viewedAtMillis = System.currentTimeMillis()
            )
        )
    }

    suspend fun updateLocation(location: String, lat: Double, lng: Double) {
        prefsRepo.updateLocation(location, lat, lng)
    }

    suspend fun updateNotificationHour(hour: String) {
        prefsRepo.updateNotificationHour(hour)
    }

    suspend fun updateNotificationSettings(
        promoEnabled: Boolean,
        orderEnabled: Boolean,
        liveEnabled: Boolean,
        walletEnabled: Boolean,
        hour: String
    ) {
        prefsRepo.updateNotificationSettings(
            promoEnabled = promoEnabled,
            orderEnabled = orderEnabled,
            liveEnabled = liveEnabled,
            walletEnabled = walletEnabled,
            hour = hour
        )
    }

    suspend fun updateAppLanguage(languageCode: String) {
        prefsRepo.updateAppLanguage(languageCode)
    }

    suspend fun updateWithdrawAccount(
        bankName: String,
        accountNumber: String,
        accountHolder: String
    ) {
        prefsRepo.updateWithdrawAccount(bankName, accountNumber, accountHolder)
    }

    suspend fun clearWithdrawAccount() {
        prefsRepo.clearWithdrawAccount()
    }

    suspend fun topUpSaldo(amount: Int) {
        prefsRepo.adjustWallet(amount)
    }

    suspend fun requestWithdraw(amountRp: Int, destinationLabel: String): WithdrawRequestEntity? {
        val currentBalance = prefsRepo.userPrefs.first().walletBalanceRp
        if (amountRp < 10000 || currentBalance < amountRp) return null
        prefsRepo.adjustWallet(-amountRp)
        val req = WithdrawRequestEntity(
            id = "WD-${(1000..9999).random()}",
            amountRp = amountRp,
            destinationLabel = destinationLabel,
            status = "Diproses",
            createdAtMillis = System.currentTimeMillis()
        )
        withdrawDao.upsert(req)
        return req
    }

    suspend fun markWithdrawCompleted(id: String) {
        withdrawDao.updateStatus(id, "Berhasil Dicairkan")
    }

    suspend fun placePickupOrder(
        product: ProductEntity,
        pickupSlot: String,
        paymentMethod: PaymentMethod,
        fromLive: Boolean
    ): OrderEntity {
        val serviceFee = 1000
        val total = product.discountPrice + serviceFee

        if (paymentMethod == PaymentMethod.SALDO_SISA_ENAK) {
            val currentWallet = prefsRepo.userPrefs.first().walletBalanceRp
            if (currentWallet >= total) {
                prefsRepo.adjustWallet(-total)
            }
        }

        productDao.adjustStock(product.id, -1)

        val randomCode = (100000..999999).random()
        val orderId = if (product.id == "prod_bakery_dago" && !fromLive) {
            "#SE102938"
        } else {
            "#SE$randomCode"
        }

        val uniqueOrder = if (orderDao.getOrderById(orderId) != null) {
            "#SE${(100000..999999).random()}"
        } else {
            orderId
        }

        val buyerName = prefsRepo.userPrefs.first().userName.ifBlank { "Siti Rahma" }

        val order = OrderEntity(
            id = uniqueOrder,
            productId = product.id,
            productTitle = product.title,
            storeName = product.storeName,
            storeAddress = product.storeAddress,
            distanceText = product.distanceText,
            imageKey = product.imageKey,
            originalPrice = product.originalPrice,
            bagPrice = product.discountPrice,
            serviceFee = serviceFee,
            totalPaid = total,
            paymentMethod = paymentMethod.label,
            pickupSlot = pickupSlot,
            status = OrderStatus.BELUM_DIAMBIL.name,
            isReadyForPickup = false,
            merchantAccepted = false,
            buyerName = buyerName,
            createdAtMillis = System.currentTimeMillis(),
            fromLive = fromLive
        )
        orderDao.upsert(order)
        return order
    }

    suspend fun advanceOrderToReady(orderId: String) {
        orderDao.setMerchantAccepted(orderId, accepted = true, ready = true)
    }

    suspend fun confirmPickupCompleted(orderId: String) {
        val order = orderDao.getOrderById(orderId) ?: return
        if (order.status != OrderStatus.SUDAH_DIAMBIL.name) {
            orderDao.updateOrderStatus(orderId, OrderStatus.SUDAH_DIAMBIL.name, ready = true)
            val saved = (order.originalPrice - order.bagPrice).coerceAtLeast(5000)
            prefsRepo.recordBagRescued(saved)
        }
    }

    /**
     * Aturan Batal Otomatis:
     * Jika user tidak ambil sampai 15 menit lewat dari slot jam yang dipilih,
     * pesanan otomatis batal, uang kembali jadi Saldo LUDES, kantong muncul lagi di Beranda.
     */
    suspend fun triggerAutoCancelExpiredOrder(orderId: String): Int {
        val order = orderDao.getOrderById(orderId) ?: return 0
        if (order.status != OrderStatus.BELUM_DIAMBIL.name) return 0
        orderDao.updateOrderStatus(orderId, OrderStatus.BATAL.name, ready = false)
        productDao.adjustStock(order.productId, +1)
        prefsRepo.adjustWallet(order.totalPaid)
        return order.totalPaid
    }

    suspend fun submitOrderRating(orderId: String, rating: Int, comment: String) {
        val cleanComment = comment.trim()
        orderDao.submitRating(orderId, rating, cleanComment)
        val order = orderDao.getOrderById(orderId) ?: return
        productReviewDao.upsert(
            ProductReviewEntity(
                id = "rev_order_$orderId",
                productId = order.productId,
                reviewerName = order.buyerName.ifBlank { "Pembeli LUDES" },
                rating = rating.coerceIn(1, 5),
                comment = cleanComment.ifEmpty { "Makanan masih segar dan enak banget!" },
                timeLabel = "Baru saja",
                createdAtMillis = System.currentTimeMillis()
            )
        )
    }

    suspend fun publishMerchantListing(
        category: FoodCategory,
        price: Int,
        stock: Int,
        pickupWindow: String,
        customImageUri: String?
    ): ProductEntity {
        val imageKey = when (category) {
            FoodCategory.ROTI -> "bakery"
            FoodCategory.NASI -> "nasi"
            FoodCategory.JAJANAN -> "jajanan"
            FoodCategory.BUAH -> "buah"
            FoodCategory.LAINNYA -> "lainnya"
        }
        val title = when (category) {
            FoodCategory.ROTI -> "Kantong Kejutan Roti Warung"
            FoodCategory.NASI -> "Paket Nasi & Lauk Hemat Malam"
            FoodCategory.JAJANAN -> "Kantong Jajanan & Kue Segar"
            FoodCategory.BUAH -> "Kotak Buah Potong Segar"
            FoodCategory.LAINNYA -> "Paket Kuliner & Minuman Hemat"
        }
        val slots = when (pickupWindow) {
            "18.00-20.00" -> "18.00 - 18.30,18.30 - 19.00,19.00 - 19.30"
            "20.00-22.00" -> "20.00 - 20.30,20.30 - 21.00,21.00 - 21.30"
            else -> "19.00 - 19.30,19.30 - 20.00,20.00 - 20.30"
        }
        val item = ProductEntity(
            id = "merchant_${System.currentTimeMillis()}",
            title = title,
            storeName = "Warung Berkah Ibu Siti",
            storeAddress = "Jl. Dago Pojok No. 14",
            distanceText = "0.3 km",
            category = category.name,
            originalPrice = price * 2,
            discountPrice = price,
            stock = stock,
            pickupWindow = pickupWindow,
            pickupSlotsCsv = slots,
            rating = 5.0,
            description = "Makanan sisa layak konsumsi hari ini dari Warung Berkah Ibu Siti. Segar, bersih, hanya ambil sendiri di warung.",
            imageKey = imageKey,
            customImageUri = customImageUri,
            isFavorite = false,
            isLive = false,
            isMerchantPublished = true
        )
        productDao.upsert(item)
        return item
    }

    suspend fun acceptMerchantOrder(orderId: String) {
        orderDao.setMerchantAccepted(orderId, accepted = true, ready = true)
    }

    suspend fun rejectMerchantOrder(orderId: String) {
        val order = orderDao.getOrderById(orderId) ?: return
        if (order.status == OrderStatus.BELUM_DIAMBIL.name) {
            orderDao.updateOrderStatus(orderId, OrderStatus.BATAL.name, ready = false)
            productDao.adjustStock(order.productId, +1)
            prefsRepo.adjustWallet(order.totalPaid)
        }
    }

    suspend fun getOrCreatePatunganForProduct(product: ProductEntity): PatunganGroupEntity {
        val existing = patunganDao.getByProductId(product.id)
        if (existing != null) return existing

        val area = resolvePatunganAreaForProduct(product)
        val firstSlot = product.pickupSlotsCsv.split(",").firstOrNull()?.trim().orEmpty()
            .ifBlank { "19.00 - 19.30" }
        val newGroup = PatunganGroupEntity(
            id = "patungan_${product.id}",
            productId = product.id,
            productTitle = product.title,
            storeName = product.storeName,
            storeAddress = product.storeAddress,
            imageKey = product.imageKey,
            bagPriceRp = product.discountPrice,
            areaName = area.areaName,
            pickupSlot = firstSlot,
            cutoffHour = "19.00",
            departHour = "19.30",
            membersJson = serializePatunganMembers(
                listOf(
                    PatunganMember(
                        slotIndex = 1,
                        aliasName = "Siti",
                        areaDetail = area.subZones.firstOrNull() ?: area.areaName,
                        addressLabel = "${area.subZones.firstOrNull() ?: area.areaName} No. 12",
                        distanceKmFromStore = 0.42,
                        paidAmountRp = product.discountPrice + 3000
                    )
                )
            ),
            status = PatunganStatus.OPEN.name
        )
        patunganDao.upsert(newGroup)
        return newGroup
    }

    suspend fun joinPatungan(
        product: ProductEntity,
        groupId: String,
        userAreaDetail: String,
        userAddressLabel: String,
        paymentOption: PatunganPaymentOption
    ): PatunganGroupEntity? {
        val group = patunganDao.getById(groupId) ?: getOrCreatePatunganForProduct(product)
        val currentMembers = group.members.toMutableList()

        if (group.userJoined && currentMembers.any { it.isCurrentUser }) {
            return group
        }
        if (currentMembers.size >= group.maxSlots || group.status != PatunganStatus.OPEN.name) {
            return null
        }

        val totalPay = group.bagPriceRp + group.patunganShippingRp
        if (paymentOption == PatunganPaymentOption.SALDO_LUDES) {
            val currentWallet = prefsRepo.userPrefs.first().walletBalanceRp
            if (currentWallet < totalPay) return null
            prefsRepo.adjustWallet(-totalPay)
        }

        val nextSlot = currentMembers.size + 1
        val userDist = (0.45 + nextSlot * 0.12).coerceAtMost(0.95)
        currentMembers += PatunganMember(
            slotIndex = nextSlot,
            aliasName = "Kamu",
            areaDetail = userAreaDetail.ifBlank { group.areaName },
            addressLabel = userAddressLabel.ifBlank { "$userAreaDetail No. 15" },
            distanceKmFromStore = userDist,
            isCurrentUser = true,
            isDelivered = false,
            paidAmountRp = totalPay
        )

        productDao.adjustStock(product.id, -1)

        val isNowFull = currentMembers.size >= group.maxSlots
        val updated = group.copy(
            membersJson = serializePatunganMembers(currentMembers),
            status = if (isNowFull) PatunganStatus.BERANGKAT.name else PatunganStatus.OPEN.name,
            userJoined = true,
            userPaymentMethod = paymentOption.label,
            userTotalPaidRp = totalPay,
            fundsHeldInEscrow = true,
            driverStage = if (isNowFull) DriverStage.MENUJU_TOKO.name else group.driverStage,
            etaMinutes = if (isNowFull) 10 else group.etaMinutes
        )
        patunganDao.upsert(updated)
        return updated
    }

    suspend fun joinSoloFullDelivery(
        product: ProductEntity,
        userAreaDetail: String,
        userAddressLabel: String,
        paymentOption: PatunganPaymentOption
    ): PatunganGroupEntity? {
        val area = resolvePatunganAreaForProduct(product)
        val normalShipping = 15000
        val totalPay = product.discountPrice + normalShipping
        if (paymentOption == PatunganPaymentOption.SALDO_LUDES) {
            val currentWallet = prefsRepo.userPrefs.first().walletBalanceRp
            if (currentWallet < totalPay) return null
            prefsRepo.adjustWallet(-totalPay)
        }

        val userName = prefsRepo.userPrefs.first().userName.ifBlank { "Kamu" }
        val soloZone = userAreaDetail.ifBlank { area.subZones.firstOrNull() ?: area.areaName }
        val soloAddress = userAddressLabel.ifBlank { "$soloZone No. 15" }
        val firstSlot = product.pickupSlotsCsv.split(",").firstOrNull()?.trim().orEmpty()
            .ifBlank { "19.00 - 19.30" }

        val stamp = System.currentTimeMillis()
        val soloGroup = PatunganGroupEntity(
            id = "solo_${product.id}_$stamp",
            productId = "${product.id}__solo_$stamp",
            productTitle = product.title,
            storeName = product.storeName,
            storeAddress = product.storeAddress,
            imageKey = product.imageKey,
            bagPriceRp = product.discountPrice,
            patunganShippingRp = normalShipping,
            normalShippingRp = normalShipping,
            areaName = area.areaName,
            pickupSlot = firstSlot,
            cutoffHour = "Langsung",
            departHour = "Sekarang",
            minSlotsToDepart = 1,
            maxSlots = 1,
            membersJson = serializePatunganMembers(
                listOf(
                    PatunganMember(
                        slotIndex = 1,
                        aliasName = userName,
                        areaDetail = soloZone,
                        addressLabel = soloAddress,
                        distanceKmFromStore = 0.65,
                        isCurrentUser = true,
                        isDelivered = false,
                        paidAmountRp = totalPay
                    )
                )
            ),
            status = PatunganStatus.BERANGKAT.name,
            userJoined = true,
            userPaymentMethod = paymentOption.label,
            userTotalPaidRp = totalPay,
            fundsHeldInEscrow = true,
            driverName = "Pak Agus",
            driverPlate = "Motor B 1234 XYZ",
            driverRating = 4.9,
            driverStage = DriverStage.MENUJU_TOKO.name,
            hasPickedUpAtStore = false,
            etaMinutes = 12,
            isSoloFallbackDelivery = true
        )

        productDao.adjustStock(product.id, -1)
        patunganDao.upsert(soloGroup)
        return soloGroup
    }

    suspend fun cancelPatungan(groupId: String): Int {
        val group = patunganDao.getById(groupId) ?: return 0
        if (!group.userJoined) return 0

        val refund = group.userTotalPaidRp.takeIf { it > 0 } ?: (group.bagPriceRp + group.patunganShippingRp)
        prefsRepo.adjustWallet(refund)
        productDao.adjustStock(group.productId, +1)

        val remainingMembers = group.members.filterNot { it.isCurrentUser }
            .mapIndexed { idx, m -> m.copy(slotIndex = idx + 1) }

        val updated = group.copy(
            membersJson = serializePatunganMembers(remainingMembers),
            status = PatunganStatus.OPEN.name,
            userJoined = false,
            fundsHeldInEscrow = false,
            userTotalPaidRp = 0
        )
        patunganDao.upsert(updated)
        return refund
    }

    suspend fun simulateNeighborJoin(groupId: String): PatunganGroupEntity? {
        val group = patunganDao.getById(groupId) ?: return null
        val members = group.members.toMutableList()
        if (members.size >= group.maxSlots) return group

        val area = PatunganArea.entries.firstOrNull { it.areaName == group.areaName } ?: PatunganArea.DAGO
        val aliasPool = listOf("Rina", "Dian", "Tono", "Nadia", "Fajar")
        val nextSlot = members.size + 1
        val neighborName = aliasPool[(nextSlot - 1) % aliasPool.size]
        val subZone = area.subZones[(nextSlot - 1) % area.subZones.size]
        members += PatunganMember(
            slotIndex = nextSlot,
            aliasName = neighborName,
            areaDetail = subZone,
            addressLabel = "$subZone No. ${10 + nextSlot * 3}",
            distanceKmFromStore = (0.35 + nextSlot * 0.12).coerceAtMost(0.95),
            isCurrentUser = false,
            isDelivered = false,
            paidAmountRp = group.bagPriceRp + group.patunganShippingRp
        )

        val isFull = members.size >= group.maxSlots
        val updated = group.copy(
            membersJson = serializePatunganMembers(members),
            status = if (isFull) PatunganStatus.BERANGKAT.name else PatunganStatus.OPEN.name,
            driverStage = if (isFull) DriverStage.MENUJU_TOKO.name else group.driverStage,
            etaMinutes = 10
        )
        patunganDao.upsert(updated)
        return updated
    }

    suspend fun simulatePatunganCutoff(groupId: String, forceFailUnderMin: Boolean = false): PatunganGroupEntity? {
        val group = patunganDao.getById(groupId) ?: return null
        val members = group.members
        val updated = if (forceFailUnderMin) {
            // Keep max 2 members (including current user if present) to demonstrate Failed screen cleanly
            val userMember = members.firstOrNull { it.isCurrentUser }
            val otherMember = members.firstOrNull { !it.isCurrentUser }
            val reduced = listOfNotNull(otherMember, userMember)
                .take(2)
                .mapIndexed { index, m -> m.copy(slotIndex = index + 1) }
            group.copy(
                membersJson = serializePatunganMembers(reduced),
                status = PatunganStatus.GAGAL_PENUH.name
            )
        } else if (members.size < group.minSlotsToDepart) {
            group.copy(status = PatunganStatus.GAGAL_PENUH.name)
        } else {
            group.copy(
                status = PatunganStatus.BERANGKAT.name,
                driverStage = DriverStage.MENUJU_TOKO.name,
                etaMinutes = 10
            )
        }
        patunganDao.upsert(updated)
        return updated
    }

    suspend fun resolveFailedKeepSoloDelivery(groupId: String): PatunganGroupEntity? {
        val group = patunganDao.getById(groupId) ?: return null
        val extraCost = group.normalShippingRp - group.patunganShippingRp // Rp12.000
        val currentUserName = prefsRepo.userPrefs.first().userName.ifBlank { "Kamu" }
        val userMember = group.members.firstOrNull { it.isCurrentUser }
            ?: PatunganMember(1, currentUserName, group.areaName, "${group.areaName} No. 15", 0.6, isCurrentUser = true)
        val totalSoloPaid = group.bagPriceRp + group.normalShippingRp
        val soloList = listOf(
            userMember.copy(slotIndex = 1, paidAmountRp = totalSoloPaid)
        )
        val updated = group.copy(
            membersJson = serializePatunganMembers(soloList),
            status = PatunganStatus.BERANGKAT.name,
            userTotalPaidRp = group.userTotalPaidRp + extraCost,
            isSoloFallbackDelivery = true,
            driverStage = DriverStage.MENUJU_TOKO.name,
            etaMinutes = 12
        )
        patunganDao.upsert(updated)
        return updated
    }

    suspend fun resolveFailedSwitchToPickup(groupId: String): OrderEntity? {
        val group = patunganDao.getById(groupId) ?: return null
        val product = productDao.getProductById(group.productId) ?: return null
        val shippingRefund = group.patunganShippingRp // Rp3.000 kembali ke Saldo LUDES
        prefsRepo.adjustWallet(shippingRefund)

        val updatedGroup = group.copy(
            status = PatunganStatus.DIBATALKAN.name,
            userJoined = false,
            fundsHeldInEscrow = false
        )
        patunganDao.upsert(updatedGroup)

        val buyerName = prefsRepo.userPrefs.first().userName.ifBlank { "Siti Rahma" }
        val order = OrderEntity(
            id = "#PT${(100000..999999).random()}",
            productId = product.id,
            productTitle = product.title,
            storeName = product.storeName,
            storeAddress = product.storeAddress,
            distanceText = product.distanceText,
            imageKey = product.imageKey,
            originalPrice = product.originalPrice,
            bagPrice = product.discountPrice,
            serviceFee = 0,
            totalPaid = product.discountPrice,
            paymentMethod = group.userPaymentMethod.ifBlank { "Saldo LUDES" },
            pickupSlot = group.pickupSlot,
            status = OrderStatus.BELUM_DIAMBIL.name,
            isReadyForPickup = true,
            merchantAccepted = true,
            buyerName = buyerName,
            createdAtMillis = System.currentTimeMillis()
        )
        orderDao.upsert(order)
        return order
    }

    suspend fun resolveFailedCancelAll(groupId: String): Int {
        return cancelPatungan(groupId)
    }

    suspend fun advanceDriverStage(groupId: String): PatunganGroupEntity? {
        val group = patunganDao.getById(groupId) ?: return null
        val current = DriverStage.entries.firstOrNull { it.name == group.driverStage } ?: DriverStage.MENUJU_TOKO
        val next = when (current) {
            DriverStage.MENUJU_TOKO -> DriverStage.AMBIL_KANTONG
            DriverStage.AMBIL_KANTONG -> DriverStage.DALAM_PERJALANAN
            DriverStage.DALAM_PERJALANAN -> DriverStage.SAMPAI
            DriverStage.SAMPAI -> DriverStage.SAMPAI
        }
        val members = group.members
        val updatedMembers = if (next == DriverStage.SAMPAI) {
            members.map { it.copy(isDelivered = true) }
        } else {
            members
        }
        val updated = group.copy(
            driverStage = next.name,
            hasPickedUpAtStore = next.stepIndex >= DriverStage.AMBIL_KANTONG.stepIndex,
            etaMinutes = when (next) {
                DriverStage.MENUJU_TOKO -> 12
                DriverStage.AMBIL_KANTONG -> 10
                DriverStage.DALAM_PERJALANAN -> 6
                DriverStage.SAMPAI -> 0
            },
            membersJson = serializePatunganMembers(updatedMembers),
            status = if (next == DriverStage.SAMPAI) PatunganStatus.SELESAI.name else PatunganStatus.BERANGKAT.name,
            fundsHeldInEscrow = next != DriverStage.SAMPAI
        )
        patunganDao.upsert(updated)
        return updated
    }

    suspend fun driverMarkPickedUpAtStore(groupId: String): PatunganGroupEntity? {
        val group = patunganDao.getById(groupId) ?: return null
        val updated = group.copy(
            hasPickedUpAtStore = true,
            driverStage = DriverStage.DALAM_PERJALANAN.name,
            status = PatunganStatus.BERANGKAT.name,
            etaMinutes = 8
        )
        patunganDao.upsert(updated)
        return updated
    }

    suspend fun driverMarkMemberDelivered(groupId: String, slotIndex: Int): PatunganGroupEntity? {
        val group = patunganDao.getById(groupId) ?: return null
        val updatedMembers = group.members.map { m ->
            if (m.slotIndex == slotIndex) m.copy(isDelivered = true) else m
        }
        val allDelivered = updatedMembers.isNotEmpty() && updatedMembers.all { it.isDelivered }
        val updated = group.copy(
            membersJson = serializePatunganMembers(updatedMembers),
            driverStage = if (allDelivered) DriverStage.SAMPAI.name else DriverStage.DALAM_PERJALANAN.name,
            status = if (allDelivered) PatunganStatus.SELESAI.name else PatunganStatus.BERANGKAT.name,
            hasPickedUpAtStore = true,
            etaMinutes = if (allDelivered) 0 else (updatedMembers.count { !it.isDelivered } * 3).coerceAtLeast(2),
            fundsHeldInEscrow = !allDelivered
        )
        patunganDao.upsert(updated)
        return updated
    }
}
