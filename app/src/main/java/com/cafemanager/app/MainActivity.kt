package com.cafemanager.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewModelScope
import com.cafemanager.app.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Locale
import java.util.UUID

private val Navy = Color(0xFF173D3A)
private val Accent = Color(0xFF138C77)
private val Bg = Color(0xFFF4F7F6)

fun money(value: Long): String = "₹" + NumberFormat.getNumberInstance(Locale("en", "IN")).format(value)
fun id() = UUID.randomUUID().toString()
fun today() = LocalDate.now().toString()

class CafeViewModel(private val repo: CafeRepository) : ViewModel() {
    val sales = repo.sales.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val expenses = repo.expenses.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val inventory = repo.inventory.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveSale(x: SaleEntity) = viewModelScope.launch { repo.saveSale(x) }
    fun deleteSale(x: SaleEntity) = viewModelScope.launch { repo.deleteSale(x) }
    fun saveExpense(x: ExpenseEntity) = viewModelScope.launch { repo.saveExpense(x) }
    fun deleteExpense(x: ExpenseEntity) = viewModelScope.launch { repo.deleteExpense(x) }
    fun saveInventory(x: InventoryItemEntity) = viewModelScope.launch { repo.saveInventory(x) }
    fun deleteInventory(x: InventoryItemEntity) = viewModelScope.launch { repo.deleteInventory(x) }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val db = AppDatabase.get(this)
        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                CafeViewModel(CafeRepository(db)) as T
        }
        setContent {
            val vm: CafeViewModel = viewModel(factory = factory)
            CafeTheme { CafeApp(vm) }
        }
    }
}

@Composable
fun CafeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Accent,
            onPrimary = Color.White,
            background = Bg,
            surface = Color.White,
            onSurface = Color(0xFF172126),
            secondary = Color(0xFF0C6F60)
        ),
        content = content
    )
}

@Composable
fun CafeApp(vm: CafeViewModel) {
    var tab by rememberSaveable { mutableStateOf(0) }
    var addType by remember { mutableStateOf<String?>(null) }
    val sales by vm.sales.collectAsStateWithLifecycle()
    val expenses by vm.expenses.collectAsStateWithLifecycle()
    val inventory by vm.inventory.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = Bg,
        floatingActionButton = {
            FloatingActionButton(onClick = { addType = "sale" }, containerColor = Accent, contentColor = Color.White) {
                Text("+", style = MaterialTheme.typography.headlineMedium)
            }
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                listOf("Home", "Money", "Analytics", "Inventory", "More").forEachIndexed { i, label ->
                    NavigationBarItem(
                        selected = tab == i,
                        onClick = { tab = i },
                        icon = { Text(listOf("⌂","₹","⌁","▦","•••")[i]) },
                        label = { Text(label) }
                    )
                }
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (tab) {
                0 -> HomeScreen(sales, expenses, inventory, onAdd = { addType = it }, onMoney = { tab = 1 })
                1 -> MoneyScreen(sales, expenses, onAdd = { addType = it }, onDeleteSale = vm::deleteSale, onDeleteExpense = vm::deleteExpense)
                2 -> AnalyticsScreen(sales, expenses)
                3 -> InventoryScreen(inventory, onAdd = { addType = "inventory" }, onDelete = vm::deleteInventory)
                else -> MoreScreen(sales, expenses, inventory)
            }
        }
    }

    addType?.let { type ->
        AddEntryDialog(
            type = type,
            onDismiss = { addType = null },
            onSaveSale = { vm.saveSale(it); addType = null },
            onSaveExpense = { vm.saveExpense(it); addType = null },
            onSaveInventory = { vm.saveInventory(it); addType = null }
        )
    }
}

