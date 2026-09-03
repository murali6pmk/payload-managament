package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance_records ORDER BY date DESC, id DESC")
    fun getAllAttendance(): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE employeeId = :employeeId ORDER BY date DESC")
    fun getAttendanceForEmployee(employeeId: Long): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE date = :date ORDER BY employeeName ASC")
    fun getAttendanceByDate(date: String): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE employeeId = :employeeId AND date = :date LIMIT 1")
    suspend fun getTodayRecordForEmployee(employeeId: Long, date: String): AttendanceRecord?

    @Query("UPDATE attendance_records SET status = :status WHERE id = :id")
    suspend fun updateAttendanceStatus(id: Long, status: AttendanceStatus)

    @Query("SELECT * FROM attendance_records WHERE id = :id LIMIT 1")
    suspend fun getAttendanceById(id: Long): AttendanceRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(record: AttendanceRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendanceList(records: List<AttendanceRecord>)

    @Update
    suspend fun updateAttendance(record: AttendanceRecord)

    @Query("DELETE FROM attendance_records WHERE id = :id")
    suspend fun deleteAttendance(id: Long)
}
