package br.universidade.approomproduto.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import br.universidade.approomproduto.dao.ProductDao
import br.universidade.approomproduto.model.Product

@Database(
    entities = [Product::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "database-name"
                )
                    // Facilita o exercício ao alterar o esquema. Em produção, prefira Migration.
                    // Ao atualizar um banco antigo sem migração, os dados anteriores são apagados.
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