@Composable
fun Header(kicker: String, title: String) {
    Column(Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
        Text(kicker.uppercase(), style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
        Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
fun HomeScreen(sales: List<SaleEntity>, expenses: List<ExpenseEntity>, inventory: List<InventoryItemEntity>, onAdd:(String)->Unit, onMoney:()->Unit) {
    val d = today()
    val ts = sales.filter { it.date == d }.sumOf { it.amount }
    val te = expenses.filter { it.date == d }.sumOf { it.amount }
    LazyColumn(contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Header("Café Manager", if (sales.isEmpty() && expenses.isEmpty()) "Welcome" else "Today") }
        item {
            Card(shape = RoundedCornerShape(23.dp), colors = CardDefaults.cardColors(containerColor = Navy)) {
                Column(Modifier.padding(20.dp)) {
                    Text("TODAY'S SALES", color = Color.White.copy(.7f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Text(money(ts), color = Color.White, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black)
                    Text("${sales.count { it.date == d }} sale entries · estimated profit ${money(ts-te)}", color = Color.White.copy(.8f))
                }
            }
        }
        item { Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard("Expenses", money(te), Modifier.weight(1f))
            StatCard("Est. Profit", money(ts-te), Modifier.weight(1f))
        }}
        item { Text("Quick actions", fontWeight = FontWeight.Bold) }
        item { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Quick("Sale","＋",Modifier.weight(1f)){onAdd("sale")}; Quick("Expense","−",Modifier.weight(1f)){onAdd("expense")}; Quick("Purchase","▣",Modifier.weight(1f)){onAdd("purchase")}; Quick("Stock","□",Modifier.weight(1f)){onAdd("inventory")}
        }}
        item { Text("Recent activity", fontWeight = FontWeight.Bold) }
        if (sales.isEmpty() && expenses.isEmpty()) item {
            Card { Column(Modifier.padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("No data yet", fontWeight = FontWeight.Bold); Spacer(Modifier.height(6.dp))
                Text("Add your first sale or expense. Nothing is pre-filled.", color = Color.Gray)
            }}
        } else item {
            Card { Column(Modifier.padding(14.dp)) {
                sales.take(4).forEach { TxRow("Sale", money(it.amount), "${it.date} · ${money(it.cash)} cash · ${money(it.upi)} UPI") }
                expenses.take(4).forEach { TxRow(it.category, "−${money(it.amount)}", it.date) }
            }}
        }
        item { Text("Inventory", fontWeight = FontWeight.Bold) }
        item { Text("${inventory.count { it.quantity <= it.minimumStock }} low-stock item(s)", color = if (inventory.any { it.quantity <= it.minimumStock }) Color(0xFFB33B3B) else Color(0xFF137A58)) }
    }
}

@Composable fun StatCard(label:String,value:String,modifier:Modifier=Modifier){Card(modifier){Column(Modifier.padding(14.dp)){Text(label.uppercase(),style=MaterialTheme.typography.labelSmall,color=Color.Gray,fontWeight=FontWeight.Bold);Text(value,fontWeight=FontWeight.ExtraBold,style=MaterialTheme.typography.titleLarge)}}}
@Composable fun Quick(label:String,icon:String,modifier:Modifier=Modifier,onClick:()->Unit){Button(onClick=onClick,modifier=modifier,shape=RoundedCornerShape(14.dp),contentPadding=PaddingValues(5.dp)){Column(horizontalAlignment=Alignment.CenterHorizontally){Text(icon);Text(label,style=MaterialTheme.typography.labelSmall)}}}
@Composable fun TxRow(title:String,amount:String,sub:String){Row(Modifier.fillMaxWidth().padding(vertical=9.dp),horizontalArrangement=Arrangement.SpaceBetween){Column(Modifier.weight(1f)){Text(title,fontWeight=FontWeight.Bold);Text(sub,style=MaterialTheme.typography.bodySmall,color=Color.Gray)};Text(amount,fontWeight=FontWeight.Bold)}}

@Composable
fun MoneyScreen(sales:List<SaleEntity>, expenses:List<ExpenseEntity>, onAdd:(String)->Unit, onDeleteSale:(SaleEntity)->Unit, onDeleteExpense:(ExpenseEntity)->Unit) {
    var confirm by remember { mutableStateOf<Any?>(null) }
    LazyColumn(contentPadding=PaddingValues(18.dp), verticalArrangement=Arrangement.spacedBy(12.dp)) {
        item { Header("Finances","Money") }
        item { Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){Button({onAdd("sale")},Modifier.weight(1f)){Text("+ Sale")};Button({onAdd("expense")},Modifier.weight(1f)){Text("+ Expense")}}}
        item { Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){StatCard("Sales",money(sales.sumOf{it.amount}),Modifier.weight(1f));StatCard("Expenses",money(expenses.sumOf{it.amount}),Modifier.weight(1f))}}
        item { Text("Transactions",fontWeight=FontWeight.Bold) }
        items(sales, key={it.id}) { s -> TransactionCard("Sale", money(s.amount), "${s.date} · Cash ${money(s.cash)} · UPI ${money(s.upi)}", onDelete={confirm=s}) }
        items(expenses, key={it.id}) { e -> TransactionCard(e.category, "−${money(e.amount)}", "${e.date} · ${e.paymentMethod}${if(e.item.isNotBlank()) " · ${e.item}" else ""}", onDelete={confirm=e}) }
    }
    confirm?.let { x -> AlertDialog(onDismissRequest={confirm=null}, title={Text("Delete transaction?")}, text={Text("This will remove the transaction and update totals and analytics.")}, confirmButton={TextButton(onClick={if(x is SaleEntity) onDeleteSale(x) else if(x is ExpenseEntity) onDeleteExpense(x);confirm=null}){Text("Delete",color=Color(0xFFB33B3B))}}, dismissButton={TextButton({confirm=null}){Text("Cancel")}}) }
}
@Composable fun TransactionCard(title:String,amount:String,sub:String,onDelete:()->Unit){Card{Row(Modifier.padding(14.dp),verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text(title,fontWeight=FontWeight.Bold);Text(sub,style=MaterialTheme.typography.bodySmall,color=Color.Gray)};Text(amount,fontWeight=FontWeight.ExtraBold);Spacer(Modifier.width(5.dp));TextButton(onClick=onDelete){Text("Delete",color=Color(0xFFB33B3B))}}}}

