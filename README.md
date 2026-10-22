# AppRoomProduto: Cadastro de Produtos com Room SQLite

**Alunos:** `Eduardo Santos Nascimento`  `Pedro Ravazzani e Silva`

**RA:** `241590`  `242904`

**Disciplina:** Programação para Dispositivos Móveis  

## Sobre o projeto

Aplicativo Android feito em Kotlin para cadastrar e consultar produtos com persistência local usando Room sobre SQLite. A interface usa XML e ViewBinding. Os dados ficam armazenados no aparelho entre uma execução e outra.

## Tecnologias

- Kotlin

- Android Views + XML e ViewBinding

- Room 2.6.1 / SQLite

- Corrotinas com `lifecycleScope`

- Material Components para os elementos da interface

- Min SDK: API 24; compile/target SDK: API 35

## Funcionalidades

- Cadastrar produto com nome, preço, descrição opcional e quantidade em estoque.

- Validar campos obrigatórios e impedir preços vazios, iguais a zero ou negativos.

- Limpar os campos após o cadastro e devolver o foco ao nome.

- Listar produtos na própria tela e registrar os resultados no Logcat.

- Mostrar quantidade de registros, soma dos preços e valor estimado do estoque.

- Pesquisar produtos por parte do nome, sem diferenciar maiúsculas e minúsculas.

- Excluir todos os produtos ou excluir um produto específico pelo ID.

- Atualizar o preço de um produto pelo ID.

## Prints do app

![Tela inicial](docs/prints/telaLogin.png)

![LogCat](docs/prints/logcat.png)


## Parte A: Complemento prático

### A1. Limpar os campos após inserir

**O que foi feito:** depois de inserir um produto com sucesso, o app limpa nome, preço, descrição e quantidade e devolve o foco ao campo Nome.

**Trecho principal (`MainActivity.kt`):**

```kotlin

private fun clearProductFields() {

    binding.editProductName.text?.clear()

    binding.editProductPrice.text?.clear()

    binding.editProductDescription.text?.clear()

    binding.editProductQuantity.text?.clear()

    binding.editProductName.requestFocus()

}

```

O método é chamado somente no caminho de sucesso da inserção. Após preencher os campos e cadastrar um produto, tire um print mostrando os campos limpos.

![A1p1](docs/prints/A1 1.png)

Após clicar em cadastrar:

![A1p2](docs/prints/A1 2.png)

### A2. Validação de preço

**O que foi feito:** o preço é convertido para número aceitando ponto ou vírgula decimal. Se estiver vazio, inválido, igual a zero ou negativo, o cadastro é interrompido e o erro aparece no campo.

**Trecho principal (`MainActivity.kt`):**

```kotlin

val price = parsePrice(priceText)

if (price == null || price <= 0.0) {

    binding.editProductPrice.error = "Informe um preço válido maior que zero"

    binding.editProductPrice.requestFocus()

    return

}

private fun parsePrice(value: String): Double? =

    value.replace(',', '.').toDoubleOrNull()

```

Tire um print tentando cadastrar com preço `0` ou vazio, mostrando a mensagem no campo.

![A2](docs/prints/A2.png)

### A3. Lista de produtos na tela

**O que foi feito:** a consulta usa o DAO e exibe os registros no `TextView` `textProducts`, em vez de depender apenas do Logcat. Cada item mostra ID, nome, preço, quantidade e descrição.

**Trechos principais (`ProductDao.kt` e `MainActivity.kt`):**

```kotlin

@Query("SELECT * FROM product ORDER BY name COLLATE NOCASE ASC")

suspend fun getAll(): List<Product>

```

```kotlin

private fun listProducts() {

    lifecycleScope.launch {

        runCatching { productDao.getAll() }

            .onSuccess { products -> displayProducts(products, "Todos os produtos") }

    }

}

```

Tire um print com pelo menos dois produtos aparecendo na lista da tela.

![A3](docs/prints/A3.png)

### A4. Total do estoque

**O que foi feito:** junto da lista, a tela mostra quantos registros estão sendo exibidos, a soma dos preços unitários e o valor estimado do estoque (preço multiplicado pela quantidade).

**Trecho principal (`MainActivity.kt`):**

```kotlin

val totalPrices = products.sumOf { it.price }

val estimatedStockValue = products.sumOf { it.price * it.quantity }

binding.textSummary.text = buildString {

    append("Produtos exibidos: ${products.size}")

    append("\nSoma dos preços: ${currencyFormatter.format(totalPrices)}")

    append("\nValor estimado do estoque: ${currencyFormatter.format(estimatedStockValue)}")

}

```

Tire um print da lista com os indicadores numéricos visíveis. Ao fazer uma busca, os totais correspondem aos resultados mostrados; ao listar todos, correspondem ao cadastro inteiro.

![A4](docs/prints/A3.png)

### A5. Buscar por nome

**O que foi feito:** foi criada uma consulta `LIKE` no DAO. A tela recebe parte do nome e acrescenta `%` antes e depois do texto para retornar correspondências parciais.

