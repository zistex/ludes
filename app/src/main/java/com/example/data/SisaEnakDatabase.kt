package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val title: String,
    val storeName: String,
    val storeAddress: String,
    val distanceText: String,
    val category: String, // FoodCategory.name
    val originalPrice: Int,
    val discountPrice: Int,
    val stock: Int,
    val pickupWindow: String,
    val pickupSlotsCsv: String,
    val rating: Double,
    val description: String,
    val imageKey: String,
    val customImageUri: String? = null,
    val isFavorite: Boolean = false,
    val isLive: Boolean = false,
    val liveViewersText: String = "",
    val liveSummary: String = "",
    val isMerchantPublished: Boolean = false,
    val viewCount: Int = 120,
    val favoriteCount: Int = 24
)

@Entity(tableName = "favorite_stores")
data class FavoriteStoreEntity(
    @PrimaryKey val id: String,
    val storeName: String,
    val rating: Double,
    val distanceText: String,
    val statusType: String, // StoreStatusType.name
    val remainingStock: Int,
    val imageKey: String,
    val linkedProductId: String,
    val isFavorite: Boolean = true
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val id: String,
    val productId: String,
    val productTitle: String,
    val storeName: String,
    val storeAddress: String,
    val distanceText: String,
    val imageKey: String,
    val originalPrice: Int,
    val bagPrice: Int,
    val serviceFee: Int,
    val totalPaid: Int,
    val paymentMethod: String,
    val pickupSlot: String,
    val status: String, // OrderStatus.name
    val isReadyForPickup: Boolean = false,
    val merchantAccepted: Boolean = false,
    val buyerName: String = "Siti Rahma",
    val createdAtMillis: Long = System.currentTimeMillis(),
    val rating: Int = 0,
    val reviewComment: String = "",
    val fromLive: Boolean = false
)

@Entity(tableName = "viewed_history")
data class ViewedHistoryEntity(
    @PrimaryKey val productId: String,
    val viewedLabel: String,
    val viewedAtMillis: Long
)

@Entity(tableName = "withdraw_requests")
data class WithdrawRequestEntity(
    @PrimaryKey val id: String,
    val amountRp: Int,
    val destinationLabel: String,
    val status: String, // "Diproses" or "Berhasil Dicairkan"
    val createdAtMillis: Long
)

@Entity(tableName = "product_reviews")
data class ProductReviewEntity(
    @PrimaryKey val id: String,
    val productId: String,
    val reviewerName: String,
    val rating: Int,
    val comment: String,
    val timeLabel: String,
    val createdAtMillis: Long
)

@Entity(tableName = "patungan_groups")
data class PatunganGroupEntity(
    @PrimaryKey val id: String,
    val productId: String,
    val productTitle: String,
    val storeName: String,
    val storeAddress: String,
    val imageKey: String,
    val bagPriceRp: Int,
    val normalShippingRp: Int = 15000,
    val patunganShippingRp: Int = 3000,
    val areaName: String, // PatunganArea.areaName: Dago, Dipatiukur, Cihampelas, Buah Batu, Cibiru
    val pickupSlot: String, // e.g. "19.00 - 19.30"
    val cutoffHour: String, // e.g. "19.00"
    val departHour: String, // e.g. "19.30"
    val maxSlots: Int = 5,
    val minSlotsToDepart: Int = 3,
    val membersJson: String,
    val status: String, // PatunganStatus.name
    val userJoined: Boolean = false,
    val userPaymentMethod: String = "",
    val userTotalPaidRp: Int = 0,
    val fundsHeldInEscrow: Boolean = false,
    val driverName: String = "Pak Agus",
    val driverPlate: String = "Motor B 1234 XYZ",
    val driverRating: Double = 4.9,
    val driverStage: String = DriverStage.MENUJU_TOKO.name,
    val hasPickedUpAtStore: Boolean = false,
    val etaMinutes: Int = 10,
    val isSoloFallbackDelivery: Boolean = false,
    val createdAtMillis: Long = System.currentTimeMillis()
) {
    val members: List<PatunganMember>
        get() = deserializePatunganMembers(membersJson)

    val filledSlots: Int
        get() = members.size.coerceAtMost(maxSlots)

    val remainingSlots: Int
        get() = (maxSlots - filledSlots).coerceAtLeast(0)
}

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY isMerchantPublished DESC, isLive DESC, stock DESC")
    fun observeAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE isLive = 1")
    fun observeLiveProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE isFavorite = 1")
    fun observeFavoriteProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: String): ProductEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ProductEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: ProductEntity)

    @Query("UPDATE products SET isFavorite = :favorite WHERE id = :id")
    suspend fun setFavorite(id: String, favorite: Boolean)

    @Query("UPDATE products SET isFavorite = :favorite, favoriteCount = MAX(0, favoriteCount + :delta) WHERE id = :id")
    suspend fun updateFavoriteWithCount(id: String, favorite: Boolean, delta: Int)

    @Query("UPDATE products SET viewCount = viewCount + 1 WHERE id = :id")
    suspend fun incrementViewCount(id: String)

    @Query("UPDATE products SET stock = MAX(0, stock + :delta) WHERE id = :id")
    suspend fun adjustStock(id: String, delta: Int)
}