@Composable
fun AnalyticsScreen(sales:List<SaleEntity>, expenses:List<ExpenseEntity>) {
    var filter by rememberSaveable { mutableStateOf("All time") }
    var customStart by rememberSaveable { mutableStateOf(today()) }
    var customEnd by rememberSaveable { mutableStateOf(today()) }
    val (start,end) = range(filter,customStart,customEnd)
    val fs = sales.filter { it.date in start..end }
    val fe = expenses.filter { it.date in start..end }
    val st=fs.sumOf{it.amount}; val et=fe.sumOf{it.amount}; val p=st-et
    LazyColumn(contentPadding=PaddingValues(18.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        item { Header("Performance","Analytics") }
        item { Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(7.dp)){listOf("All time","Today","7 days","This month","Previous month","Custom").forEach{TextButton(onClick={filter=it}){Text(if(filter==it) "✓ $it" else it)}}}}
        if(filter=="Custom") item { Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){OutlinedTextField(customStart,{customStart=it},label={Text("Start")},modifier=Modifier.weight(1f));OutlinedTextField(customEnd,{customEnd=it},label={Text("End")},modifier=Modifier.weight(1f))}}
        item { Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){StatCard("Sales",money(st),Modifier.weight(1f));StatCard("Expenses",money(et),Modifier.weight(1f))}}
        item { Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){StatCard("Est. Profit",money(p),Modifier.weight(1f));StatCard("Margin",if(st==0L)"0%" else "${(p*100/st)}%",Modifier.weight(1f))}}
        item { Text("Expense categories",fontWeight=FontWeight.Bold) }
        item { Card { Column(Modifier.padding(14.dp)){val cats=fe.groupBy{it.category}.mapValues{(_,v)->v.sumOf{it.amount}};if(cats.isEmpty())Text("No expenses in this period.",color=Color.Gray) else cats.toList().sortedByDescending{it.second}.forEach{(k,v)->Row(Modifier.fillMaxWidth().padding(8.dp),horizontalArrangement=Arrangement.SpaceBetween){Text(k);Text(money(v),fontWeight=FontWeight.Bold)}}}}}
        item { Text("Interpretation",fontWeight=FontWeight.Bold) }
        item { Card { Text(if(fs.isEmpty()&&fe.isEmpty())"No records exist in the selected period." else "Estimated profit uses only recorded sales minus recorded expenses. Missing days are not treated as zero.",Modifier.padding(15.dp),color=Color.Gray)}}
    }
}
fun range(filter:String,start:String,end:String):Pair<String,String>{
    val t=LocalDate.now()
    return when(filter){
        "Today"->t.toString() to t.toString()
        "7 days"->t.minusDays(6).toString() to t.toString()
        "This month"->t.withDayOfMonth(1).toString() to t.toString()
        "Previous month"->{val p=t.minusMonths(1);p.withDayOfMonth(1).toString() to p.withDayOfMonth(p.lengthOfMonth()).toString()}
        "Custom"->start to end
        else->"0000-01-01" to "9999-12-31"
    }
}

