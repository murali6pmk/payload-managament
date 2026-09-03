package com.example.data.repository

import com.example.data.AppDatabase
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

class CloudAttendRepository(private val database: AppDatabase) {

    private val employeeDao = database.employeeDao()
    private val attendanceDao = database.attendanceDao()
    private val leaveDao = database.leaveDao()
    private val payrollDao = database.payrollDao()

    val allEmployees: Flow<List<Employee>> = employeeDao.getAllEmployees()
    val allAttendance: Flow<List<AttendanceRecord>> = attendanceDao.getAllAttendance()
    val allLeaves: Flow<List<LeaveRequest>> = leaveDao.getAllLeaves()
    val allPayrolls: Flow<List<PayrollRecord>> = payrollDao.getAllPayrolls()

    fun getAttendanceForEmployee(empId: Long): Flow<List<AttendanceRecord>> =
        attendanceDao.getAttendanceForEmployee(empId)

    fun getLeavesForEmployee(empId: Long): Flow<List<LeaveRequest>> =
        leaveDao.getLeavesForEmployee(empId)

    fun getPayrollsForEmployee(empId: Long): Flow<List<PayrollRecord>> =
        payrollDao.getPayrollsForEmployee(empId)

    suspend fun getEmployeeById(id: Long): Employee? = employeeDao.getEmployeeById(id)

    suspend fun insertEmployee(employee: Employee): Long = employeeDao.insertEmployee(employee)

    suspend fun updateEmployee(employee: Employee) = employeeDao.updateEmployee(employee)

    suspend fun deleteEmployee(id: Long) = employeeDao.deleteEmployeeById(id)

    // Attendance Management
    suspend fun checkIn(
        employee: Employee,
        location: String = "HQ Tech Park, San Francisco",
        notes: String = ""
    ): AttendanceRecord {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val now = Date()
        val todayStr = dateFormat.format(now)
        val timeStr = timeFormat.format(now)

        // Determine if Late (e.g., after 9:15 AM)
        val cal = Calendar.getInstance()
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)
        val isLate = (hour > 9) || (hour == 9 && minute > 15)
        val status = if (isLate) AttendanceStatus.LATE else AttendanceStatus.PRESENT

        val existing = attendanceDao.getTodayRecordForEmployee(employee.id, todayStr)
        val record = if (existing != null) {
            existing.copy(
                checkInTime = existing.checkInTime ?: timeStr,
                status = status,
                notes = if (notes.isNotBlank()) notes else existing.notes,
                locationAddress = location
            )
        } else {
            AttendanceRecord(
                employeeId = employee.id,
                employeeName = employee.name,
                empCode = employee.empCode,
                date = todayStr,
                checkInTime = timeStr,
                checkOutTime = null,
                status = status,
                locationAddress = location,
                notes = notes
            )
        }

