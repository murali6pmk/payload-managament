package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.PayrollRecord
import com.example.data.model.PayrollStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface PayrollDao {
    @Query("SELECT * FROM payroll_records ORDER BY year DESC, month DESC, id DESC")
    fun getAllPayrolls(): Flow<List<PayrollRecord>>

    @Query("SELECT * FROM payroll_records WHERE employeeId = :employeeId ORDER BY year DESC, month DESC")
    fun getPayrollsForEmployee(employeeId: Long): Flow<List<PayrollRecord>>

    @Query("SELECT * FROM payroll_records WHERE monthYear = :monthYear ORDER BY employeeName ASC")
    fun getPayrollsForMonth(monthYear: String): Flow<List<PayrollRecord>>

    @Query("SELECT * FROM payroll_records WHERE id = :id LIMIT 1")
    suspend fun getPayrollById(id: Long): PayrollRecord?

    @Query("SELECT * FROM payroll_records WHERE employeeId = :employeeId AND monthYear = :monthYear LIMIT 1")
    suspend fun getPayrollForEmployeeMonth(employeeId: Long, monthYear: String): PayrollRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayroll(payroll: PayrollRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayrolls(payrolls: List<PayrollRecord>)

    @Update
    suspend fun updatePayroll(payroll: PayrollRecord)

    @Query("UPDATE payroll_records SET status = :status, paymentDate = :paymentDate WHERE id = :id")
    suspend fun updatePayrollStatus(id: Long, status: PayrollStatus, paymentDate: String)

    @Query("DELETE FROM payroll_records WHERE id = :id")
    suspend fun deletePayroll(id: Long)
}
