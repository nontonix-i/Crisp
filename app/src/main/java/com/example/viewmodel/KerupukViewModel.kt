package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.model.KasTransaksi
import com.example.data.model.PenjualanHarian
import com.example.data.model.PinjamanSetoran
import com.example.data.model.StokMutasi
import com.example.data.model.TransferPedagang
import com.example.data.repository.ActiveLoanWithBalance
import com.example.data.repository.DashboardSummary
import com.example.data.repository.KerupukRepository
import com.example.utils.FormatUtils
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private data class StockSummary(
    val pool: Int,
    val tangan: Int,
    val todaySale: PenjualanHarian?,
    val nabungTotal: Int
)

private data class FinanceSummary(
    val kewajiban: Int,
    val sudahSetor: Int,
    val kasMasuk: Int,
    val kasKeluar: Int
)

class KerupukViewModel(
    application: Application,
    private val repository: KerupukRepository
) : AndroidViewModel(application) {

    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent.asSharedFlow()

    private val _activeLoansWithBalance = MutableStateFlow<List<ActiveLoanWithBalance>>(emptyList())
    val activeLoansWithBalance: StateFlow<List<ActiveLoanWithBalance>> = _activeLoansWithBalance.asStateFlow()

    init {
        viewModelScope.launch {
            repository.ensureDefaultBarang()
            refreshActiveLoans()
        }
    }

    fun refreshActiveLoans() {
        viewModelScope.launch {
            _activeLoansWithBalance.value = repository.getActiveLoansWithBalance()
        }
    }

    val stokPool = repository.stokPool.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val stokTangan = repository.stokTangan.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val todayMutasi = repository.observeTodayMutasi().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val recentMutasi = repository.recentMutasi.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allMutasi = repository.allMutasi.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val todayPenjualan = repository.observeTodayPenjualan().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val allPenjualan = repository.allPenjualan.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allTransfer = repository.allTransfer.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val recentKas = repository.recentKas.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allKas = repository.allKas.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allPinjaman = repository.allPinjaman.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val stockSummaryFlow = combine(
        stokPool,
        stokTangan,
        todayPenjualan,
        repository.totalNabungWajib
    ) { pool, tangan, todaySale, nabungTotal ->
        StockSummary(pool, tangan, todaySale, nabungTotal)
    }

    private val financeSummaryFlow = combine(
        repository.totalKewajibanSetor,
        repository.totalSudahSetor,
        repository.totalKasMasuk,
        repository.totalKasKeluar
    ) { kewajiban, sudahSetor, kasMasuk, kasKeluar ->
        FinanceSummary(kewajiban, sudahSetor, kasMasuk, kasKeluar)
    }

    // Combine dashboard metrics
    val dashboardSummary: StateFlow<DashboardSummary> = combine(
        stockSummaryFlow,
        financeSummaryFlow,
        _activeLoansWithBalance
    ) { stock, finance, loans ->
        val lakuHariIni = stock.todaySale?.jumlah_laku ?: 0
        val pendapatanHariIni = stock.todaySale?.pendapatan ?: 0
        val nabungWajibHariIni = stock.todaySale?.nabung_wajib ?: 0
        val sisaKewajiban = finance.kewajiban - finance.sudahSetor
        val saldoUmum = finance.kasMasuk - finance.kasKeluar

        val piutang = loans.filter { it.loan.pihak != "Sendiri" }.sumOf { it.remainingBalance }
        val prive = loans.filter { it.loan.pihak == "Sendiri" }.sumOf { it.remainingBalance }
        val totalPinjaman = piutang + prive

        val labaKotor = pendapatanHariIni - (lakuHariIni * 11000)
        val labaBersih = labaKotor - nabungWajibHariIni

        // Saldo kas setoran yang tersedia dipegang user
        val uangSetoranTersedia = (sisaKewajiban - totalPinjaman).coerceAtLeast(0)

        DashboardSummary(
            stokPool = stock.pool,
            stokTangan = stock.tangan,
            lakuHariIni = lakuHariIni,
            pendapatanHariIni = pendapatanHariIni,
            nabungWajibHariIni = nabungWajibHariIni,
            totalKewajibanSetor = finance.kewajiban,
            totalSudahSetor = finance.sudahSetor,
            sisaKewajibanSetor = sisaKewajiban,
            saldoKasUmum = saldoUmum,
            saldoKasSetoranTersedia = uangSetoranTersedia,
            totalNabungWajib = stock.nabungTotal,
            piutangPedagang = piutang,
            priveSendiri = prive,
            totalPinjamanAktif = totalPinjaman,
            labaKotorHariIni = labaKotor,
            labaBersihHariIni = labaBersih
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        DashboardSummary()
    )

    // Actions
    fun catatLaku(jumlah: Int, hargaJual: Int, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val res = repository.catatPenjualanHariIni(jumlah, hargaJual)
            if (res.isSuccess) {
                _snackbarEvent.emit("Tersimpan ✓ Penjualan dicatat")
                onResult(true, "Tersimpan ✓")
            } else {
                val err = res.exceptionOrNull()?.message ?: "Gagal mencatat penjualan"
                _snackbarEvent.emit(err)
                onResult(false, err)
            }
        }
    }

    fun ambilJatahPool(jumlah: Int, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val res = repository.ambilJatahPool(jumlah)
            if (res.isSuccess) {
                _snackbarEvent.emit("Tersimpan ✓ $jumlah pack diambil ke tangan")
                onResult(true, "Tersimpan ✓")
            } else {
                val err = res.exceptionOrNull()?.message ?: "Gagal mengambil jatah"
                _snackbarEvent.emit(err)
                onResult(false, err)
            }
        }
    }

    fun balikSisaPool(jumlah: Int, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val res = repository.balikSisaPool(jumlah)
            if (res.isSuccess) {
                _snackbarEvent.emit("Tersimpan ✓ $jumlah pack kembali ke pool")
                onResult(true, "Tersimpan ✓")
            } else {
                val err = res.exceptionOrNull()?.message ?: "Gagal mengembalikan sisa"
                _snackbarEvent.emit(err)
                onResult(false, err)
            }
        }
    }

    fun masukPabrik(jumlah: Int, keterangan: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val res = repository.masukDariPabrik(jumlah, keterangan)
            if (res.isSuccess) {
                _snackbarEvent.emit("Tersimpan ✓ $jumlah pack masuk")
                onResult(true, "Tersimpan ✓")
            } else {
                val err = res.exceptionOrNull()?.message ?: "Gagal menambah stok"
                _snackbarEvent.emit(err)
                onResult(false, err)
            }
        }
    }

    fun transferPedagang(arah: String, nama: String, jumlah: Int, keterangan: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val res = repository.transferPedagang(arah, nama, jumlah, keterangan)
            if (res.isSuccess) {
                val dir = if (arah == "keluar") "ke" else "dari"
                _snackbarEvent.emit("Tersimpan ✓ Transfer $jumlah pack $dir $nama")
                onResult(true, "Tersimpan ✓")
            } else {
                val err = res.exceptionOrNull()?.message ?: "Gagal transfer pack"
                _snackbarEvent.emit(err)
                onResult(false, err)
            }
        }
    }

    fun setorWajib(jumlah: Int, keterangan: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val res = repository.setorWajib(jumlah, keterangan)
            if (res.isSuccess) {
                _snackbarEvent.emit("Tersimpan ✓ Setoran ${FormatUtils.formatRupiah(jumlah)} tercatat")
                onResult(true, "Tersimpan ✓")
            } else {
                val err = res.exceptionOrNull()?.message ?: "Gagal mencatat setoran"
                _snackbarEvent.emit(err)
                onResult(false, err)
            }
        }
    }

    fun pinjamUang(pihak: String, jumlah: Int, keterangan: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val res = repository.pinjamUangSetoran(pihak, jumlah, keterangan)
            if (res.isSuccess) {
                refreshActiveLoans()
                _snackbarEvent.emit("Tersimpan ✓ Pinjaman ${FormatUtils.formatRupiah(jumlah)} ($pihak)")
                onResult(true, "Tersimpan ✓")
            } else {
                val err = res.exceptionOrNull()?.message ?: "Gagal mencatat pinjaman"
                _snackbarEvent.emit(err)
                onResult(false, err)
            }
        }
    }

    fun kembalikanPinjaman(loanId: Long, jumlah: Int, keterangan: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val res = repository.kembalikanPinjaman(loanId, jumlah, keterangan)
            if (res.isSuccess) {
                refreshActiveLoans()
                _snackbarEvent.emit("Tersimpan ✓ Pengembalian ${FormatUtils.formatRupiah(jumlah)} diterima")
                onResult(true, "Tersimpan ✓")
            } else {
                val err = res.exceptionOrNull()?.message ?: "Gagal mencatat pengembalian"
                _snackbarEvent.emit(err)
                onResult(false, err)
            }
        }
    }

    fun catatKas(tipe: String, kategori: String, jumlah: Int, keterangan: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val res = repository.catatKasUmum(tipe, kategori, jumlah, keterangan)
            if (res.isSuccess) {
                _snackbarEvent.emit("Tersimpan ✓ Transaksi kas dicatat")
                onResult(true, "Tersimpan ✓")
            } else {
                val err = res.exceptionOrNull()?.message ?: "Gagal mencatat kas"
                _snackbarEvent.emit(err)
                onResult(false, err)
            }
        }
    }

    // Seed initial consignment stock if stock is empty
    fun seedInitialStockIfNeeded() {
        viewModelScope.launch {
            val tangan = repository.stokTangan.first()
            if (tangan == 0) {
                repository.ambilJatahPool(60)
                _snackbarEvent.emit("Ambil jatah awal berhasil (Stok Saya: 60 pack)")
            }
        }
    }
}

class KerupukViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(KerupukViewModel::class.java)) {
            val db = AppDatabase.getInstance(application)
            val repo = KerupukRepository(db.kerupukDao())
            return KerupukViewModel(application, repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