**Trechos principais (`ProductDao.kt` e `MainActivity.kt`):**

```kotlin

@Query("SELECT * FROM product WHERE name LIKE :nome COLLATE NOCASE ORDER BY name COLLATE NOCASE ASC")

suspend fun buscarPorNome(nome: String): List<Product>

```

```kotlin

val text = binding.editSearchName.text.toString().trim()

lifecycleScope.launch {

    val products = productDao.buscarPorNome("%$text%")

    displayProducts(products, "Resultado da busca por \\"$text\\"")

}

```

Tire um print de uma busca que encontre um produto pelo nome ou por parte dele.

![A5](docs/prints/A5.png)

### A6. Excluir produtos

**O que foi feito:** o botão de exclusão geral pede confirmação antes de apagar todos os registros. Também existe exclusão individual por ID, buscando a entidade e removendo-a com `@Delete`.

**Trechos principais (`ProductDao.kt` e `MainActivity.kt`):**

```kotlin

@Query("DELETE FROM product")

suspend fun deleteAll(): Int

@Delete

suspend fun delete(product: Product): Int

```

```kotlin

val product = productDao.getById(id)

    ?: throw IllegalArgumentException("Não existe produto com ID $id.")

productDao.delete(product)

```

Tire prints dos botões de exclusão e, se possível, do diálogo de confirmação ou da lista depois da exclusão. Faça isso em um ambiente de teste, pois apagar registros é uma ação destrutiva.

![A6](docs/prints/A6.png)

### A7. Atualizar produto

**O que foi feito:** para atualizar, o app recebe o ID e o novo preço, procura o produto existente e grava uma cópia dele com o preço alterado usando `@Update`.

**Trechos principais (`ProductDao.kt` e `MainActivity.kt`):**

```kotlin

@Query("SELECT * FROM product WHERE id = :id LIMIT 1")

suspend fun getById(id: Int): Product?

@Update

suspend fun update(product: Product): Int

```

```kotlin

val current = productDao.getById(id)

    ?: throw IllegalArgumentException("Não existe produto com ID $id.")

productDao.update(current.copy(price = newPrice))

```

Tire um print mostrando o ID usado e a lista após a alteração do preço.

![A7](docs/prints/A7.png)

### A8. Novo campo na tabela: quantidade

**O que foi feito:** a entidade `Product` ganhou o campo `quantity: Int` e a tela ganhou um campo para informar o estoque. A versão do banco foi elevada para `2`.

**Trechos principais (`Product.kt` e `AppDatabase.kt`):**

```kotlin

@ColumnInfo(name = "quantity")

val quantity: Int = 0

```

```kotlin

@Database(entities = [Product::class], version = 2, exportSchema = false)

abstract class AppDatabase : RoomDatabase()

```

**O que acontece com dados de uma versão anterior:** este projeto usa `fallbackToDestructiveMigration()` para simplificar o exercício. Se um aparelho já tiver o banco da versão 1 e o app for atualizado para a versão 2 sem uma migração definida, o Room recriará a tabela e os dados antigos serão apagados. Em produção, seria necessário criar uma `Migration` que adicione a coluna `quantity` preservando os registros, ou definir uma estratégia explícita para os dados.

![A8](docs/prints/A8.png)

## Parte B: Questões dissertativas

### B1. O que é persistência de dados? O que aconteceria se usássemos apenas uma `MutableList` na `MainActivity`?

Persistência de dados é a capacidade de guardar informações para que elas continuem disponíveis mesmo depois que o aplicativo é fechado. Neste projeto, os produtos são gravados em um banco SQLite gerenciado pelo Room, que mantém os registros no armazenamento local do aparelho. Quando a tela é aberta novamente, o método `getAll()` consulta o banco e recupera os produtos salvos anteriormente. Se usássemos apenas uma `MutableList` dentro da `MainActivity`, os dados existiriam somente enquanto aquela instância estivesse na memória. Ao fechar o processo ou recriar a Activity, a lista poderia ser perdida e os produtos precisariam ser cadastrados novamente. Por isso, a lista em memória não substituiria a persistência oferecida pelo Room.

### B2. Qual é a diferença entre usar SQLite diretamente e usar Room? Cite pelo menos duas vantagens do Room.

Com SQLite diretamente, o programador costuma gerenciar a criação do banco, escrever os comandos SQL e converter manualmente os resultados das consultas em objetos Kotlin. O Room funciona como uma camada de abstração sobre o SQLite e usa anotações como `@Entity`, `@Dao`, `@Query`, `@Insert` e `@Update` para gerar parte desse trabalho. Uma vantagem é que o código fica mais organizado, porque a definição dos dados, as consultas e a tela ficam separadas. Outra vantagem é que o Room verifica consultas SQL durante a compilação, ajudando a identificar certos erros antes de executar o aplicativo. Ele também integra as operações com corrotinas e oferece suporte a migrações, facilitando o desenvolvimento e a manutenção do banco.