@Dao
interface FavoriteStoreDao {
    @Query("SELECT * FROM favorite_stores WHERE isFavorite = 1")
    fun observeFavoriteStores(): Flow<List<FavoriteStoreEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<FavoriteStoreEntity>)

    @Query("UPDATE favorite_stores SET isFavorite = :favorite WHERE id = :id")
    suspend fun setStoreFavorite(id: String, favorite: Boolean)
}

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders ORDER BY createdAtMillis DESC")
    fun observeAllOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE id = :id LIMIT 1")
    suspend fun getOrderById(id: String): OrderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<OrderEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(order: OrderEntity)

    @Query("UPDATE orders SET status = :status, isReadyForPickup = :ready WHERE id = :id")
    suspend fun updateOrderStatus(id: String, status: String, ready: Boolean)

    @Query("UPDATE orders SET merchantAccepted = :accepted, isReadyForPickup = :ready WHERE id = :id")
    suspend fun setMerchantAccepted(id: String, accepted: Boolean, ready: Boolean)

    @Query("UPDATE orders SET rating = :rating, reviewComment = :comment WHERE id = :id")
    suspend fun submitRating(id: String, rating: Int, comment: String)
}

@Dao
interface ViewedHistoryDao {
    @Query("SELECT * FROM viewed_history ORDER BY viewedAtMillis DESC")
    fun observeHistory(): Flow<List<ViewedHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ViewedHistoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: ViewedHistoryEntity)
}

@Dao
interface WithdrawDao {
    @Query("SELECT * FROM withdraw_requests ORDER BY createdAtMillis DESC")
    fun observeAllWithdraws(): Flow<List<WithdrawRequestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: WithdrawRequestEntity)

    @Query("UPDATE withdraw_requests SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: String)
}

@Dao
interface ProductReviewDao {
    @Query("SELECT * FROM product_reviews ORDER BY createdAtMillis DESC")
    fun observeAllReviews(): Flow<List<ProductReviewEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ProductReviewEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: ProductReviewEntity)
}

@Dao
interface PatunganDao {
    @Query("SELECT * FROM patungan_groups ORDER BY userJoined DESC, createdAtMillis DESC")
    fun observeAllPatungan(): Flow<List<PatunganGroupEntity>>

    @Query("SELECT * FROM patungan_groups WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): PatunganGroupEntity?

    @Query("SELECT * FROM patungan_groups WHERE productId = :productId LIMIT 1")
    suspend fun getByProductId(productId: String): PatunganGroupEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<PatunganGroupEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: PatunganGroupEntity)
}