@Composable
fun InventoryScreen(items:List<InventoryItemEntity>,onAdd:()->Unit,onDelete:(InventoryItemEntity)->Unit){
    var confirm by remember{mutableStateOf<InventoryItemEntity?>(null)}
    LazyColumn(contentPadding=PaddingValues(18.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
        item{Header("Stock","Inventory")}
        item{Button(onClick=onAdd,modifier=Modifier.fillMaxWidth()){Text("+ Add inventory item")}}
        if(items.isEmpty()) item{Card{Text("No inventory yet. Add items you regularly keep in stock.",Modifier.padding(25.dp),color=Color.Gray)}}
        items(items,key={it.id}){x->Card{Row(Modifier.padding(15.dp),verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text(x.name,fontWeight=FontWeight.Bold);Text("${x.quantity} ${x.unit} · minimum ${x.minimumStock} ${x.unit}",style=MaterialTheme.typography.bodySmall,color=Color.Gray)};Text(if(x.quantity<=x.minimumStock)"LOW" else "OK",color=if(x.quantity<=x.minimumStock)Color(0xFFB33B3B) else Color(0xFF137A58),fontWeight=FontWeight.Bold);TextButton({confirm=x}){Text("Delete")}}}}
    }
    confirm?.let{x->AlertDialog(onDismissRequest={confirm=null},title={Text("Delete inventory item?")},text={Text(x.name)},confirmButton={TextButton({onDelete(x);confirm=null}){Text("Delete",color=Color.Red)}},dismissButton={TextButton({confirm=null}){Text("Cancel")}})}
}