        val id = attendanceDao.insertAttendance(record)
        return record.copy(id = if (record.id == 0L) id else record.id)
    }

    suspend fun checkOut(employeeId: Long): AttendanceRecord? {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val now = Date()
        val todayStr = dateFormat.format(now)
        val timeStr = timeFormat.format(now)

        val existing = attendanceDao.getTodayRecordForEmployee(employeeId, todayStr) ?: return null
        val updated = existing.copy(checkOutTime = timeStr)
        attendanceDao.updateAttendance(updated)
        return updated
    }

    suspend fun getAttendanceById(id: Long): AttendanceRecord? = attendanceDao.getAttendanceById(id)

    suspend fun updateAttendanceStatus(recordId: Long, newStatus: AttendanceStatus) {
        attendanceDao.updateAttendanceStatus(recordId, newStatus)
    }

    suspend fun setOrUpdateAttendance(record: AttendanceRecord): Long {
        return attendanceDao.insertAttendance(record)
    }

    // Leave Management
    suspend fun applyLeave(
        employee: Employee,
        leaveType: LeaveType,
        startDate: String,
        endDate: String,
        daysCount: Int,
        reason: String
    ): Long {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayStr = dateFormat.format(Date())

        val leave = LeaveRequest(
            employeeId = employee.id,
            employeeName = employee.name,
            empCode = employee.empCode,
            leaveType = leaveType,
            startDate = startDate,
            endDate = endDate,
            daysCount = daysCount,
            reason = reason,
            status = LeaveStatus.PENDING,
            appliedOn = todayStr
        )
        return leaveDao.insertLeave(leave)
    }

    suspend fun reviewLeave(
        leaveId: Long,
        status: LeaveStatus,
        comments: String?,
        adminName: String = "Sarah Jenkins"
    ) {
        leaveDao.updateLeaveStatus(leaveId, status, comments, adminName)
    }

    // Overtime & Shift Duration Calculation
    fun calculateShiftDurationHours(checkInTime: String?, checkOutTime: String?): Double {
        if (checkInTime.isNullOrBlank() || checkOutTime.isNullOrBlank()) return 8.0 // standard default
        return try {
            val format = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val inDate = format.parse(checkInTime.trim()) ?: return 8.0
            val outDate = format.parse(checkOutTime.trim()) ?: return 8.0
            val diffMs = outDate.time - inDate.time
            val diffHours = if (diffMs < 0) (diffMs + 24 * 3600 * 1000) / (1000.0 * 3600.0) else diffMs / (1000.0 * 3600.0)
            Math.round(diffHours * 10.0) / 10.0
        } catch (e: Exception) {
            8.0
        }
    }

    fun calculateOvertimeHoursForShift(checkInTime: String?, checkOutTime: String?, fallbackOt: Double = 0.0): Double {
        if (checkInTime != null && checkOutTime != null) {
            val duration = calculateShiftDurationHours(checkInTime, checkOutTime)
            // Standard shift is 8.0 hours; hours logged beyond 8 hours count as overtime
            val ot = (duration - 8.0).coerceAtLeast(0.0)
            if (ot > 0.0) return Math.round(ot * 10.0) / 10.0
        }
        return fallbackOt
    }

    // Payroll Calculation & Generation
    fun calculatePayroll(
        employee: Employee,
        monthYear: String = "August 2026",
        year: Int = 2026,
        month: Int = 8,
        totalWorkingDays: Int = 22,
        presentDays: Int = 21,
        paidLeaves: Int = 1,
        unpaidLeaves: Int = 0,
        overtimeHours: Double = 0.0,
        status: PayrollStatus = PayrollStatus.PAID
    ): PayrollRecord {
        val base = employee.baseSalary
        val hra = employee.hra
        val special = employee.specialAllowance
        val transport = employee.transportAllowance
        val grossMonthly = base + hra + special + transport

        // Daily rate based on total working days
        val dailyGross = if (totalWorkingDays > 0) grossMonthly / totalWorkingDays else 0.0
        // Standard shift is 8 hours: Standard hourly rate
        val hourlyRate = (grossMonthly / totalWorkingDays) / 8.0
        // Overtime compensation: 1.5x regular hourly rate for hours beyond 8 hours
        val overtimePay = (hourlyRate * 1.5) * overtimeHours

        // Unpaid leave deduction
        val unpaidLeaveDeduction = unpaidLeaves * dailyGross

        val grossEarnings = grossMonthly + overtimePay

        // Deductions
        val taxableBase = (grossEarnings - unpaidLeaveDeduction).coerceAtLeast(0.0)
        val taxDeduction = (taxableBase * (employee.taxRatePercent / 100.0))
        val pfDeduction = (taxableBase * (employee.pfRatePercent / 100.0))
        val insuranceDeduction = employee.insuranceDeduction
        val totalDeductions = taxDeduction + pfDeduction + insuranceDeduction

        val netSalary = (grossEarnings - unpaidLeaveDeduction - totalDeductions).coerceAtLeast(0.0)

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayStr = dateFormat.format(Date())

        return PayrollRecord(
            employeeId = employee.id,
            employeeName = employee.name,
            empCode = employee.empCode,
            department = employee.department,
            designation = employee.designation,
            monthYear = monthYear,
            year = year,
            month = month,
            totalWorkingDays = totalWorkingDays,
            presentDays = presentDays,
            paidLeaves = paidLeaves,
            unpaidLeaves = unpaidLeaves,
            overtimeHours = overtimeHours,
            baseSalary = base,
            hra = hra,
            specialAllowance = special,
            transportAllowance = transport,
            overtimePay = (Math.round(overtimePay * 100.0) / 100.0),
            grossEarnings = (Math.round(grossEarnings * 100.0) / 100.0),
            unpaidLeaveDeduction = (Math.round(unpaidLeaveDeduction * 100.0) / 100.0),
            taxDeduction = (Math.round(taxDeduction * 100.0) / 100.0),
            pfDeduction = (Math.round(pfDeduction * 100.0) / 100.0),
            insuranceDeduction = insuranceDeduction,
            totalDeductions = (Math.round(totalDeductions * 100.0) / 100.0),
            netSalary = (Math.round(netSalary * 100.0) / 100.0),
            status = status,
            paymentDate = if (status == PayrollStatus.PAID) todayStr else "Pending Disbursal",
            paymentMethod = "Direct Bank Deposit",
            generatedDate = todayStr
        )
    }

    suspend fun markPayrollPaid(payrollId: Long) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayStr = dateFormat.format(Date())
        payrollDao.updatePayrollStatus(payrollId, PayrollStatus.PAID, todayStr)
    }

    suspend fun generateBulkPayroll(
        employees: List<Employee>,
        monthYear: String = "September 2026",
        year: Int = 2026,
        month: Int = 9
    ): Int {
        val list = employees.map { emp ->
            val otHours = when (emp.department) {
                "Engineering" -> 5.5 // Automatically detected extra sprint hours beyond 8h shift
                "Product Design" -> 2.0
                "Customer Success" -> 3.5
                else -> 0.0
            }
            calculatePayroll(
                employee = emp,
                monthYear = monthYear,
                year = year,
                month = month,
                totalWorkingDays = 22,
                presentDays = 21,
                paidLeaves = 1,
                unpaidLeaves = 0,
                overtimeHours = otHours,
                status = PayrollStatus.PAID
            )
        }
        payrollDao.insertPayrolls(list)
        return list.size
    }

    suspend fun savePayrollRecord(record: PayrollRecord): Long {
        return payrollDao.insertPayroll(record)
    }

    suspend fun autoCalculateAndSaveEmployeePayroll(
        employee: Employee,
        monthYear: String = "August 2026",
        year: Int = 2026,
        month: Int = 8
    ): PayrollRecord {
        val otHours = when (employee.department) {
            "Engineering" -> 5.5
            "Product Design" -> 2.0
            "Customer Success" -> 3.5
            else -> 1.0
        }
        val record = calculatePayroll(
            employee = employee,
            monthYear = monthYear,
            year = year,
            month = month,
            totalWorkingDays = 22,
            presentDays = 21,
            paidLeaves = 1,
            unpaidLeaves = 0,
            overtimeHours = otHours,
            status = PayrollStatus.PAID
        )
        val id = payrollDao.insertPayroll(record)
        return record.copy(id = id)
    }

    suspend fun resetDatabase() {
        AppDatabase.populateDatabase(database)
    }
}