@Database(
    entities = [
        ProductEntity::class,
        FavoriteStoreEntity::class,
        OrderEntity::class,
        ViewedHistoryEntity::class,
        WithdrawRequestEntity::class,
        ProductReviewEntity::class,
        PatunganGroupEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class SisaEnakDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun favoriteStoreDao(): FavoriteStoreDao
    abstract fun orderDao(): OrderDao
    abstract fun viewedHistoryDao(): ViewedHistoryDao
    abstract fun withdrawDao(): WithdrawDao
    abstract fun productReviewDao(): ProductReviewDao
    abstract fun patunganDao(): PatunganDao

    companion object {
        @Volatile
        private var INSTANCE: SisaEnakDatabase? = null

        fun getInstance(context: Context): SisaEnakDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context.applicationContext).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): SisaEnakDatabase {
            return Room.databaseBuilder(
                context,
                SisaEnakDatabase::class.java,
                "sisa_enak_db"
            ).fallbackToDestructiveMigration(true)
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            seedInitialData(getInstance(context))
                        }
                    }
                }).build()
        }

        suspend fun ensureSeeded(db: SisaEnakDatabase) {
            if (db.productDao().getProductById("prod_bakery_dago") == null) {
                seedInitialData(db)
            } else if (db.patunganDao().getById("patungan_prod_bakery_dago") == null) {
                seedPatunganData(db, System.currentTimeMillis())
            }
        }

        private suspend fun seedInitialData(db: SisaEnakDatabase) {
            val now = System.currentTimeMillis()
            val products = listOf(
                ProductEntity(
                    id = "prod_bakery_dago",
                    title = "Kantong Kejutan Roti - Bakery Dago",
                    storeName = "Bakery Dago",
                    storeAddress = "Jl. Dago No. 88",
                    distanceText = "0.8 km",
                    category = FoodCategory.ROTI.name,
                    originalPrice = 25000,
                    discountPrice = 10000,
                    stock = 3,
                    pickupWindow = "19.00-21.00",
                    pickupSlotsCsv = "19.00 - 19.30,19.30 - 20.00,20.00 - 20.30",
                    rating = 4.9,
                    description = "Isi acak roti, croissant, pastry, 3-5 item per kantong. Fresh sisa hari ini",
                    imageKey = "bakery",
                    isFavorite = true,
                    isLive = true,
                    liveViewersText = "940 penonton",
                    liveSummary = "3 roti manis + 2 croissant, Hemat 60%",
                    viewCount = 1240,
                    favoriteCount = 318
                ),
                ProductEntity(
                    id = "prod_warung_bu_sari",
                    title = "Kantong Hemat Warung Bu Sari",
                    storeName = "Warung Bu Sari",
                    storeAddress = "Jl. Dago No. 88",
                    distanceText = "500m",
                    category = FoodCategory.NASI.name,
                    originalPrice = 18000,
                    discountPrice = 10000,
                    stock = 3,
                    pickupWindow = "17.00-18.00",
                    pickupSlotsCsv = "17.00-17.30,17.30-18.00",
                    rating = 4.8,
                    description = "Paket makan malam hemat: nasi hangat, lauk pilihan ibu warung, dan gorengan renyah sisa sore.",
                    imageKey = "nasi",
                    isFavorite = true,
                    isLive = true,
                    liveViewersText = "1.2rb penonton",
                    liveSummary = "2 roti + 1 nasi, Hemat 40%",
                    viewCount = 980,
                    favoriteCount = 245
                ),
                ProductEntity(
                    id = "prod_warteg_bahari",
                    title = "Paket Nasi Rendang & Telur Balado",
                    storeName = "Warteg Bahari Dago",
                    storeAddress = "Jl. Ir. H. Juanda No. 112",
                    distanceText = "1.1 km",
                    category = FoodCategory.NASI.name,
                    originalPrice = 28000,
                    discountPrice = 12000,
                    stock = 5,
                    pickupWindow = "19.00-21.00",
                    pickupSlotsCsv = "19.00 - 19.30,19.30 - 20.00,20.00 - 20.30",
                    rating = 4.8,
                    description = "Nasi putih pulen dengan rendang empuk, telur balado, tempe orek, dan sambal ijo segar.",
                    imageKey = "nasi",
                    isFavorite = true,
                    isLive = false,
                    viewCount = 740,
                    favoriteCount = 189
                ),
                ProductEntity(
                    id = "prod_martabak_simpang",
                    title = "Kantong Martabak & Kue Basah",
                    storeName = "Martabak Simpang Dago",
                    storeAddress = "Jl. Dipatiukur No. 45",
                    distanceText = "0.6 km",
                    category = FoodCategory.JAJANAN.name,
                    originalPrice = 32000,
                    discountPrice = 15000,
                    stock = 4,
                    pickupWindow = "20.00-22.00",
                    pickupSlotsCsv = "20.00 - 20.30,20.30 - 21.00,21.00 - 21.30",
                    rating = 4.9,
                    description = "Potongan martabak manis cokelat keju kacang plus aneka kue basah pasar masih hangat.",
                    imageKey = "jajanan",
                    isFavorite = true,
                    isLive = true,
                    liveViewersText = "680 penonton",
                    liveSummary = "Martabak 6 potong + 3 kue basah, Hemat 53%",
                    viewCount = 860,
                    favoriteCount = 212
                ),
                ProductEntity(
                    id = "prod_buah_segar",
                    title = "Kotak Buah Potong Tropis Segar",
                    storeName = "Kedai Buah Segar Dago",
                    storeAddress = "Jl. Tubagus Ismail No. 19",
                    distanceText = "0.9 km",
                    category = FoodCategory.BUAH.name,
                    originalPrice = 24000,
                    discountPrice = 10000,
                    stock = 6,
                    pickupWindow = "18.30-20.30",
                    pickupSlotsCsv = "18.30 - 19.00,19.00 - 19.30,19.30 - 20.00",
                    rating = 4.7,
                    description = "Campuran mangga harum manis, semangka merah, pepaya, dan buah naga potong segar hari ini.",
                    imageKey = "buah",
                    isFavorite = true,
                    isLive = false,
                    viewCount = 520,
                    favoriteCount = 134
                ),
                ProductEntity(
                    id = "prod_roti_tawar_sore",
                    title = "Paket Roti Sobek & Donat Kampung",
                    storeName = "Toko Roti Sederhana",
                    storeAddress = "Jl. Cisitu Lama No. 24",
                    distanceText = "1.3 km",
                    category = FoodCategory.ROTI.name,
                    originalPrice = 22000,
                    discountPrice = 10000,
                    stock = 5,
                    pickupWindow = "19.00-21.00",
                    pickupSlotsCsv = "19.00 - 19.30,19.30 - 20.00,20.00 - 20.30",
                    rating = 4.8,
                    description = "Roti sobek cokelat keju dan 4 donat gula halus lembut, masih fresh dari panggangan siang.",
                    imageKey = "bakery",
                    isFavorite = true,
                    isLive = false,
                    viewCount = 610,
                    favoriteCount = 156
                ),
                ProductEntity(
                    id = "prod_kue_subuh_teh_rina",
                    title = "Jajanan Pasar Campur Teh Rina",
                    storeName = "Warung Kue Teh Rina",
                    storeAddress = "Jl. Siliwangi No. 12",
                    distanceText = "0.7 km",
                    category = FoodCategory.JAJANAN.name,
                    originalPrice = 20000,
                    discountPrice = 10000,
                    stock = 3,
                    pickupWindow = "17.30-19.30",
                    pickupSlotsCsv = "17.30 - 18.00,18.00 - 18.30,18.30 - 19.00",
                    rating = 4.9,
                    description = "Kue lapis legit, dadar gulung, kue ku, dan bolu kukus empuk tanpa pengawet.",
                    imageKey = "jajanan",
                    isFavorite = true,
                    isLive = true,
                    liveViewersText = "410 penonton",
                    liveSummary = "8 pcs jajanan pasar campur, Hemat 50%",
                    viewCount = 470,
                    favoriteCount = 142
                ),
                ProductEntity(
                    id = "prod_salad_buah_dago",
                    title = "Paket Rujak & Buah Potong Dingin",
                    storeName = "Warung Buah Cihampelas",
                    storeAddress = "Jl. Cihampelas No. 67",
                    distanceText = "1.4 km",
                    category = FoodCategory.BUAH.name,
                    originalPrice = 25000,
                    discountPrice = 12000,
                    stock = 2,
                    pickupWindow = "19.00-20.30",
                    pickupSlotsCsv = "19.00 - 19.30,19.30 - 20.00,20.00 - 20.30",
                    rating = 4.6,
                    description = "Buah segar dingin siap santap dengan bumbu rujak gula aren kacang mete.",
                    imageKey = "buah",
                    isFavorite = false,
                    isLive = false,
                    viewCount = 315,
                    favoriteCount = 74
                ),
                ProductEntity(
                    id = "prod_kopi_dago",
                    title = "Paket Es Kopi Susu & Camilan Sore",
                    storeName = "Kedai Kopi Dago",
                    storeAddress = "Jl. Dago No. 55",
                    distanceText = "0.5 km",
                    category = FoodCategory.LAINNYA.name,
                    originalPrice = 26000,
                    discountPrice = 11000,
                    stock = 4,
                    pickupWindow = "18.30-21.00",
                    pickupSlotsCsv = "18.30 - 19.00,19.00 - 19.30,19.30 - 20.00",
                    rating = 4.8,
                    description = "2 cup es kopi susu gula aren + camilan ringan sisa produksi hari ini.",
                    imageKey = "lainnya",
                    isFavorite = false,
                    isLive = false,
                    viewCount = 430,
                    favoriteCount = 109
                ),
                ProductEntity(
                    id = "prod_bubur_bandung",
                    title = "Paket Bubur Ayam & Sate Hangat",
                    storeName = "Bubur Mang Ujang",
                    storeAddress = "Jl. Dipatiukur No. 72",
                    distanceText = "0.8 km",
                    category = FoodCategory.LAINNYA.name,
                    originalPrice = 22000,
                    discountPrice = 10000,
                    stock = 3,
                    pickupWindow = "19.00-21.00",
                    pickupSlotsCsv = "19.00 - 19.30,19.30 - 20.00,20.00 - 20.30",
                    rating = 4.7,
                    description = "Bubur ayam suwir lengkap cakwe, kacang kedelai, dan 2 tusuk sate usus/telur.",
                    imageKey = "nasi",
                    isFavorite = false,
                    isLive = false,
                    viewCount = 385,
                    favoriteCount = 92
                )
            )
            db.productDao().insertAll(products)

            val favoriteStores = listOf(
                FavoriteStoreEntity(
                    id = "store_bakery_dago",
                    storeName = "Bakery Dago",
                    rating = 4.9,
                    distanceText = "0.8 km",
                    statusType = StoreStatusType.OPEN_LIMITED.name,
                    remainingStock = 3,
                    imageKey = "bakery",
                    linkedProductId = "prod_bakery_dago",
                    isFavorite = true
                ),
                FavoriteStoreEntity(
                    id = "store_warteg_bahari",
                    storeName = "Warteg Bahari Dago",
                    rating = 4.8,
                    distanceText = "1.1 km",
                    statusType = StoreStatusType.OPEN_MANY.name,
                    remainingStock = 5,
                    imageKey = "nasi",
                    linkedProductId = "prod_warteg_bahari",
                    isFavorite = true
                ),
                FavoriteStoreEntity(
                    id = "store_warung_bu_sari",
                    storeName = "Warung Bu Sari",
                    rating = 4.8,
                    distanceText = "500m",
                    statusType = StoreStatusType.OPEN_LIMITED.name,
                    remainingStock = 3,
                    imageKey = "nasi",
                    linkedProductId = "prod_warung_bu_sari",
                    isFavorite = true
                ),
                FavoriteStoreEntity(
                    id = "store_buah_segar",
                    storeName = "Kedai Buah Segar Dago",
                    rating = 4.7,
                    distanceText = "0.9 km",
                    statusType = StoreStatusType.OPEN_MANY.name,
                    remainingStock = 6,
                    imageKey = "buah",
                    linkedProductId = "prod_buah_segar",
                    isFavorite = true
                ),
                FavoriteStoreEntity(
                    id = "store_kue_riau",
                    storeName = "Kue Tradisional Riau",
                    rating = 4.7,
                    distanceText = "1.5 km",
                    statusType = StoreStatusType.CLOSED.name,
                    remainingStock = 0,
                    imageKey = "jajanan",
                    linkedProductId = "prod_kue_subuh_teh_rina",
                    isFavorite = true
                )
            )
            db.favoriteStoreDao().insertAll(favoriteStores)

            val orders = listOf(
                OrderEntity(
                    id = "PES12345",
                    productId = "prod_bakery_dago",
                    productTitle = "Kantong Kejutan Roti - Bakery Dago",
                    storeName = "Bakery Dago",
                    storeAddress = "Jl. Dago No. 88",
                    distanceText = "0.8 km",
                    imageKey = "bakery",
                    originalPrice = 25000,
                    bagPrice = 10000,
                    serviceFee = 1000,
                    totalPaid = 11000,
                    paymentMethod = "QRIS",
                    pickupSlot = "19.00 - 19.30",
                    status = OrderStatus.BELUM_DIAMBIL.name,
                    isReadyForPickup = true,
                    merchantAccepted = true,
                    buyerName = "Siti Rahma",
                    createdAtMillis = now - 600_000L
                ),
                OrderEntity(
                    id = "#SE102910",
                    productId = "prod_warteg_bahari",
                    productTitle = "Paket Nasi Rendang & Telur Balado",
                    storeName = "Warteg Bahari Dago",
                    storeAddress = "Jl. Ir. H. Juanda No. 112",
                    distanceText = "1.1 km",
                    imageKey = "nasi",
                    originalPrice = 28000,
                    bagPrice = 12000,
                    serviceFee = 1000,
                    totalPaid = 13000,
                    paymentMethod = "GoPay",
                    pickupSlot = "18.30 - 19.00",
                    status = OrderStatus.SUDAH_DIAMBIL.name,
                    isReadyForPickup = true,
                    merchantAccepted = true,
                    buyerName = "Ibu Rina",
                    createdAtMillis = now - 86_400_000L,
                    rating = 5,
                    reviewComment = "Porsi banyak dan masih hangat!"
                ),
                OrderEntity(
                    id = "#SE102877",
                    productId = "prod_martabak_simpang",
                    productTitle = "Kantong Martabak & Kue Basah",
                    storeName = "Martabak Simpang Dago",
                    storeAddress = "Jl. Dipatiukur No. 45",
                    distanceText = "0.6 km",
                    imageKey = "jajanan",
                    originalPrice = 32000,
                    bagPrice = 15000,
                    serviceFee = 1000,
                    totalPaid = 16000,
                    paymentMethod = "OVO",
                    pickupSlot = "20.00 - 20.30",
                    status = OrderStatus.BATAL.name,
                    isReadyForPickup = false,
                    merchantAccepted = false,
                    buyerName = "Siti Rahma",
                    createdAtMillis = now - 172_800_000L
                )
            )
            db.orderDao().insertAll(orders)

            val history = listOf(
                ViewedHistoryEntity(
                    productId = "prod_bakery_dago",
                    viewedLabel = "Dilihat 2 jam lalu",
                    viewedAtMillis = now - 7_200_000L
                ),
                ViewedHistoryEntity(
                    productId = "prod_warteg_bahari",
                    viewedLabel = "Dilihat 5 jam lalu",
                    viewedAtMillis = now - 18_000_000L
                ),
                ViewedHistoryEntity(
                    productId = "prod_buah_segar",
                    viewedLabel = "Dilihat kemarin",
                    viewedAtMillis = now - 86_400_000L
                )
            )
            db.viewedHistoryDao().insertAll(history)

            db.withdrawDao().upsert(
                WithdrawRequestEntity(
                    id = "WD-9012",
                    amountRp = 25000,
                    destinationLabel = "BCA •••• 4821 (Siti Rahma)",
                    status = "Berhasil Dicairkan",
                    createdAtMillis = now - 172_800_000L
                )
            )

            val reviews = listOf(
                ProductReviewEntity(
                    id = "rev_1",
                    productId = "prod_bakery_dago",
                    reviewerName = "Ibu Ani",
                    rating = 5,
                    comment = "Enak banget, rotinya masih empuk dan fresh! Dapat 2 croissant, 1 roti cokelat, dan 1 roti keju.",
                    timeLabel = "Kemarin",
                    createdAtMillis = now - 86_400_000L
                ),
                ProductReviewEntity(
                    id = "rev_2",
                    productId = "prod_bakery_dago",
                    reviewerName = "Rizky Mahasiswa",
                    rating = 5,
                    comment = "10 ribu dapat roti sebanyak ini penyelamat anak kos banget. Ambil sendiri di kasir cepat.",
                    timeLabel = "2 hari lalu",
                    createdAtMillis = now - 172_800_000L
                ),
                ProductReviewEntity(
                    id = "rev_3",
                    productId = "prod_bakery_dago",
                    reviewerName = "Teh Dewi",
                    rating = 4,
                    comment = "Rasanya tetap enak walau sisa sore, tinggal dihangatkan sebentar langsung mantap.",
                    timeLabel = "3 hari lalu",
                    createdAtMillis = now - 259_200_000L
                ),
                ProductReviewEntity(
                    id = "rev_4",
                    productId = "prod_warung_bu_sari",
                    reviewerName = "Budi Dago",
                    rating = 5,
                    comment = "Ibu warungnya ramah pisan, porsinya banyak buat makan malam berdua.",
                    timeLabel = "Kemarin",
                    createdAtMillis = now - 90_000_000L
                ),
                ProductReviewEntity(
                    id = "rev_5",
                    productId = "prod_warteg_bahari",
                    reviewerName = "Ibu Rina",
                    rating = 5,
                    comment = "Porsi banyak dan masih hangat! Rendangnya empuk, sambal ijonya pas.",
                    timeLabel = "Kemarin",
                    createdAtMillis = now - 86_400_000L
                ),
                ProductReviewEntity(
                    id = "rev_6",
                    productId = "prod_martabak_simpang",
                    reviewerName = "Dina",
                    rating = 5,
                    comment = "Martabaknya masih wangi mentega, kejunya melimpah padahal cuma Rp15.000!",
                    timeLabel = "2 hari lalu",
                    createdAtMillis = now - 160_000_000L
                ),
                ProductReviewEntity(
                    id = "rev_7",
                    productId = "prod_buah_segar",
                    reviewerName = "Ibu Sari",
                    rating = 5,
                    comment = "Buah potongnya masih manis, segar, dan dingin dalam kotak rapi.",
                    timeLabel = "Kemarin",
                    createdAtMillis = now - 100_000_000L
                ),
                ProductReviewEntity(
                    id = "rev_8",
                    productId = "prod_kopi_dago",
                    reviewerName = "Fajar",
                    rating = 5,
                    comment = "Kopi susu gula arennya mantap, dapat donat gula juga!",
                    timeLabel = "Kemarin",
                    createdAtMillis = now - 110_000_000L
                ),
                ProductReviewEntity(
                    id = "rev_9",
                    productId = "prod_bubur_bandung",
                    reviewerName = "Teh Maya",
                    rating = 5,
                    comment = "Buburnya masih panas, satenya enak, cocok buat makan malam praktis.",
                    timeLabel = "2 hari lalu",
                    createdAtMillis = now - 190_000_000L
                )
            )
            db.productReviewDao().insertAll(reviews)
            seedPatunganData(db, now)
        }

        private suspend fun seedPatunganData(db: SisaEnakDatabase, now: Long) {
            val groups = listOf(
                // 1. Bakery Dago: 3/5 already joined -> Kamu joins as #4, leaving #5 Slot Kosong -> Waiting 4/5!
                PatunganGroupEntity(
                    id = "patungan_prod_bakery_dago",
                    productId = "prod_bakery_dago",
                    productTitle = "Kantong Kejutan Roti",
                    storeName = "Bakery Dago",
                    storeAddress = "Jl. Dago No. 88",
                    imageKey = "bakery",
                    bagPriceRp = 10000,
                    areaName = PatunganArea.DAGO.areaName,
                    pickupSlot = "19.00 - 19.30",
                    cutoffHour = "19.00",
                    departHour = "19.30",
                    membersJson = serializePatunganMembers(
                        listOf(
                            PatunganMember(1, "Siti", "Dago Atas", "Dago Atas No. 12", 0.35, paidAmountRp = 13000),
                            PatunganMember(2, "Budi", "Dago Bawah", "Dago Bawah No. 5", 0.55, paidAmountRp = 13000),
                            PatunganMember(3, "Andi", "Dago Atas", "Dago Atas No. 8", 0.72, paidAmountRp = 13000)
                        )
                    ),
                    status = PatunganStatus.OPEN.name,
                    createdAtMillis = now - 300_000L
                ),
                // 2. Warung Bu Sari: 4/5 orang sudah ikut - Sisa 1 Slot - Berangkat Jam 19.30
                PatunganGroupEntity(
                    id = "patungan_prod_warung_bu_sari",
                    productId = "prod_warung_bu_sari",
                    productTitle = "Kantong Hemat Warung Bu Sari",
                    storeName = "Warung Bu Sari",
                    storeAddress = "Jl. Dago No. 88",
                    imageKey = "nasi",
                    bagPriceRp = 10000,
                    areaName = PatunganArea.DAGO.areaName,
                    pickupSlot = "19.00 - 19.30",
                    cutoffHour = "19.00",
                    departHour = "19.30",
                    membersJson = serializePatunganMembers(
                        listOf(
                            PatunganMember(1, "Siti", "Dago Atas", "Dago Atas No. 12", 0.30, paidAmountRp = 13000),
                            PatunganMember(2, "Budi", "Dago Bawah", "Dago Bawah No. 5", 0.50, paidAmountRp = 13000),
                            PatunganMember(3, "Andi", "Dago Atas", "Dago Atas No. 8", 0.68, paidAmountRp = 13000),
                            PatunganMember(4, "Rina", "Dago Pojok", "Dago Pojok No. 3", 0.85, paidAmountRp = 13000)
                        )
                    ),
                    status = PatunganStatus.OPEN.name,
                    createdAtMillis = now - 420_000L
                ),
                // 3. Warteg Bahari Dago: 2/5 orang
                PatunganGroupEntity(
                    id = "patungan_prod_warteg_bahari",
                    productId = "prod_warteg_bahari",
                    productTitle = "Paket Nasi Rendang & Telur Balado",
                    storeName = "Warteg Bahari Dago",
                    storeAddress = "Jl. Ir. H. Juanda No. 112",
                    imageKey = "nasi",
                    bagPriceRp = 12000,
                    areaName = PatunganArea.DAGO.areaName,
                    pickupSlot = "19.00 - 19.30",
                    cutoffHour = "19.00",
                    departHour = "19.30",
                    membersJson = serializePatunganMembers(
                        listOf(
                            PatunganMember(1, "Fajar", "Dago Atas", "Dago Atas No. 21", 0.40, paidAmountRp = 15000),
                            PatunganMember(2, "Dewi", "Dago Bawah", "Dago Bawah No. 14", 0.65, paidAmountRp = 15000)
                        )
                    ),
                    status = PatunganStatus.OPEN.name,
                    createdAtMillis = now - 540_000L
                ),
                // 4. Martabak Simpang: Area Dipatiukur 3/5
                PatunganGroupEntity(
                    id = "patungan_prod_martabak_simpang",
                    productId = "prod_martabak_simpang",
                    productTitle = "Kantong Martabak & Kue Basah",
                    storeName = "Martabak Simpang Dago",
                    storeAddress = "Jl. Dipatiukur No. 45",
                    imageKey = "jajanan",
                    bagPriceRp = 15000,
                    areaName = PatunganArea.DIPATIUKUR.areaName,
                    pickupSlot = "20.00 - 20.30",
                    cutoffHour = "19.45",
                    departHour = "20.00",
                    membersJson = serializePatunganMembers(
                        listOf(
                            PatunganMember(1, "Tio", "Dipatiukur Utara", "Dipatiukur No. 18", 0.30, paidAmountRp = 18000),
                            PatunganMember(2, "Nisa", "Sekitar ITB", "Ganesha No. 9", 0.48, paidAmountRp = 18000),
                            PatunganMember(3, "Hendra", "Dipatiukur Selatan", "Dipatiukur No. 66", 0.76, paidAmountRp = 18000)
                        )
                    ),
                    status = PatunganStatus.OPEN.name,
                    createdAtMillis = now - 600_000L
                ),
                // 5. Kedai Buah Segar Dago: 4/5 -> Sisa 1 Slot
                PatunganGroupEntity(
                    id = "patungan_prod_buah_segar",
                    productId = "prod_buah_segar",
                    productTitle = "Kotak Buah Potong Tropis Segar",
                    storeName = "Kedai Buah Segar Dago",
                    storeAddress = "Jl. Tubagus Ismail No. 19",
                    imageKey = "buah",
                    bagPriceRp = 10000,
                    areaName = PatunganArea.DAGO.areaName,
                    pickupSlot = "19.00 - 19.30",
                    cutoffHour = "19.00",
                    departHour = "19.30",
                    membersJson = serializePatunganMembers(
                        listOf(
                            PatunganMember(1, "Maya", "Dago Atas", "Tubagus Ismail No. 7", 0.25, paidAmountRp = 13000),
                            PatunganMember(2, "Rudi", "Dago Atas", "Tubagus Ismail No. 22", 0.45, paidAmountRp = 13000),
                            PatunganMember(3, "Laras", "Dago Bawah", "Dago Bawah No. 31", 0.62, paidAmountRp = 13000),
                            PatunganMember(4, "Bagas", "Dago Pojok", "Dago Pojok No. 11", 0.88, paidAmountRp = 13000)
                        )
                    ),
                    status = PatunganStatus.OPEN.name,
                    createdAtMillis = now - 700_000L
                ),
                // 6. Warung Kue Teh Rina: 5/5 PATUNGAN PENUH (already departed / ready for Driver demo!)
                PatunganGroupEntity(
                    id = "patungan_prod_kue_subuh_teh_rina",
                    productId = "prod_kue_subuh_teh_rina",
                    productTitle = "Jajanan Pasar Campur Teh Rina",
                    storeName = "Warung Kue Teh Rina",
                    storeAddress = "Jl. Siliwangi No. 12",
                    imageKey = "jajanan",
                    bagPriceRp = 10000,
                    areaName = PatunganArea.DAGO.areaName,
                    pickupSlot = "19.00 - 19.30",
                    cutoffHour = "19.00",
                    departHour = "19.30",
                    membersJson = serializePatunganMembers(
                        listOf(
                            PatunganMember(1, "Siti", "Dago Atas", "Dago Atas No. 12", 0.30, paidAmountRp = 13000),
                            PatunganMember(2, "Budi", "Dago Bawah", "Dago Bawah No. 5", 0.45, paidAmountRp = 13000),
                            PatunganMember(3, "Andi", "Dago Atas", "Dago Atas No. 8", 0.60, paidAmountRp = 13000),
                            PatunganMember(4, "Rina", "Dago Pojok", "Dago Pojok No. 3", 0.78, paidAmountRp = 13000),
                            PatunganMember(5, "Dian", "Cisitu Dago", "Cisitu Indah No. 19", 0.92, paidAmountRp = 13000)
                        )
                    ),
                    status = PatunganStatus.BERANGKAT.name,
                    driverStage = DriverStage.MENUJU_TOKO.name,
                    etaMinutes = 12,
                    createdAtMillis = now - 800_000L
                ),
                // 7. Kedai Kopi Dago: 1/5 (if user joins -> 2/5 -> great for testing Gagal Penuh <3 orang!)
                PatunganGroupEntity(
                    id = "patungan_prod_kopi_dago",
                    productId = "prod_kopi_dago",
                    productTitle = "Paket Es Kopi Susu & Camilan Sore",
                    storeName = "Kedai Kopi Dago",
                    storeAddress = "Jl. Dago No. 55",
                    imageKey = "lainnya",
                    bagPriceRp = 11000,
                    areaName = PatunganArea.DAGO.areaName,
                    pickupSlot = "19.00 - 19.30",
                    cutoffHour = "19.00",
                    departHour = "19.30",
                    membersJson = serializePatunganMembers(
                        listOf(
                            PatunganMember(1, "Gilang", "Dago Bawah", "Dago Bawah No. 19", 0.40, paidAmountRp = 14000)
                        )
                    ),
                    status = PatunganStatus.OPEN.name,
                    createdAtMillis = now - 900_000L
                ),
                // 8. Warung Buah Cihampelas: Area Cihampelas 3/5
                PatunganGroupEntity(
                    id = "patungan_prod_salad_buah_dago",
                    productId = "prod_salad_buah_dago",
                    productTitle = "Paket Rujak & Buah Potong Dingin",
                    storeName = "Warung Buah Cihampelas",
                    storeAddress = "Jl. Cihampelas No. 67",
                    imageKey = "buah",
                    bagPriceRp = 12000,
                    areaName = PatunganArea.CIHAMPELAS.areaName,
                    pickupSlot = "19.30 - 20.00",
                    cutoffHour = "19.15",
                    departHour = "19.30",
                    membersJson = serializePatunganMembers(
                        listOf(
                            PatunganMember(1, "Vina", "Cihampelas Atas", "Cihampelas No. 14", 0.35, paidAmountRp = 15000),
                            PatunganMember(2, "Arif", "Tamansari", "Tamansari No. 28", 0.58, paidAmountRp = 15000),
                            PatunganMember(3, "Siska", "Cihampelas Bawah", "Cihampelas No. 92", 0.80, paidAmountRp = 15000)
                        )
                    ),
                    status = PatunganStatus.OPEN.name,
                    createdAtMillis = now - 950_000L
                )
            )
            db.patunganDao().insertAll(groups)
        }
    }
}
