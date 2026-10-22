package br.universidade.approomproduto.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import br.universidade.approomproduto.model.Product

@Dao
interface ProductDao {
    @Query("SELECT * FROM product ORDER BY name COLLATE NOCASE ASC")
    suspend fun getAll(): List<Product>

    @Insert
    suspend fun insertAll(vararg products: Product)

    @Query("SELECT * FROM product WHERE name LIKE :nome COLLATE NOCASE ORDER BY name COLLATE NOCASE ASC")
    suspend fun buscarPorNome(nome: String): List<Product>

    @Query("SELECT * FROM product WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): Product?

    @Update
    suspend fun update(product: Product): Int

    @Query("DELETE FROM product")
    suspend fun deleteAll(): Int

    @Delete
    suspend fun delete(product: Product): Int
}