### B3. Explique o papel de Entity, DAO e Database e como se relacionam.

A `Entity` representa a estrutura de uma tabela do banco de dados. Neste app, `Product` define as colunas `id`, `name`, `price`, `description` e `quantity`, e cada objeto representa um produto cadastrado. O `DAO` contém as operações que podem ser feitas nessa tabela, como inserir, listar, buscar por nome, atualizar preço e excluir registros. Já `AppDatabase` declara quais entidades fazem parte do banco e fornece acesso ao `ProductDao`. A tela `MainActivity` chama os métodos do DAO, sem precisar executar comandos SQL diretamente. Assim, a entidade descreve os dados, o DAO define como acessá-los e o Database conecta essas partes ao SQLite.

### B4. Por que não acessar o banco na thread principal? O que é ANR? Como `suspend` e `lifecycleScope.launch` ajudam?

A thread principal do Android é responsável por desenhar a interface e responder a toques. Se uma operação de banco demorar nessa thread, a tela pode congelar e o sistema pode mostrar um aviso de ANR, sigla de **Application Not Responding**. O ANR indica que o aplicativo deixou de responder por tempo suficiente para o sistema considerar a interface travada. O modificador `suspend` permite que uma chamada seja suspensa enquanto aguarda; no caso das funções DAO do Room, a biblioteca executa o acesso ao banco de forma apropriada para não bloquear a UI. `suspend`, isoladamente, não muda automaticamente qualquer função para uma thread de fundo. `lifecycleScope.launch` inicia as chamadas em uma corrotina associada ao ciclo de vida da Activity, que é cancelada quando ela é destruída. Assim, inserções e consultas podem ocorrer sem prender a tela durante a operação.

### B5. O que faz `fallbackToDestructiveMigration()` e por que é perigoso? O que é uma `Migration`?

O método `fallbackToDestructiveMigration()` permite que o Room recrie o banco quando detecta uma mudança de versão para a qual não existe um caminho de migração configurado. Isso facilita exercícios e protótipos, mas pode apagar todos os dados já cadastrados durante uma atualização. Em um aplicativo publicado na Play Store, perder o estoque ou os dados dos usuários sem aviso seria um problema sério. Uma `Migration` é um procedimento que descreve como transformar o esquema antigo em um novo, por exemplo, acrescentando a coluna `quantity` sem apagar as linhas existentes. Neste projeto, a versão foi alterada para `2` e o fallback destrutivo foi mantido para demonstrar o comportamento do tutorial. Em um sistema real, eu criaria e testaria uma migração explícita para preservar as informações.

### B6. Qual é a vantagem do ViewBinding em relação ao `findViewById`?

O ViewBinding gera uma classe de ligação a partir do XML da tela; neste caso, a classe gerada é `ActivityMainBinding`. Com ela, a Activity acessa os componentes por nomes como `binding.editProductName` e `binding.btnInserirProduto`. Isso evita repetir chamadas a `findViewById` e deixa o código mais legível. Como os campos são gerados a partir dos IDs presentes no layout, vários erros de referência incorreta podem ser identificados durante a compilação. Também reduz o risco de fazer um cast manual para um tipo de componente errado. Se um componente for removido ou tiver seu ID alterado no XML, o código que usa o binding antigo deixa de compilar, ajudando a localizar a inconsistência.

### B7. Por que criar o banco como Singleton em apps maiores?

Um Singleton permite reutilizar uma única instância do banco de dados durante a execução do aplicativo. Neste projeto, `AppDatabase.getInstance()` guarda essa instância em uma propriedade compartilhada e usa sincronização para evitar a criação concorrente de múltiplas instâncias. Essa abordagem reduz recursos desperdiçados e deixa o acesso ao banco centralizado. Se cada tela criasse sua própria instância, poderia haver várias conexões, mais trabalho de inicialização e comportamentos inconsistentes na configuração do banco. Além disso, a manutenção ficaria mais difícil porque cada tela poderia ter uma configuração diferente. Em projetos maiores, centralizar a instância favorece a organização e torna mais fácil fornecer o mesmo DAO às várias telas.

### B8. Reflexão pessoal: quais foram as principais dificuldades e como foram resolvidas? Em qual app real Room seria útil?

Uma das principais dificuldades que encontrei neste projeto foi compreender como configurar o Room e conectar o banco de dados com a interface do aplicativo. Também tive que entender como cadastrar, listar, buscar, atualizar e excluir produtos, além de validar os campos para evitar informações incorretas. Essas dificuldades foram resolvidas com a análise dos códigos, a organização das classes e a realização de testes no Android Studio. Com isso, consegui compreender melhor como funciona a persistência de dados em aplicativos Android. Um exemplo de aplicativo real em que o Room seria útil é um sistema de controle de estoque de uma loja. Ele permitiria cadastrar produtos, registrar preços e quantidades disponíveis e consultar essas informações mesmo quando o aplicativo fosse fechado, pois os dados ficariam armazenados localmente no dispositivo.#
