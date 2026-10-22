package br.universidade.approomproduto.view

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import br.universidade.approomproduto.dao.ProductDao
import br.universidade.approomproduto.databinding.ActivityMainBinding
import br.universidade.approomproduto.db.AppDatabase
import br.universidade.approomproduto.model.Product
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var productDao: ProductDao

    private val currencyFormatter: NumberFormat by lazy {
        NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        productDao = AppDatabase.getInstance(applicationContext).productDao()
        configureActions()
        showEmptyState()
    }

    private fun configureActions() {
        binding.btnInserirProduto.setOnClickListener { insertProduct() }
        binding.btnListarProdutos.setOnClickListener { listProducts() }
        binding.btnBuscar.setOnClickListener { searchProducts() }
        binding.btnAtualizarPreco.setOnClickListener { updateProductPrice() }
        binding.btnExcluirPorId.setOnClickListener { deleteProductById() }
        binding.btnExcluirTodos.setOnClickListener { confirmDeleteAll() }
    }

    private fun insertProduct() {
        val name = binding.editProductName.text.toString().trim()
        val priceText = binding.editProductPrice.text.toString().trim()
        val description = binding.editProductDescription.text.toString().trim()
        val quantityText = binding.editProductQuantity.text.toString().trim()

        binding.editProductName.error = null
        binding.editProductPrice.error = null
        binding.editProductQuantity.error = null

        if (name.isBlank()) {
            binding.editProductName.error = "Informe o nome do produto"
            binding.editProductName.requestFocus()
            return
        }

        val price = parsePrice(priceText)
        if (price == null || price <= 0.0) {
            binding.editProductPrice.error = "Informe um preço válido maior que zero"
            binding.editProductPrice.requestFocus()
            return
        }

        val quantity = quantityText.toIntOrNull()
        if (quantity == null || quantity < 0) {
            binding.editProductQuantity.error = "Informe uma quantidade inteira igual ou maior que zero"
            binding.editProductQuantity.requestFocus()
            return
        }

        val product = Product(
            name = name,
            price = price,
            description = description.ifBlank { null },
            quantity = quantity
        )

        lifecycleScope.launch {
            runCatching { productDao.insertAll(product) }
                .onSuccess {
                    Toast.makeText(this@MainActivity, "Produto cadastrado com sucesso!", Toast.LENGTH_SHORT).show()
                    clearProductFields()
                    listProducts()
                    Log.i(TAG, "Produto inserido: ${product.name} - ${currencyFormatter.format(product.price)}")
                }
                .onFailure { error ->
                    Log.e(TAG, "Erro ao inserir produto", error)
                    Toast.makeText(this@MainActivity, "Não foi possível cadastrar o produto.", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun clearProductFields() {
        binding.editProductName.text?.clear()
        binding.editProductPrice.text?.clear()
        binding.editProductDescription.text?.clear()
        binding.editProductQuantity.text?.clear()
        binding.editProductName.requestFocus()
    }

    private fun listProducts() {
        lifecycleScope.launch {
            runCatching { productDao.getAll() }
                .onSuccess { products ->
                    displayProducts(products, "Todos os produtos")
                }
                .onFailure { error ->
                    Log.e(TAG, "Erro ao listar produtos", error)
                    Toast.makeText(this@MainActivity, "Não foi possível carregar os produtos.", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun searchProducts() {
        val text = binding.editSearchName.text.toString().trim()
        binding.editSearchName.error = null
        if (text.isBlank()) {
            binding.editSearchName.error = "Digite um nome para pesquisar"
            binding.editSearchName.requestFocus()
            return
        }

        lifecycleScope.launch {
            runCatching { productDao.buscarPorNome("%$text%") }
                .onSuccess { products -> displayProducts(products, "Resultado da busca por \"$text\"") }
                .onFailure { error ->
                    Log.e(TAG, "Erro ao buscar produtos", error)
                    Toast.makeText(this@MainActivity, "Não foi possível pesquisar.", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun updateProductPrice() {
        val id = binding.editProductId.text.toString().trim().toIntOrNull()
        val priceText = binding.editNewPrice.text.toString().trim()
        binding.editProductId.error = null
        binding.editNewPrice.error = null

        if (id == null || id <= 0) {
            binding.editProductId.error = "Informe um ID válido"
            binding.editProductId.requestFocus()
            return
        }

        val newPrice = parsePrice(priceText)
        if (newPrice == null || newPrice <= 0.0) {
            binding.editNewPrice.error = "Informe um preço maior que zero"
            binding.editNewPrice.requestFocus()
            return
        }

        lifecycleScope.launch {
            runCatching {
                val current = productDao.getById(id)
                    ?: throw IllegalArgumentException("Não existe produto com ID $id.")
                productDao.update(current.copy(price = newPrice))
            }.onSuccess { changedRows ->
                if (changedRows > 0) {
                    Toast.makeText(this@MainActivity, "Preço atualizado!", Toast.LENGTH_SHORT).show()
                    listProducts()
                } else {
                    Toast.makeText(this@MainActivity, "Nenhum produto foi atualizado.", Toast.LENGTH_SHORT).show()
                }
            }.onFailure { error ->
                Log.e(TAG, "Erro ao atualizar preço", error)
                Toast.makeText(this@MainActivity, error.message ?: "Não foi possível atualizar o preço.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun deleteProductById() {
        val id = binding.editProductId.text.toString().trim().toIntOrNull()
        if (id == null || id <= 0) {
            binding.editProductId.error = "Informe o ID do produto que deseja excluir"
            binding.editProductId.requestFocus()
            return
        }

        lifecycleScope.launch {
            runCatching {
                val product = productDao.getById(id)
                    ?: throw IllegalArgumentException("Não existe produto com ID $id.")
                productDao.delete(product)
            }.onSuccess { deletedRows ->
                if (deletedRows > 0) {
                    Toast.makeText(this@MainActivity, "Produto excluído!", Toast.LENGTH_SHORT).show()
                    listProducts()
                } else {
                    Toast.makeText(this@MainActivity, "Nenhum produto foi excluído.", Toast.LENGTH_SHORT).show()
                }
            }.onFailure { error ->
                Log.e(TAG, "Erro ao excluir produto", error)
                Toast.makeText(this@MainActivity, error.message ?: "Não foi possível excluir o produto.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun confirmDeleteAll() {
        AlertDialog.Builder(this)
            .setTitle("Excluir todos os produtos?")
            .setMessage("Essa ação remove todos os registros do banco de dados e não pode ser desfeita.")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Excluir") { _, _ ->
                lifecycleScope.launch {
                    runCatching { productDao.deleteAll() }
                        .onSuccess {
                            Toast.makeText(this@MainActivity, "Todos os produtos foram excluídos.", Toast.LENGTH_SHORT).show()
                            listProducts()
                        }
                        .onFailure { error ->
                            Log.e(TAG, "Erro ao excluir todos os produtos", error)
                            Toast.makeText(this@MainActivity, "Não foi possível excluir os produtos.", Toast.LENGTH_LONG).show()
                        }
                }
            }
            .show()
    }

    private fun displayProducts(products: List<Product>, title: String) {
        binding.textListTitle.text = title
        val totalPrices = products.sumOf { it.price }
        val estimatedStockValue = products.sumOf { it.price * it.quantity }
        binding.textSummary.text = buildString {
            append("Produtos exibidos: ${products.size}")
            append("\nSoma dos preços: ${currencyFormatter.format(totalPrices)}")
            append("\nValor estimado do estoque: ${currencyFormatter.format(estimatedStockValue)}")
        }

        if (products.isEmpty()) {
            binding.textProducts.text = "Nenhum produto encontrado. Cadastre um produto ou faça outra busca."
            return
        }

        binding.textProducts.text = products.joinToString("\n\n────────────────────\n\n") { product ->
            buildString {
                append("#${product.id}  ${product.name}")
                append("\nPreço: ${currencyFormatter.format(product.price)}")
                append("\nQuantidade em estoque: ${product.quantity}")
                append("\nDescrição: ${product.description ?: "Sem descrição"}")
            }
        }

        products.forEach { product ->
            Log.i(TAG, "Produto = ${product.name}, Preço = ${currencyFormatter.format(product.price)}, Quantidade = ${product.quantity}, Descrição = ${product.description}")
        }
    }

    private fun showEmptyState() {
        binding.textListTitle.text = "Seus produtos"
        binding.textSummary.text = "Cadastre um produto e toque em Listar produtos para visualizar os registros."
        binding.textProducts.text = "A lista aparecerá aqui. Os dados ficam salvos no SQLite do aparelho."
    }

    private fun parsePrice(value: String): Double? = value.replace(',', '.').toDoubleOrNull()

    companion object {
        private const val TAG = "AppRoomProduto"
    }
}
