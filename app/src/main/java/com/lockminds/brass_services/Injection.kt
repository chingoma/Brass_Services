package com.lockminds.brass_services

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import com.lockminds.brass_services.database.AppDatabase
import com.lockminds.brass_services.database.daos.UserDao
import com.lockminds.brass_services.database.repositories.*
import com.lockminds.brass_services.retrofit.AttendanceService
import com.lockminds.brass_services.retrofit.OffloadLotService
import com.lockminds.brass_services.retrofit.ReceiverLotService
import com.lockminds.brass_services.viewmodel.*

/**
 * Class that handles object creation.
 * Like this, objects can be passed as parameters in the constructors and then replaced for
 * testing, where needed.
 */
object Injection {

    private fun providePermissionsRepository(context: Context): PermissionsRepository{
        return PermissionsRepository(AppDatabase.getInstance(context).permissionsDao())
    }

    fun permissionsViewModelFactory(context: Context): ViewModelProvider.Factory{
        return  PermissionsViewModelFactory(providePermissionsRepository(context))
    }

    private fun provideUserRepository(context: Context): UserRepository{
        return UserRepository(AppDatabase.getInstance(context).userDao())
    }

    fun userViewModelFactory(context: Context): ViewModelProvider.Factory{
        return  UserViewModelFactory(provideUserRepository(context))
    }

    private fun attendancePagedRepository(context: Context): AttendancePagedRepository {
        return AttendancePagedRepository(AttendanceService.create(), AppDatabase.getInstance(context))
    }

    fun attendancePagedViewModelFactory(context: Context): ViewModelProvider.Factory {
        return AttendancePagedViewModelFactory(attendancePagedRepository(context))
    }

    private fun provideCurrentOffice(context: Context): CurrentOfficeRepository{
        return CurrentOfficeRepository(AppDatabase.getInstance(context).currentOfficeDao())
    }

    fun currentOfficeModelFactory(context: Context) : CurrentOfficeViewModelFactory{
        return CurrentOfficeViewModelFactory(provideCurrentOffice(context))
    }

    private fun receiverLotPagedRepository(context: Context): ReceiverLotPagedRepository {
        return ReceiverLotPagedRepository(ReceiverLotService.create(), AppDatabase.getInstance(context))
    }

    fun receiverLotPagedViewModelFactory(context: Context): ViewModelProvider.Factory {
        return ReceiverLotPagedViewModelFactory(receiverLotPagedRepository(context))
    }

    private fun offloadLotPagedRepository(context: Context): OffloadLotPagedRepository {
        return OffloadLotPagedRepository(OffloadLotService.create(), AppDatabase.getInstance(context))
    }

    fun offloadLotPagedViewModelFactory(context: Context): ViewModelProvider.Factory {
        return OffloadLotPagedViewModelFactory(offloadLotPagedRepository(context))
    }
}
