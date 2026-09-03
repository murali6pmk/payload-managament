package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.LeaveRequest
import com.example.data.model.LeaveStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface LeaveDao {
    @Query("SELECT * FROM leave_requests ORDER BY appliedOn DESC, id DESC")
    fun getAllLeaves(): Flow<List<LeaveRequest>>

    @Query("SELECT * FROM leave_requests WHERE employeeId = :employeeId ORDER BY appliedOn DESC")
    fun getLeavesForEmployee(employeeId: Long): Flow<List<LeaveRequest>>

    @Query("SELECT * FROM leave_requests WHERE status = :status ORDER BY appliedOn DESC")
    fun getLeavesByStatus(status: LeaveStatus): Flow<List<LeaveRequest>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeave(leave: LeaveRequest): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeaves(leaves: List<LeaveRequest>)

    @Update
    suspend fun updateLeave(leave: LeaveRequest)

    @Query("UPDATE leave_requests SET status = :status, adminComments = :comments, reviewedBy = :reviewedBy WHERE id = :id")
    suspend fun updateLeaveStatus(id: Long, status: LeaveStatus, comments: String?, reviewedBy: String?)

    @Query("DELETE FROM leave_requests WHERE id = :id")
    suspend fun deleteLeave(id: Long)
}