@Composable fun MoreScreen(sales:List<SaleEntity>,expenses:List<ExpenseEntity>,inventory:List<InventoryItemEntity>){
    LazyColumn(contentPadding=PaddingValues(18.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){item{Header("Business","More")};item{Card{Column(Modifier.padding(16.dp)){listOf("Business profile","Backup & restore","Export CSV / Excel","Suppliers","Expense categories","Day status").forEach{Row(Modifier.fillMaxWidth().padding(vertical=13.dp),horizontalArrangement=Arrangement.SpaceBetween){Text(it,fontWeight=FontWeight.Bold);Text("›",color=Color.Gray)}}}}};item{Card{Column(Modifier.padding(16.dp)){Text("Current data",fontWeight=FontWeight.Bold);Text("${sales.size} sales · ${expenses.size} expenses · ${inventory.size} inventory items",color=Color.Gray)}}};item{Text("No ads, subscriptions or revenue model are included in V1.",color=Color.Gray,style=MaterialTheme.typography.bodySmall)}}}

@Composable
fun AddEntryDialog(type:String,onDismiss:()->Unit,onSaveSale:(SaleEntity)->Unit,onSaveExpense:(ExpenseEntity)->Unit,onSaveInventory:(InventoryItemEntity)->Unit){
    var amount by remember{mutableStateOf("")}; var cash by remember{mutableStateOf("")}; var upi by remember{mutableStateOf("")}
    var category by remember{mutableStateOf("Food & Raw Materials")}; var payment by remember{mutableStateOf("UPI")}; var item by remember{mutableStateOf("")}
    var qty by remember{mutableStateOf("")}; var unit by remember{mutableStateOf("")}; var unitPrice by remember{mutableStateOf("")}; var min by remember{mutableStateOf("")}; var supplier by remember{mutableStateOf("")}; var notes by remember{mutableStateOf("")}
    var date by remember{mutableStateOf(today())}; var orders by remember{mutableStateOf("")}
    val title=when(type){"sale"->"Add Sale";"expense"->"Add Expense";"purchase"->"Add Purchase";else->"Add Inventory"}
    AlertDialog(onDismissRequest=onDismiss,title={Text(title)},text={
        Column(Modifier.fillMaxWidth().heightIn(max=540.dp).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(8.dp)){
            if(type=="sale"){
                Field("Total sale amount",amount,{amount=it})
                Field("Cash",cash,{cash=it}); Field("UPI",upi,{upi=it})
                val a=amount.toLongOrNull()?:0; val c=cash.toLongOrNull()?:0; val u=upi.toLongOrNull()?:0
                Text(if(a==0L)"Enter sale amount." else if(a==c+u)"✓ Cash + UPI matches total." else "Cash + UPI must equal ${money(a)}",color=if(a==c+u)Color(0xFF137A58) else Color(0xFFB33B3B))
                Field("Order count (optional)",orders,{orders=it}); Field("Date",date,{date=it}); Field("Notes (optional)",notes,{notes=it})
            } else if(type=="expense"){
                Field("Amount",amount,{amount=it}); Dropdown("Category",category,listOf("Food & Raw Materials","Beverages","Packaging","Staff","Electricity","Gas","Cleaning","Maintenance","Gaming","Marketing","Rent","Delivery","Equipment","Other")){category=it}
                Dropdown("Payment method",payment,listOf("Cash","UPI","Bank","Card","Other")){payment=it}
                Field("What did you buy? (optional)",item,{item=it}); Field("Quantity (optional)",qty,{qty=it}); Field("Unit (optional)",unit,{unit=it}); Field("Unit price (optional)",unitPrice,{unitPrice=it}); Field("Supplier (optional)",supplier,{supplier=it}); Field("Date (optional)",date,{date=it}); Field("Notes (optional)",notes,{notes=it})
            } else if(type=="purchase"){
                Field("Amount",amount,{amount=it}); Field("Item (optional)",item,{item=it}); Field("Quantity (optional)",qty,{qty=it}); Field("Unit (optional)",unit,{unit=it}); Field("Unit price (optional)",unitPrice,{unitPrice=it}); Field("Supplier (optional)",supplier,{supplier=it}); Field("Date",date,{date=it}); Field("Notes",notes,{notes=it})
            } else {
                Field("Item name",item,{item=it}); Field("Quantity",qty,{qty=it}); Field("Unit (optional)",unit,{unit=it}); Field("Minimum stock (optional)",min,{min=it}); Field("Purchase price (optional)",unitPrice,{unitPrice=it}); Field("Supplier (optional)",supplier,{supplier=it}); Field("Notes (optional)",notes,{notes=it})
            }
        }
    },confirmButton={
        TextButton(onClick={
            val a=amount.toLongOrNull()?:0; val c=cash.toLongOrNull()?:0; val u=upi.toLongOrNull()?:0
            when(type){
                "sale"->{if(a>0&&a==c+u)onSaveSale(SaleEntity(id(),date,a,c,u,orderCount=orders.toIntOrNull()?:0,notes=notes))}
                "expense"->{if(a>0)onSaveExpense(ExpenseEntity(id(),date,a,category,item,qty.toDoubleOrNull(),unit,unitPrice.toLongOrNull(),payment,supplier,notes))}
                "purchase"->{if(a>0)onSaveExpense(ExpenseEntity(id(),date,a,"Food & Raw Materials",if(item.isBlank())"Purchase" else "Purchase: $item",qty.toDoubleOrNull(),unit,unitPrice.toLongOrNull(),"",supplier,notes))}
                "inventory"->{if(item.isNotBlank()&&qty.toDoubleOrNull()!=null)onSaveInventory(InventoryItemEntity(id(),item,qty.toDouble(),unit.ifBlank{"pcs"},min.toDoubleOrNull()?:0.0,unitPrice.toLongOrNull()?:0,supplier,notes))}
            }
        }){Text("Save")}
    },dismissButton={TextButton(onClick=onDismiss){Text("Cancel")}})
}

@Composable fun Field(label:String,value:String,onValue:(String)->Unit){OutlinedTextField(value,onValue,label={Text(label)},singleLine=true,modifier=Modifier.fillMaxWidth())}
@Composable fun Dropdown(label:String,value:String,options:List<String>,onValue:(String)->Unit){var open by remember{mutableStateOf(false)};Box{OutlinedButton(onClick={open=true},modifier=Modifier.fillMaxWidth()){Text("$label: $value")};DropdownMenu(open,{open=false}){options.forEach{DropdownMenuItem(text={Text(it)},onClick={onValue(it);open=false})}}}}
